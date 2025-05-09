package indiv.neitdev.nollie_furniture.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionCreateRequest {

    @NotBlank(message = "OPTION_NAME_BLANK")
    String name;

    @NotNull(message = "OPTION_VALUES_EMPTY")
    List<OptionValueCreateRequest> values;
}
