package indiv.neitdev.nollie_furniture.configuration;

import indiv.neitdev.nollie_furniture.entity.Category;
import indiv.neitdev.nollie_furniture.entity.Option;
import indiv.neitdev.nollie_furniture.entity.OptionValue;
import indiv.neitdev.nollie_furniture.entity.User;
import indiv.neitdev.nollie_furniture.enums.Role;
import indiv.neitdev.nollie_furniture.repository.CategoryRepository;
import indiv.neitdev.nollie_furniture.repository.OptionRepository;
import indiv.neitdev.nollie_furniture.repository.OptionValueRepository;
import indiv.neitdev.nollie_furniture.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
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

import java.util.*;

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
    ApplicationRunner applicationRunner(UserRepository userRepository, CategoryRepository categoryRepository,
                                        OptionRepository optionRepository, OptionValueRepository optionValueRepository) {
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
            
            // Initialize predefined categories if they don't exist
            initializeCategories(categoryRepository);
            
            // Initialize predefined options and their values
            Map<String, Option> createdOptions = initializeOptions(optionRepository);
            initializeOptionValues(createdOptions, optionValueRepository);
            
            log.info("Application initialization completed .....");
        };
    }
    
    private void initializeCategories(CategoryRepository categoryRepository) {
        log.info("Initializing predefined categories...");
        String[] categoryNames = {"table", "chair", "sofa", "storage", "rug", "lamp", "bed", "outdoor", "accessories"};
        
        for (String name : categoryNames) {
            if (categoryRepository.findByName(name).isEmpty()) {
                Category category = Category.builder()
                        .name(name)
                        .build();
                categoryRepository.save(category);
                log.info("Category '{}' has been created", name);
            }
        }
        log.info("Category initialization completed");
    }
    
    private Map<String, Option> initializeOptions(OptionRepository optionRepository) {
        log.info("Initializing predefined options...");
        String[] optionNames = {"color", "material", "leg", "tabletop", "Upholstery"};
        
        Map<String, Option> createdOptions = new HashMap<>();
        
        for (String name : optionNames) {
            if (!optionRepository.existsByNameIgnoreCase(name)) {
                Option option = Option.builder()
                        .name(name)
                        .build();
                Option savedOption = optionRepository.save(option);
                createdOptions.put(name, savedOption);
                log.info("Option '{}' has been created", name);
            } else {
                // If option already exists, retrieve it for the return map
                optionRepository.findAll().stream()
                    .filter(opt -> opt.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .ifPresent(existingOption -> createdOptions.put(name, existingOption));
            }
        }
        log.info("Options initialization completed");
        return createdOptions;
    }
    
    private void initializeOptionValues(Map<String, Option> options, OptionValueRepository optionValueRepository) {
        log.info("Initializing predefined option values...");
        
        // Define values for each option
        Map<String, List<String>> optionValues = new HashMap<>();
        optionValues.put("color", Arrays.asList("red", "green", "blue"));
        optionValues.put("material", Arrays.asList(
            "fabric-capri", "fabic-nani", "fabric-bresso", "fabric-bristol", 
            "fabric-ravello", "fabric-bologna", "fabric-skagen", "metal", 
            "aluminium", "steel", "wood", "laccquered", "leather", "plastic", "oak"
        ));
        optionValues.put("leg", Arrays.asList(
            "matt black structure lacquered", "matt ash grey structure lacquered"
        ));
        optionValues.put("tabletop", Arrays.asList(
            "natural oak veneer", "dark oak veneer"
        ));
        optionValues.put("Upholstery", Arrays.asList(
            "fabric-capri", "fabic-nani", "fabric-bresso", "fabric-bristol", 
            "fabric-ravello", "fabric-bologna", "fabric-skagen", "leather"
        ));
        
        // Create option values for each option
        for (Map.Entry<String, Option> entry : options.entrySet()) {
            String optionName = entry.getKey();
            Option option = entry.getValue();
            List<String> values = optionValues.getOrDefault(optionName, Collections.emptyList());
            
            for (String value : values) {
                if (!optionValueRepository.existsByOptionAndValueIgnoreCase(option, value)) {
                    OptionValue optionValue = OptionValue.builder()
                            .option(option)
                            .value(value)
                            .build();
                    optionValueRepository.save(optionValue);
                    log.info("Option value '{}' for option '{}' has been created", value, optionName);
                }
            }
        }
        
        log.info("Option values initialization completed");
    }
}
