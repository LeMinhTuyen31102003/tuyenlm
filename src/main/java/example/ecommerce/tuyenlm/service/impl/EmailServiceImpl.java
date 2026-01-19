package example.ecommerce.tuyenlm.service.impl;

import example.ecommerce.tuyenlm.entity.Order;
import example.ecommerce.tuyenlm.entity.OrderItem;
import example.ecommerce.tuyenlm.service.inter.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.sender-name}")
    private String senderName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void sendOrderConfirmationEmail(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, senderName);
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("✅ Đơn hàng " + order.getOrderNumber() + " đã được tạo thành công!");

            String htmlContent = buildOrderConfirmationEmailHtml(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Order confirmation email sent to: {} for order: {}",
                    order.getCustomerEmail(), order.getOrderNumber());

        } catch (MessagingException e) {
            log.error("Failed to send order confirmation email for order: {}", order.getOrderNumber(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email for order: {}", order.getOrderNumber(), e);
        }
    }

    @Override
    public void sendOrderStatusUpdateEmail(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, senderName);
            helper.setTo(order.getCustomerEmail());
            helper.setSubject(
                    "🔔 Cập nhật đơn hàng " + order.getOrderNumber() + " - " + getStatusText(order.getStatus().name()));

            String htmlContent = buildOrderStatusUpdateEmailHtml(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Order status update email sent to: {} for order: {} - status: {}",
                    order.getCustomerEmail(), order.getOrderNumber(), order.getStatus());

        } catch (MessagingException e) {
            log.error("Failed to send status update email for order: {}", order.getOrderNumber(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending status update email for order: {}", order.getOrderNumber(), e);
        }
    }

    private String buildOrderConfirmationEmailHtml(Order order) {
        String trackingUrl = frontendUrl + "/track/" + order.getTrackingToken();

        StringBuilder itemsHtml = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            itemsHtml.append(String.format(
                    "<tr>" +
                            "<td style='padding: 12px; border-bottom: 1px solid #eee;'>%s</td>" +
                            "<td style='padding: 12px; border-bottom: 1px solid #eee; text-align: center;'>x%d</td>" +
                            "<td style='padding: 12px; border-bottom: 1px solid #eee; text-align: right;'>%s</td>" +
                            "</tr>",
                    item.getNameSnapshot(),
                    item.getQuantity(),
                    currencyFormat.format(item.getPriceSnapshot())));
        }

        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        </head>
                        <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                            <div style="max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">

                                <!-- Header -->
                                <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center;">
                                    <h1 style="color: white; margin: 0; font-size: 28px;">🎉 Cảm ơn bạn đã mua hàng!</h1>
                                </div>

                                <!-- Content -->
                                <div style="padding: 30px;">
                                    <p style="font-size: 16px; color: #333; margin-bottom: 20px;">
                                        Xin chào <strong>%s</strong>,
                                    </p>

                                    <p style="font-size: 14px; color: #666; margin-bottom: 20px;">
                                        Chúng tôi đã nhận được đơn hàng của bạn và đang xử lý. Dưới đây là thông tin chi tiết:
                                    </p>

                                    <!-- Order Info Box -->
                                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 6px; margin-bottom: 20px;">
                                        <table style="width: 100%%; border-collapse: collapse;">
                                            <tr>
                                                <td style="padding: 8px 0; color: #666;">Mã đơn hàng:</td>
                                                <td style="padding: 8px 0; text-align: right; font-weight: bold; color: #333;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 8px 0; color: #666;">Ngày đặt:</td>
                                                <td style="padding: 8px 0; text-align: right; color: #333;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 8px 0; color: #666;">Phương thức thanh toán:</td>
                                                <td style="padding: 8px 0; text-align: right; color: #333;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 8px 0; color: #666;">Trạng thái:</td>
                                                <td style="padding: 8px 0; text-align: right;">
                                                    <span style="background-color: #ffc107; color: white; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: bold;">
                                                        %s
                                                    </span>
                                                </td>
                                            </tr>
                                        </table>
                                    </div>

                                    <!-- Order Items -->
                                    <h3 style="color: #333; font-size: 18px; margin-bottom: 15px;">Sản phẩm đã đặt:</h3>
                                    <table style="width: 100%%; border-collapse: collapse; margin-bottom: 20px;">
                                        <thead>
                                            <tr style="background-color: #f8f9fa;">
                                                <th style="padding: 12px; text-align: left; color: #666; font-weight: 600;">Sản phẩm</th>
                                                <th style="padding: 12px; text-align: center; color: #666; font-weight: 600;">Số lượng</th>
                                                <th style="padding: 12px; text-align: right; color: #666; font-weight: 600;">Đơn giá</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            %s
                                        </tbody>
                                    </table>

                                    <!-- Total -->
                                    <div style="text-align: right; margin-top: 20px; padding-top: 20px; border-top: 2px solid #667eea;">
                                        <p style="font-size: 18px; color: #333; margin: 5px 0;">
                                            <strong>Tổng cộng: <span style="color: #667eea;">%s</span></strong>
                                        </p>
                                    </div>

                                    <!-- Shipping Address -->
                                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 6px; margin: 20px 0;">
                                        <h4 style="color: #333; margin: 0 0 10px 0; font-size: 14px;">📍 Địa chỉ giao hàng:</h4>
                                        <p style="margin: 0; color: #666; font-size: 14px;">%s</p>
                                        <p style="margin: 5px 0 0 0; color: #666; font-size: 14px;">📞 %s</p>
                                    </div>

                                    <!-- Tracking Button -->
                                    <div style="text-align: center; margin: 30px 0;">
                                        <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; padding: 15px 40px; border-radius: 6px; font-weight: bold; font-size: 16px;">
                                            🔍 Theo dõi đơn hàng
                                        </a>
                                    </div>

                                    <p style="font-size: 12px; color: #999; text-align: center; margin: 20px 0;">
                                        Hoặc copy link sau vào trình duyệt:<br>
                                        <a href="%s" style="color: #667eea; word-break: break-all;">%s</a>
                                    </p>

                                    <div style="background-color: #e3f2fd; padding: 15px; border-radius: 6px; border-left: 4px solid #2196f3; margin-top: 20px;">
                                        <p style="margin: 0; font-size: 14px; color: #1976d2;">
                                            💡 <strong>Lưu ý:</strong> Bạn có thể theo dõi trạng thái đơn hàng bất cứ lúc nào bằng cách click vào link trên.
                                            Không cần đăng nhập hay tạo tài khoản!
                                        </p>
                                    </div>
                                </div>

                                <!-- Footer -->
                                <div style="background-color: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #eee;">
                                    <p style="margin: 0; font-size: 14px; color: #666;">
                                        Cảm ơn bạn đã tin tưởng <strong>%s</strong>! 🙏
                                    </p>
                                    <p style="margin: 10px 0 0 0; font-size: 12px; color: #999;">
                                        Email này được gửi tự động, vui lòng không trả lời.
                                    </p>
                                </div>

                            </div>
                        </body>
                        </html>
                        """,
                order.getCustomerName(),
                order.getOrderNumber(),
                order.getCreatedAt().format(dateFormatter),
                getPaymentMethodText(order.getPaymentMethod().name()),
                getStatusText(order.getStatus().name()),
                itemsHtml.toString(),
                currencyFormat.format(order.getTotal()),
                order.getShippingAddress(),
                order.getCustomerPhone(),
                trackingUrl,
                trackingUrl,
                trackingUrl,
                senderName);
    }

    private String buildOrderStatusUpdateEmailHtml(Order order) {
        String trackingUrl = frontendUrl + "/track/" + order.getTrackingToken();
        String statusColor = getStatusColor(order.getStatus().name());
        String statusIcon = getStatusIcon(order.getStatus().name());

        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        </head>
                        <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                            <div style="max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">

                                <!-- Header -->
                                <div style="background: linear-gradient(135deg, %s 0%%, %s 100%%); padding: 30px; text-align: center;">
                                    <h1 style="color: white; margin: 0; font-size: 28px;">%s Cập nhật đơn hàng</h1>
                                </div>

                                <!-- Content -->
                                <div style="padding: 30px;">
                                    <p style="font-size: 16px; color: #333; margin-bottom: 20px;">
                                        Xin chào <strong>%s</strong>,
                                    </p>

                                    <p style="font-size: 14px; color: #666; margin-bottom: 20px;">
                                        Đơn hàng <strong>%s</strong> của bạn đã được cập nhật trạng thái:
                                    </p>

                                    <!-- Status Update Box -->
                                    <div style="background: linear-gradient(135deg, %s 0%%, %s 100%%); padding: 25px; border-radius: 8px; text-align: center; margin: 25px 0;">
                                        <p style="color: white; font-size: 48px; margin: 0;">%s</p>
                                        <h2 style="color: white; margin: 10px 0 0 0; font-size: 24px;">%s</h2>
                                    </div>

                                    <p style="font-size: 14px; color: #666; margin: 20px 0;">
                                        %s
                                    </p>

                                    <!-- Tracking Button -->
                                    <div style="text-align: center; margin: 30px 0;">
                                        <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; padding: 15px 40px; border-radius: 6px; font-weight: bold; font-size: 16px;">
                                            🔍 Xem chi tiết đơn hàng
                                        </a>
                                    </div>
                                </div>

                                <!-- Footer -->
                                <div style="background-color: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #eee;">
                                    <p style="margin: 0; font-size: 14px; color: #666;">
                                        Cảm ơn bạn đã tin tưởng <strong>%s</strong>! 🙏
                                    </p>
                                    <p style="margin: 10px 0 0 0; font-size: 12px; color: #999;">
                                        Email này được gửi tự động, vui lòng không trả lời.
                                    </p>
                                </div>

                            </div>
                        </body>
                        </html>
                        """,
                statusColor, statusColor,
                statusIcon,
                order.getCustomerName(),
                order.getOrderNumber(),
                statusColor, statusColor,
                statusIcon,
                getStatusText(order.getStatus().name()),
                getStatusMessage(order.getStatus().name()),
                trackingUrl,
                senderName);
    }

    private String getPaymentMethodText(String method) {
        return switch (method) {
            case "COD" -> "💵 COD (Thanh toán khi nhận hàng)";
            case "BANK_TRANSFER" -> "🏦 Chuyển khoản";
            default -> method;
        };
    }

    private String getStatusText(String status) {
        return switch (status) {
            case "PENDING" -> "Chờ xác nhận";
            case "PAID" -> "Đã thanh toán";
            case "CONFIRMED" -> "Đã xác nhận";
            case "PROCESSING" -> "Đang chuẩn bị hàng";
            case "SHIPPING" -> "Đang giao hàng";
            case "DELIVERED" -> "Đã giao hàng";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "PENDING" -> "#ffc107";
            case "PAID" -> "#28a745";
            case "CONFIRMED" -> "#17a2b8";
            case "PROCESSING" -> "#007bff";
            case "SHIPPING" -> "#6f42c1";
            case "DELIVERED" -> "#28a745";
            case "CANCELLED" -> "#dc3545";
            default -> "#6c757d";
        };
    }

    private String getStatusIcon(String status) {
        return switch (status) {
            case "PENDING" -> "⏳";
            case "PAID" -> "✅";
            case "CONFIRMED" -> "✅";
            case "PROCESSING" -> "📦";
            case "SHIPPING" -> "🚚";
            case "DELIVERED" -> "🎉";
            case "CANCELLED" -> "❌";
            default -> "🔔";
        };
    }

    private String getStatusMessage(String status) {
        return switch (status) {
            case "PENDING" -> "Đơn hàng của bạn đang chờ được xác nhận. Chúng tôi sẽ liên hệ với bạn sớm nhất!";
            case "PAID" -> "Chúng tôi đã nhận được thanh toán của bạn. Đơn hàng sẽ được xác nhận và xử lý ngay!";
            case "CONFIRMED" -> "Đơn hàng đã được xác nhận thành công! Chúng tôi đang chuẩn bị hàng cho bạn.";
            case "PROCESSING" -> "Đơn hàng đang được đóng gói cẩn thận. Sẽ sớm được giao đến bạn!";
            case "SHIPPING" -> "Đơn hàng đang trên đường giao đến bạn! Vui lòng để ý điện thoại để nhận hàng.";
            case "DELIVERED" -> "Đơn hàng đã được giao thành công! Cảm ơn bạn đã mua hàng. Hẹn gặp lại! 🎉";
            case "CANCELLED" -> "Đơn hàng đã bị hủy. Nếu có thắc mắc, vui lòng liên hệ với chúng tôi.";
            default -> "Trạng thái đơn hàng đã được cập nhật.";
        };
    }
}
