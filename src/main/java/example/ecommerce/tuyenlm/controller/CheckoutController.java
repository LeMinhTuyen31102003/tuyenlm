package example.ecommerce.tuyenlm.controller;

import example.ecommerce.tuyenlm.dto.request.CheckoutRequest;
import example.ecommerce.tuyenlm.dto.request.LockInventoryRequest;
import example.ecommerce.tuyenlm.dto.request.UnlockInventoryRequest;
import example.ecommerce.tuyenlm.dto.response.CheckoutResponse;
import example.ecommerce.tuyenlm.dto.response.LockInventoryResponse;
import example.ecommerce.tuyenlm.service.inter.ICheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final ICheckoutService checkoutService;

    /**
     * BƯỚC 1: Lock inventory - Người dùng bấm "Thanh toán"
     * Trừ stock ngay và tạo reservation, trả về reservationIds để dùng cho bước 3
     */
    @PostMapping("/lock")
    public ResponseEntity<LockInventoryResponse> lockInventory(@Valid @RequestBody LockInventoryRequest request) {
        LockInventoryResponse response = checkoutService.lockInventory(request);
        return ResponseEntity.ok(response);
    }

    /**
     * BƯỚC 2 (Optional): Unlock inventory - Người dùng bấm Cancel
     * Hoàn lại stock cho sản phẩm
     */
    @PostMapping("/unlock")
    public ResponseEntity<Void> unlockInventory(@Valid @RequestBody UnlockInventoryRequest request) {
        checkoutService.unlockInventory(request);
        return ResponseEntity.ok().build();
    }

    /**
     * BƯỚC 3: Process checkout - Người dùng điền form và submit
     * Tạo đơn hàng với các reservation đã lock ở bước 1
     * Frontend phải gửi reservationIds nhận được từ API lock
     */
    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(
            @Valid @RequestBody CheckoutRequest request,
            @RequestParam List<Long> reservationIds) {
        CheckoutResponse response = checkoutService.processCheckout(request, reservationIds);
        return ResponseEntity.ok(response);
    }
}
