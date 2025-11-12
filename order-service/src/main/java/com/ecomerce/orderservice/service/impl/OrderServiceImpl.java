package com.ecomerce.orderservice.service.impl;

import com.ecomerce.orderservice.advice.exeption.ResourceNotFoundException;
import com.ecomerce.orderservice.client.InventoryServiceClient;
import com.ecomerce.orderservice.client.ProductServiceClient;
import com.ecomerce.orderservice.client.dto.InventoryResponse;
import com.ecomerce.orderservice.client.dto.ProductDetailResponse;
import com.ecomerce.orderservice.dto.request.BuyNowRequest;
import com.ecomerce.orderservice.dto.request.OrderCreateRequest;
import com.ecomerce.orderservice.dto.request.OrderUpdateRequest;
import com.ecomerce.orderservice.dto.response.OrderResponse;
import com.ecomerce.orderservice.dto.response.PageResponseDto;
import com.ecomerce.orderservice.event.OrderCreatedEvent;
import com.ecomerce.orderservice.event.OrderStatusChangedEvent;
import com.ecomerce.orderservice.model.Order;
import com.ecomerce.orderservice.model.OrderItem;
import com.ecomerce.orderservice.repository.OrderRepository;
import com.ecomerce.orderservice.service.KafkaProducerService;
import com.ecomerce.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;

    @Override
    public OrderResponse createOrder(Long userId, OrderCreateRequest request) {
        // Generate order number
        String orderNumber = generateOrderNumber();
        
        // Create order
        Order order = Order.builder()
                .userId(userId)
                .orderNumber(orderNumber)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .phone(request.getPhone())
                .notes(request.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        // Create order items
        for (OrderCreateRequest.OrderItemRequest itemRequest : request.getItems()) {
            // Validate price
            if (itemRequest.getPrice() == null || itemRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                    String.format("Giá sản phẩm không hợp lệ cho productId: %d. Giá phải lớn hơn 0.", 
                        itemRequest.getProductId())
                );
            }
            
            // Validate quantity
            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new IllegalStateException(
                    String.format("Số lượng không hợp lệ cho productId: %d. Số lượng phải lớn hơn 0.", 
                        itemRequest.getProductId())
                );
            }
            
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productId(itemRequest.getProductId())
                    .productName(itemRequest.getProductName() != null 
                            ? itemRequest.getProductName() 
                            : "Product " + itemRequest.getProductId())
                    .productImage(itemRequest.getProductImage())
                    .price(itemRequest.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .build();
            order.addItem(item);
            log.debug("Added item: productId={}, price={}, quantity={}, subtotal={}", 
                    item.getProductId(), item.getPrice(), item.getQuantity(),
                    item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // Calculate total amount
        order.calculateTotal();
        log.info("📊 Calculated order total: {} for {} items", order.getTotalAmount(), order.getItems().size());

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("✅ Created order: {} for user: {}", orderNumber, userId);

        // Publish OrderCreatedEvent to Kafka
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .orderNumber(savedOrder.getOrderNumber())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus().name())
                .shippingAddress(savedOrder.getShippingAddress())
                .phone(savedOrder.getPhone())
                .items(savedOrder.getItems().stream()
                        .map(item -> OrderCreatedEvent.OrderItemEvent.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .timestamp(Instant.now())
                .build();
        
        kafkaProducerService.publishOrderCreatedEvent(event);

        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse buyNow(Long userId, BuyNowRequest request) {
        // Get product info from product-service
        ProductDetailResponse product = productServiceClient.getProductById(request.getProductId());
        
        if (product == null) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + request.getProductId());
        }
        
        if (!product.isActive()) {
            throw new IllegalStateException("Sản phẩm này không còn hoạt động");
        }
        
        // Check inventory availability
        InventoryResponse inventory = inventoryServiceClient.getInventoryByProductId(request.getProductId());
        
        if (inventory == null) {
            throw new ResourceNotFoundException("Không tìm thấy kho hàng cho sản phẩm: " + request.getProductId());
        }
        
        if (inventory.getIsActive() == null || !inventory.getIsActive()) {
            throw new IllegalStateException("Sản phẩm này hiện không có trong kho");
        }
        
        Integer availableQuantity = inventory.getAvailableQuantity();
        if (availableQuantity == null || availableQuantity < request.getQuantity()) {
            throw new IllegalStateException(
                String.format("Không đủ hàng trong kho. Số lượng có sẵn: %d, số lượng yêu cầu: %d", 
                    availableQuantity != null ? availableQuantity : 0, request.getQuantity())
            );
        }
        
        // Get product price (use discount price if available, otherwise regular price)
        BigDecimal productPrice = product.getDiscountPrice() != null 
                ? product.getDiscountPrice() 
                : product.getPrice();
        
        if (productPrice == null || productPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Giá sản phẩm không hợp lệ");
        }
        
        // Generate order number
        String orderNumber = generateOrderNumber();
        
        // Create order
        Order order = Order.builder()
                .userId(userId)
                .orderNumber(orderNumber)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .phone(request.getPhone())
                .notes(request.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .build();
        
        // Create order item
        OrderItem item = OrderItem.builder()
                .order(order)
                .productId(product.getId())
                .productName(product.getName())
                .productImage(product.getThumbnail())
                .price(productPrice)
                .quantity(request.getQuantity())
                .build();
        order.addItem(item);
        
        // Calculate total amount
        order.calculateTotal();
        log.info("📊 Buy Now - Calculated order total: {} for productId: {}, quantity: {}", 
                order.getTotalAmount(), request.getProductId(), request.getQuantity());
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("✅ Buy Now - Created order: {} for user: {}, productId: {}", 
                orderNumber, userId, request.getProductId());
        
        // Publish OrderCreatedEvent to Kafka
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .orderNumber(savedOrder.getOrderNumber())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus().name())
                .shippingAddress(savedOrder.getShippingAddress())
                .phone(savedOrder.getPhone())
                .items(savedOrder.getItems().stream()
                        .map(orderItem -> OrderCreatedEvent.OrderItemEvent.builder()
                                .productId(orderItem.getProductId())
                                .productName(orderItem.getProductName())
                                .price(orderItem.getPrice())
                                .quantity(orderItem.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .timestamp(Instant.now())
                .build();
        
        kafkaProducerService.publishOrderCreatedEvent(event);
        
        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Đơn hàng không thuộc về user này");
        }

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber, Long userId) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderNumber));
        
        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Đơn hàng không thuộc về user này");
        }

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<OrderResponse> getUserOrders(Long userId, Specification<Order> spec, Pageable pageable) {
        // Add userId filter to specification
        Specification<Order> userIdSpec = (root, query, cb) -> cb.equal(root.get("userId"), userId);
        Specification<Order> userSpec = spec != null ? userIdSpec.and(spec) : userIdSpec;
        
        Page<Order> orderPage = orderRepository.findAll(userSpec, pageable);
        
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new PageResponseDto<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<OrderResponse> getAllOrders(Specification<Order> spec, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponseDto<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderUpdateRequest request) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        Order.OrderStatus oldStatus = order.getStatus();
        Order.OrderStatus newStatus = request.getStatus();
        
        // Validate state transition
        validateStateTransition(oldStatus, newStatus);
        
        order.setStatus(newStatus);
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        orderRepository.save(order);
        log.info("✅ Updated order status: {} from {} to {}", orderId, oldStatus, newStatus);

        // Publish OrderStatusChangedEvent to Kafka
        if (!oldStatus.equals(request.getStatus())) {
            OrderStatusChangedEvent event = OrderStatusChangedEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .orderNumber(order.getOrderNumber())
                    .oldStatus(oldStatus.name())
                    .newStatus(request.getStatus().name())
                    .items(order.getItems().stream()
                            .map(item -> OrderStatusChangedEvent.OrderItemEvent.builder()
                                    .productId(item.getProductId())
                                    .productName(item.getProductName())
                                    .price(item.getPrice())
                                    .quantity(item.getQuantity())
                                    .build())
                            .collect(Collectors.toList()))
                    .timestamp(Instant.now())
                    .build();
            
            kafkaProducerService.publishOrderStatusChangedEvent(event);
        }

        return mapToResponse(order);
    }

    @Override
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Đơn hàng không thuộc về user này");
        }

        if (order.getStatus() == Order.OrderStatus.DELIVERED || 
            order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalStateException("Không thể hủy đơn hàng ở trạng thái: " + order.getStatus());
        }

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("✅ Cancelled order: {} by user: {}", orderId, userId);

        // Publish OrderStatusChangedEvent to Kafka
        OrderStatusChangedEvent event = OrderStatusChangedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .oldStatus(oldStatus.name())
                .newStatus(Order.OrderStatus.CANCELLED.name())
                .items(order.getItems().stream()
                        .map(item -> OrderStatusChangedEvent.OrderItemEvent.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .timestamp(Instant.now())
                .build();
        
        kafkaProducerService.publishOrderStatusChangedEvent(event);
    }

    /**
     * Validate state transition for order status
     * Business rules:
     * - PENDING -> CONFIRMED, CANCELLED
     * - CONFIRMED -> PROCESSING, CANCELLED
     * - PROCESSING -> SHIPPED, CANCELLED
     * - SHIPPED -> DELIVERED, CANCELLED
     * - DELIVERED -> (no transitions allowed)
     * - CANCELLED -> (no transitions allowed)
     */
    private void validateStateTransition(Order.OrderStatus oldStatus, Order.OrderStatus newStatus) {
        // No change - allowed
        if (oldStatus == newStatus) {
            return;
        }
        
        // Cannot change from terminal states
        if (oldStatus == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Không thể thay đổi trạng thái đơn hàng đã giao hàng");
        }
        
        if (oldStatus == Order.OrderStatus.CANCELLED) {
            throw new IllegalStateException("Không thể thay đổi trạng thái đơn hàng đã hủy");
        }
        
        // Validate transitions
        switch (oldStatus) {
            case PENDING:
                if (newStatus != Order.OrderStatus.CONFIRMED && 
                    newStatus != Order.OrderStatus.CANCELLED) {
                    throw new IllegalStateException(
                        String.format("Không thể chuyển từ %s sang %s. Chỉ có thể chuyển sang CONFIRMED hoặc CANCELLED", 
                            oldStatus, newStatus)
                    );
                }
                break;
                
            case CONFIRMED:
                if (newStatus != Order.OrderStatus.PROCESSING && 
                    newStatus != Order.OrderStatus.CANCELLED) {
                    throw new IllegalStateException(
                        String.format("Không thể chuyển từ %s sang %s. Chỉ có thể chuyển sang PROCESSING hoặc CANCELLED", 
                            oldStatus, newStatus)
                    );
                }
                break;
                
            case PROCESSING:
                if (newStatus != Order.OrderStatus.SHIPPED && 
                    newStatus != Order.OrderStatus.CANCELLED) {
                    throw new IllegalStateException(
                        String.format("Không thể chuyển từ %s sang %s. Chỉ có thể chuyển sang SHIPPED hoặc CANCELLED", 
                            oldStatus, newStatus)
                    );
                }
                break;
                
            case SHIPPED:
                if (newStatus != Order.OrderStatus.DELIVERED && 
                    newStatus != Order.OrderStatus.CANCELLED) {
                    throw new IllegalStateException(
                        String.format("Không thể chuyển từ %s sang %s. Chỉ có thể chuyển sang DELIVERED hoặc CANCELLED", 
                            oldStatus, newStatus)
                    );
                }
                break;
                
            default:
                throw new IllegalStateException("Trạng thái không hợp lệ: " + oldStatus);
        }
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int)(Math.random() * 10000));
        return "ORD-" + timestamp + "-" + random;
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productImage(item.getProductImage())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .phone(order.getPhone())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }
}

