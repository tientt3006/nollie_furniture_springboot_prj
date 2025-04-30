package indiv.neitdev.nollie_furniture.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "orders_item_options")
public class OrderItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "order_item_id", nullable = false)
    OrderItem orderItem;

    @ManyToOne
    @JoinColumn(name = "product_option_value_id", nullable = false)
    ProductOptionValue productOptionValue;
}
