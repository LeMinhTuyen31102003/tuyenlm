package example.ecommerce.tuyenlm.controller;

import example.ecommerce.tuyenlm.dto.request.LoginRequest;
import example.ecommerce.tuyenlm.dto.response.LoginResponse;
import example.ecommerce.tuyenlm.entity.User;
import example.ecommerce.tuyenlm.repository.UserRepository;
import example.ecommerce.tuyenlm.security.JwtTokenProvider;
import example.ecommerce.tuyenlm.security.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Authentication", description = "API đăng nhập/đăng xuất cho Admin và Warehouse")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Operation(summary = "Đăng nhập Admin", description = "Đăng nhập để lấy JWT token. Tài khoản test: admin/admin123 hoặc warehouse/warehouse123")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication.getName());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("User logged in successfully: {}", user.getUsername());

        return ResponseEntity.ok(LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build());
    }

    @Operation(summary = "Đăng xuất", description = "Đăng xuất và thêm token vào blacklist", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        try {
            // Extract token from request
            String token = getJwtFromRequest(request);

            if (StringUtils.hasText(token)) {
                // Get token expiry time
                LocalDateTime expiryTime = tokenProvider.getExpiryTime(token);

                // Add token to blacklist
                tokenBlacklistService.blacklistToken(token, expiryTime);

                log.info("Token blacklisted successfully");
            }
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
        }

        // Clear security context
        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
