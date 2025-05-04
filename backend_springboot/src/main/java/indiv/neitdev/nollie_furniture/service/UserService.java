package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.ChangeForgotPasswordRequest;
import indiv.neitdev.nollie_furniture.dto.request.ChangePasswordRequest;
import indiv.neitdev.nollie_furniture.dto.request.UserCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.UserUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.UserResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);

    List<UserResponse> getUsers();

    UserResponse getUser(Integer id);

    UserResponse getMyInfo();

    UserResponse updateUser(UserUpdateRequest request);

    void deleteUser(Integer userId);

    UserResponse changePassword(ChangePasswordRequest request);

}
