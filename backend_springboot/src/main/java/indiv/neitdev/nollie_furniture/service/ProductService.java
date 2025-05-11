package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.*;
import indiv.neitdev.nollie_furniture.dto.response.ProductResponse;
import indiv.neitdev.nollie_furniture.entity.Product;

import java.util.List;

public interface ProductService {
    /**
     * Creates a new product with options, option values, and images
     * @param request the product creation request
     * @return the created product response
     */
    ProductResponse createProduct(ProductCreateRequest request);
    
    /**
     * Gets all products
     * @return list of all products
     */
    List<ProductResponse> getAllProducts();
    
    /**
     * Gets a product by its ID
     * @param id the product ID
     * @return the product response
     */
    ProductResponse getProductById(Integer id);
    
    /**
     * Updates the base information of a product
     * @param request the product base info update request
     * @return the updated product response
     */
    ProductResponse updateProductBaseInfo(ProdBaseInfoUpdateReq request);
    
    /**
     * Updates product images including base image and other images
     * @param request the product image update request
     * @return the updated product response
     */
    ProductResponse updateProductImages(ProdImgUpdateReq request);
    
    /**
     * Updates a product option value
     * @param request the product option value update request
     * @return the updated product response
     */
    ProductResponse updateProductOptionValue(ProdOptValUpdReq request);
    
    /**
     * Adds a new option value to a product
     * @param request the product option value add request
     * @return the updated product response
     */
    ProductResponse addProductOptionValue(ProdOptValAddReq request);
}
