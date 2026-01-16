package example.ecommerce.tuyenlm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private VariantResponse variant;
    private int quantity;
    private BigDecimal priceSnapshot;
    private BigDecimal subtotal;
}
