package indiv.neitdev.nollie_furniture.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionValueDto {

    @NotBlank(message = "OPTION_VALUE_BLANK")
    String value;

    MultipartFile img;
}
