package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.repository.CartItemRepository;
import indiv.neitdev.nollie_furniture.repository.CartRepository;
import indiv.neitdev.nollie_furniture.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartServiceImpl implements CartService {
    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
}
