package example.ecommerce.tuyenlm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import example.ecommerce.tuyenlm.entity.ProductVariant;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findBySku(String sku);

    List<ProductVariant> findByProductId(Long productId);

    List<ProductVariant> findByProductIdAndActiveTrue(Long productId);

    boolean existsBySku(String sku);

    // Pessimistic lock for last item problem with timeout to prevent deadlock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") // 3 seconds timeout
    })
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithLock(@Param("id") Long id);

    // Find with lock by SKU with timeout to prevent deadlock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") // 3 seconds timeout
    })
    @Query("SELECT v FROM ProductVariant v WHERE v.sku = :sku")
    Optional<ProductVariant> findBySkuWithLock(@Param("sku") String sku);
}
