package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.repository.OptionRepository;
import indiv.neitdev.nollie_furniture.repository.OptionValueRepository;
import indiv.neitdev.nollie_furniture.service.OptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OptionServiceImpl implements OptionService {
    OptionRepository optionRepository;
    OptionValueRepository optionValueRepository;

}
