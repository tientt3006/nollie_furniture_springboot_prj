package indiv.neitdev.nollie_furniture.exception;

import indiv.neitdev.nollie_furniture.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    // Handle uncategorized exception
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        return ResponseEntity.internalServerError().body(apiResponse);
    }
    
    // Handle RuntimeException
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException exception) {
        log.error(exception.getMessage(), exception);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        return ResponseEntity.internalServerError().body(apiResponse);
    }

    // Handle MissingServletRequestPartException (for multipart form data issues)
    @ExceptionHandler(value = MissingServletRequestPartException.class)
    ResponseEntity<ApiResponse> handleMissingServletRequestPartException(MissingServletRequestPartException exception) {
        log.error("Missing required request part: {}", exception.getMessage());
        ApiResponse apiResponse = new ApiResponse();
        // If the missing part is "image", use CATEGORY_IMAGE_REQUIRED error code
        if ("image".equals(exception.getRequestPartName())) {
            apiResponse.setCode(ErrorCode.CATEGORY_IMAGE_REQUIRED.getCode());
            apiResponse.setMessage(ErrorCode.CATEGORY_IMAGE_REQUIRED.getMessage());
            return ResponseEntity.status(ErrorCode.CATEGORY_IMAGE_REQUIRED.getHttpStatusCode()).body(apiResponse);
        } else {
            apiResponse.setCode(ErrorCode.INVALID_KEY.getCode());
            apiResponse.setMessage("Required part '" + exception.getRequestPartName() + "' is missing");
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }
    
    // Handle MultipartException (general multipart issues)
    @ExceptionHandler(value = MultipartException.class)
    ResponseEntity<ApiResponse> handleMultipartException(MultipartException exception) {
        log.error("Multipart error: {}", exception.getMessage());
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.INVALID_KEY.getCode());
        apiResponse.setMessage("Error processing the uploaded file: " + exception.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Handle categorized exception, normalize response
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException exception) {
        // Get error message form annotation: @Notnull(message = ""), @Min(message = "", ...)
        String enumKey = exception.getFieldError().getDefaultMessage();
        // Set default ErrorCode
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        // Store attributes of validation constrain
        Map<String, Object> attributes = null;

        try {
            // Find exact errorCode from enumKey, if not find any, use default INVALID_KEY above
            errorCode = ErrorCode.valueOf(enumKey);
            // Get first ConstraintViolation
            var constraintViolation =
                    exception.getBindingResult()
                        .getAllErrors()
                        .getFirst()
                        .unwrap(ConstraintViolation.class);
            // Log out validation error
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            log.info(attributes.toString());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid error code enum key: {}", enumKey, e);
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        // Custom validation error if there are attributes
        apiResponse.setMessage(
                Objects.nonNull(attributes)
                    ? mapAttribute(errorCode.getMessage(), attributes)
                    : errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Method for custom validation error message: change placeholder to specific value
    private String mapAttribute(String message, Map<String, Object> attributes) {
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            message = message.replace("{" + key + "}", value);
        }
        return message;
    }

}
