package example.ecommerce.tuyenlm.service.inter;

import example.ecommerce.tuyenlm.dto.request.CheckoutRequest;
import example.ecommerce.tuyenlm.dto.response.CheckoutResponse;

public interface ICheckoutService {
    CheckoutResponse processCheckout(CheckoutRequest request);
}
