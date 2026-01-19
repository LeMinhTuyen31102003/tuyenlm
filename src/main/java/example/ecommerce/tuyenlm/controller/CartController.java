package example.ecommerce.tuyenlm.controller;

import example.ecommerce.tuyenlm.dto.request.AddCartItemRequest;
import example.ecommerce.tuyenlm.dto.request.UpdateCartItemRequest;
import example.ecommerce.tuyenlm.dto.response.CartItemResponse;
import example.ecommerce.tuyenlm.dto.response.CartResponse;
import example.ecommerce.tuyenlm.service.inter.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestParam String sessionId) {
        CartResponse cart = cartService.getCart(sessionId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItemToCart(@Valid @RequestBody AddCartItemRequest request) {
        CartItemResponse cartItem = cartService.addItemToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartItemResponse> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        CartItemResponse cartItem = cartService.updateCartItem(itemId, request);
        return ResponseEntity.ok(cartItem);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long itemId) {
        cartService.removeCartItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestParam String sessionId) {
        cartService.clearCart(sessionId);
        return ResponseEntity.noContent().build();
    }
}
