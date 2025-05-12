package indiv.neitdev.nollie_furniture.controller;

import indiv.neitdev.nollie_furniture.dto.request.ChangeForgotPasswordRequest;
import indiv.neitdev.nollie_furniture.dto.request.ChangePasswordRequest;
import indiv.neitdev.nollie_furniture.dto.request.UserUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.ApiResponse;
import indiv.neitdev.nollie_furniture.dto.request.UserCreateRequest;
import indiv.neitdev.nollie_furniture.dto.response.UserResponse;
import indiv.neitdev.nollie_furniture.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")  // auto add prefix ROLE_ or SCOPE_, ~ hasAuthority("ROLE_ADMIN")
    ApiResponse<List<UserResponse>> getUsers() {
//        var authentication = SecurityContextHolder.getContext().getAuthentication();
//        log.info("UserName: {}", authentication.getName());
//        authentication.getAuthorities().forEach(grantedAuthority -> log.info("GrantedAuthority: {}", grantedAuthority));
//        xem log auth

        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") Integer userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @PutMapping("/update-info")
    ApiResponse<UserResponse> updateUser(@RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser( request))
                .build();
    }

    @PutMapping("/change-password")
    ApiResponse<UserResponse> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.changePassword(request))
                .build();
    }

    /**
     * Admin API: Search users with pagination and filtering
     * @param page page number (0-based)
     * @param size page size
     * @param userId optional user ID to filter by
     * @param search optional search term for name, email, or phone
     * @param isActive optional filter for active status
     * @param sortBy optional field to sort by (id, fullName, email)
     * @param sortDirection optional sort direction (ASC or DESC)
     * @return page of user responses matching the criteria
     */
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserResponse>> adminSearchUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {
        
        log.info("Admin user search request: page={}, size={}, userId={}, search={}, isActive={}, sortBy={}, sortDir={}",
                page, size, userId, search, isActive, sortBy, sortDirection);
        
        // Handle sorting
        Sort sort;
        if (sortDirection.equalsIgnoreCase("DESC")) {
            sort = Sort.by(Sort.Direction.DESC, sortBy);
        } else {
            sort = Sort.by(Sort.Direction.ASC, sortBy);
        }
        
        // Create pageable with sorting
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        // Execute search
        Page<UserResponse> result = userService.searchUsers(
                pageRequest, userId, search, isActive);
                
        return ApiResponse.<Page<UserResponse>>builder().result(result).build();
    }
}
