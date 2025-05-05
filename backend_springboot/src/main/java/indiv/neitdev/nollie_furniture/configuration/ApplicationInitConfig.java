package indiv.neitdev.nollie_furniture.configuration;

import indiv.neitdev.nollie_furniture.entity.User;
import indiv.neitdev.nollie_furniture.enums.Role;
import indiv.neitdev.nollie_furniture.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_EMAIL = "admin@admin.com";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @NonFinal
    @Value("${S3_ACCESS_KEY}")
    String awsS3AccessKey;

    @NonFinal
    @Value("${S3_SECRET_KEY}")
    String awsS3SecretKey;

    @NonFinal
    Region region = Region.AP_SOUTHEAST_2;

    @Bean
    S3Client createS3Client() {
        return S3Client.builder()
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(awsS3AccessKey, awsS3SecretKey))
                )
                .region(region)
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.microsoft.sqlserver.jdbc.SQLServerDriver")
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
                User user = User.builder()
                        .fullName("admin")
                        .email(ADMIN_EMAIL)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .role(Role.ADMIN)
                        .active(true)
                        .build();
                userRepository.save(user);
                log.warn("Admin account has been created with default password: 1234, please change it");
            }
            log.info("Application initialization completed .....");
        };
    }
}
