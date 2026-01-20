package example.ecommerce.tuyenlm.config;

import example.ecommerce.tuyenlm.entity.*;
import example.ecommerce.tuyenlm.enums.UserRole;
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

                // Check if we already have 50+ products
                long productCount = productRepository.count();
                if (productCount >= 50) {
                        log.info("Database already contains {} products. Skipping seeding.", productCount);
                        return;
                }

                log.info("Starting database seeding (current products: {})...", productCount);

                // Seed categories (or get existing ones)
                List<Category> categories = seedCategories();

                // Clear old products if needed
                if (productCount > 0 && productCount < 50) {
                        log.info("Clearing old product data to reseed...");
                        variantRepository.deleteAll();
                        productRepository.deleteAll();
                }

                // Seed products and variants
                seedProducts(categories);

                log.info("Database seeding completed successfully! Total products: {}", productRepository.count());
        }

        private void seedUsers() {
                if (userRepository.count() > 0) {
                        log.info("Users already exist. Skipping user seeding.");
                        return;
                }

                log.info("Seeding admin user...");

                // Admin user
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("Administrator");
                admin.setEmail("admin@hunghypebeast.com");
                admin.setRole(UserRole.ADMIN);
                admin.setActive(true);
                userRepository.save(admin);

                log.info("Created admin user: admin/admin123");
        }

        private List<Category> seedCategories() {
                log.info("Seeding categories...");

                List<Category> categories = new ArrayList<>();

                // Check if categories already exist, reuse them
                Category aoThun = categoryRepository.findBySlug("ao-thun").orElseGet(() -> {
                        Category cat = new Category();
                        cat.setName("Áo Thun");
                        cat.setSlug("ao-thun");
                        cat.setDescription("Bộ sưu tập áo thun streetwear chất lượng cao");
                        cat.setActive(true);
                        return cat;
                });
                categories.add(aoThun);

                Category hoodie = categoryRepository.findBySlug("hoodie").orElseGet(() -> {
                        Category cat = new Category();
                        cat.setName("Hoodie");
                        cat.setSlug("hoodie");
                        cat.setDescription("Hoodie oversized phong cách Hàn Quốc");
                        cat.setActive(true);
                        return cat;
                });
                categories.add(hoodie);

                Category aoKhoac = categoryRepository.findBySlug("ao-khoac").orElseGet(() -> {
                        Category cat = new Category();
                        cat.setName("Áo Khoác");
                        cat.setSlug("ao-khoac");
                        cat.setDescription("Áo khoác bomber, varsity jacket");
                        cat.setActive(true);
                        return cat;
                });
                categories.add(aoKhoac);

                categoryRepository.saveAll(categories);
                log.info("Categories ready: {} categories", categories.size());

                return categories;
        }

        private void seedProducts(List<Category> categories) {
                log.info("Seeding products...");

                Category aoThun = categories.get(0);
                Category hoodie = categories.get(1);
                Category aoKhoac = categories.get(2);

                // ===== ÁO THUN CATEGORY (20 products) =====
                createProduct("Áo Thun Oversize Basic", "ao-thun-oversize-basic",
                                "Áo thun form oversize 100% cotton cao cấp, thấm hút mồ hôi tốt",
                                aoThun, BigDecimal.valueOf(299000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Trắng", "Đen", "Xám" },
                                new int[] { 50, 100, 30, 20, 45, 80, 25, 15, 35, 60, 20, 10 });

                createProduct("Áo Thun Graphic Tee Dragon", "ao-thun-graphic-dragon",
                                "Áo thun in họa tiết rồng phương Đông, phiên bản giới hạn",
                                aoThun, BigDecimal.valueOf(399000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Trắng" },
                                new int[] { 5, 10, 1, 15, 8, 12 });

                createProduct("Áo Thun Polo Vintage", "ao-thun-polo-vintage",
                                "Áo polo phong cách retro, chất liệu pique cao cấp",
                                aoThun, BigDecimal.valueOf(349000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Navy", "Xanh rêu" },
                                new int[] { 25, 40, 30, 20, 15, 35, 25, 18 });

                createProduct("Áo Thun Tie Dye Psychedelic", "ao-thun-tie-dye",
                                "Áo thun nhuộm batik màu sắc độc đáo, không có 2 chiếc giống nhau",
                                aoThun, BigDecimal.valueOf(449000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Rainbow", "Purple Mix" },
                                new int[] { 12, 18, 8, 15, 20, 10 });

                createProduct("Áo Thun Striped Sailor", "ao-thun-striped-sailor",
                                "Áo thun kẻ sọc phong cách thủy thủ Pháp classic",
                                aoThun, BigDecimal.valueOf(329000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Đen/Trắng", "Navy/Trắng" },
                                new int[] { 30, 45, 35, 25, 28, 42, 32, 22 });

                createProduct("Áo Thun Typography Minimal", "ao-thun-typography",
                                "Áo thun in chữ minimalist, phong cách Hàn Quốc",
                                aoThun, BigDecimal.valueOf(319000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Trắng", "Xám", "Be" },
                                new int[] { 40, 50, 30, 45, 55, 35, 38, 48, 28 });

                createProduct("Áo Thun Pocket Tee Premium", "ao-thun-pocket-premium",
                                "Áo thun có túi ngực, cotton Mỹ cao cấp 220gsm",
                                aoThun, BigDecimal.valueOf(369000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Đen", "Trắng", "Olive" },
                                new int[] { 35, 50, 40, 25, 32, 48, 38, 22, 30, 45, 35, 20 });

                createProduct("Áo Thun Vintage Wash", "ao-thun-vintage-wash",
                                "Áo thun wash cổ điển, hiệu ứng phai màu tự nhiên",
                                aoThun, BigDecimal.valueOf(389000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Black Wash", "Grey Wash" },
                                new int[] { 22, 35, 18, 25, 38, 20 });

                createProduct("Áo Thun Henley Collar", "ao-thun-henley",
                                "Áo thun cổ henley có cúc, phong cách casual",
                                aoThun, BigDecimal.valueOf(359000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Đen", "Navy", "Xám" },
                                new int[] { 28, 42, 32, 20, 25, 40, 30, 18, 22, 38, 28, 15 });

                createProduct("Áo Thun Ringer Tee Retro", "ao-thun-ringer-retro",
                                "Áo thun ringer cổ và tay viền màu phong cách 70s",
                                aoThun, BigDecimal.valueOf(339000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Trắng/Đỏ", "Trắng/Xanh" },
                                new int[] { 18, 28, 15, 20, 30, 18 });

                createProduct("Áo Thun Dropped Shoulder", "ao-thun-dropped-shoulder",
                                "Áo thun vai rơi form boxy, phong cách unisex",
                                aoThun, BigDecimal.valueOf(349000),
                                new String[] { "M", "L", "XL", "XXL" },
                                new String[] { "Đen", "Trắng", "Xám" },
                                new int[] { 35, 48, 38, 25, 32, 45, 35, 22, 28, 42, 32, 20 });

                createProduct("Áo Thun Longline Extended", "ao-thun-longline",
                                "Áo thun form dài qua mông, streetwear style",
                                aoThun, BigDecimal.valueOf(379000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Xám", "Trắng" },
                                new int[] { 25, 38, 20, 22, 35, 18, 20, 32, 15 });

                createProduct("Áo Thun Raglan Sleeve", "ao-thun-raglan",
                                "Áo thun tay raglan phối màu baseball style",
                                aoThun, BigDecimal.valueOf(329000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Trắng/Đen", "Xám/Navy" },
                                new int[] { 30, 45, 35, 22, 28, 42, 32, 20 });

                createProduct("Áo Thun Cropped Fit", "ao-thun-cropped",
                                "Áo thun form ngắn crop, phong cách Y2K",
                                aoThun, BigDecimal.valueOf(299000),
                                new String[] { "S", "M", "L" },
                                new String[] { "Đen", "Trắng", "Baby Pink" },
                                new int[] { 40, 55, 35, 38, 52, 32, 35, 48, 28 });

                createProduct("Áo Thun Embroidery Logo", "ao-thun-embroidery",
                                "Áo thun thêu logo ngực, cao cấp premium",
                                aoThun, BigDecimal.valueOf(429000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Navy", "Trắng" },
                                new int[] { 20, 32, 18, 18, 30, 15, 22, 35, 20 });

                createProduct("Áo Thun Muscle Fit", "ao-thun-muscle-fit",
                                "Áo thun form ôm tôn dáng, co giãn 4 chiều",
                                aoThun, BigDecimal.valueOf(359000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Đen", "Xám", "Navy" },
                                new int[] { 35, 48, 38, 25, 32, 45, 35, 22, 28, 42, 32, 20 });

                createProduct("Áo Thun Scoop Neck Deep", "ao-thun-scoop-neck",
                                "Áo thun cổ tim sâu, phong cách layer",
                                aoThun, BigDecimal.valueOf(309000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Trắng", "Đen", "Xám" },
                                new int[] { 28, 40, 25, 25, 38, 22, 22, 35, 18 });

                createProduct("Áo Thun Tank Top Sleeveless", "ao-thun-tank-top",
                                "Áo thun ba lỗ gym wear, thấm hút mồ hôi tốt",
                                aoThun, BigDecimal.valueOf(249000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Đen", "Trắng", "Xám" },
                                new int[] { 50, 65, 45, 30, 48, 62, 42, 28, 42, 58, 38, 25 });

                createProduct("Áo Thun Acid Wash Grunge", "ao-thun-acid-wash",
                                "Áo thun acid wash hiệu ứng mài, phong cách grunge",
                                aoThun, BigDecimal.valueOf(449000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Black Acid", "Grey Acid" },
                                new int[] { 15, 25, 12, 18, 28, 15 });

                createProduct("Áo Thun Color Block Panel", "ao-thun-color-block",
                                "Áo thun phối màu khối geometric hiện đại",
                                aoThun, BigDecimal.valueOf(389000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Multi Color A", "Multi Color B" },
                                new int[] { 20, 30, 15, 22, 32, 18 });

                // ===== HOODIE CATEGORY (15 products) =====
                createProduct("Hoodie Oversize Street", "hoodie-oversize-street",
                                "Hoodie form rộng phong cách streetwear, nỉ ngoại 3 lớp",
                                hoodie, BigDecimal.valueOf(599000),
                                new String[] { "M", "L", "XL", "XXL" },
                                new String[] { "Đen", "Xám", "Be" },
                                new int[] { 80, 60, 40, 20, 75, 55, 38, 18, 70, 52, 35, 15 });

                createProduct("Hoodie Zipper Premium", "hoodie-zipper-premium",
                                "Hoodie có khóa kéo chất liệu nỉ bông cao cấp",
                                hoodie, BigDecimal.valueOf(649000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Navy", "Xanh rêu" },
                                new int[] { 30, 45, 25, 28, 42, 22, 25, 38, 20 });

                createProduct("Hoodie Graphic Limited Edition", "hoodie-graphic-limited",
                                "Hoodie in họa tiết nghệ thuật đường phố, số lượng giới hạn",
                                hoodie, BigDecimal.valueOf(799000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Trắng" },
                                new int[] { 2, 5, 1, 3, 6, 2 });

                createProduct("Hoodie Cropped Short", "hoodie-cropped-short",
                                "Hoodie form ngắn crop, phong cách streetwear nữ",
                                hoodie, BigDecimal.valueOf(579000),
                                new String[] { "S", "M", "L" },
                                new String[] { "Đen", "Xám", "Pink" },
                                new int[] { 35, 50, 30, 32, 48, 28, 30, 45, 25 });

                createProduct("Hoodie Tech Fleece Sport", "hoodie-tech-fleece",
                                "Hoodie tech fleece nhẹ, thoáng khí cho thể thao",
                                hoodie, BigDecimal.valueOf(729000),
                                new String[] { "M", "L", "XL", "XXL" },
                                new String[] { "Đen", "Navy" },
                                new int[] { 40, 55, 35, 20, 38, 52, 32, 18 });

                createProduct("Hoodie Heavyweight 400GSM", "hoodie-heavyweight",
                                "Hoodie nỉ dày 400gsm, ấm áp mùa đông",
                                hoodie, BigDecimal.valueOf(699000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Xám", "Burgundy" },
                                new int[] { 35, 48, 28, 32, 45, 25, 30, 42, 22 });

                createProduct("Hoodie Color Block Retro", "hoodie-color-block",
                                "Hoodie phối màu khối phong cách retro 90s",
                                hoodie, BigDecimal.valueOf(649000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Multi A", "Multi B" },
                                new int[] { 25, 38, 20, 28, 40, 22 });

                createProduct("Hoodie Tie Dye Unique", "hoodie-tie-dye",
                                "Hoodie nhuộm batik độc đáo, mỗi chiếc một màu",
                                hoodie, BigDecimal.valueOf(759000),
                                new String[] { "L", "XL" },
                                new String[] { "Purple Mix", "Blue Mix" },
                                new int[] { 15, 10, 18, 12 });

                createProduct("Hoodie Embroidery Dragon", "hoodie-embroidery-dragon",
                                "Hoodie thêu rồng sau lưng, cao cấp premium",
                                hoodie, BigDecimal.valueOf(899000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Navy" },
                                new int[] { 12, 20, 8, 15, 22, 10 });

                createProduct("Hoodie Half Zip Pullover", "hoodie-half-zip",
                                "Hoodie khóa kéo nửa ngực, phong cách preppy",
                                hoodie, BigDecimal.valueOf(629000),
                                new String[] { "M", "L", "XL", "XXL" },
                                new String[] { "Navy", "Xám", "Đen" },
                                new int[] { 32, 45, 28, 18, 30, 42, 25, 15, 28, 40, 22, 12 });

                createProduct("Hoodie Patchwork Vintage", "hoodie-patchwork",
                                "Hoodie ghép vải patchwork phong cách vintage",
                                hoodie, BigDecimal.valueOf(849000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Multi Patch" },
                                new int[] { 10, 15, 8 });

                createProduct("Hoodie Sherpa Lined Winter", "hoodie-sherpa-lined",
                                "Hoodie lót lông cừu sherpa, siêu ấm mùa đông",
                                hoodie, BigDecimal.valueOf(999000),
                                new String[] { "L", "XL", "XXL" },
                                new String[] { "Đen", "Xám" },
                                new int[] { 20, 15, 10, 22, 18, 12 });

                createProduct("Hoodie Reflective Print Night", "hoodie-reflective",
                                "Hoodie in phản quang, nổi bật ban đêm",
                                hoodie, BigDecimal.valueOf(729000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Navy" },
                                new int[] { 18, 28, 15, 20, 30, 18 });

                createProduct("Hoodie Asymmetric Cut", "hoodie-asymmetric",
                                "Hoodie cắt bất đối xứng, thiết kế độc đáo",
                                hoodie, BigDecimal.valueOf(779000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Xám" },
                                new int[] { 12, 20, 10, 15, 22, 12 });

                createProduct("Hoodie Distressed Grunge", "hoodie-distressed",
                                "Hoodie rách bạc màu phong cách grunge",
                                hoodie, BigDecimal.valueOf(699000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Black Wash", "Grey Wash" },
                                new int[] { 15, 25, 12, 18, 28, 15 });

                // ===== ÁO KHOÁC CATEGORY (15 products) =====
                createProduct("Bomber Jacket Classic MA-1", "bomber-jacket-ma1",
                                "Áo bomber jacket MA-1 phong cách pilot cổ điển",
                                aoKhoac, BigDecimal.valueOf(899000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Đen", "Xanh rêu", "Navy" },
                                new int[] { 20, 35, 25, 10, 18, 32, 22, 8, 15, 28, 18, 5 });

                createProduct("Varsity Jacket Retro College", "varsity-jacket-retro",
                                "Áo khoác varsity phong cách đại học Mỹ cổ điển",
                                aoKhoac, BigDecimal.valueOf(1099000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen/Trắng", "Navy/Xám", "Burgundy/Be" },
                                new int[] { 15, 20, 5, 18, 22, 8, 12, 18, 5 });

                createProduct("Windbreaker Sport Track", "windbreaker-sport",
                                "Áo khoác gió thể thao, chống nước nhẹ thoáng khí",
                                aoKhoac, BigDecimal.valueOf(749000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Đen", "Xanh dương", "Đỏ" },
                                new int[] { 40, 50, 30, 20, 38, 48, 28, 18, 35, 45, 25, 15 });

                createProduct("Denim Jacket Vintage Wash", "denim-jacket-vintage",
                                "Áo khoác jean wash cổ điển, hiệu ứng phai tự nhiên",
                                aoKhoac, BigDecimal.valueOf(849000),
                                new String[] { "M", "L", "XL", "XXL" },
                                new String[] { "Blue Wash", "Black Wash" },
                                new int[] { 25, 30, 15, 10, 28, 32, 18, 12 });

                createProduct("Coach Jacket Waterproof", "coach-jacket-waterproof",
                                "Áo khoác coach chống thấm nước, có lót lưới",
                                aoKhoac, BigDecimal.valueOf(679000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Navy", "Olive" },
                                new int[] { 30, 42, 25, 28, 40, 22, 25, 38, 20 });

                createProduct("Leather Jacket Biker", "leather-jacket-biker",
                                "Áo khoác da PU phong cách biker motor",
                                aoKhoac, BigDecimal.valueOf(1299000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Brown" },
                                new int[] { 12, 18, 8, 15, 20, 10 });

                createProduct("Parka Jacket Winter Long", "parka-jacket-winter",
                                "Áo khoác parka dài, lót lông ấm mùa đông",
                                aoKhoac, BigDecimal.valueOf(1499000),
                                new String[] { "M", "L", "XL", "XXL" },
                                new String[] { "Đen", "Xanh rêu", "Xám" },
                                new int[] { 15, 20, 12, 8, 18, 22, 15, 10, 12, 18, 10, 6 });

                createProduct("Blazer Jacket Formal", "blazer-jacket-formal",
                                "Áo khoác blazer lịch sự, phong cách công sở",
                                aoKhoac, BigDecimal.valueOf(999000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Đen", "Navy", "Xám" },
                                new int[] { 18, 28, 22, 12, 20, 30, 25, 15, 15, 25, 18, 10 });

                createProduct("Trucker Jacket Sherpa", "trucker-jacket-sherpa",
                                "Áo khoác trucker lót lông cừu, ấm áp vintage",
                                aoKhoac, BigDecimal.valueOf(1149000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Blue Denim", "Black Denim" },
                                new int[] { 20, 28, 15, 22, 30, 18 });

                createProduct("Harrington Jacket Classic", "harrington-jacket-classic",
                                "Áo khoác harrington cổ điển Anh Quốc, có lót caro",
                                aoKhoac, BigDecimal.valueOf(879000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Đen", "Navy", "Tan" },
                                new int[] { 22, 32, 25, 15, 25, 35, 28, 18, 20, 30, 22, 12 });

                createProduct("Puffer Jacket Down", "puffer-jacket-down",
                                "Áo khoác phao lông vũ siêu nhẹ siêu ấm",
                                aoKhoac, BigDecimal.valueOf(1399000),
                                new String[] { "M", "L", "XL", "XXL" },
                                new String[] { "Đen", "Navy", "Red" },
                                new int[] { 18, 25, 15, 10, 20, 28, 18, 12, 15, 22, 12, 8 });

                createProduct("Track Jacket Retro Sport", "track-jacket-retro",
                                "Áo khoác track phong cách thể thao retro 80s",
                                aoKhoac, BigDecimal.valueOf(729000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Navy/Trắng", "Đen/Vàng", "Đỏ/Xanh" },
                                new int[] { 25, 35, 20, 28, 38, 22, 22, 32, 18 });

                createProduct("Anorak Pullover Half Zip", "anorak-pullover",
                                "Áo khoác anorak kéo qua đầu, phong cách outdoor",
                                aoKhoac, BigDecimal.valueOf(799000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Đen", "Olive", "Navy" },
                                new int[] { 20, 30, 18, 22, 32, 20, 18, 28, 15 });

                createProduct("Safari Jacket Utility", "safari-jacket-utility",
                                "Áo khoác safari đa túi, phong cách military",
                                aoKhoac, BigDecimal.valueOf(949000),
                                new String[] { "M", "L", "XL" },
                                new String[] { "Beige", "Olive", "Khaki" },
                                new int[] { 15, 22, 12, 18, 25, 15, 12, 20, 10 });

                createProduct("Fleece Jacket Zip Through", "fleece-jacket-zip",
                                "Áo khoác nỉ lông cừu nhẹ, ấm áp thoải mái",
                                aoKhoac, BigDecimal.valueOf(679000),
                                new String[] { "S", "M", "L", "XL" },
                                new String[] { "Đen", "Xám", "Navy", "Burgundy" },
                                new int[] { 30, 42, 32, 20, 28, 40, 30, 18, 25, 38, 28, 15, 22, 35, 25, 12 });

                log.info("Seeding completed! Created 50 products with multiple variants.");
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
                // Use entire slug to ensure uniqueness across products
                String slugPart = slug.toUpperCase().replace("-", "");
                String sizePart = size.replace(" ", "").toUpperCase();
                String colorPart = color.toUpperCase();

                return String.format("%s-%s-%s", slugPart, sizePart, colorPart);
        }
}
