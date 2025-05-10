package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.ProductCreateRequest;
import indiv.neitdev.nollie_furniture.dto.response.ProductResponse;
import indiv.neitdev.nollie_furniture.entity.Product;

public interface ProductService {
    /**
     * Creates a new product with options, option values, and images
     * @param request the product creation request
     * @return the created product response
     */
    ProductResponse createProduct(ProductCreateRequest request);
}
