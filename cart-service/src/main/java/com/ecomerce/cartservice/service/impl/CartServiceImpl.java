package com.ecomerce.cartservice.service.impl;

import com.ecomerce.cartservice.advice.exeption.ResourceNotFoundException;
import com.ecomerce.cartservice.client.InventoryServiceClient;
import com.ecomerce.cartservice.client.OrderServiceClient;
import com.ecomerce.cartservice.client.PaymentServiceClient;
import com.ecomerce.cartservice.client.ProductServiceClient;
import com.ecomerce.cartservice.client.dto.InventoryResponse;
import com.ecomerce.cartservice.client.dto.OrderCreateRequest;
import com.ecomerce.cartservice.client.dto.OrderResponse;
import com.ecomerce.cartservice.client.dto.PaymentCreateResponse;
import com.ecomerce.cartservice.client.dto.ProductDetailResponse;
import com.ecomerce.cartservice.dto.request.CartItemRequest;
import com.ecomerce.cartservice.dto.request.CartItemUpdateRequest;
import com.ecomerce.cartservice.dto.request.CheckoutRequest;
import com.ecomerce.cartservice.dto.response.CartItemResponse;
import com.ecomerce.cartservice.dto.response.CartResponse;
import com.ecomerce.cartservice.dto.response.CheckoutResponse;
import com.ecomerce.cartservice.dto.response.PageResponseDto;
import org.springframework.data.jpa.domain.Specification;
import com.ecomerce.cartservice.event.CartItemAddedEvent;
import com.ecomerce.cartservice.model.Cart;
import com.ecomerce.cartservice.model.CartItem;
import com.ecomerce.cartservice.repository.CartItemRepository;
import com.ecomerce.cartservice.repository.CartRepository;
import com.ecomerce.cartservice.service.CartService;
import com.ecomerce.cartservice.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Tìm hoặc tạo cart mới cho user
     */
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdAndIsActiveTrueWithItems(userId)
                .orElseGet(() -> {
                    log.info("Creating new cart for user: {}", userId);
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .isActive(true)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Kiểm tra số lượng hàng có sẵn trong kho
     * @param productId ID sản phẩm
     * @param requestedQuantity Số lượng yêu cầu
     * @throws IllegalStateException nếu không đủ hàng
     */
    private void validateInventoryAvailability(Long productId, Integer requestedQuantity) {
        try {
            InventoryResponse inventory = inventoryServiceClient.getInventoryByProductId(productId);
            
            // Nếu inventory service down (circuit breaker), cho phép add to cart
            // Business decision: ưu tiên UX, sẽ validate lại khi checkout
            if (inventory == null) {
                log.warn("⚠️ Inventory service unavailable for productId: {}. Allowing add to cart, will validate at checkout.", productId);
                return;
            }
            
            // Kiểm tra inventory có active không
            if (inventory.getIsActive() == null || !inventory.getIsActive()) {
                throw new IllegalStateException("Sản phẩm này hiện không có trong kho");
            }
            
            // Kiểm tra số lượng có đủ không
            Integer availableQuantity = inventory.getAvailableQuantity();
            if (availableQuantity == null || availableQuantity < requestedQuantity) {
                throw new IllegalStateException(
                    String.format("Không đủ hàng trong kho. Số lượng có sẵn: %d, số lượng yêu cầu: %d", 
                        availableQuantity != null ? availableQuantity : 0, requestedQuantity)
                );
            }
            
            log.debug("✅ Inventory check passed - ProductId: {}, Available: {}, Requested: {}", 
                    productId, availableQuantity, requestedQuantity);
        } catch (IllegalStateException e) {
            // Re-throw business exceptions
            throw e;
        } catch (Exception e) {
            // Nếu có lỗi khác (network, timeout, etc.), log và cho phép add to cart
            // Sẽ validate lại khi checkout
            log.warn("⚠️ Error checking inventory for productId: {}. Error: {}. Allowing add to cart, will validate at checkout.", 
                    productId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse addItemToCart(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        // Check if item already exists
        cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId())
                .ifPresentOrElse(
                        existingItem -> {
                            // Tính tổng số lượng sau khi cập nhật
                            Integer newQuantity = existingItem.getQuantity() + request.getQuantity();
                            
                            // Kiểm tra inventory availability
                            validateInventoryAvailability(request.getProductId(), newQuantity);
                            
                            // Update quantity
                            existingItem.setQuantity(newQuantity);
                            cartItemRepository.save(existingItem);
                            log.info("Updated cart item quantity for product: {} to {}", 
                                    request.getProductId(), newQuantity);
                        },
                        () -> {
                            // Create new item - Get product info from product-service via Feign Client
                            ProductDetailResponse product = productServiceClient.getProductById(request.getProductId());
                            
                            if (product == null) {
                                throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + request.getProductId());
                            }
                            
                            // Log product details for debugging
                            log.debug("Product details from product-service - ID: {}, Name: {}, Active: {}", 
                                    product.getId(), product.getName(), product.isActive());
                            
                            if (!product.isActive()) {
                                log.warn("⚠️ Attempted to add inactive product to cart - Product ID: {}, User ID: {}", 
                                        request.getProductId(), userId);
                                throw new IllegalStateException("Sản phẩm này không còn hoạt động");
                            }
                            
                            // Kiểm tra inventory availability trước khi thêm vào cart
                            validateInventoryAvailability(request.getProductId(), request.getQuantity());
                            
                            // Sử dụng giá discount nếu có, ngược lại dùng giá gốc
                            BigDecimal productPrice = product.getDiscountPrice() != null 
                                    ? product.getDiscountPrice() 
                                    : product.getPrice();
                            
                            CartItem newItem = CartItem.builder()
                                    .cart(cart)
                                    .productId(product.getId())
                                    .productName(product.getName())
                                    .productImage(product.getThumbnail())
                                    .price(productPrice)
                                    .quantity(request.getQuantity())
                                    .build();
                            cartItemRepository.save(newItem);
                            cart.addItem(newItem);
                            log.info("✅ Added new cart item for product: {} ({}), price: {}", 
                                    product.getId(), product.getName(), productPrice);
                            
                            // Publish event to Kafka for notification
                            CartItemAddedEvent event = CartItemAddedEvent.builder()
                                    .userId(userId)
                                    .cartId(cart.getId())
                                    .cartItemId(newItem.getId())
                                    .productId(product.getId())
                                    .productName(product.getName())
                                    .productPrice(productPrice)
                                    .quantity(newItem.getQuantity())
                                    .timestamp(Instant.now())
                                    .build();
                            kafkaProducerService.publishCartItemAddedEvent(event);
                        }
                );

        cart.calculateTotal();
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse updateCartItem(Long userId, Long itemId, CartItemUpdateRequest request) {
        Cart cart = cartRepository.findByUserIdAndIsActiveTrueWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng của user"));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Sản phẩm không thuộc giỏ hàng của bạn");
        }

        // Kiểm tra inventory availability trước khi cập nhật
        validateInventoryAvailability(item.getProductId(), request.getQuantity());

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        cart.calculateTotal();
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse removeItemFromCart(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserIdAndIsActiveTrueWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng của user"));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Sản phẩm không thuộc giỏ hàng của bạn");
        }

        cart.removeItem(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse clearCart(Long userId) {
        Cart cart = cartRepository.findByUserIdAndIsActiveTrueWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng của user"));

        cart.clear();
        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    public void deleteCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng của user"));

        cart.setIsActive(false);
        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CartResponse> getAllCarts(Specification<Cart> spec, Pageable pageable) {
        Page<Cart> cartPage = cartRepository.findAll(spec, pageable);
        
        List<CartResponse> content = cartPage.getContent().stream()
                .map(this::mapToCartResponse)
                .collect(Collectors.toList());
        
        return new PageResponseDto<>(
                content,
                cartPage.getNumber(),
                cartPage.getSize(),
                cartPage.getTotalElements(),
                cartPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CartItemResponse getCartItemById(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng với ID: " + itemId));
        
        return mapToCartItemResponse(item);
    }

    @Override
    public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
        Cart cart = cartRepository.findByUserIdAndIsActiveTrueWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng của user"));

        // Validate và lấy cart items
        List<CartItem> itemsToCheckout = new java.util.ArrayList<>();
        for (Long itemId : request.getItemIds()) {
            CartItem item = cartItemRepository.findByIdAndCartUserId(itemId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng với ID: " + itemId));
            
            // Validate với product-service
            ProductDetailResponse product = productServiceClient.getProductById(item.getProductId());
            
            if (product == null) {
                throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + item.getProductId());
            }
            
            if (!product.isActive()) {
                throw new IllegalStateException("Sản phẩm '" + product.getName() + "' không còn hoạt động");
            }
            
            // Kiểm tra giá có thay đổi không
            BigDecimal currentPrice = product.getDiscountPrice() != null 
                    ? product.getDiscountPrice() 
                    : product.getPrice();
            
            if (item.getPrice().compareTo(currentPrice) != 0) {
                throw new IllegalStateException(
                    String.format("Giá sản phẩm '%s' đã thay đổi từ %s thành %s. Vui lòng cập nhật giỏ hàng.",
                        product.getName(), item.getPrice(), currentPrice)
                );
            }
            
            itemsToCheckout.add(item);
        }

        // Tạo OrderCreateRequest từ cart items (gửi kèm thông tin sản phẩm đã validate)
        List<OrderCreateRequest.OrderItemRequest> orderItems = itemsToCheckout.stream()
                .map(item -> OrderCreateRequest.OrderItemRequest.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .productName(item.getProductName())
                        .productImage(item.getProductImage())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        OrderCreateRequest orderRequest = OrderCreateRequest.builder()
                .items(orderItems)
                .shippingAddress(request.getShippingAddress())
                .phone(request.getPhone())
                .notes(request.getNotes())
                .build();

        // Gọi order-service để tạo đơn hàng
        OrderResponse orderResponse;
        try {
            orderResponse = orderServiceClient.createOrder(orderRequest);
            
            if (orderResponse == null) {
                log.error("❌ Order-service trả về null response");
                throw new RuntimeException("Không thể tạo đơn hàng. Order-service trả về null response.");
            }
            
            log.info("✅ Created order: {} for user: {} from cart items: {}", 
                    orderResponse.getOrderNumber(), userId, request.getItemIds());
        } catch (Exception e) {
            log.error("❌ Error creating order via order-service: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo đơn hàng: " + e.getMessage(), e);
        }

        // Tự động tạo payment nếu paymentMethod được specify
        PaymentCreateResponse paymentResponse = null;
        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isEmpty()) {
            try {
                log.info("💳 Creating payment for order: {} with method: {}", 
                        orderResponse.getId(), request.getPaymentMethod());
                
                PaymentServiceClient.PaymentCreateRequest paymentRequest = 
                        new PaymentServiceClient.PaymentCreateRequest(
                                orderResponse.getId(),
                                request.getPaymentMethod(),
                                "Thanh toan don hang #" + orderResponse.getOrderNumber()
                        );
                
                paymentResponse = paymentServiceClient.createPayment(paymentRequest);
                log.info("✅ Created payment: {} for order: {} with method: {}", 
                        paymentResponse.getPaymentId(), orderResponse.getId(), request.getPaymentMethod());
            } catch (Exception e) {
                log.error("❌ Error creating payment via payment-service: {}", e.getMessage(), e);
                // Không throw exception - order đã được tạo, payment có thể được tạo sau
                log.warn("⚠️ Order created but payment creation failed. User can create payment later.");
            }
        }

        // Xóa các cart items đã checkout
        List<Long> removedItemIds = new java.util.ArrayList<>();
        for (CartItem item : itemsToCheckout) {
            cart.removeItem(item);
            cartItemRepository.delete(item);
            removedItemIds.add(item.getId());
        }

        // Recalculate cart total
        cart.calculateTotal();
        cartRepository.save(cart);

        log.info("✅ Removed {} items from cart after checkout", removedItemIds.size());

        return CheckoutResponse.builder()
                .order(orderResponse)
                .removedItemIds(removedItemIds)
                .payment(paymentResponse) // Include payment info if created
                .build();
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getItems().size())
                .isActive(cart.getIsActive())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}

