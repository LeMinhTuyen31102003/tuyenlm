package example.ecommerce.tuyenlm.controller;

import example.ecommerce.tuyenlm.dto.response.OrderResponse;
import example.ecommerce.tuyenlm.service.inter.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class UserOrderController {

    private final IOrderService orderService;

    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String email = authentication.getName(); // Username is email for customers
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Find orders by customer email
        Page<OrderResponse> orders = orderService.getOrdersByEmail(email, pageable);
        return ResponseEntity.ok(orders);
    }
}
