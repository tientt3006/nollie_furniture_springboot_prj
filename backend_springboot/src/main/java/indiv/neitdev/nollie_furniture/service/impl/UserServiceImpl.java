package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.dto.request.UserCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.UserUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.UserResponse;
import indiv.neitdev.nollie_furniture.entity.User;
import indiv.neitdev.nollie_furniture.enums.Role;
import indiv.neitdev.nollie_furniture.exception.AppException;
import indiv.neitdev.nollie_furniture.exception.ErrorCode;
import indiv.neitdev.nollie_furniture.mapper.UserMapper;
import indiv.neitdev.nollie_furniture.repository.UserRepository;
import indiv.neitdev.nollie_furniture.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        return userMapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @Override
    public UserResponse getUser(Integer id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @Override
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse updateUser(UserUpdateRequest request) {

        User updateUser = userRepository
                    .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(updateUser, request);

        return userMapper.toUserResponse(userRepository.save(updateUser));
    }

    @Override
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }
}
