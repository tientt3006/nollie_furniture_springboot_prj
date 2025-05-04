package indiv.neitdev.nollie_furniture.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import indiv.neitdev.nollie_furniture.dto.request.AuthenticationRequest;
import indiv.neitdev.nollie_furniture.dto.request.ChangeForgotPasswordRequest;
import indiv.neitdev.nollie_furniture.dto.request.IntrospectRequest;
import indiv.neitdev.nollie_furniture.dto.response.AuthenticationResponse;
import indiv.neitdev.nollie_furniture.dto.response.IntrospectResponse;
import indiv.neitdev.nollie_furniture.entity.InvalidatedToken;
import indiv.neitdev.nollie_furniture.entity.User;
import indiv.neitdev.nollie_furniture.entity.VerificationCode;
import indiv.neitdev.nollie_furniture.enums.Role;
import indiv.neitdev.nollie_furniture.exception.AppException;
import indiv.neitdev.nollie_furniture.exception.ErrorCode;
import indiv.neitdev.nollie_furniture.repository.InvalidatedTokenRepository;
import indiv.neitdev.nollie_furniture.repository.UserRepository;
import indiv.neitdev.nollie_furniture.repository.VerificationCodeRepository;
import indiv.neitdev.nollie_furniture.service.AuthenticationService;
import indiv.neitdev.nollie_furniture.service.MailService;
import indiv.neitdev.nollie_furniture.util.VerificationCodeUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    VerificationCodeRepository verificationCodeRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;
    VerificationCodeUtil verificationCodeUtil;
    MailService mailService;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        var user = userRepository.findByEmail(authenticationRequest.getEmail())
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if(user.getActive() == null || !user.getActive()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
        var token =  generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false); // param: token and isRefresh
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("nollie.neitdev.indiv")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("role", user.getRole().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Can create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    public AuthenticationResponse refreshToken(IntrospectRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var email = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var newToken = generateToken(user);

        return AuthenticationResponse.builder().token(newToken).authenticated(true).build();
    }

    public void logout(IntrospectRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    @Override
    public String verifyVerificationCode(String code) {
        VerificationCode verificationCode = verificationCodeRepository.findByCode(code);

        if (verificationCode == null) {
            throw new AppException(ErrorCode.VERIFICATION_CODE_NOT_CORRECT);
        }

        return "";
    }

    @Override
    public String verifyRegistrationCode(String code) {
        VerificationCode verificationCode = verificationCodeRepository.findByCode(code);

        if (verificationCode == null) {
            throw new AppException(ErrorCode.VERIFICATION_CODE_NOT_CORRECT);
        }

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime codeExpiryTime = verificationCode.getExpiresAt();

        if (codeExpiryTime == null || currentTime.isAfter(codeExpiryTime)) {
            throw new AppException(ErrorCode.VERIFICATION_CODE_NOT_CORRECT);
        }

        User user = verificationCode.getUser();
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        // Xác thực thành công → kích hoạt tài khoản và vô hiệu hóa mã
        verificationCode.setCode(null);
        verificationCode.setExpiresAt(null);
        user.setActive(true);

        userRepository.save(user);
        verificationCodeRepository.save(verificationCode); // lưu lại trạng thái mới

        return "valid";
    }

    @Override
    public String reSendVerificationCode(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        VerificationCode verificationCode = new VerificationCode();

        try {
            verificationCode = verificationCodeRepository.findByUserId(user.getId());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.VERIFICATION_CODE_FAIL_TO_SEND);
        }

        verificationCode.setCode(verificationCodeUtil.generateVerificationCode());
        verificationCode.setExpiresAt(verificationCodeUtil.generateVerificationCodeExpireTime());
        verificationCodeRepository.save(verificationCode);

        try {
            mailService.sendRegistrationCode(user, verificationCode);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.VERIFICATION_CODE_FAIL_TO_SEND);
        }
        return "Resend successfully";
    }


    @Override
    public String changeForgotPassword(ChangeForgotPasswordRequest request) {
        VerificationCode verificationCode = verificationCodeRepository.findByCode(request.getForgotPasswordRecoveryCode());

        if (verificationCode == null) {
            throw new AppException(ErrorCode.VERIFICATION_CODE_NOT_CORRECT);
        }

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime codeExpiryTime = verificationCode.getExpiresAt();

        if (codeExpiryTime == null || currentTime.isAfter(codeExpiryTime)) {
            throw new AppException(ErrorCode.VERIFICATION_CODE_NOT_CORRECT);
        }

        User user = verificationCode.getUser();
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        // Xác thực thành công → kích hoạt tài khoản và vô hiệu hóa mã
        verificationCode.setCode(null);
        verificationCode.setExpiresAt(null);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
        verificationCodeRepository.save(verificationCode); // lưu lại trạng thái mới

        return "success change forgot password";
    }
//    private String buildScope(User user) {
//        StringJoiner scopeJoiner = new StringJoiner(" ");
//
//        if(user.getRole() != null && !user.getRole().toString().isEmpty()) {
//            scopeJoiner.add("ROLE_" + user.getRole().toString());
//        }
//
//        return scopeJoiner.toString();
//    }
}
