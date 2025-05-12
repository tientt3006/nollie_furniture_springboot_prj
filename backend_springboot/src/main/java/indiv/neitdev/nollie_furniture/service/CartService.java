package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.AddToCartReq;
import indiv.neitdev.nollie_furniture.dto.response.CartResponse;

public interface CartService {
    String addToCart(AddToCartReq request);
    
    // Add a new method to view cart contents
    CartResponse viewCart();
}
