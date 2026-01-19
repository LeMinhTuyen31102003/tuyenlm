package example.ecommerce.tuyenlm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private String username;
    private String fullName;
    private String role;
}
