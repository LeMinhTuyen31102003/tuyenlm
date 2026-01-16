package example.ecommerce.tuyenlm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import example.ecommerce.tuyenlm.entity.Cart;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findBySessionId(String sessionId);

    boolean existsBySessionId(String sessionId);

    // Cleanup old carts (e.g., older than 30 days)
    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}
