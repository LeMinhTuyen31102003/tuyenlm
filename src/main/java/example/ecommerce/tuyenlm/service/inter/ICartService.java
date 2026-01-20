package example.ecommerce.tuyenlm.service.inter;

import example.ecommerce.tuyenlm.dto.request.AddCartItemRequest;
import example.ecommerce.tuyenlm.dto.request.UpdateCartItemRequest;
import example.ecommerce.tuyenlm.dto.response.CartItemResponse;
import example.ecommerce.tuyenlm.dto.response.CartResponse;

public interface ICartService {
    CartItemResponse addItemToCart(AddCartItemRequest request);

    CartResponse getCart(String sessionId);

    CartItemResponse updateCartItem(Long itemId, UpdateCartItemRequest request);

    void removeCartItem(Long itemId, String sessionId);

    void clearCart(String sessionId);
}
