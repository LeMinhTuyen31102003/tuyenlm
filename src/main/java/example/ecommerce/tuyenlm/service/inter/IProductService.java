package example.ecommerce.tuyenlm.service.inter;

import example.ecommerce.tuyenlm.dto.response.CategoryResponse;
import example.ecommerce.tuyenlm.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface IProductService {
    Page<ProductResponse> getProducts(Long categoryId, BigDecimal priceMin, BigDecimal priceMax,
            String sortBy, String sortDirection, Pageable pageable);

    ProductResponse getProductById(Long id);

    ProductResponse getProductBySlug(String slug);

    List<CategoryResponse> getAllCategories();
}
