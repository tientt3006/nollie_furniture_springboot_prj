package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.dto.request.OptionCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.OptionUpdateRequest;
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

    @Override
    public OptionResponse getOptionById(int id) {
        try {
            // Find option by ID
            Option option = optionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.OPTION_NOT_FOUND));
            
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
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching option by id {}: {}", id, e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public OptionResponse updateOption(OptionUpdateRequest request) {
        try {
            // Find option by ID
            Option option = optionRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.OPTION_NOT_FOUND));
            
            // Update option name if provided
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                String newName = request.getName().trim();
                // Check if name changed and new name exists
                if (!option.getName().equalsIgnoreCase(newName) && 
                    optionRepository.existsByNameIgnoreCase(newName)) {
                    throw new AppException(ErrorCode.OPTION_NAME_EXISTS);
                }
                option.setName(newName);
            }
            
            // Save option changes
            option = optionRepository.save(option);
            
            // Delete option values if specified
            if (request.getValueIdsForDelete() != null && !request.getValueIdsForDelete().isEmpty()) {
                for (Integer valueId : request.getValueIdsForDelete()) {
                    optionValueRepository.findById(valueId).ifPresent(optionValue -> {
                        // Delete image from S3 if exists
                        if (optionValue.getImgUrl() != null && !optionValue.getImgUrl().isEmpty()) {
                            String filename = optionValue.getImgUrl().substring(
                                optionValue.getImgUrl().lastIndexOf('/') + 1);
                            awsS3Service.deleteImageFromS3(filename);
                        }
                        optionValueRepository.delete(optionValue);
                    });
                }
            }
            
            // Add new values if specified
            List<OptionValueResponse> optionValuesResponse = new ArrayList<>();
            Set<String> existingValues = new HashSet<>();
            
            // Get existing values to check for duplicates
            optionValueRepository.findByOption(option).forEach(val -> 
                existingValues.add(val.getValue().toLowerCase()));
            
            if (request.getNewValuesForAdd() != null && !request.getNewValuesForAdd().isEmpty()) {
                Set<String> newValuesSet = new HashSet<>(); // For checking duplicates within new values
                
                for (OptionValueCreateRequest valueDto : request.getNewValuesForAdd()) {
                    if (valueDto.getValue() == null || valueDto.getValue().trim().isEmpty()) {
                        throw new AppException(ErrorCode.OPTION_VALUE_BLANK);
                    }
                    
                    String normalizedValue = valueDto.getValue().trim().toLowerCase();
                    
                    // Check for duplicate values within the request
                    if (!newValuesSet.add(normalizedValue)) {
                        throw new AppException(ErrorCode.OPTION_VALUE_DUPLICATE);
                    }
                    
                    // Check for duplicate with existing values
                    if (existingValues.contains(normalizedValue)) {
                        throw new AppException(ErrorCode.OPTION_VALUE_EXISTS);
                    }
                    
                    // Add the value to the set for future duplicate checks
                    existingValues.add(normalizedValue);
                    
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
                }
            }
            
            // Get updated option values
            List<OptionValue> optionValues = optionValueRepository.findByOption(option);
            
            // Map to response DTOs
            optionValuesResponse = optionValues.stream()
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
                .values(optionValuesResponse)
                .build();
        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation during option update: {}", e.getMessage());
            if (e.getMessage().toLowerCase().contains("option") && e.getMessage().toLowerCase().contains("name")) {
                throw new AppException(ErrorCode.OPTION_NAME_EXISTS);
            } else if (e.getMessage().toLowerCase().contains("option") && e.getMessage().toLowerCase().contains("value")) {
                throw new AppException(ErrorCode.OPTION_VALUE_EXISTS);
            } else {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating option with id {}: {}", request.getId(), e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
