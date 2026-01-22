package example.ecommerce.tuyenlm.service.inter;

import example.ecommerce.tuyenlm.dto.request.CheckoutRequest;
import example.ecommerce.tuyenlm.dto.request.LockInventoryRequest;
import example.ecommerce.tuyenlm.dto.request.UnlockInventoryRequest;
import example.ecommerce.tuyenlm.dto.response.CheckoutResponse;
import example.ecommerce.tuyenlm.dto.response.LockInventoryResponse;

import java.util.List;

public interface ICheckoutService {
    /**
     * Lock inventory (trừ stock ngay) khi người dùng bấm "Thanh toán"
     */
    LockInventoryResponse lockInventory(LockInventoryRequest request);

    /**
     * Unlock inventory (hoàn stock) khi người dùng bấm Cancel
     */
    void unlockInventory(UnlockInventoryRequest request);

    /**
     * Process checkout (tạo đơn hàng) khi người dùng submit form thanh toán
     */
    CheckoutResponse processCheckout(CheckoutRequest request, List<Long> reservationIds);
}
