package indiv.neitdev.nollie_furniture.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "verification_code")
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @OneToOne
    @JoinColumn(name = "users_id", nullable = false)
    User user;

    @Column(name = "code")
    String code;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "expires_at")
    LocalDateTime expiresAt;
}