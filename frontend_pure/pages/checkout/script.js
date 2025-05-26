// document.querySelector('.checkout').addEventListener('click', function() {
//     alert('Checkout.');
//     window.location.href = '../io/order_detail.html?orderId=${orderId}'; // Redirect to homepage
// });
// let orderId = null;
// document.querySelector('.quotation').addEventListener('click', function(event) {
//     event.preventDefault();
//     alert('Báo giá đã được lưu. Bạn có thể quay lại sau.');
// });

  

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


// API configuration
const API_BASE_URL = 'http://localhost:8080/api';

// Get auth token from localStorage
function getAuthToken() {
    return localStorage.getItem('authToken');
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Load cart items and display order summary
async function loadCartItems() {
    try {
        const token = getAuthToken();
        if (!token) {
            throw new Error('Please login to continue');
        }

        const response = await fetch(`${API_BASE_URL}/cart/view`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load cart items');
        }

        const data = await response.json();
        displayCartItems(data.result);
    } catch (error) {
        console.error('Error loading cart:', error);
        document.getElementById('cart-items').innerHTML = `
            <div class="error">
                Failed to load cart items. Please try again.
            </div>
        `;
    }
}

// Display cart items in order summary
function displayCartItems(cartData) {
    const cartItemsContainer = document.getElementById('cart-items');
    const totalAmountElement = document.getElementById('total-amount');

    if (!cartData || !cartData.items || cartData.items.length === 0) {
        cartItemsContainer.innerHTML = '<p>Your cart is empty</p>';
        totalAmountElement.textContent = formatCurrency(0);
        return;
    }

    const itemsHTML = cartData.items.map(item => `
        <div class="cart-item">
            <div class="item-info">
                <div class="item-name">${item.productName}</div>
                <div class="item-details">
                    ${item.optionName}: ${item.optionValueName} × ${item.quantity}
                </div>
            </div>
            <div class="item-price">${formatCurrency(item.totalPrice)}</div>
        </div>
    `).join('');

    cartItemsContainer.innerHTML = itemsHTML;
    totalAmountElement.textContent = formatCurrency(cartData.total);
}

// Handle checkout form submission
async function handleCheckout(event) {
    event.preventDefault();
    
    const checkoutBtn = document.getElementById('checkout-btn');
    const originalText = checkoutBtn.textContent;
    
    try {
        // Disable button and show loading state
        checkoutBtn.disabled = true;
        checkoutBtn.textContent = 'Processing...';

        // Get form data
        const fullName = document.getElementById('fullName').value.trim();
        const address = document.getElementById('address').value.trim();
        const phone = document.getElementById('phone').value.trim();
        const email = document.getElementById('email').value.trim();
        const notes = document.getElementById('notes').value.trim();
        const paymentMethod = document.querySelector('input[name="payment"]:checked').value;

        // Validate required fields
        if (!fullName || !address || !phone) {
            throw new Error('Please fill in all required fields');
        }

        const token = getAuthToken();
        if (!token) {
            throw new Error('Please login to continue');
        }

        // Prepare order data
        const orderData = {
            fullName,
            address,
            phone,
            email,
            paymentMethod,
            notes
        };

        // Make order API call
        const response = await fetch(`${API_BASE_URL}/order/make-order`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(orderData)
        });

        if (!response.ok) {
            throw new Error('Failed to create order');
        }

        const result = await response.json();
        
        if (result.code === 1000) {
            // Extract order ID from response
            const orderId = Object.values(result.result)[0];
            
            // Show success message
            alert('Order placed successfully!');
            
            // Redirect to order details page with order ID
            window.location.href = `../io/order_detail.html?orderId=${orderId}`;
        } else {
            throw new Error('Order creation failed');
        }

    } catch (error) {
        console.error('Checkout error:', error);
        alert(error.message || 'Failed to place order. Please try again.');
    } finally {
        // Re-enable button
        checkoutBtn.disabled = false;
        checkoutBtn.textContent = originalText;
    }
}

// Load user information and pre-fill form
async function loadUserInfo() {
    try {
        const token = getAuthToken();
        if (!token) {
            console.log('No auth token found, skipping user info load');
            return;
        }

        const response = await fetch(`${API_BASE_URL}/user/my-info`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
        });

        if (!response.ok) {
            throw new Error('Failed to load user information');
        }

        const data = await response.json();
        
        if (data.code === 1000 && data.result) {
            // Pre-fill form fields with user data
            const userInfo = data.result;
            
            document.getElementById('fullName').value = userInfo.fullName || '';
            document.getElementById('address').value = userInfo.address || '';
            document.getElementById('phone').value = userInfo.phone || '';
            document.getElementById('email').value = userInfo.email || '';
        }
    } catch (error) {
        console.error('Error loading user info:', error);
        // Don't show error to user as this is optional functionality
    }
}

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    // Load user information first
    loadUserInfo();
    
    // Load cart items
    loadCartItems();
    
    // Set up checkout form handler
    const checkoutBtn = document.getElementById('checkout-btn');
    checkoutBtn.addEventListener('click', handleCheckout);
    
    // Remove old quotation button handler if it exists
    const quotationBtn = document.querySelector('.quotation');
    if (quotationBtn) {
        quotationBtn.remove();
    }
});
