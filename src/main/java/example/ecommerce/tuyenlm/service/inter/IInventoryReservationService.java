package example.ecommerce.tuyenlm.service.inter;

import example.ecommerce.tuyenlm.entity.InventoryReservation;
import example.ecommerce.tuyenlm.entity.Order;
import example.ecommerce.tuyenlm.entity.ProductVariant;

import java.util.List;

public interface IInventoryReservationService {
    InventoryReservation createReservation(ProductVariant variant, int quantity, int expirationMinutes);

    void commitReservations(Order order, List<InventoryReservation> reservations);

    void expireReservations(List<InventoryReservation> reservations);

    void releaseReservations(List<InventoryReservation> reservations);

    void autoExpireReservations();

    int getAvailableStock(Long variantId);
}
