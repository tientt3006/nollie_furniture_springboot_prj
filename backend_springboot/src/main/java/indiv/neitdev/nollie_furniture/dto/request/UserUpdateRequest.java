package indiv.neitdev.nollie_furniture.dto.request;

import indiv.neitdev.nollie_furniture.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    String fullName;
    String email;
    String phone;
    String address;
}
