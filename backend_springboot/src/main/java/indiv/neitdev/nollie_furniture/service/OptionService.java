package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.OptionCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.OptionUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.OptionResponse;

import java.util.List;

public interface OptionService {
    OptionResponse createOption(OptionCreateRequest request);
    List<OptionResponse> getAllOptions();
    OptionResponse getOptionById(int id);
    OptionResponse updateOption(OptionUpdateRequest request);
    void deleteOption(int id);
}
