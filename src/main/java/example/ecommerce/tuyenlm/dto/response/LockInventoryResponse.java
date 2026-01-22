package example.ecommerce.tuyenlm.dto.response;

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
public class LockInventoryResponse {

    private List<Long> reservationIds;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private LocalDateTime expiresAt;
    private String message;
}
