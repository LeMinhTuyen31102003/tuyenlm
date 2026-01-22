package example.ecommerce.tuyenlm.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnlockInventoryRequest {

    @NotEmpty(message = "Reservation IDs are required")
    private List<Long> reservationIds;
}
