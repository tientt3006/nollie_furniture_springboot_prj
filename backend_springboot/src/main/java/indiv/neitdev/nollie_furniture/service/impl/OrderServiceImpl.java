package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.repository.OrderItemRepository;
import indiv.neitdev.nollie_furniture.repository.OrderRepository;
import indiv.neitdev.nollie_furniture.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderServiceImpl  implements OrderService {
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
}
