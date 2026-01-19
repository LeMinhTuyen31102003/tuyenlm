package example.ecommerce.tuyenlm.service.impl;

import example.ecommerce.tuyenlm.dto.request.AddCartItemRequest;
import example.ecommerce.tuyenlm.dto.request.UpdateCartItemRequest;
import example.ecommerce.tuyenlm.dto.response.CartItemResponse;
import example.ecommerce.tuyenlm.dto.response.CartResponse;
import example.ecommerce.tuyenlm.entity.Cart;
import example.ecommerce.tuyenlm.entity.CartItem;
import example.ecommerce.tuyenlm.entity.ProductVariant;
import example.ecommerce.tuyenlm.entity.ReservationStatus;
import example.ecommerce.tuyenlm.exception.InsufficientStockException;
import example.ecommerce.tuyenlm.exception.ResourceNotFoundException;
import example.ecommerce.tuyenlm.mapping.CartMapper;
import example.ecommerce.tuyenlm.repository.CartItemRepository;
import example.ecommerce.tuyenlm.repository.CartRepository;
import example.ecommerce.tuyenlm.repository.InventoryReservationRepository;
import example.ecommerce.tuyenlm.repository.ProductVariantRepository;
import example.ecommerce.tuyenlm.service.inter.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryReservationRepository reservationRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartItemResponse addItemToCart(AddCartItemRequest request) {
        log.info("Adding item to cart: sessionId={}, variantId={}, quantity={}",
                request.getSessionId(), request.getVariantId(), request.getQuantity());

        // Find or create cart
        Cart cart = cartRepository.findBySessionId(request.getSessionId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(request.getSessionId());
                    return cartRepository.save(newCart);
                });

        // Find variant
        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", request.getVariantId()));

        // Check stock availability
        validateStockAvailability(variant, request.getQuantity());

        // Check if item already exists in cart
        CartItem cartItem = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId())
                .orElse(null);

        if (cartItem != null) {
            // Update quantity
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            validateStockAvailability(variant, newQuantity);
            cartItem.setQuantity(newQuantity);
        } else {
            // Create new cart item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setVariant(variant);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPriceSnapshot(variant.getPrice());
        }

        cartItem = cartItemRepository.save(cartItem);
        log.info("Cart item added/updated: id={}, quantity={}", cartItem.getId(), cartItem.getQuantity());

        return cartMapper.toCartItemResponse(cartItem);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", sessionId));

        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartItemResponse updateCartItem(Long itemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        // Validate stock availability
        validateStockAvailability(cartItem.getVariant(), request.getQuantity());

        cartItem.setQuantity(request.getQuantity());
        cartItem = cartItemRepository.save(cartItem);

        log.info("Cart item updated: id={}, newQuantity={}", itemId, request.getQuantity());
        return cartMapper.toCartItemResponse(cartItem);
    }

    @Override
    @Transactional
    public void removeCartItem(Long itemId) {
        if (!cartItemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("CartItem", itemId);
        }
        cartItemRepository.deleteById(itemId);
        log.info("Cart item removed: id={}", itemId);
    }

    @Override
    @Transactional
    public void clearCart(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", sessionId));

        cartItemRepository.deleteByCartId(cart.getId());
        log.info("Cart cleared: sessionId={}", sessionId);
    }

    // Helper methods
    private void validateStockAvailability(ProductVariant variant, int requestedQuantity) {
        int reservedStock = reservationRepository.sumQuantityByVariantAndStatus(
                variant.getId(), ReservationStatus.ACTIVE);
        int availableStock = variant.getStockQuantity() - reservedStock;

        if (availableStock < requestedQuantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for variant %s. Available: %d, Requested: %d",
                            variant.getSku(), availableStock, requestedQuantity),
                    Map.of(
                            "variantId", variant.getId(),
                            "sku", variant.getSku(),
                            "available", availableStock,
                            "requested", requestedQuantity));
        }
    }
}
