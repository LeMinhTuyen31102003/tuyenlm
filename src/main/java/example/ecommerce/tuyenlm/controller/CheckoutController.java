package example.ecommerce.tuyenlm.controller;

import example.ecommerce.tuyenlm.dto.request.CheckoutRequest;
import example.ecommerce.tuyenlm.dto.response.CheckoutResponse;
import example.ecommerce.tuyenlm.service.inter.ICheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final ICheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse response = checkoutService.processCheckout(request);
        return ResponseEntity.ok(response);
    }
}
