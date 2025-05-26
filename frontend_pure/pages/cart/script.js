document.querySelectorAll('.remove').forEach(button => {
    button.addEventListener('click', function() {
      this.closest('.cart-item').remove();
    });
  });
  
//   document.querySelector('.apply-discount').addEventListener('click', function() {
//     alert('Mã giảm giá chưa được áp dụng.');
//   });
  
//   document.querySelector('.checkout').addEventListener('click', function() {
//     alert('Đi đến thanh toán chưa được triển khai.');
//   });
//   document.querySelector('.toggle-discount').addEventListener('click', function() {
//     let discountSection = document.querySelector('.discount');
//     if (discountSection.style.display === 'none') {
//       discountSection.style.display = 'flex';
//       this.innerHTML = '<div>Add discount code</div><div>▲</div>';
//     } else {
//       discountSection.style.display = 'none';
//       this.innerHTML = '<div>Add discount code</div><div>▼</div>';
//     }
//   });
//   ▲ ▼



// Header and Navigation Menu
const header = document.querySelector(".header");
const navLinks = document.querySelectorAll(".nav-links a");
const navIcons = document.querySelectorAll(".nav-icons img");
const menuIcon = document.querySelector(".menu-icon");
const furMenu = document.querySelector(".fur-menu");
const collectionMenu = document.querySelector(".collecion-menu");
const careMenu = document.querySelector(".care-menu");
const logo = document.querySelector(".logo img");
const logoName = document.querySelector(".logo-name");

menuIcon.addEventListener("click", () => {
    document.getElementById('sidebar').classList.toggle('show');
    document.getElementById('menu-overlay').classList.toggle('show');
});

furMenu.addEventListener("click", (event) => {
    event.preventDefault();
    document.getElementById('sidebar-fur').classList.toggle('show');
    document.getElementById('sidebar-collection').classList.remove('show');
    document.getElementById('sidebar-care').classList.remove('show');
});

// collectionMenu.addEventListener("click", (event) => {
//     event.preventDefault();
//     document.getElementById('sidebar-collection').classList.toggle('show');
//     document.getElementById('sidebar-fur').classList.remove('show');
//     document.getElementById('sidebar-care').classList.remove('show');
// });
careMenu.addEventListener("click", (event) => {
    event.preventDefault();
    document.getElementById('sidebar-care').classList.toggle('show');
    document.getElementById('sidebar-collection').classList.remove('show');
    document.getElementById('sidebar-fur').classList.remove('show');
});


// User dropdown menu
const userIcon = document.querySelector(".user-icon");
const dropdownMenu = document.querySelector(".dropdown-menu");
const signinItem = document.getElementById("signin");
const signupItem = document.getElementById("signup");
const infoItem = document.getElementById("info");
const logoutItem = document.getElementById("logout");

// Simulate login state (replace with actual logic)
let isLoggedIn = false;

function updateDropdownMenu() {
    if (isLoggedIn) {
        signinItem.style.display = "none";
        signupItem.style.display = "none";
        infoItem.style.display = "block";
        logoutItem.style.display = "block";
    } else {
        signinItem.style.display = "block";
        signupItem.style.display = "block";
        infoItem.style.display = "none";
        logoutItem.style.display = "none";
    }
}

// Update dropdown menu on page load
updateDropdownMenu();

// Example: Toggle login state on logout click
logoutItem.addEventListener("click", (event) => {
    event.preventDefault();
    isLoggedIn = false;
    updateDropdownMenu();
});

// Example: Simulate login on sign-in click
signinItem.addEventListener("click", (event) => {
    event.preventDefault();
    isLoggedIn = true;
    updateDropdownMenu();
});

userIcon.addEventListener("click", (event) => {
    event.stopPropagation(); // Prevent click from propagating to the document
    document.querySelector(".user-dropdown").classList.toggle("active");
});

document.addEventListener("click", () => {
    document.querySelector(".user-dropdown").classList.remove("active");
});

// Prevent closing the dropdown when clicking inside it
dropdownMenu.addEventListener("click", (event) => {
    event.stopPropagation();
});

function allSidebarOff() {
    document.getElementById('sidebar').classList.remove('show');
    document.getElementById('sidebar-fur').classList.remove('show');
    document.getElementById('sidebar-collection').classList.remove('show');
    document.getElementById('menu-overlay').classList.remove('show');
    document.getElementById('sidebar-care').classList.remove('show');
    searchPopup.classList.remove('show');
}

