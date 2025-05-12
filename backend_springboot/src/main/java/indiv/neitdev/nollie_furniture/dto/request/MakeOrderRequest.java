package indiv.neitdev.nollie_furniture.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MakeOrderRequest {
    // Customer information for shipping
    String fullName;
    String address;
    String phone;
    
    // Optional email field - if not provided, will use authenticated user's email
    String email;
    
    // Payment method (e.g., "CASH_ON_DELIVERY", "CREDIT_CARD", etc.)
    String paymentMethod;
    
    // Additional notes or special instructions for the order
    String notes;
}
