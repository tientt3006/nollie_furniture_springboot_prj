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
@Table(name = "options_value", uniqueConstraints = {
    @UniqueConstraint(name = "uk_option_value", columnNames = {"option_id", "value"})
})
public class OptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "option_id", nullable = false)
    Option option;

    @Column(name = "value", nullable = false)
    String value;

    @Column(name = "img_url")
    String imgUrl;
}
