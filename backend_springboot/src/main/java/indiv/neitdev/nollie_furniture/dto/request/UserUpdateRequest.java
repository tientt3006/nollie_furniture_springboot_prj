package indiv.neitdev.nollie_furniture.dto.request;

import indiv.neitdev.nollie_furniture.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @NotBlank(message = "USERNAME_REQUIRED")
    String fullName;

    @NotBlank(message = "EMAIL_REQUIRED")
    String email;

    String phone;

    String address;
}
