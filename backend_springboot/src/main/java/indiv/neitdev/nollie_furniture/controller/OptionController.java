package indiv.neitdev.nollie_furniture.controller;

import indiv.neitdev.nollie_furniture.dto.request.OptionCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.OptionValueCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.OptionUpdateRequest;
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

        List<OptionValueCreateRequest> optionValues = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            OptionValueCreateRequest valueDto = new OptionValueCreateRequest();
            valueDto.setValue(values.get(i));

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

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OptionResponse> updateOption(
            @RequestBody @Valid OptionUpdateRequest request) {
        var result = optionService.updateOption(request);
        return ApiResponse.<OptionResponse>builder().result(result).build();
    }

    @PostMapping(value = "/update-with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OptionResponse> updateOptionWithImages(
            @RequestParam("id") int id,
            @RequestParam("name") String name,
            @RequestParam(value = "valueIdsForDelete", required = false) List<Integer> valueIdsForDelete,
            @RequestParam(value = "newValuesForAdd", required = false) List<String> values,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        // Build newValuesForAdd manually
        List<OptionValueCreateRequest> newValues = new ArrayList<>();
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                OptionValueCreateRequest valueDto = new OptionValueCreateRequest();
                valueDto.setValue(values.get(i));

                if (images != null && i < images.size() && images.get(i) != null && !images.get(i).isEmpty()) {
                    valueDto.setImg(images.get(i));
                }

                newValues.add(valueDto);
            }
        }

        OptionUpdateRequest request = new OptionUpdateRequest();
        request.setId(id);
        request.setName(name);
        request.setValueIdsForDelete(valueIdsForDelete);
        request.setNewValuesForAdd(newValues);

        var result = optionService.updateOption(request);
        return ApiResponse.<OptionResponse>builder().result(result).build();
    }


    @GetMapping("/all")
    public ApiResponse<List<OptionResponse>> getAllOptions() {
        var result = optionService.getAllOptions();
        return ApiResponse.<List<OptionResponse>>builder().result(result).build();
    }
    
    @GetMapping("/{id}")
    public ApiResponse<OptionResponse> getOptionById(@PathVariable int id) {
        var result = optionService.getOptionById(id);
        return ApiResponse.<OptionResponse>builder().result(result).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteOption(@PathVariable int id) {
        optionService.deleteOption(id);
        return ApiResponse.<Void>builder().message("Option deleted successfully").build();
    }
}
