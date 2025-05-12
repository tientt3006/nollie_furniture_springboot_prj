package indiv.neitdev.nollie_furniture.dto.response;

import indiv.neitdev.nollie_furniture.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Integer orderId;
    LocalDateTime orderDate;
    LocalDateTime cancelDate;
    LocalDateTime startDeliveryDate;
    LocalDateTime receiveDate;
    BigDecimal total;
    OrderStatus status;
    String statusDetail;
    BigDecimal refund;
    
    // Customer information
    String fullName;
    String address;
    String email;
    String phone;
    String paymentMethod;
    
    // Order items (similar to cart items)
    List<OrderItemResponse> items;
}
