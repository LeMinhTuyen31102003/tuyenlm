package example.ecommerce.tuyenlm.controller;

import example.ecommerce.tuyenlm.dto.request.LoginRequest;
import example.ecommerce.tuyenlm.dto.request.RegisterRequest;
import example.ecommerce.tuyenlm.dto.response.LoginResponse;
import example.ecommerce.tuyenlm.entity.User;
import example.ecommerce.tuyenlm.entity.UserRole;
import example.ecommerce.tuyenlm.repository.UserRepository;
import example.ecommerce.tuyenlm.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Username already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // Create new customer user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);

        userRepository.save(user);
        log.info("New customer registered: {}", user.getUsername());

        // Auto login after registration
        String token = tokenProvider.generateToken(user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build());
    }

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

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");

        return ResponseEntity.ok(response);
    }
}
