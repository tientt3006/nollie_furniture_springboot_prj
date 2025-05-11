package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.entity.Product;
import indiv.neitdev.nollie_furniture.exception.AppException;
import indiv.neitdev.nollie_furniture.exception.ErrorCode;
import indiv.neitdev.nollie_furniture.repository.OrderItemRepository;
import indiv.neitdev.nollie_furniture.repository.OrderRepository;
import indiv.neitdev.nollie_furniture.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    
    @Override
    public List<Product> getTopSellingProducts(int limit) {
        try {
            // Calculate the date one month ago from now
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            
            // Get top selling products from the repository
            return orderItemRepository.findTopSellingProducts(oneMonthAgo, limit);
        } catch (Exception e) {
            log.error("Error getting top selling products: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
