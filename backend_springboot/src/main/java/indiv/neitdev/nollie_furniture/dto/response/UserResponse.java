package indiv.neitdev.nollie_furniture.dto.response;

import indiv.neitdev.nollie_furniture.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Integer id;
    String fullName;
    String email;
    String phone;
    Role role;
    String address;
    Boolean active;
}
