package indiv.neitdev.nollie_furniture.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryWithImageUpdateRequest {
    @NotBlank(message = "CATEGORY_NAME_REQUIRED")
    String name;

    @NotNull(message = "CATEGORY_IMAGE_REQUIRED") // this is not working for MultipartFile, valid it manually in service layer
    MultipartFile image;
}
