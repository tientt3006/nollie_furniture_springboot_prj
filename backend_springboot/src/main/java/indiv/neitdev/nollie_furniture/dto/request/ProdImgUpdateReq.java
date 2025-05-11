package indiv.neitdev.nollie_furniture.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProdImgUpdateReq {
    int baseProdImgId;
    MultipartFile newBaseProdImg;
    List<Integer> otherProdImgIdsForDelete;
    List<MultipartFile> newOtherProdImgList;
}
