package indiv.neitdev.nollie_furniture.dto.response;

import indiv.neitdev.nollie_furniture.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A simplified version of OrderResponse for listing orders
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderSummaryResponse {
    Integer orderId;
    LocalDateTime orderDate;
    BigDecimal total;
    OrderStatus status;
    String statusDetail;
    
    // Customer information
    String fullName;
    String address;
    String phone;
    String paymentMethod;
    
    // Optional: number of items in the order
    Integer itemCount;
    
    // Optional: thumbnail image URL (first product in order)
    String thumbnailUrl;
}
