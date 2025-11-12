package com.ecomerce.cartservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    
    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    private List<Long> itemIds;
    
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;
    
    private String notes;
    
    /**
     * Phương thức thanh toán: VNPAY hoặc COD
     * Default: VNPAY (nếu không được specify)
     * Nếu không có, payment sẽ được tạo riêng sau
     */
    @Pattern(regexp = "VNPAY|COD", message = "Phương thức thanh toán phải là VNPAY hoặc COD")
    private String paymentMethod;
}

