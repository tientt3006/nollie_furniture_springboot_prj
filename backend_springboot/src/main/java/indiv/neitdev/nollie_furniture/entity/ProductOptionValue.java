package indiv.neitdev.nollie_furniture.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "products_options_value")
public class ProductOptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "product_option_id", nullable = false)
    ProductOption productOption;

    @ManyToOne
    @JoinColumn(name = "option_value_id", nullable = false)
    OptionValue optionValue;

    @Column(name = "quantity", nullable = false)
    Integer quantity;

    @Column(name = "add_price", nullable = false, precision = 18, scale = 2)
    BigDecimal addPrice;

    @Column(name = "img_url")
    String imgUrl;
}
