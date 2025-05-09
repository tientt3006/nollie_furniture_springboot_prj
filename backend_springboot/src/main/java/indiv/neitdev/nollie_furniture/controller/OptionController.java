package indiv.neitdev.nollie_furniture.controller;

import indiv.neitdev.nollie_furniture.dto.request.OptionCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.OptionValueDto;
import indiv.neitdev.nollie_furniture.dto.response.ApiResponse;
import indiv.neitdev.nollie_furniture.dto.response.OptionResponse;
import indiv.neitdev.nollie_furniture.service.OptionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/option")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class OptionController {
    OptionService optionService;

    // Endpoint for JSON request
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OptionResponse> createOption(@RequestBody @Valid OptionCreateRequest request) {
        var result = optionService.createOption(request);
        return ApiResponse.<OptionResponse>builder().result(result).build();
    }

    // Endpoint for multipart form data request
    @PostMapping(value = "/create-with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OptionResponse> createOptionWithImages(
            @RequestParam("name") String name,
            @RequestParam("values") List<String> values,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        // Create request object from form data
        List<OptionValueDto> optionValues = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            OptionValueDto valueDto = new OptionValueDto();
            valueDto.setValue(values.get(i));
            
            // Assign image if available
            if (images != null && i < images.size()) {
                valueDto.setImg(images.get(i));
            }
            
            optionValues.add(valueDto);
        }
        
        OptionCreateRequest request = OptionCreateRequest.builder()
                .name(name)
                .values(optionValues)
                .build();

        var result = optionService.createOption(request);
        return ApiResponse.<OptionResponse>builder().result(result).build();
    }
}
