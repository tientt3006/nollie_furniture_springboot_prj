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
@Table(name = "options", uniqueConstraints = {
    @UniqueConstraint(name = "uk_option_name", columnNames = {"name"})
}) // Option is an SQL keyword, so using "options" as table name
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "name", nullable = false)
    String name;
}
