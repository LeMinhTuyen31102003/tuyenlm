package example.ecommerce.tuyenlm.controller;

import example.ecommerce.tuyenlm.dto.response.CategoryResponse;
import example.ecommerce.tuyenlm.dto.response.ProductResponse;
import example.ecommerce.tuyenlm.service.inter.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Products", description = "API quản lý sản phẩm - Public (không cần đăng nhập)")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @Operation(summary = "Danh sách sản phẩm", description = "Lấy danh sách sản phẩm với phân trang, lọc theo category và khoảng giá")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @Parameter(description = "ID danh mục (1=Áo Thun, 2=Hoodie, 3=Áo Khoác)") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Giá tối thiểu (VND)") @RequestParam(required = false) BigDecimal priceMin,
            @Parameter(description = "Giá tối đa (VND)") @RequestParam(required = false) BigDecimal priceMax,
            @Parameter(description = "Sắp xếp theo: id, name, createdAt, updatedAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp: asc, desc") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số sản phẩm mỗi trang") @RequestParam(defaultValue = "20") int size) {

        // Validate sortBy - only allow Product entity fields
        List<String> validSortFields = List.of("id", "name", "createdAt", "updatedAt");
        if (!validSortFields.contains(sortBy)) {
            sortBy = "createdAt"; // Default to createdAt if invalid field
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductResponse> products = productService.getProducts(
                categoryId, priceMin, priceMax, sortBy, sortDirection, pageable);

        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Chi tiết sản phẩm theo ID", description = "Lấy thông tin chi tiết của sản phẩm bao gồm tất cả variants (size, màu, giá)")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID sản phẩm") @PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Chi tiết sản phẩm theo slug", description = "Lấy thông tin sản phẩm bằng slug (ví dụ: ao-thun-basic-oversize)")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(
            @Parameter(description = "Slug sản phẩm (URL-friendly)") @PathVariable String slug) {
        ProductResponse product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Danh sách danh mục", description = "Lấy tất cả danh mục sản phẩm (Áo Thun, Hoodie, Áo Khoác)")
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        List<CategoryResponse> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
}
