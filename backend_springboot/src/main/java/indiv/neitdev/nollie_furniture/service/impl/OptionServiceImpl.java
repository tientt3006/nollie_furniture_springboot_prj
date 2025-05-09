package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.dto.request.OptionCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.OptionValueCreateRequest;
import indiv.neitdev.nollie_furniture.dto.response.OptionResponse;
import indiv.neitdev.nollie_furniture.dto.response.OptionValueResponse;
import indiv.neitdev.nollie_furniture.entity.Option;
import indiv.neitdev.nollie_furniture.entity.OptionValue;
import indiv.neitdev.nollie_furniture.exception.AppException;
import indiv.neitdev.nollie_furniture.exception.ErrorCode;
import indiv.neitdev.nollie_furniture.repository.OptionRepository;
import indiv.neitdev.nollie_furniture.repository.OptionValueRepository;
import indiv.neitdev.nollie_furniture.service.AwsS3Service;
import indiv.neitdev.nollie_furniture.service.OptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OptionServiceImpl implements OptionService {

    OptionRepository optionRepository;
    OptionValueRepository optionValueRepository;
    AwsS3Service awsS3Service;

    @Override
    @Transactional
    public OptionResponse createOption(OptionCreateRequest request) {
        // Validate request
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new AppException(ErrorCode.OPTION_NAME_BLANK);
        }

        if (request.getValues() == null || request.getValues().isEmpty()) {
            throw new AppException(ErrorCode.OPTION_VALUES_EMPTY);
        }

        try {
            // Create option
            Option option = Option.builder()
                    .name(request.getName().trim())
                    .build();
            option = optionRepository.save(option);

            // Check for duplicate values in the request (local validation)
            Set<String> valueSet = new HashSet<>();
            
            // Create option values
            List<OptionValueResponse> optionValuesResponse = new ArrayList<>();
            for (OptionValueCreateRequest valueDto : request.getValues()) {
                if (valueDto.getValue() == null || valueDto.getValue().trim().isEmpty()) {
                    throw new AppException(ErrorCode.OPTION_VALUE_BLANK);
                }
                
                String normalizedValue = valueDto.getValue().trim().toLowerCase();
                
                // Check for duplicate values within the request only
                if (!valueSet.add(normalizedValue)) {
                    throw new AppException(ErrorCode.OPTION_VALUE_DUPLICATE);
                }

                String imgUrl = null;
                if (valueDto.getImg() != null && !valueDto.getImg().isEmpty()) {
                    imgUrl = awsS3Service.saveImageToS3(valueDto.getImg());
                }

                OptionValue optionValue = OptionValue.builder()
                        .option(option)
                        .value(valueDto.getValue().trim())
                        .imgUrl(imgUrl)
                        .build();
                optionValue = optionValueRepository.save(optionValue);

                OptionValueResponse optionValueResponse = OptionValueResponse.builder()
                        .id(optionValue.getId())
                        .name(optionValue.getValue())
                        .imgUrl(imgUrl)
                        .build();
                optionValuesResponse.add(optionValueResponse);
            }

            // Return response
            return OptionResponse.builder()
                    .id(option.getId())
                    .name(option.getName())
                    .values(optionValuesResponse)
                    .build();
        } catch (DataIntegrityViolationException e) {
            // Handle constraint violations from database
            log.error("Database constraint violation: {}", e.getMessage());
            
            // Check error message to determine whether it's option name or value uniqueness violation
            if (e.getMessage().toLowerCase().contains("option") && e.getMessage().toLowerCase().contains("name")) {
                throw new AppException(ErrorCode.OPTION_NAME_EXISTS);
            } else if (e.getMessage().toLowerCase().contains("option") && e.getMessage().toLowerCase().contains("value")) {
                throw new AppException(ErrorCode.OPTION_VALUE_EXISTS);
            } else {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }
    }

    @Override
    public List<OptionResponse> getAllOptions() {
        try {
            // Fetch all options from the database
            List<Option> options = optionRepository.findAll();
            
            // Convert to DTO response
            return options.stream().map(option -> {
                // Get option values for this option
                List<OptionValue> optionValues = optionValueRepository.findByOption(option);
                
                // Map option values to response DTOs
                List<OptionValueResponse> optionValueResponses = optionValues.stream()
                    .map(value -> OptionValueResponse.builder()
                        .id(value.getId())
                        .name(value.getValue())
                        .imgUrl(value.getImgUrl())
                        .build())
                    .collect(Collectors.toList());
                
                // Build and return option response with its values
                return OptionResponse.builder()
                    .id(option.getId())
                    .name(option.getName())
                    .values(optionValueResponses)
                    .build();
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all options: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
