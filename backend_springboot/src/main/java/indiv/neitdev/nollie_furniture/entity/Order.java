package indiv.neitdev.nollie_furniture.entity;

import indiv.neitdev.nollie_furniture.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "order_date", nullable = false)
    LocalDateTime orderDate;

    @Column(name = "cancel_date")
    LocalDateTime cancelDate;

    @Column(name = "start_delivery_date")
    LocalDateTime startDeliveryDate;

    @Column(name = "receive_date")
    LocalDateTime receiveDate;

    @Column(name = "total", nullable = false, precision = 18, scale = 2)
    BigDecimal total;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    OrderStatus status;

    @Column(name = "status_detail")
    String statusDetail;

    @Column(name = "refund", precision = 2, scale = 2)
    @DecimalMin(value = "0.00", inclusive = true, message = "Refund must be greater than or equal to 0%")
    @DecimalMax(value = "1.00", inclusive = true, message = "Refund must be less than or equal to 100%")
    BigDecimal refund;

    @Column(name = "full_name", nullable = false)
    String fullName;

    @Column(name = "address", nullable = false)
    String address;

    @Column(name = "email", nullable = false)
    String email;

    @Column(name = "phone", nullable = false)
    String phone;

    @Column(name = "payment_method", nullable = false)
    String paymentMethod;
}
