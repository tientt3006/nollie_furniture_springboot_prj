package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.OptionCreateRequest;
import indiv.neitdev.nollie_furniture.dto.response.OptionResponse;

public interface OptionService {
    OptionResponse createOption(OptionCreateRequest request);
}
