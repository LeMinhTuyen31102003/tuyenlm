package example.ecommerce.tuyenlm.controller;

import example.ecommerce.tuyenlm.dto.response.OrderResponse;
import example.ecommerce.tuyenlm.service.inter.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @GetMapping("/track/{trackingToken}")
    public ResponseEntity<OrderResponse> trackOrder(@PathVariable String trackingToken) {
        OrderResponse order = orderService.getOrderByTrackingToken(trackingToken);
        return ResponseEntity.ok(order);
    }
}
