package example.ecommerce.tuyenlm.service.inter;

import example.ecommerce.tuyenlm.entity.Order;

public interface IEmailService {

    /**
     * Send order confirmation email with tracking link
     * 
     * @param order The order to send confirmation for
     */
    void sendOrderConfirmationEmail(Order order);

    /**
     * Send order status update email
     * 
     * @param order The order with updated status
     */
    void sendOrderStatusUpdateEmail(Order order);
}
