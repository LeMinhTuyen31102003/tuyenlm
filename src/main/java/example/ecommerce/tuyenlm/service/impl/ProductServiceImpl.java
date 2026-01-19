package example.ecommerce.tuyenlm.service.impl;

import example.ecommerce.tuyenlm.dto.response.CategoryResponse;
import example.ecommerce.tuyenlm.dto.response.ProductResponse;
import example.ecommerce.tuyenlm.entity.Product;
import example.ecommerce.tuyenlm.exception.ResourceNotFoundException;
import example.ecommerce.tuyenlm.mapping.ProductMapper;
import example.ecommerce.tuyenlm.repository.ProductRepository;
import example.ecommerce.tuyenlm.repository.CategoryRepository;
import example.ecommerce.tuyenlm.service.inter.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(Long categoryId, BigDecimal priceMin, BigDecimal priceMax,
            String sortBy, String sortDirection, Pageable pageable) {

        // Start with active filter
        Specification<Product> spec = (root, query, cb) -> cb.equal(root.get("active"), true);

        // Filter by category
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        // Filter by price range (at variant level)
        if (priceMin != null || priceMax != null) {
            spec = spec.and((root, query, cb) -> {
                var variantsJoin = root.join("variants");

                if (priceMin != null && priceMax != null) {
                    // Both min and max specified
                    return cb.between(variantsJoin.get("price"), priceMin, priceMax);
                } else if (priceMin != null) {
                    // Only min specified
                    return cb.greaterThanOrEqualTo(variantsJoin.get("price"), priceMin);
                } else {
                    // Only max specified
                    return cb.lessThanOrEqualTo(variantsJoin.get("price"), priceMax);
                }
            });

            // Apply DISTINCT to avoid duplicate products when joining with variants
            spec = spec.and((root, query, cb) -> {
                if (query != null) {
                    query.distinct(true);
                }
                return cb.conjunction();
            });
        }

        Page<Product> products = productRepository.findAll(spec, pageable);

        return products.map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));

        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return productMapper.toCategoryResponseList(categoryRepository.findByActiveTrue());
    }
}
