package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.dto.request.ChangeForgotPasswordRequest;
import indiv.neitdev.nollie_furniture.dto.request.ChangePasswordRequest;
import indiv.neitdev.nollie_furniture.dto.request.UserCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.UserUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.UserResponse;
import indiv.neitdev.nollie_furniture.entity.User;
import indiv.neitdev.nollie_furniture.entity.VerificationCode;
import indiv.neitdev.nollie_furniture.enums.Role;
import indiv.neitdev.nollie_furniture.exception.AppException;
import indiv.neitdev.nollie_furniture.exception.ErrorCode;
import indiv.neitdev.nollie_furniture.mapper.UserMapper;
import indiv.neitdev.nollie_furniture.repository.UserRepository;
import indiv.neitdev.nollie_furniture.repository.VerificationCodeRepository;
import indiv.neitdev.nollie_furniture.service.MailService;
import indiv.neitdev.nollie_furniture.service.UserService;
import indiv.neitdev.nollie_furniture.util.VerificationCodeUtil;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    VerificationCodeRepository verificationCodeRepository;
    UserMapper userMapper;
    MailService mailService;
    PasswordEncoder passwordEncoder;
    VerificationCodeUtil verificationCodeUtil;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setActive(false);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(verificationCodeUtil.generateVerificationCode());
        verificationCode.setUser(user);
        verificationCode.setExpiresAt(verificationCodeUtil.generateVerificationCodeExpireTime());
        try {
            verificationCodeRepository.save(verificationCode);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        try {
            mailService.sendRegistrationCode(user, verificationCode);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.VERIFICATION_CODE_FAIL_TO_SEND);
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

        try{
            return userMapper.toUserResponse(userRepository.save(updateUser));
        } catch (DataIntegrityViolationException e){
            log.error(e.getMessage());
            throw new AppException(ErrorCode.EMAIL_PHONE_EXISTED);
        }

    }

    @Override
    public void deleteUser(Integer userId) {
        verificationCodeRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public UserResponse changePassword(ChangePasswordRequest request) {
        User user = userRepository
                .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Check Old password
        boolean authenticated = passwordEncoder.matches(request.getOldPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.OLD_PASSWORD_NOT_CORRECT);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        try{
            return userMapper.toUserResponse(userRepository.save(user));
        } catch (Exception e){
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public Page<UserResponse> searchUsers(
            Pageable pageable,
            Integer userId,
            String searchTerm,
            Boolean isActive) {
        try {
            log.info("Searching users with filters: userId={}, searchTerm={}, isActive={}", 
                    userId, searchTerm, isActive);
            
            // Clean up search term if provided
            String cleanSearchTerm = null;
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                cleanSearchTerm = searchTerm.trim();
            }
            
            // Execute the search with filters
            Page<User> usersPage = userRepository.searchUsers(
                    userId, 
                    cleanSearchTerm,
                    isActive,
                    pageable);
            
            // Map entities to DTOs
            return usersPage.map(userMapper::toUserResponse);
            
        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