// serach popup
const searchIcon = document.querySelector(".nav-icons img[alt='Search']");
const searchPopup = document.getElementById("search-popup");
const searchInput = document.getElementById("search-input");
const searchResultsList = document.getElementById("search-results-list");

searchIcon.addEventListener("click", () => {
const searchPopup = document.getElementById("search-popup");
    searchPopup.classList.toggle('show');
    document.getElementById('menu-overlay').classList.toggle('show');
});

function CloseSearchPopup() {
    searchPopup.classList.remove('show');
    // searchPopup.style.display = "none";
    document.getElementById('menu-overlay').classList.remove('show');
    searchInput.value = "";
    searchResultsList.innerHTML = "";
}
async function performSearch() {
    const query = searchInput.value.trim();
    if (!query) return;

    try {
        // API configuration
        const API_BASE_URL = 'http://localhost:8080/api';
        // Show loading state
        searchResultsList.innerHTML = '<li style="text-align: center; padding: 20px;">Searching...</li>';
        
        // Call API with search parameter
        const response = await fetch(`${API_BASE_URL}/product/page/customer?page=1&size=10&search=${encodeURIComponent(query)}`);
        const data = await response.json();
        
        if (data.code === 1000 && data.result && data.result.products) {
            displaySearchResults(data.result.products);
        } else {
            searchResultsList.innerHTML = '<li style="text-align: center; padding: 20px; color: #666;">No products found</li>';
        }
    } catch (error) {
        console.error('Search error:', error);
        searchResultsList.innerHTML = '<li style="text-align: center; padding: 20px; color: #dc3545;">Error occurred while searching</li>';
    }
}

function displaySearchResults(products) {
    if (!products || products.length === 0) {
        searchResultsList.innerHTML = '<li style="text-align: center; padding: 20px; color: #666;">No products found</li>';
        return;
    }
    
    searchResultsList.innerHTML = '';
    
    products.forEach(product => {
        // Get base image URL
        let baseImageUrl = '../../images/placeholder.png';
        if (product.baseImageUrl) {
            const imageId = Object.keys(product.baseImageUrl)[0];
            baseImageUrl = product.baseImageUrl[imageId];
        }
        
        // Format price
        const formattedPrice = new Intl.NumberFormat('vi-VN').format(product.basePrice) + ' đ';
        
        // Create search result item
        const li = document.createElement('li');
        li.style.cssText = 'padding: 10px; border-bottom: 1px solid #eee; cursor: pointer; display: flex; align-items: center; gap: 15px;';
        li.innerHTML = `
            <img src="${baseImageUrl}" alt="${product.name}" 
                 style="width: 60px; height: 60px; object-fit: cover; border-radius: 5px;"
                 onerror="this.src='../../images/placeholder.png'">
            <div style="flex: 1;">
                <h4 style="margin: 0 0 5px 0; font-size: 16px; font-weight: 600;">${product.name}</h4>
                <p style="margin: 0; color: #666; font-size: 14px;">${product.category ? product.category.name : ''}</p>
                <p style="margin: 5px 0 0 0; font-weight: bold; color: #000;">${formattedPrice}</p>
            </div>
        `;
        
        // Add click handler to navigate to product page
        li.addEventListener('click', () => {
            window.location.href = `../product/product.html?id=${product.productId}`;
        });
        
        // Add hover effect
        li.addEventListener('mouseenter', () => {
            li.style.backgroundColor = '#f5f5f5';
        });
        li.addEventListener('mouseleave', () => {
            li.style.backgroundColor = 'transparent';
        });
        
        searchResultsList.appendChild(li);
    });
}

// Add Enter key support for search input in the main search functionality
searchInput.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        performSearch();
        // Don't close popup - let user see results
    }
});


// Cart API functions
const API_BASE_URL = 'http://localhost:8080/api';

function getAuthToken() {
    return localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
}

