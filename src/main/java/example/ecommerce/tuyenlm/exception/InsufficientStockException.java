package example.ecommerce.tuyenlm.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final Map<String, Object> details;

    public InsufficientStockException(String message) {
        super(message);
        this.details = null;
    }

    public InsufficientStockException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }
}
