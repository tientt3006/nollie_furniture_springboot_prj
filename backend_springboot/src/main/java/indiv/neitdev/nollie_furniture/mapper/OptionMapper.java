package indiv.neitdev.nollie_furniture.mapper;

import indiv.neitdev.nollie_furniture.dto.request.OptionCreateRequest;
import indiv.neitdev.nollie_furniture.dto.response.OptionResponse;
import indiv.neitdev.nollie_furniture.entity.Option;
import indiv.neitdev.nollie_furniture.entity.OptionValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OptionMapper {

}
