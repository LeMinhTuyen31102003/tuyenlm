package example.ecommerce.tuyenlm.mapping;

import example.ecommerce.tuyenlm.dto.response.CartItemResponse;
import example.ecommerce.tuyenlm.dto.response.CartResponse;
import example.ecommerce.tuyenlm.entity.Cart;
import example.ecommerce.tuyenlm.entity.CartItem;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface CartMapper {

    // Cart → CartResponse
    @Mapping(target = "totalItems", source = "cart", qualifiedByName = "calculateTotalItems")
    @Mapping(target = "totalAmount", source = "cart", qualifiedByName = "calculateTotalAmount")
    CartResponse toCartResponse(Cart cart);

    // CartItem → CartItemResponse
    @Mapping(target = "variant", source = "variant")
    @Mapping(target = "subtotal", expression = "java(cartItem.getSubtotal())")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    List<CartItemResponse> toCartItemResponseList(List<CartItem> cartItems);

    @Named("calculateTotalItems")
    default int calculateTotalItems(Cart cart) {
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Named("calculateTotalAmount")
    default BigDecimal calculateTotalAmount(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
