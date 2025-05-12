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
    CATEGORY_ID_BLANK(2008, "Category ID cannot be blank", HttpStatus.BAD_REQUEST),


    // Option related errors
    OPTION_NAME_BLANK(3001, "Option name is blank", HttpStatus.BAD_REQUEST),
    OPTION_VALUE_BLANK(3002, "Option value is blank", HttpStatus.BAD_REQUEST),
    OPTION_VALUE_IMG_BLANK(3003, "Option value image is blank", HttpStatus.BAD_REQUEST),
    OPTION_VALUES_EMPTY(3004, "Option values is empty", HttpStatus.BAD_REQUEST),
    OPTION_NAME_EXISTS(3005, "Option with this name already exists", HttpStatus.CONFLICT),
    OPTION_VALUE_EXISTS(3006, "This value already exists for this option", HttpStatus.CONFLICT),
    OPTION_VALUE_DUPLICATE(3007, "Option value duplicate detected", HttpStatus.CONFLICT),
    OPTION_NOT_FOUND(3008, "Option not found", HttpStatus.NOT_FOUND),
    OPTION_DELETE_CONSTRAINT(3009, "Cannot delete option because it is being used by other entities", HttpStatus.CONFLICT),
    OPTION_ID_BLANK(3010, "Option ID cannot be blank", HttpStatus.BAD_REQUEST),
    OPTION_VALUE_ID_BLANK(3011, "Option value ID cannot be blank", HttpStatus.BAD_REQUEST),
    OPTION_VALUE_NOT_FOUND(3012, "Option value not found", HttpStatus.BAD_REQUEST),
    OPTION_ALREADY_ADDED_TO_PRODUCT(4014, "This option has already been added to this product", HttpStatus.CONFLICT),

    // Product related errors
    PRODUCT_NAME_BLANK(4001, "Product name cannot be blank", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_INVALID(4002, "Product price must be greater than zero", HttpStatus.BAD_REQUEST),
    PRODUCT_QUANTITY_INVALID(4003, "Product quantity cannot be negative", HttpStatus.BAD_REQUEST),
    PRODUCT_NAME_ALREADY_EXISTS(4004, "Product name already exists", HttpStatus.BAD_REQUEST),
    DUPLICATE_OPTION_IDS(4005, "Duplicate option IDs are not allowed", HttpStatus.BAD_REQUEST),
    DUPLICATE_OPTION_VALUE_IDS(4006, "Duplicate option value IDs are not allowed within an option", HttpStatus.BAD_REQUEST),
    OPTION_VALUE_NOT_BELONG_TO_OPTION(4007, "Option value does not belong to the specified option", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(4008, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_IMAGE_NOT_FOUND(4009, "Product image not found", HttpStatus.NOT_FOUND),
    PRODUCT_IMAGE_NOT_BELONG_TO_PRODUCT(4010, "Product image does not belong to the specified product", HttpStatus.BAD_REQUEST),
    PRODUCT_OPTION_VALUE_NOT_FOUND(4011, "Product option value not found", HttpStatus.NOT_FOUND),
    PRODUCT_OPTION_NOT_FOUND(4012, "Product option not found", HttpStatus.NOT_FOUND),
    OPTION_VALUE_ALREADY_ADDED(4013, "This option value has already been added to this product option", HttpStatus.CONFLICT),
    PRODUCT_QUANTITY_INSUFFICIENT(4015, "Insufficient product quantity available", HttpStatus.BAD_REQUEST),
    PRODUCT_OPTION_VALUE_NOT_BELONG_TO_PRODUCT(4016, "Product option value does not belong to this product", HttpStatus.BAD_REQUEST),
    
    // Cart related errors
    CART_NOT_FOUND(5001, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(5002, "Cart item not found", HttpStatus.NOT_FOUND),
    CART_INVALID_REQUEST(5003, "Invalid cart request - must specify either baseProductQuantity or productOptionValueIdsAndQuantity", HttpStatus.BAD_REQUEST),
    CART_IS_EMPTY(5004, "Your cart is empty", HttpStatus.BAD_REQUEST),

    // Order related errors
    ORDER_FULLNAME_REQUIRED(6001, "Full name is required", HttpStatus.BAD_REQUEST),
    ORDER_ADDRESS_REQUIRED(6002, "Shipping address is required", HttpStatus.BAD_REQUEST),
    ORDER_PHONE_REQUIRED(6003, "Phone number is required", HttpStatus.BAD_REQUEST),
    ORDER_PAYMENT_METHOD_REQUIRED(6004, "Payment method is required", HttpStatus.BAD_REQUEST),
    ORDER_CREATE_FAILED(6005, "Failed to create order", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_NOT_FOUND(6006, "Order not found", HttpStatus.NOT_FOUND),
    
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
