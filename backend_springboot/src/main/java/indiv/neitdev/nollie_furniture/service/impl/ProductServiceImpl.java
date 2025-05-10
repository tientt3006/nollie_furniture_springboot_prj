package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.dto.request.ProductCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.ProductOptionCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.ProductOptionValueCreateRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
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
            String baseImageUrl = null;
            List<String> otherImagesUrl = new ArrayList<>();
            
            // Save base image
            if (request.getBaseProductImage() != null && !request.getBaseProductImage().isEmpty()) {
                baseImageUrl = saveProductImage(product, request.getBaseProductImage(), true);
            }
            
            // Save other images
            if (request.getOtherProductImages() != null && !request.getOtherProductImages().isEmpty()) {
                for (MultipartFile image : request.getOtherProductImages()) {
                    if (!image.isEmpty()) {
                        String imageUrl = saveProductImage(product, image, false);
                        otherImagesUrl.add(imageUrl);
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
                    .otherImagesUrl(otherImagesUrl)
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

    private String saveProductImage(Product product, MultipartFile image, boolean isBaseImage) {
        try {
            String imageUrl = awsS3Service.saveImageToS3(image);
            String imageName = image.getOriginalFilename();
            
            ProductImg productImg = ProductImg.builder()
                    .product(product)
                    .imgUrl(imageUrl)
                    .imgName(imageName)
                    .build();
            
            productImgRepository.save(productImg);
            
            return imageUrl;
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
        
        // Build and return response
        return ProductOptionResponse.builder()
                .optionId(option.getId())
                .optionName(option.getName())
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
        String baseImageUrl = null;
        List<String> otherImagesUrl = new ArrayList<>();
        
        for (ProductImg img : productImgs) {
            // Add image URLs to the appropriate lists
            if (baseImageUrl == null) {
                baseImageUrl = img.getImgUrl(); // First image is used as base image
            } else {
                otherImagesUrl.add(img.getImgUrl());
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
                .otherImagesUrl(otherImagesUrl)
                .productOptionResponseList(productOptionResponses)
                .build();
    }
}