function getHeaders() {
    const token = getAuthToken();
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

async function viewCart() {
    try {
        document.getElementById('loading').style.display = 'block';
        
        const response = await fetch(`${API_BASE_URL}/cart/view`, {
            method: 'GET',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to fetch cart');
        }

        const data = await response.json();
        
        if (data.code === 1000) {
            renderCart(data.result);
        } else {
            throw new Error('Failed to load cart');
        }
    } catch (error) {
        console.error('Error viewing cart:', error);
        showEmptyCart();
    } finally {
        document.getElementById('loading').style.display = 'none';
    }
}

async function updateCartItemQuantity(itemId, quantity) {
    try {
        const response = await fetch(`${API_BASE_URL}/cart/item/${itemId}/quantity/${quantity}`, {
            method: 'PUT',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to update quantity');
        }

        const data = await response.json();
        
        if (data.code === 1000) {
            // Refresh cart after successful update
            viewCart();
        } else {
            throw new Error('Failed to update quantity');
        }
    } catch (error) {
        console.error('Error updating quantity:', error);
        alert('Failed to update quantity');
    }
}

async function removeCartItem(itemId) {
    try {
        const response = await fetch(`${API_BASE_URL}/cart/item/${itemId}`, {
            method: 'DELETE',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to remove item');
        }

        const data = await response.json();
        
        if (data.code === 1000) {
            // Refresh cart after successful removal
            viewCart();
        } else {
            throw new Error('Failed to remove item');
        }
    } catch (error) {
        console.error('Error removing item:', error);
        alert('Failed to remove item');
    }
}

async function clearCart() {
    if (!confirm('Are you sure you want to clear your cart?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/cart/clear`, {
            method: 'DELETE',
            headers: getHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to clear cart');
        }

        const data = await response.json();
        
        if (data.code === 1000) {
            showEmptyCart();
        } else {
            throw new Error('Failed to clear cart');
        }
    } catch (error) {
        console.error('Error clearing cart:', error);
        alert('Failed to clear cart');
    }
}

function renderCart(cartData) {
    const cartContainer = document.getElementById('cart-container');
    const cartItemsContainer = document.getElementById('cart-items');
    const emptyCart = document.getElementById('empty-cart');
    
    if (!cartData.items || cartData.items.length === 0) {
        showEmptyCart();
        return;
    }

    // Show cart container
    cartContainer.style.display = 'flex';
    emptyCart.style.display = 'none';

    // Update item count
    document.getElementById('cart-item-count').textContent = `${cartData.items.length} items`;

    // Render cart items
    cartItemsContainer.innerHTML = cartData.items.map(item => `
        <div class="cart-item" data-item-id="${item.id}">
            <div class="cart-image">
                <img src="${item.productImageUrl}" alt="${item.productName}">
            </div>
            <div class="item-info">
                <div class="item-info-row1">
                    <div>
                        <h2><b>${item.productName}</b></h2>
                        ${item.optionName ? `<p>${item.optionName}: ${item.optionValueName}</p>` : ''}
                        <br>
                        <span style="color: gray; font-size: 12px">Price: </span>
                        <p class="price"><b>${formatPrice(item.itemPrice)} đ</b></p>
                    </div>
                    <div class="item-actions">
                        <input class="quantity" type="number" value="${item.quantity}" min="1" max="50" 
                               onchange="updateQuantity(${item.id}, this.value)">
                    </div>
                </div>
                <hr>
                <div class="item-info-row2">
                    <div>
                        <p><span style="color: gray;">Total: </span> ${formatPrice(item.totalPrice)} đ</p>
                    </div>
                    <div class="item-actions">
                        <button class="remove" onclick="removeItem(${item.id})">Remove</button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');

    // Update totals
    document.getElementById('subtotal').textContent = `${formatPrice(cartData.total)} đ`;
    document.getElementById('total-price').textContent = `${formatPrice(cartData.total)} đ`;
}

function showEmptyCart() {
    document.getElementById('cart-container').style.display = 'none';
    document.getElementById('empty-cart').style.display = 'block';
    document.getElementById('cart-item-count').textContent = '0 items';
}

function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price);
}

function updateQuantity(itemId, quantity) {
    if (quantity < 1 || quantity > 50) {
        alert('Quantity must be between 1 and 50');
        viewCart(); // Refresh to reset quantity
        return;
    }
    updateCartItemQuantity(itemId, quantity);
}

function removeItem(itemId) {
    if (confirm('Are you sure you want to remove this item?')) {
        removeCartItem(itemId);
    }
}

// Load cart when page loads
document.addEventListener('DOMContentLoaded', function() {
    viewCart();
});
