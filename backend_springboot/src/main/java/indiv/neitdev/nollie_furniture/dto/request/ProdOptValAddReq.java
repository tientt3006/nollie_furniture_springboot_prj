package indiv.neitdev.nollie_furniture.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProdOptValAddReq {
    int productOptionId;
    int optionValueId;
    int quantity;
    BigDecimal addPrice;
    MultipartFile ProdOptValImg;
}
