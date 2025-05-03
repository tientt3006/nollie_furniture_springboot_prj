package indiv.neitdev.nollie_furniture.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @Size(min = 4, max = 50, message = "INVALID_PASSWORD")
    String oldPassword;

    @Size(min = 4, max = 50, message = "INVALID_PASSWORD")
    String newPassword;
}
