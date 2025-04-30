package indiv.neitdev.nollie_furniture.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    ORDER_SUCCESSFUL("Order Successful"),
    ON_DELIVERY("On Delivery"),
    RECEIVED("Received"),
    CANCELED("Canceled");

    private final String status;

    OrderStatus(String status) {
        this.status = status;
    }

}
