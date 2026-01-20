package example.ecommerce.tuyenlm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update cart item request")
public class UpdateCartItemRequest {

    @NotBlank(message = "Session ID is required")
    @Schema(description = "Session ID để xác thực quyền sở hữu item", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479", required = true)
    private String sessionId;

    @Positive(message = "Quantity must be greater than 0")
    @Schema(description = "Số lượng mới (SET, không phải cộng thêm)", example = "3", required = true, minimum = "1")
    private int quantity;
}
