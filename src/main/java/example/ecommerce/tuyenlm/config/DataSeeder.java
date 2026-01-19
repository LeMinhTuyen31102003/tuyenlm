package example.ecommerce.tuyenlm.config;

import example.ecommerce.tuyenlm.entity.*;
import example.ecommerce.tuyenlm.repository.CategoryRepository;
import example.ecommerce.tuyenlm.repository.ProductRepository;
import example.ecommerce.tuyenlm.repository.ProductVariantRepository;
import example.ecommerce.tuyenlm.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void seedData() {
        // Seed admin users first
        seedUsers();

        // Check if data already exists
        if (categoryRepository.count() > 0) {
            log.info("Database already contains data. Skipping seeding.");
            return;
        }

        log.info("Starting database seeding...");

        // Seed categories
        List<Category> categories = seedCategories();

        // Seed products and variants
        seedProducts(categories);

        log.info("Database seeding completed successfully!");
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            log.info("Users already exist. Skipping user seeding.");
            return;
        }

        log.info("Seeding admin users...");

        // Admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("Administrator");
        admin.setEmail("admin@hunghypebeast.com");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        // Warehouse user
        User warehouse = new User();
        warehouse.setUsername("warehouse");
        warehouse.setPassword(passwordEncoder.encode("warehouse123"));
        warehouse.setFullName("Nhân viên kho");
        warehouse.setEmail("warehouse@hunghypebeast.com");
        warehouse.setRole(UserRole.ADMIN); // Cũng dùng ADMIN role cho đơn giản
        warehouse.setActive(true);
        userRepository.save(warehouse);

        log.info("Created 2 admin users: admin/admin123, warehouse/warehouse123");
    }

    private List<Category> seedCategories() {
        log.info("Seeding categories...");

        List<Category> categories = new ArrayList<>();

        Category aoThun = new Category();
        aoThun.setName("Áo Thun");
        aoThun.setSlug("ao-thun");
        aoThun.setDescription("Bộ sưu tập áo thun streetwear chất lượng cao");
        aoThun.setActive(true);
        categories.add(aoThun);

        Category hoodie = new Category();
        hoodie.setName("Hoodie");
        hoodie.setSlug("hoodie");
        hoodie.setDescription("Hoodie oversized phong cách Hàn Quốc");
        hoodie.setActive(true);
        categories.add(hoodie);

        Category aoKhoac = new Category();
        aoKhoac.setName("Áo Khoác");
        aoKhoac.setSlug("ao-khoac");
        aoKhoac.setDescription("Áo khoác bomber, varsity jacket");
        aoKhoac.setActive(true);
        categories.add(aoKhoac);

        categoryRepository.saveAll(categories);
        log.info("Seeded {} categories", categories.size());

        return categories;
    }

    private void seedProducts(List<Category> categories) {
        log.info("Seeding products...");

        Category aoThun = categories.get(0);
        Category hoodie = categories.get(1);
        Category aoKhoac = categories.get(2);

        // Áo Thun products
        createProduct("Áo Thun Oversize Basic", "ao-thun-oversize-basic",
                "Áo thun form oversize 100% cotton cao cấp, thấm hút mồ hôi tốt",
                aoThun, BigDecimal.valueOf(299000),
                new String[] { "S", "M", "L", "XL" },
                new String[] { "Trắng", "Đen", "Xám" },
                new int[] { 50, 100, 30, 20 });

        createProduct("Áo Thun Graphic Tee Limited", "ao-thun-graphic-limited",
                "Áo thun in họa tiết độc quyền, phiên bản giới hạn",
                aoThun, BigDecimal.valueOf(399000),
                new String[] { "M", "L", "XL" },
                new String[] { "Đen", "Trắng" },
                new int[] { 5, 10, 1, 15 });

        createProduct("Áo Thun Polo Vintage", "ao-thun-polo-vintage",
                "Áo polo phong cách retro, chất liệu pique cao cấp",
                aoThun, BigDecimal.valueOf(349000),
                new String[] { "S", "M", "L" },
                new String[] { "Navy", "Xanh rêu" },
                new int[] { 25, 40, 0 });

        // Hoodie products
        createProduct("Hoodie Oversize Street", "hoodie-oversize-street",
                "Hoodie form rộng phong cách streetwear, nỉ ngoại 3 lớp",
                hoodie, BigDecimal.valueOf(599000),
                new String[] { "M", "L", "XL", "XXL" },
                new String[] { "Đen", "Xám", "Be" },
                new int[] { 80, 60, 40, 20 });

        createProduct("Hoodie Zipper Premium", "hoodie-zipper-premium",
                "Hoodie có khóa kéo chất liệu nỉ bông cao cấp",
                hoodie, BigDecimal.valueOf(649000),
                new String[] { "L", "XL" },
                new String[] { "Đen", "Xanh navy" },
                new int[] { 30, 15 });

        createProduct("Hoodie Graphic Limited Edition", "hoodie-graphic-limited",
                "Hoodie in họa tiết nghệ thuật, số lượng giới hạn",
                hoodie, BigDecimal.valueOf(799000),
                new String[] { "M", "L" },
                new String[] { "Đen" },
                new int[] { 2, 0 });

        // Áo Khoác products
        createProduct("Bomber Jacket Classic", "bomber-jacket-classic",
                "Áo bomber jacket phong cách pilot cổ điển",
                aoKhoac, BigDecimal.valueOf(899000),
                new String[] { "S", "M", "L", "XL" },
                new String[] { "Đen", "Xanh rêu" },
                new int[] { 20, 35, 25, 10 });

        createProduct("Varsity Jacket Retro", "varsity-jacket-retro",
                "Áo khoác varsity phong cách Mỹ cổ điển",
                aoKhoac, BigDecimal.valueOf(1099000),
                new String[] { "M", "L", "XL" },
                new String[] { "Đen/Trắng", "Navy/Xám" },
                new int[] { 15, 20, 5 });

        createProduct("Windbreaker Sport", "windbreaker-sport",
                "Áo khoác gió thể thao, chống nước nhẹ",
                aoKhoac, BigDecimal.valueOf(749000),
                new String[] { "S", "M", "L" },
                new String[] { "Đen", "Xanh dương" },
                new int[] { 40, 50, 30 });

        createProduct("Denim Jacket Vintage", "denim-jacket-vintage",
                "Áo khoác jean wash nhẹ phong cách vintage",
                aoKhoac, BigDecimal.valueOf(849000),
                new String[] { "M", "L", "XL" },
                new String[] { "Blue", "Black" },
                new int[] { 25, 30, 15 });

        log.info("Seeding completed!");
    }

    private void createProduct(String name, String slug, String description,
            Category category, BigDecimal basePrice,
            String[] sizes, String[] colors, int[] stocks) {
        Product product = new Product();
        product.setName(name);
        product.setSlug(slug);
        product.setDescription(description);
        product.setCategory(category);
        product.setActive(true);

        product = productRepository.save(product);

        // Create variants for each size-color combination
        int stockIndex = 0;
        for (String size : sizes) {
            for (String color : colors) {
                if (stockIndex < stocks.length) {
                    createVariant(product, size, color, basePrice, stocks[stockIndex]);
                    stockIndex++;
                }
            }
        }

        log.info("Created product: {} with {} variants", name, stockIndex);
    }

    private void createVariant(Product product, String size, String color,
            BigDecimal price, int stock) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(generateSKU(product.getSlug(), size, color));
        variant.setSize(size);
        variant.setColor(color);
        variant.setPrice(price);
        variant.setStockQuantity(stock);
        variant.setActive(true);

        variantRepository.save(variant);
    }

    private String generateSKU(String slug, String size, String color) {
        String slugPart = slug.substring(0, Math.min(8, slug.length())).toUpperCase().replace("-", "");
        String sizePart = size.replace(" ", "").toUpperCase();
        String colorPart = color.substring(0, Math.min(3, color.length())).toUpperCase();

        return String.format("%s-%s-%s", slugPart, sizePart, colorPart);
    }
}
