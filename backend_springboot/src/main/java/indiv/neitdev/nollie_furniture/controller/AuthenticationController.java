package indiv.neitdev.nollie_furniture.controller;

import com.nimbusds.jose.JOSEException;
import indiv.neitdev.nollie_furniture.dto.request.*;
import indiv.neitdev.nollie_furniture.dto.response.ApiResponse;
import indiv.neitdev.nollie_furniture.dto.response.AuthenticationResponse;
import indiv.neitdev.nollie_furniture.dto.response.IntrospectResponse;
import indiv.neitdev.nollie_furniture.service.AuthenticationService;
import indiv.neitdev.nollie_furniture.service.UserService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    //why dont use this:
//    @PostMapping("/introspect")
//    ApiResponse<IntrospectResponse> introspect(@RequestHeader("Authorization") String authHeader)
//            throws ParseException, JOSEException {
//        String token = authHeader.replace("Bearer ", "").trim();
//        var result = authenticationService.introspect(new IntrospectRequest(token));
//        return ApiResponse.<IntrospectResponse>builder().result(result).build();
//    }
    //because: BearerTokenAuthenticationFilter will try to decode token even its endpoint has been permitAll()
    // and the customdecoder include introspect so that it throw out 401 unauthen, not the expected response for this (200 and isValid?)
    // the ather reason is token in this endpoint is data to process, not normal token for authen...

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/verify-new-acct")
    public ApiResponse<String> verifyUserRegistration(@RequestBody RegistrationCodeVerificationRequest request) {
        var result = authenticationService.verifyRegistrationCode(request.getVerificationCode());
        return ApiResponse.<String>builder().result(result).build();
    }

    @PostMapping("/re-send-verification-code")
    public ApiResponse<String> verifyUserRegistration(@RequestBody ReSendVerificationCodeRequest request) {
        var result = authenticationService.reSendVerificationCode(request.getEmail());
        return ApiResponse.<String>builder().result(result).build();
    }

    @PutMapping("/change-forgot-password")
    ApiResponse<String> changeForgotPassword(@RequestBody @Valid ChangeForgotPasswordRequest request) {
        return ApiResponse.<String>builder()
                .result(authenticationService.changeForgotPassword(request))
                .build();
    }
}
