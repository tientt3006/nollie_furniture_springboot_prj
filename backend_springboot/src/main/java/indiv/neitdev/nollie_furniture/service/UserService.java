package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.ChangeForgotPasswordRequest;
import indiv.neitdev.nollie_furniture.dto.request.ChangePasswordRequest;
import indiv.neitdev.nollie_furniture.dto.request.UserCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.UserUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);

    List<UserResponse> getUsers();

    UserResponse getUser(Integer id);

    UserResponse getMyInfo();

    UserResponse updateUser(UserUpdateRequest request);

    void deleteUser(Integer userId);

    UserResponse changePassword(ChangePasswordRequest request);
    
    /**
     * Search users with pagination and filtering
     * @param pageable pagination information
     * @param userId optional user ID to filter by
     * @param searchTerm optional search term for name, email, or phone
     * @param isActive optional filter for active status
     * @return page of user responses matching the criteria
     */
    Page<UserResponse> searchUsers(
            Pageable pageable,
            Integer userId,
            String searchTerm,
            Boolean isActive);
}
