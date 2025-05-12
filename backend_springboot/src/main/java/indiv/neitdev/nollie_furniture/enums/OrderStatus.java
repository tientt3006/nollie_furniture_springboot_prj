package indiv.neitdev.nollie_furniture.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    ORDER_SUCCESSFUL("ORDER SUCCESSFUL"),
    ON_DELIVERY("ON DELIVERY"),
    RECEIVED("RECEIVED"),
    CANCELED("CANCELED");

    private final String status;

    OrderStatus(String status) {
        this.status = status;
    }

}
