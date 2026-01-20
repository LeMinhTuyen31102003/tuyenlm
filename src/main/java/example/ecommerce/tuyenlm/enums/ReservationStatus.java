package example.ecommerce.tuyenlm.enums;

public enum ReservationStatus {
    ACTIVE, // Đang giữ hàng (trong thời gian checkout)
    EXPIRED, // Hết hạn (timeout)
    COMMITTED // Đã commit vào order
}
