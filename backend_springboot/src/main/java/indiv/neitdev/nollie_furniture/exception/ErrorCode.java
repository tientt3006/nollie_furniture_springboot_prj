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
    
    // Category related errors
    CATEGORY_NAME_REQUIRED(2001, "Category name is required", HttpStatus.BAD_REQUEST),
    CATEGORY_IMAGE_REQUIRED(2002, "Category image is required", HttpStatus.BAD_REQUEST),
    CATEGORY_CREATE_FAILED(2003, "Failed to create category, category name is unique,...", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_FETCH_FAILED(2004, "Failed to fetch categories", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_NOT_FOUND(2005, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_UPDATE_FAILED(2006, "Failed to update category, category name is unique,...", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_DELETE_FAILED(2007, "Failed to delete category", HttpStatus.INTERNAL_SERVER_ERROR),

    // Option related errors
    OPTION_NAME_BLANK(3001, "Option name is blank", HttpStatus.BAD_REQUEST),
    OPTION_VALUE_BLANK(3002, "Option value is blank", HttpStatus.BAD_REQUEST),
    OPTION_VALUE_IMG_BLANK(3003, "Option value image is blank", HttpStatus.BAD_REQUEST),
    OPTION_VALUES_EMPTY(3004, "Option values is empty", HttpStatus.BAD_REQUEST),
    OPTION_NAME_EXISTS(3005, "Option with this name already exists", HttpStatus.CONFLICT),
    OPTION_VALUE_EXISTS(3006, "This value already exists for this option", HttpStatus.CONFLICT),
    OPTION_VALUE_DUPLICATE(3007, "Option value duplicate detected", HttpStatus.CONFLICT),
    OPTION_NOT_FOUND(3008, "Option not found", HttpStatus.NOT_FOUND),

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
