package example.ecommerce.tuyenlm.service.impl;

import example.ecommerce.tuyenlm.dto.request.AddCartItemRequest;
import example.ecommerce.tuyenlm.dto.request.UpdateCartItemRequest;
import example.ecommerce.tuyenlm.dto.response.CartItemResponse;
import example.ecommerce.tuyenlm.dto.response.CartResponse;
import example.ecommerce.tuyenlm.entity.Cart;
import example.ecommerce.tuyenlm.entity.CartItem;
import example.ecommerce.tuyenlm.entity.ProductVariant;
import example.ecommerce.tuyenlm.enums.ReservationStatus;
import example.ecommerce.tuyenlm.exception.BadRequestException;
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
import java.util.UUID;

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
        // ✅ AUTO GENERATE sessionId nếu client không gửi
        String sessionId = request.getSessionId();
        boolean isNewSession = false;

        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            isNewSession = true;
            log.info("🆕 Generated new sessionId: {}", sessionId);
        } else {
            log.info("♻️ Reusing existing sessionId: {}", sessionId);
        }

        // Make sessionId final for lambda usage
        final String finalSessionId = sessionId;

        // Find or create cart
        Cart cart = cartRepository.findBySessionId(finalSessionId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(finalSessionId);
                    Cart savedCart = cartRepository.save(newCart);
                    log.info("🛒 Created new cart with sessionId: {}", finalSessionId);
                    return savedCart;
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
        log.info("📈 Cart item added/updated: id={}, quantity={}", cartItem.getId(), cartItem.getQuantity());

        // Build response và LUÔN LUÔN trả về sessionId
        CartItemResponse response = cartMapper.toCartItemResponse(cartItem);
        response.setSessionId(finalSessionId); // ✅ Client nhận sessionId từ đây

        if (isNewSession) {
            log.info("✅ New session created and returned to client: {}", finalSessionId);
        }

        return response;
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

        // ✅ VALIDATE: Item có thuộc giỏ hàng của sessionId này không?
        if (!cartItem.getCart().getSessionId().equals(request.getSessionId())) {
            throw new BadRequestException("This cart item does not belong to your session");
        }

        // Validate stock availability
        validateStockAvailability(cartItem.getVariant(), request.getQuantity());

        cartItem.setQuantity(request.getQuantity());
        cartItem = cartItemRepository.save(cartItem);

        log.info("🔄 Cart item updated: id={}, newQuantity={}, sessionId={}", itemId, request.getQuantity(),
                request.getSessionId());

        CartItemResponse response = cartMapper.toCartItemResponse(cartItem);
        response.setSessionId(cartItem.getCart().getSessionId());
        return response;
    }

    @Override
    @Transactional
    public void removeCartItem(Long itemId, String sessionId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        // ✅ VALIDATE: Item có thuộc giỏ hàng của sessionId này không?
        if (!cartItem.getCart().getSessionId().equals(sessionId)) {
            throw new BadRequestException("This cart item does not belong to your session");
        }

        cartItemRepository.delete(cartItem);
        log.info("🗑️ Cart item removed: id={}, sessionId={}", itemId, sessionId);
    }

    @Override
    @Transactional
    public void clearCart(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", sessionId));

        cartItemRepository.deleteByCartId(cart.getId());
        log.info("🧹 Cart cleared: sessionId={}", sessionId);
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
