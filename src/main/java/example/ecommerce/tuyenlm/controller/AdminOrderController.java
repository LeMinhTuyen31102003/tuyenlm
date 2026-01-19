package example.ecommerce.tuyenlm.controller;

import example.ecommerce.tuyenlm.dto.request.UpdateOrderStatusRequest;
import example.ecommerce.tuyenlm.dto.response.OrderResponse;
import example.ecommerce.tuyenlm.entity.OrderStatus;
import example.ecommerce.tuyenlm.service.inter.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final IOrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<OrderResponse> orders = orderService.getOrders(status, keyword, fromDate, toDate, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse order = orderService.updateOrderStatus(id, request.getStatus(), request.getNote());
        return ResponseEntity.ok(order);
    }
}
