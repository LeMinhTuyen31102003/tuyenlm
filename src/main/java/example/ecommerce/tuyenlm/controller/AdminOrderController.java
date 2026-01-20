package example.ecommerce.tuyenlm.controller;

import example.ecommerce.tuyenlm.dto.request.UpdateOrderStatusRequest;
import example.ecommerce.tuyenlm.dto.response.OrderResponse;
import example.ecommerce.tuyenlm.enums.OrderStatus;
import example.ecommerce.tuyenlm.service.inter.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Admin - Orders", description = "API quản lý đơn hàng (Chỉ ADMIN)")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final IOrderService orderService;

    @Operation(summary = "Danh sách đơn hàng", description = "Xem tất cả đơn hàng với filter theo trạng thái, keyword, và thời gian")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
            @Parameter(description = "Lọc theo trạng thái: PENDING, CONFIRMED, PAID, SHIPPING, DELIVERED, CANCELLED") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Tìm kiếm theo tên/email/phone khách hàng") @RequestParam(required = false) String keyword,
            @Parameter(description = "Từ ngày (ISO format: 2026-01-19T00:00:00)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "Đến ngày (ISO format: 2026-01-19T23:59:59)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số đơn hàng mỗi trang") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<OrderResponse> orders = orderService.getOrders(status, keyword, fromDate, toDate, pageable);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Chi tiết đơn hàng", description = "Xem thông tin chi tiết của 1 đơn hàng")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "ID đơn hàng") @PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Cập nhật trạng thái đơn hàng", description = "Đổi trạng thái đơn hàng (PENDING → CONFIRMED → PAID → SHIPPING → DELIVERED hoặc CANCELLED)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "ID đơn hàng") @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse order = orderService.updateOrderStatus(id, request.getStatus(), request.getNote());
        return ResponseEntity.ok(order);
    }
}
