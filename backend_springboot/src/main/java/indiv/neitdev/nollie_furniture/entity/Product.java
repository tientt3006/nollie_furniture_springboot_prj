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
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "base_price", nullable = false, precision = 18, scale = 2)
    BigDecimal basePrice;

    @Column(name = "height", precision = 18, scale = 2)
    BigDecimal height;

    @Column(name = "width", precision = 18, scale = 2)
    BigDecimal width;

    @Column(name = "length", precision = 18, scale = 2)
    BigDecimal length;

    @Column(name = "description")
    String description;

    @Column(name = "base_product_quantity", nullable = false)
    Integer baseProductQuantity;
}
