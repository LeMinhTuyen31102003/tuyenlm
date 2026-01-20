package example.ecommerce.tuyenlm.enums;

public enum OrderStatus {
    PENDING, // Đơn hàng mới tạo, chưa thanh toán
    PAID, // Đã thanh toán (Bank Transfer)
    CONFIRMED, // Đã xác nhận đơn hàng
    PROCESSING, // Đang chuẩn bị hàng
    SHIPPING, // Đang giao hàng
    DELIVERED, // Đã giao thành công
    CANCELLED // Đã hủy
}
