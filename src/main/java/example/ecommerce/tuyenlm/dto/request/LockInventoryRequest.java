package example.ecommerce.tuyenlm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockInventoryRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;
}
