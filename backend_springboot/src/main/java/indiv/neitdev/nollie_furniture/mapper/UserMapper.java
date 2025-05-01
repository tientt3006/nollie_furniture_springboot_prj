package indiv.neitdev.nollie_furniture.mapper;

import indiv.neitdev.nollie_furniture.dto.request.UserCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.UserUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.UserResponse;
import indiv.neitdev.nollie_furniture.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    User toUser(UserCreateRequest user);

//    @Mapping(source = "", target = "", ignore = true)
    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
