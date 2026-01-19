package example.ecommerce.tuyenlm.entity;

public enum UserRole {
    ADMIN, // Quản trị viên - Full access
    WAREHOUSE, // Nhân viên kho - Chỉ xem và update đơn hàng
    CUSTOMER // Khách hàng - Xem đơn hàng của mình, cập nhật thông tin cá nhân
}
