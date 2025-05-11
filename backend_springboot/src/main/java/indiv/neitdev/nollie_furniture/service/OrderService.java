package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.entity.Product;

import java.util.List;

public interface OrderService {
    /**
     * Get top selling products in the last month
     * @param limit the number of products to return
     * @return list of top selling products
     */
    List<Product> getTopSellingProducts(int limit);
}
