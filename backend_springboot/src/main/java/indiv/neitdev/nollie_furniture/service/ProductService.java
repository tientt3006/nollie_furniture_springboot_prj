package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.*;
import indiv.neitdev.nollie_furniture.dto.response.ProductResponse;
import indiv.neitdev.nollie_furniture.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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
    
    /**
     * Deletes a product option value
     * @param prodOptValId the ID of the product option value to delete
     * @return the updated product response
     */
    ProductResponse deleteProductOptionValue(Integer prodOptValId);
    
    /**
     * Adds a new option to an existing product
     * @param request the product option add request
     * @return the updated product response
     */
    ProductResponse addProductOption(ProductOptionAddRequest request);

    ProductResponse deleteProductOption(Integer prodOptId);

    /**
     * Deletes a product and all related data (images, options, option values)
     * @param prodId the ID of the product to delete
     * @return success message
     */
    String deleteProduct(Integer prodId);

    /**
     * Gets a paginated list of products with optional filtering and searching
     * @param pageable the pagination information
     * @param category optional category name to filter by
     * @param search optional search term (product ID or name)
     * @return page of products matching the criteria
     */
    Page<Product> getProducts(Pageable pageable, String category, String search);
    
    /**
     * Converts a Product entity to a ProductResponse DTO
     * @param product the product entity
     * @return the product response DTO
     */
    ProductResponse toProductResponse(Product product);

    Page<Product> getProductPageForCustomer(
        PageRequest pageRequest,
        String search,
        List<Integer> categories,
        List<Integer> colors,
        List<Integer> materials,
        BigDecimal minPrice, BigDecimal maxPrice,
        BigDecimal minHeight, BigDecimal maxHeight,
        BigDecimal minWidth, BigDecimal maxWidth,
        BigDecimal minLength, BigDecimal maxLength
    );
}
