package indiv.neitdev.nollie_furniture.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {

    @NotBlank(message = "USERNAME_REQUIRED")
    String fullName;

    @NotBlank(message = "EMAIL_REQUIRED")
    String email;

    @Size(min = 4, max = 50, message = "INVALID_PASSWORD")
    String password;
}
