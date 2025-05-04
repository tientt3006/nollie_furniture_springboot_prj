package indiv.neitdev.nollie_furniture.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(1009, "Your name should not be blanked", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1010, "Your email should not be blanked", HttpStatus.BAD_REQUEST),
    EMAIL_PHONE_EXISTED(1011, "Your email or phone number already used by other people", HttpStatus.CONFLICT),
    OLD_PASSWORD_NOT_CORRECT(1012, "Old password not correct", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_NOT_CORRECT(1013, "Verification code not correct or expired", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_ACTIVE(1014, "Account not active", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_FAIL_TO_SEND(1015, "Verification code fail to send", HttpStatus.BAD_REQUEST),
    FAIL_UPLOAD_TO_S3(1016, "S3 Upload failed", HttpStatus.BAD_REQUEST),
    ;


    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = statusCode;
    }
}
