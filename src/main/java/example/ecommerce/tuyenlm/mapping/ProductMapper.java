package example.ecommerce.tuyenlm.mapping;

import example.ecommerce.tuyenlm.dto.response.CategoryResponse;
import example.ecommerce.tuyenlm.dto.response.ProductResponse;
import example.ecommerce.tuyenlm.dto.response.VariantResponse;
import example.ecommerce.tuyenlm.entity.Category;
import example.ecommerce.tuyenlm.entity.Product;
import example.ecommerce.tuyenlm.entity.ProductVariant;
import example.ecommerce.tuyenlm.entity.ReservationStatus;
import example.ecommerce.tuyenlm.repository.InventoryReservationRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ProductMapper {

    @Autowired
    protected InventoryReservationRepository reservationRepository;

    // Product → ProductResponse
    @Mapping(target = "variants", source = "product", qualifiedByName = "getActiveVariants")
    public abstract ProductResponse toProductResponse(Product product);

    // ProductVariant → VariantResponse
    @Mapping(target = "availableStock", source = "variant", qualifiedByName = "calculateAvailableStock")
    public abstract VariantResponse toVariantResponse(ProductVariant variant);

    // Category → CategoryResponse
    public abstract CategoryResponse toCategoryResponse(Category category);

    // List mappings
    public abstract List<VariantResponse> toVariantResponseList(List<ProductVariant> variants);

    public abstract List<CategoryResponse> toCategoryResponseList(List<Category> categories);

    @Named("calculateAvailableStock")
    protected int calculateAvailableStock(ProductVariant variant) {
        int reservedStock = reservationRepository.sumQuantityByVariantAndStatus(
                variant.getId(), ReservationStatus.ACTIVE);
        return Math.max(0, variant.getStockQuantity() - reservedStock);
    }

    @Named("getActiveVariants")
    protected List<VariantResponse> getActiveVariants(Product product) {
        return product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .map(this::toVariantResponse)
                .toList();
    }
}
