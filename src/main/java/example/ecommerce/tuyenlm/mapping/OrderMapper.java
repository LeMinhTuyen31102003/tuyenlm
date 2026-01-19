package example.ecommerce.tuyenlm.mapping;

import example.ecommerce.tuyenlm.dto.response.OrderItemResponse;
import example.ecommerce.tuyenlm.dto.response.OrderResponse;
import example.ecommerce.tuyenlm.entity.Order;
import example.ecommerce.tuyenlm.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // Order → OrderResponse
    OrderResponse toOrderResponse(Order order);

    // OrderItem → OrderItemResponse
    @Mapping(target = "name", source = "nameSnapshot")
    @Mapping(target = "sku", source = "skuSnapshot")
    @Mapping(target = "price", source = "priceSnapshot")
    @Mapping(target = "subtotal", expression = "java(orderItem.getSubtotal())")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems);
}
