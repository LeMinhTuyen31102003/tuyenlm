package example.ecommerce.tuyenlm.dto.response;

import example.ecommerce.tuyenlm.entity.OrderStatus;
import example.ecommerce.tuyenlm.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shippingAddress;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private PaymentMethod paymentMethod;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

