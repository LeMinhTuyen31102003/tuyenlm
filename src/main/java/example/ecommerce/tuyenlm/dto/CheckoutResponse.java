package example.ecommerce.tuyenlm.dto;

import example.ecommerce.tuyenlm.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private Long orderId;
    private String orderNumber;
    private String trackingToken;
    private String trackingUrl;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime reservationExpiresAt;
}
