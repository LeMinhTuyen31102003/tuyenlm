package example.ecommerce.tuyenlm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.frontend.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API - Hung Hypebeast")
                        .description("REST API cho hệ thống bán hàng thời trang streetwear\n\n" +
                                "## Luồng khách hàng (Guest Checkout)\n" +
                                "1. Xem danh sách sản phẩm (catalog)\n" +
                                "2. Thêm vào giỏ hàng (không cần đăng nhập)\n" +
                                "3. Thanh toán (COD hoặc chuyển khoản)\n" +
                                "4. Theo dõi đơn hàng qua tracking token\n\n" +
                                "## Luồng Admin\n" +
                                "1. Đăng nhập để lấy JWT token\n" +
                                "2. Click 'Authorize' ở góc trên bên phải\n" +
                                "3. Nhập: `Bearer {token}` (có chữ Bearer + khoảng trắng)\n" +
                                "4. Quản lý đơn hàng\n\n" +
                                "## Tài khoản test:\n" +
                                "- **Admin:** username: `admin`, password: `admin123`")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hung Hypebeast Team")
                                .email("support@hunghypebeast.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description("Server chính")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT token sau khi đăng nhập. Format: `Bearer {token}`")));
    }
}
