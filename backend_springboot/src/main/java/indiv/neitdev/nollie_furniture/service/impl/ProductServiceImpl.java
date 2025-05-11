package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.dto.request.*;
import indiv.neitdev.nollie_furniture.dto.response.ProductOptionResponse;
import indiv.neitdev.nollie_furniture.dto.response.ProductOptionValueResponse;
import indiv.neitdev.nollie_furniture.dto.response.ProductResponse;
import indiv.neitdev.nollie_furniture.entity.*;
import indiv.neitdev.nollie_furniture.exception.AppException;
import indiv.neitdev.nollie_furniture.exception.ErrorCode;
import indiv.neitdev.nollie_furniture.repository.*;
import indiv.neitdev.nollie_furniture.service.AwsS3Service;
import indiv.neitdev.nollie_furniture.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    ProductOptionRepository productOptionRepository;
    ProductOptionValueRepository productOptionValueRepository;
    ProductImgRepository productImgRepository;
    CategoryRepository categoryRepository;
    OptionRepository optionRepository;
    OptionValueRepository optionValueRepository;
    AwsS3Service awsS3Service;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        try {
            // 1. Validate request
            validateProductCreateRequest(request);
            
            // 2. Get the category
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

            // 3. Create and save the product
            Product product = Product.builder()
                    .category(category)
                    .name(request.getName().trim())
                    .basePrice(request.getBasePrice())
                    .height(request.getHeight())
                    .width(request.getWidth())
                    .length(request.getLength())
                    .description(request.getDescription())
                    .baseProductQuantity(request.getBaseProductQuantity())
                    .build();
            
            product = productRepository.save(product);

            // 4. Handle product images
            Map<Integer, String> baseImageUrl = null;
            List<Map<Integer, String>> otherImagesUrl = new ArrayList<>();
            
            // Save base image
            if (request.getBaseProductImage() != null && !request.getBaseProductImage().isEmpty()) {
                ProductImg baseImg = saveProductImage(product, request.getBaseProductImage(), true);
                baseImageUrl = Map.of(baseImg.getId(), baseImg.getImgUrl());
            }
            
            // Save other images
            if (request.getOtherProductImages() != null && !request.getOtherProductImages().isEmpty()) {
                for (MultipartFile image : request.getOtherProductImages()) {
                    if (!image.isEmpty()) {
                        ProductImg otherImg = saveProductImage(product, image, false);
                        otherImagesUrl.add(Map.of(otherImg.getId(), otherImg.getImgUrl()));
                    }
                }
            }

            // 5. Handle product options and their values
            List<ProductOptionResponse> productOptionResponses = new ArrayList<>();
            
            if (request.getProductOptionCreateRequestList() != null && !request.getProductOptionCreateRequestList().isEmpty()) {
                for (ProductOptionCreateRequest optionRequest : request.getProductOptionCreateRequestList()) {
                    ProductOptionResponse optionResponse = createProductOption(product, optionRequest);
                    productOptionResponses.add(optionResponse);
                }
            }

            // 6. Build and return response
            return ProductResponse.builder()
                    .productId(product.getId())
                    .category(product.getCategory())
                    .name(product.getName())
                    .basePrice(product.getBasePrice())
                    .height(product.getHeight())
                    .width(product.getWidth())
                    .length(product.getLength())
                    .description(product.getDescription())
                    .baseProductQuantity(product.getBaseProductQuantity())
                    .baseImageUrl(baseImageUrl)
                    .otherImageUrl(otherImagesUrl)
                    .productOptionResponseList(productOptionResponses)
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private void validateProductCreateRequest(ProductCreateRequest request) {
        // Basic validation
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NAME_BLANK);
        }
        
        // Check if product name is unique
        if (productRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new AppException(ErrorCode.PRODUCT_NAME_ALREADY_EXISTS);
        }
        
        if (request.getBasePrice() == null || request.getBasePrice().doubleValue() <= 0) {
            throw new AppException(ErrorCode.PRODUCT_PRICE_INVALID);
        }
        
        if (request.getCategoryId() == null) {
            throw new AppException(ErrorCode.CATEGORY_ID_BLANK);
        }
        
        if (request.getBaseProductQuantity() == null || request.getBaseProductQuantity() < 0) {
            throw new AppException(ErrorCode.PRODUCT_QUANTITY_INVALID);
        }
        
        // Options validation
        if (request.getProductOptionCreateRequestList() != null && !request.getProductOptionCreateRequestList().isEmpty()) {
            // Check for duplicate option IDs
            List<Integer> optionIds = request.getProductOptionCreateRequestList().stream()
                    .map(ProductOptionCreateRequest::getOptionId)
                    .collect(Collectors.toList());
            
            if (optionIds.size() != optionIds.stream().distinct().count()) {
                throw new AppException(ErrorCode.DUPLICATE_OPTION_IDS);
            }
            
            for (ProductOptionCreateRequest optionRequest : request.getProductOptionCreateRequestList()) {
                if (optionRequest.getOptionId() == null) {
                    throw new AppException(ErrorCode.OPTION_ID_BLANK);
                }
                
                // Verify option exists
                Option option = optionRepository.findById(optionRequest.getOptionId())
                        .orElseThrow(() -> new AppException(ErrorCode.OPTION_NOT_FOUND));
                
                if (optionRequest.getProductOptionValueCreateRequestList() == null || 
                    optionRequest.getProductOptionValueCreateRequestList().isEmpty()) {
                    throw new AppException(ErrorCode.OPTION_VALUES_EMPTY);
                }
                
                // Check for duplicate option value IDs
                List<Integer> optionValueIds = optionRequest.getProductOptionValueCreateRequestList().stream()
                        .map(ProductOptionValueCreateRequest::getOptionValueId)
                        .collect(Collectors.toList());
                
                if (optionValueIds.size() != optionValueIds.stream().distinct().count()) {
                    throw new AppException(ErrorCode.DUPLICATE_OPTION_VALUE_IDS);
                }
                
                for (ProductOptionValueCreateRequest valueRequest : optionRequest.getProductOptionValueCreateRequestList()) {
                    if (valueRequest.getOptionValueId() == null) {
                        throw new AppException(ErrorCode.OPTION_VALUE_ID_BLANK);
                    }
                    
                    // Verify option value exists and belongs to the correct option
                    OptionValue optionValue = optionValueRepository.findById(valueRequest.getOptionValueId())
                            .orElseThrow(() -> new AppException(ErrorCode.OPTION_VALUE_NOT_FOUND));
                    
                    if (!optionValue.getOption().getId().equals(option.getId())) {
                        throw new AppException(ErrorCode.OPTION_VALUE_NOT_BELONG_TO_OPTION);
                    }
                    
                    if (valueRequest.getQuantity() == null || valueRequest.getQuantity() < 0) {
                        throw new AppException(ErrorCode.PRODUCT_QUANTITY_INVALID);
                    }
                }
            }
        }
    }

    private ProductImg saveProductImage(Product product, MultipartFile image, boolean isBaseImage) {
        try {
            String imageUrl = awsS3Service.saveImageToS3(image);
            String imageName = image.getOriginalFilename();
            
            ProductImg productImg = ProductImg.builder()
                    .product(product)
                    .imgUrl(imageUrl)
                    .imgName(imageName)
                    .build();
            
            return productImgRepository.save(productImg);
        } catch (Exception e) {
            log.error("Error saving product image: {}", e.getMessage());
            throw new AppException(ErrorCode.FAIL_UPLOAD_TO_S3);
        }
    }

    private ProductOptionResponse createProductOption(Product product, ProductOptionCreateRequest optionRequest) {
        // Get the option
        Option option = optionRepository.findById(optionRequest.getOptionId())
                .orElseThrow(() -> new AppException(ErrorCode.OPTION_NOT_FOUND));
        
        // Create product option
        ProductOption productOption = ProductOption.builder()
                .product(product)
                .option(option)
                .build();
        
        productOption = productOptionRepository.save(productOption);
        
        // Process option values
        List<ProductOptionValueResponse> valueResponses = new ArrayList<>();
        
        for (ProductOptionValueCreateRequest valueRequest : optionRequest.getProductOptionValueCreateRequestList()) {
            ProductOptionValueResponse valueResponse = createProductOptionValue(productOption, valueRequest);
            valueResponses.add(valueResponse);
        }
        
        // Build and return response - UPDATED to include productOptionId
        return ProductOptionResponse.builder()
                .optionId(option.getId())
                .optionName(option.getName())
                .productOptionId(productOption.getId())  // Add productOptionId
                .productOptionValueResponseList(valueResponses)
                .build();
    }

    private ProductOptionValueResponse createProductOptionValue(ProductOption productOption, ProductOptionValueCreateRequest valueRequest) {
        // Get option value
        OptionValue optionValue = optionValueRepository.findById(valueRequest.getOptionValueId())
                .orElseThrow(() -> new AppException(ErrorCode.OPTION_VALUE_NOT_FOUND));
        
        // Create product option value
        ProductOptionValue productOptionValue = ProductOptionValue.builder()
                .productOption(productOption)
                .optionValue(optionValue)
                .quantity(valueRequest.getQuantity())
                .addPrice(valueRequest.getAddPrice() != null ? valueRequest.getAddPrice() : java.math.BigDecimal.ZERO)
                .build();
        
        // Handle option value images if any
        if (valueRequest.getProductOptionValueImages() != null && !valueRequest.getProductOptionValueImages().isEmpty()) {
            MultipartFile image = valueRequest.getProductOptionValueImages().get(0); // Use first image
            if (image != null && !image.isEmpty()) {
                try {
                    String imageUrl = awsS3Service.saveImageToS3(image);
                    productOptionValue.setImgUrl(imageUrl);
                } catch (Exception e) {
                    log.error("Error saving option value image: {}", e.getMessage());
                    throw new AppException(ErrorCode.FAIL_UPLOAD_TO_S3);
                }
            }
        }
        
        productOptionValue = productOptionValueRepository.save(productOptionValue);
        
        // Build and return response
        return ProductOptionValueResponse.builder()
                .optionValueId(optionValue.getId())
                .optionValueName(optionValue.getValue())
                .optionValueImgUrl(optionValue.getImgUrl())
                .productOptionValueId(productOptionValue.getId())
                .quantity(productOptionValue.getQuantity())
                .addPrice(productOptionValue.getAddPrice())
                .productOptionValueImgUrl(productOptionValue.getImgUrl())
                .build();
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        try {
            // 1. Get all products from the repository
            List<Product> products = productRepository.findAll();
            
            // 2. Convert each product to a ProductResponse
            List<ProductResponse> responses = new ArrayList<>();
            
            for (Product product : products) {
                // 3. Build product response for each product
                ProductResponse response = buildProductResponse(product);
                responses.add(response);
            }
            
            return responses;
        } catch (Exception e) {
            log.error("Error fetching all products: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    
    @Override
    public ProductResponse getProductById(Integer id) {
        try {
            // Find product by ID
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            
            // Build and return product response
            return buildProductResponse(product);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching product by id {}: {}", id, e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    
    private ProductResponse buildProductResponse(Product product) {
        // 1. Get product images
        List<ProductImg> productImgs = productImgRepository.findByProduct(product);
        
        // 2. Find base image and other images
        Map<Integer, String> baseImageUrl = null;
        List<Map<Integer, String>> otherImagesUrl = new ArrayList<>();
        
        for (ProductImg img : productImgs) {
            // Add image URLs to the appropriate lists
            if (baseImageUrl == null) {
                baseImageUrl = Map.of(img.getId(), img.getImgUrl()); // First image is used as base image
            } else {
                otherImagesUrl.add(Map.of(img.getId(), img.getImgUrl()));
            }
        }
        
        // 3. Get product options
        List<ProductOption> productOptions = productOptionRepository.findByProduct(product);
        List<ProductOptionResponse> productOptionResponses = new ArrayList<>();
        
        for (ProductOption productOption : productOptions) {
            Option option = productOption.getOption();
            
            // 4. Get product option values for this option
            List<ProductOptionValue> productOptionValues = productOptionValueRepository.findByProductOption(productOption);
            List<ProductOptionValueResponse> valueResponses = new ArrayList<>();
            
            for (ProductOptionValue productOptionValue : productOptionValues) {
                OptionValue optionValue = productOptionValue.getOptionValue();
                
                // 5. Build product option value response
                ProductOptionValueResponse valueResponse = ProductOptionValueResponse.builder()
                        .optionValueId(optionValue.getId())
                        .optionValueName(optionValue.getValue())
                        .optionValueImgUrl(optionValue.getImgUrl())
                        .productOptionValueId(productOptionValue.getId())
                        .quantity(productOptionValue.getQuantity())
                        .addPrice(productOptionValue.getAddPrice())
                        .productOptionValueImgUrl(productOptionValue.getImgUrl())
                        .build();
                
                valueResponses.add(valueResponse);
            }
            
            // 6. Build product option response with its values - UPDATED to include productOptionId
            ProductOptionResponse optionResponse = ProductOptionResponse.builder()
                    .optionId(option.getId())
                    .optionName(option.getName())
                    .productOptionId(productOption.getId())  // Add productOptionId
                    .productOptionValueResponseList(valueResponses)
                    .build();
            
            productOptionResponses.add(optionResponse);
        }
        
        // 7. Build and return complete product response
        return ProductResponse.builder()
                .productId(product.getId())
                .category(product.getCategory())
                .name(product.getName())
                .basePrice(product.getBasePrice())
                .height(product.getHeight())
                .width(product.getWidth())
                .length(product.getLength())
                .description(product.getDescription())
                .baseProductQuantity(product.getBaseProductQuantity())
                .baseImageUrl(baseImageUrl)
                .otherImageUrl(otherImagesUrl)
                .productOptionResponseList(productOptionResponses)
                .build();
    }

    @Override
    @Transactional
    public ProductResponse updateProductBaseInfo(ProdBaseInfoUpdateReq request) {
        try {
            // Find product by ID
            Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            
            // Check if name is being changed and if the new name already exists
            if (request.getName() != null && !request.getName().trim().isEmpty() && 
                !request.getName().equalsIgnoreCase(product.getName())) {
                if (productRepository.existsByNameIgnoreCase(request.getName().trim())) {
                    throw new AppException(ErrorCode.PRODUCT_NAME_ALREADY_EXISTS);
                }
                product.setName(request.getName().trim());
            }
            
            // Update category if provided
            if (request.getCategoryId() > 0) {
                Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
                product.setCategory(category);
            }
            
            // Update other fields if provided
            if (request.getBasePrice() != null) {
                product.setBasePrice(request.getBasePrice());
            }
            
            if (request.getHeight() != null) {
                product.setHeight(request.getHeight());
            }
            
            if (request.getWidth() != null) {
                product.setWidth(request.getWidth());
            }
            
            if (request.getLength() != null) {
                product.setLength(request.getLength());
            }
            
            if (request.getDescription() != null) {
                product.setDescription(request.getDescription());
            }
            
            if (request.getBaseProductQuantity() > 0) {
                product.setBaseProductQuantity(request.getBaseProductQuantity());
            }
            
            // Save the updated product
            product = productRepository.save(product);
            
            // Build and return the updated product response
            return buildProductResponse(product);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating product base info: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public ProductResponse updateProductImages(ProdImgUpdateReq request) {
        try {
            // Find base image by ID
            ProductImg baseProductImg = productImgRepository.findById(request.getBaseProdImgId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
            
            // Get product from base image
            Product product = baseProductImg.getProduct();
            
            // Update base image if new image is provided
            if (request.getNewBaseProdImg() != null && !request.getNewBaseProdImg().isEmpty()) {
                // Delete old image from S3 if it exists
                if (baseProductImg.getImgUrl() != null && !baseProductImg.getImgUrl().isEmpty()) {
                    String filename = baseProductImg.getImgUrl().substring(
                            baseProductImg.getImgUrl().lastIndexOf('/') + 1);
                    try {
                        awsS3Service.deleteImageFromS3(filename);
                    } catch (Exception e) {
                        log.error("Failed to delete product image from S3: {}", e.getMessage());
                        // Continue process even if image deletion fails
                    }
                }
                
                // Upload new image
                String newImageUrl = awsS3Service.saveImageToS3(request.getNewBaseProdImg());
                String newImageName = request.getNewBaseProdImg().getOriginalFilename();
                
                // Update image details
                baseProductImg.setImgUrl(newImageUrl);
                baseProductImg.setImgName(newImageName);
                
                // Save updated base image
                productImgRepository.save(baseProductImg);
            }
            
            // Delete other images if specified
            if (request.getOtherProdImgIdsForDelete() != null && !request.getOtherProdImgIdsForDelete().isEmpty()) {
                for (Integer imgId : request.getOtherProdImgIdsForDelete()) {
                    productImgRepository.findById(imgId).ifPresent(img -> {
                        // Verify this image belongs to the same product
                        if (!img.getProduct().getId().equals(product.getId())) {
                            throw new AppException(ErrorCode.PRODUCT_IMAGE_NOT_BELONG_TO_PRODUCT);
                        }
                        
                        // Delete image from S3
                        if (img.getImgUrl() != null && !img.getImgUrl().isEmpty()) {
                            String filename = img.getImgUrl().substring(
                                    img.getImgUrl().lastIndexOf('/') + 1);
                            try {
                                awsS3Service.deleteImageFromS3(filename);
                            } catch (Exception e) {
                                log.error("Failed to delete product image from S3: {}", e.getMessage());
                                // Continue deletion process even if image deletion fails
                            }
                        }
                        
                        // Delete image record from database
                        productImgRepository.delete(img);
                    });
                }
            }
            
            // Add new other images if specified
            if (request.getNewOtherProdImgList() != null && !request.getNewOtherProdImgList().isEmpty()) {
                for (MultipartFile image : request.getNewOtherProdImgList()) {
                    if (!image.isEmpty()) {
                        saveProductImage(product, image, false);
                    }
                }
            }
            
            // Return updated product response
            return buildProductResponse(product);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating product images: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public ProductResponse updateProductOptionValue(ProdOptValUpdReq request) {
        try {
            // Find product option value by ID
            ProductOptionValue productOptionValue = productOptionValueRepository.findById((long) request.getProdOptValId())
                    .orElseThrow(() -> new AppException(ErrorCode.OPTION_VALUE_NOT_FOUND));
            
            // Get product from product option value
            ProductOption productOption = productOptionValue.getProductOption();
            Product product = productOption.getProduct();
            
            // Update quantity if provided
            if (request.getQuantity() > 0) {
                productOptionValue.setQuantity(request.getQuantity());
            }
            
            // Update addPrice if provided
            if (request.getAddPrice() != null) {
                productOptionValue.setAddPrice(request.getAddPrice());
            }
            
            // Update image if new image is provided
            if (request.getNewProdOptValImg() != null && !request.getNewProdOptValImg().isEmpty()) {
                // Delete old image from S3 if exists
                if (productOptionValue.getImgUrl() != null && !productOptionValue.getImgUrl().isEmpty()) {
                    String filename = productOptionValue.getImgUrl().substring(
                            productOptionValue.getImgUrl().lastIndexOf('/') + 1);
                    try {
                        awsS3Service.deleteImageFromS3(filename);
                    } catch (Exception e) {
                        log.error("Failed to delete product option value image from S3: {}", e.getMessage());
                        // Continue process even if image deletion fails
                    }
                }
                
                // Upload new image
                String newImageUrl = awsS3Service.saveImageToS3(request.getNewProdOptValImg());
                
                // Update image URL
                productOptionValue.setImgUrl(newImageUrl);
            }
            
            // Save the updated product option value
            productOptionValueRepository.save(productOptionValue);
            
            // Return updated product response
            return buildProductResponse(product);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating product option value: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public ProductResponse addProductOptionValue(ProdOptValAddReq request) {
        try {
            // Find product option by ID
            ProductOption productOption = productOptionRepository.findById(request.getProductOptionId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));
            
            // Get product from product option
            Product product = productOption.getProduct();
            
            // Find option value by ID
            OptionValue optionValue = optionValueRepository.findById(request.getOptionValueId())
                    .orElseThrow(() -> new AppException(ErrorCode.OPTION_VALUE_NOT_FOUND));
            
            // Validate that option value belongs to the option associated with product option
            if (!optionValue.getOption().getId().equals(productOption.getOption().getId())) {
                throw new AppException(ErrorCode.OPTION_VALUE_NOT_BELONG_TO_OPTION);
            }

            // Validate quantity
            if (request.getQuantity() < 0) {
                throw new AppException(ErrorCode.PRODUCT_QUANTITY_INVALID);
            }
            
            // Check if this option value is already added to this product option
            boolean valueExists = productOptionValueRepository.findByProductOption(productOption)
                    .stream()
                    .anyMatch(pov -> pov.getOptionValue().getId().equals(optionValue.getId()));
                    
            if (valueExists) {
                throw new AppException(ErrorCode.OPTION_VALUE_ALREADY_ADDED);
            }
            
            // Create new product option value
            ProductOptionValue productOptionValue = ProductOptionValue.builder()
                    .productOption(productOption)
                    .optionValue(optionValue)
                    .quantity(request.getQuantity())
                    .addPrice(request.getAddPrice() != null ? request.getAddPrice() : BigDecimal.ZERO)
                    .build();
            
            // Handle image if provided
            if (request.getProdOptValImg() != null && !request.getProdOptValImg().isEmpty()) {
                String imageUrl = awsS3Service.saveImageToS3(request.getProdOptValImg());
                productOptionValue.setImgUrl(imageUrl);
            }
            
            // Save to database
            productOptionValueRepository.save(productOptionValue);
            
            // Return updated product
            return buildProductResponse(product);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding product option value: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public ProductResponse deleteProductOptionValue(Integer prodOptValId) {
        try {
            // Find product option value by ID
            ProductOptionValue productOptionValue = productOptionValueRepository.findById(prodOptValId.longValue())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_OPTION_VALUE_NOT_FOUND));
            
            // Get product from product option value
            ProductOption productOption = productOptionValue.getProductOption();
            Product product = productOption.getProduct();
            
            // Delete image from S3 if it exists
            if (productOptionValue.getImgUrl() != null && !productOptionValue.getImgUrl().isEmpty()) {
                String filename = productOptionValue.getImgUrl().substring(
                        productOptionValue.getImgUrl().lastIndexOf('/') + 1);
                try {
                    awsS3Service.deleteImageFromS3(filename);
                } catch (Exception e) {
                    log.error("Failed to delete product option value image from S3: {}", e.getMessage());
                    // Continue deletion process even if image deletion fails
                }
            }
            
            // Delete product option value from database
            productOptionValueRepository.delete(productOptionValue);
            
            // Return updated product response
            return buildProductResponse(product);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting product option value with ID {}: {}", prodOptValId, e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public ProductResponse addProductOption(ProductOptionAddRequest request) {
        try {
            // Find product by ID
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            
            // Find option by ID
            Option option = optionRepository.findById(request.getOptionId())
                    .orElseThrow(() -> new AppException(ErrorCode.OPTION_NOT_FOUND));
            
            // Check if this option is already added to this product
            boolean optionExists = productOptionRepository.findByProduct(product)
                    .stream()
                    .anyMatch(po -> po.getOption().getId().equals(option.getId()));
                    
            if (optionExists) {
                throw new AppException(ErrorCode.OPTION_ALREADY_ADDED_TO_PRODUCT);
            }
            
            // Create new product option
            ProductOption productOption = ProductOption.builder()
                    .product(product)
                    .option(option)
                    .build();
            
            // Save to database
            productOptionRepository.save(productOption);
            
            // Return updated product
            return buildProductResponse(product);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding product option: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public ProductResponse deleteProductOption(Integer prodOptId) {
        try {
            // Find product option by ID
            ProductOption productOption = productOptionRepository.findById(prodOptId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));
            
            // Get product from product option for the response
            Product product = productOption.getProduct();
            
            // Find all related product option values
            List<ProductOptionValue> productOptionValues = productOptionValueRepository.findByProductOption(productOption);
            
            // Delete all product option values first
            for (ProductOptionValue productOptionValue : productOptionValues) {
                // Delete image from S3 if it exists
                if (productOptionValue.getImgUrl() != null && !productOptionValue.getImgUrl().isEmpty()) {
                    String filename = productOptionValue.getImgUrl().substring(
                            productOptionValue.getImgUrl().lastIndexOf('/') + 1);
                    try {
                        awsS3Service.deleteImageFromS3(filename);
                    } catch (Exception e) {
                        log.error("Failed to delete product option value image from S3: {}", e.getMessage());
                        // Continue deletion process even if image deletion fails
                    }
                }
                
                // Delete product option value from database
                productOptionValueRepository.delete(productOptionValue);
            }
            
            // After deleting all option values, delete the product option itself
            productOptionRepository.delete(productOption);
            
            // Return updated product response
            return buildProductResponse(product);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting product option with ID {}: {}", prodOptId, e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public String deleteProduct(Integer prodId) {
        try {
            // Find product by ID
            Product product = productRepository.findById(prodId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            
            // 1. Delete all product images
            List<ProductImg> productImages = productImgRepository.findByProduct(product);
            for (ProductImg img : productImages) {
                // Delete image file from S3 bucket
                if (img.getImgUrl() != null && !img.getImgUrl().isEmpty()) {
                    String filename = img.getImgUrl().substring(img.getImgUrl().lastIndexOf('/') + 1);
                    try {
                        awsS3Service.deleteImageFromS3(filename);
                    } catch (Exception e) {
                        log.error("Failed to delete product image from S3: {}", e.getMessage());
                        // Continue deletion process even if image deletion fails
                    }
                }
            }
            // Delete all image records from database
            productImgRepository.deleteAll(productImages);
            
            // 2. Get all product options for this product
            List<ProductOption> productOptions = productOptionRepository.findByProduct(product);
            
            for (ProductOption productOption : productOptions) {
                // 3. For each product option, get and delete all product option values
                List<ProductOptionValue> productOptionValues = productOptionValueRepository.findByProductOption(productOption);
                
                for (ProductOptionValue value : productOptionValues) {
                    // Delete option value image from S3 if exists
                    if (value.getImgUrl() != null && !value.getImgUrl().isEmpty()) {
                        String filename = value.getImgUrl().substring(value.getImgUrl().lastIndexOf('/') + 1);
                        try {
                            awsS3Service.deleteImageFromS3(filename);
                        } catch (Exception e) {
                            log.error("Failed to delete product option value image from S3: {}", e.getMessage());
                            // Continue deletion process even if image deletion fails
                        }
                    }
                }
                
                // Delete all product option values for this option
                productOptionValueRepository.deleteAll(productOptionValues);
            }
            
            // 4. Delete all product options
            productOptionRepository.deleteAll(productOptions);
            
            // 5. Finally delete the product itself
            productRepository.delete(product);
            
            return "Product deleted successfully";
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting product with ID {}: {}", prodId, e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public Page<Product> getProducts(Pageable pageable, String category, String search) {
        try {
            // If both category and search are provided
            if (category != null && !category.isEmpty() && search != null && !search.isEmpty()) {
                return productRepository.findByCategoryAndSearch(category, search, pageable);
            }
            // If only category is provided
            else if (category != null && !category.isEmpty()) {
                return productRepository.findByCategory(category, pageable);
            }
            // If only search is provided
            else if (search != null && !search.isEmpty()) {
                return productRepository.findByCategoryAndSearch(null, search, pageable);
            }
            // If no filters are provided
            else {
                return productRepository.findAll(pageable);
            }
        } catch (Exception e) {
            log.error("Error fetching products with pagination: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public ProductResponse toProductResponse(Product product) {
        try {
            // 1. Get product images
            List<ProductImg> productImgs = productImgRepository.findByProduct(product);
            
            // 2. Find base image and other images
            Map<Integer, String> baseImageUrl = null;
            List<Map<Integer, String>> otherImagesUrl = new ArrayList<>();
            
            for (ProductImg img : productImgs) {
                // Add image URLs to the appropriate lists
                if (baseImageUrl == null) {
                    baseImageUrl = Map.of(img.getId(), img.getImgUrl()); // First image is used as base image
                } else {
                    otherImagesUrl.add(Map.of(img.getId(), img.getImgUrl()));
                }
            }
            
            // 3. Get product options
            List<ProductOption> productOptions = productOptionRepository.findByProduct(product);
            List<ProductOptionResponse> productOptionResponses = new ArrayList<>();
            
            for (ProductOption productOption : productOptions) {
                Option option = productOption.getOption();
                
                // 4. Get product option values for this option
                List<ProductOptionValue> productOptionValues = productOptionValueRepository.findByProductOption(productOption);
                List<ProductOptionValueResponse> valueResponses = new ArrayList<>();
                
                for (ProductOptionValue productOptionValue : productOptionValues) {
                    OptionValue optionValue = productOptionValue.getOptionValue();
                    
                    // 5. Build product option value response
                    ProductOptionValueResponse valueResponse = ProductOptionValueResponse.builder()
                            .optionValueId(optionValue.getId())
                            .optionValueName(optionValue.getValue())
                            .optionValueImgUrl(optionValue.getImgUrl())
                            .productOptionValueId(productOptionValue.getId())
                            .quantity(productOptionValue.getQuantity())
                            .addPrice(productOptionValue.getAddPrice())
                            .productOptionValueImgUrl(productOptionValue.getImgUrl())
                            .build();
                    
                    valueResponses.add(valueResponse);
                }
                
                // 6. Build product option response with its values
                ProductOptionResponse optionResponse = ProductOptionResponse.builder()
                        .optionId(option.getId())
                        .optionName(option.getName())
                        .productOptionId(productOption.getId())
                        .productOptionValueResponseList(valueResponses)
                        .build();
                
                productOptionResponses.add(optionResponse);
            }
            
            // 7. Build and return complete product response
            return ProductResponse.builder()
                    .productId(product.getId())
                    .category(product.getCategory())
                    .name(product.getName())
                    .basePrice(product.getBasePrice())
                    .height(product.getHeight())
                    .width(product.getWidth())
                    .length(product.getLength())
                    .description(product.getDescription())
                    .baseProductQuantity(product.getBaseProductQuantity())
                    .baseImageUrl(baseImageUrl)
                    .otherImageUrl(otherImagesUrl)
                    .productOptionResponseList(productOptionResponses)
                    .build();
        } catch (Exception e) {
            log.error("Error converting Product to ProductResponse: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
