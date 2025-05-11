package indiv.neitdev.nollie_furniture.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomProductResponse {

    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;

    private List<ProductResponse> products;
}
