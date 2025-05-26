// API base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Product state to store current selections
let productState = {
    product: null,
    selectedOptions: {},
    quantity: 1,
    totalPrice: 0,
    basePrice: 0
};

// Initialize the page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Get product ID from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');
    
    if (productId) {
        // Fetch product details
        fetchProductDetails(productId);
    } else {
        // Handle missing product ID
        document.querySelector('.product-section').innerHTML = '<div class="error-message">Product not found. Please try another product.</div>';
    }

    // Setup quantity selector events
    const quantityInput = document.getElementById('quantity-input');
    const increaseBtn = document.getElementById('increase-quantity');
    const decreaseBtn = document.getElementById('decrease-quantity');
    
    if (quantityInput && increaseBtn && decreaseBtn) {
        increaseBtn.addEventListener('click', () => {
            quantityInput.value = parseInt(quantityInput.value) + 1;
            updateProductState();
        });
        
        decreaseBtn.addEventListener('click', () => {
            if (parseInt(quantityInput.value) > 1) {
                quantityInput.value = parseInt(quantityInput.value) - 1;
                updateProductState();
            }
        });
        
        quantityInput.addEventListener('change', () => {
            if (parseInt(quantityInput.value) < 1) {
                quantityInput.value = 1;
            }
            updateProductState();
        });
    }
    
    // Setup add to cart button event
    const addToCartBtn = document.querySelector('.add-to-cart');
    if (addToCartBtn) {
        addToCartBtn.addEventListener('click', addToCart);
    }
});

// Fetch product details from API
async function fetchProductDetails(productId) {
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE_URL}/product/${productId}`);
        const data = await response.json();
        
        if (data.code === 1000 && data.result) {
            // Store product data
            productState.product = data.result;
            productState.basePrice = data.result.basePrice;
            productState.totalPrice = data.result.basePrice;
            
            // Render product details
            renderProductDetails(data.result);
        } else {
            document.querySelector('.product-section').innerHTML = '<div class="error-message">Failed to load product details. Please try again later.</div>';
        }
    } catch (error) {
        console.error('Error fetching product details:', error);
        document.querySelector('.product-section').innerHTML = '<div class="error-message">An error occurred while loading product details. Please try again later.</div>';
    } finally {
        showLoading(false);
    }
}

// Render product details to the page
function renderProductDetails(product) {
    try {
        // Update product name and description
        const nameElement = document.querySelector('.product-info h1');
        const subtitleElement = document.querySelector('.product-info .subtitle');
        const descriptionElement = document.querySelector('.product-info .description');
        const priceElement = document.querySelector('.product-info .price');
        
        if (nameElement) nameElement.textContent = product.name || 'Product Name';
        if (subtitleElement) subtitleElement.textContent = product.category ? product.category.name : '';
        if (descriptionElement) descriptionElement.textContent = product.description || 'Quality takes time – most of our furniture is made to order and thoughtfully crafted just for you.';
        
        // Format and update price
        const formattedPrice = new Intl.NumberFormat('vi-VN').format(product.basePrice) + ' đ';
        if (priceElement) priceElement.textContent = formattedPrice;
        
        // Update dimensions if available and element exists
        const dimensionElement = document.querySelector('.option-btn[data-option="dimension"]');
        if (dimensionElement && product.length && product.width && product.height) {
            const dimensionText = `${product.length}×${product.width}×${product.height}`;
            dimensionElement.innerHTML = `Dimension: ${dimensionText} <span>Change ></span>`;
        } else if (dimensionElement) {
            dimensionElement.innerHTML = `Dimension: Not specified <span>Change ></span>`;
        }
        
        // Update images
        updateProductImages(product);
        
        // Create option selectors if options exist
        if (product.productOptionResponseList && product.productOptionResponseList.length > 0) {
            createOptionSelectors(product.productOptionResponseList);
        }
        
        // Enable add to cart button
        const addToCartBtn = document.querySelector('.add-to-cart');
        if (addToCartBtn) addToCartBtn.disabled = false;
        
        // Initialize product state
        updateProductState();
        
    } catch (error) {
        console.error('Error rendering product details:', error);
        showMessage('Error displaying product details', 'error');
    }
}

// Update product images from API data
function updateProductImages(product) {
    try {
        // Get image containers
        const mainImageContainer = document.getElementById('main-image');
        const thumbnailContainer = document.querySelector('.thumbnail-container');
        
        if (!mainImageContainer || !thumbnailContainer) {
            console.warn('Image containers not found');
            return;
        }
        
        // Clear existing thumbnails
        thumbnailContainer.innerHTML = '';
        
        // Get base image URL
        let baseImageUrl = '../../images/placeholder.png';
        if (product.baseImageUrl && typeof product.baseImageUrl === 'object') {
            const imageId = Object.keys(product.baseImageUrl)[0];
            if (imageId && product.baseImageUrl[imageId]) {
                baseImageUrl = product.baseImageUrl[imageId];
            }
        }
        
        // Set main image
        mainImageContainer.src = baseImageUrl;
        mainImageContainer.onerror = function() {
            this.src = '../../images/placeholder.png';
        };
        
        // Add base image as first thumbnail
        const baseThumbnail = createThumbnail(baseImageUrl, 'Main View');
        thumbnailContainer.appendChild(baseThumbnail);
        
        // Add other images as thumbnails
        if (product.otherImageUrl && Array.isArray(product.otherImageUrl) && product.otherImageUrl.length > 0) {
            product.otherImageUrl.forEach((imgObj, index) => {
                if (typeof imgObj === 'object') {
                    const imgId = Object.keys(imgObj)[0];
                    const imgUrl = imgObj[imgId];
                    if (imgUrl) {
                        const thumbnail = createThumbnail(imgUrl, `View ${index + 2}`);
                        thumbnailContainer.appendChild(thumbnail);
                    }
                }
            });

            // If there are more than 3 images total, hide the rest and show the "Show More" button
            const allThumbnails = thumbnailContainer.querySelectorAll('.thumbnail');
            const showMoreBtn = document.getElementById('show-more');
            if (allThumbnails.length > 3 && showMoreBtn) {
                for (let i = 3; i < allThumbnails.length; i++) {
                    allThumbnails[i].style.display = 'none';
                }
                showMoreBtn.style.display = 'block';
            } else if (showMoreBtn) {
                showMoreBtn.style.display = 'none';
            }
        } else {
            // If no additional images, hide the "Show More" button
            const showMoreBtn = document.getElementById('show-more');
            if (showMoreBtn) showMoreBtn.style.display = 'none';
        }
    } catch (error) {
        console.error('Error updating product images:', error);
    }
}

// Create a thumbnail image element
function createThumbnail(src, alt) {
    const img = document.createElement('img');
    img.src = src;
    img.alt = alt;
    img.classList.add('thumbnail');
    img.addEventListener('click', function() {
        document.getElementById('main-image').src = this.src;
    });
    return img;
}

// Create option selectors based on product options
function createOptionSelectors(options) {
    try {
        if (!options || options.length === 0) return;
        
        const optionsContainer = document.getElementById('product-options');
        if (!optionsContainer) {
            console.warn('Options container not found');
            return;
        }
        
        optionsContainer.innerHTML = '<p>Choose your design</p>';
        
        options.forEach(option => {
            if (!option.productOptionValueResponseList || option.productOptionValueResponseList.length === 0) {
                return; // Skip options without values
            }
            
            // Create option container
            const optionContainer = document.createElement('div');
            optionContainer.className = 'option-container';
            
            // Create option title
            const optionTitle = document.createElement('p');
            optionTitle.className = 'option-title';
            optionTitle.textContent = `Select ${option.optionName || 'Option'}:`;
            optionContainer.appendChild(optionTitle);
            
            // Create select element
            const select = document.createElement('select');
            select.className = 'option-select';
            select.dataset.optionId = option.optionId;
            select.dataset.optionName = option.optionName;
            
            // Add default option
            const defaultOption = document.createElement('option');
            defaultOption.value = '';
            defaultOption.textContent = `-- Select ${option.optionName || 'Option'} --`;
            select.appendChild(defaultOption);
            
            // Add option values
            option.productOptionValueResponseList.forEach(value => {
                const optionElement = document.createElement('option');
                optionElement.value = value.productOptionValueId;
                const addPriceText = value.addPrice > 0 ? ' (+' + new Intl.NumberFormat('vi-VN').format(value.addPrice) + ' đ)' : '';
                optionElement.textContent = `${value.optionValueName || 'Value'}${addPriceText}`;
                optionElement.dataset.addPrice = value.addPrice || 0;
                optionElement.dataset.quantity = value.quantity || 0;
                optionElement.dataset.imgUrl = value.productOptionValueImgUrl || '';
                select.appendChild(optionElement);
            });
            
            // Add change event listener
            select.addEventListener('change', function() {
                handleOptionChange(this);
            });
            
            optionContainer.appendChild(select);
            optionsContainer.appendChild(optionContainer);
        });
    } catch (error) {
        console.error('Error creating option selectors:', error);
    }
}

// Handle option change
function handleOptionChange(selectElement) {
    try {
        const selectedOption = selectElement.options[selectElement.selectedIndex];
        
        if (selectElement.value) {
            productState.selectedOptions[selectElement.dataset.optionId] = {
                optionValueId: selectElement.value,
                addPrice: parseFloat(selectedOption.dataset.addPrice) || 0,
                quantity: parseInt(selectedOption.dataset.quantity) || 0,
                imgUrl: selectedOption.dataset.imgUrl
            };
            
            // If option has an image, update the main image
            const mainImage = document.getElementById('main-image');
            if (selectedOption.dataset.imgUrl && mainImage) {
                mainImage.src = selectedOption.dataset.imgUrl;
                mainImage.onerror = function() {
                    // Fallback to original image if option image fails to load
                    if (productState.product && productState.product.baseImageUrl) {
                        const imageId = Object.keys(productState.product.baseImageUrl)[0];
                        this.src = productState.product.baseImageUrl[imageId] || '../../images/placeholder.png';
                    }
                };
            }
        } else {
            delete productState.selectedOptions[selectElement.dataset.optionId];
        }
        
        // Update price and available quantity
        updateProductState();
    } catch (error) {
        console.error('Error handling option change:', error);
    }
}

// Update product state (price, quantity limits, etc.)
function updateProductState() {
    try {
        if (!productState.product) return;
        
        // Calculate total price based on base price and selected options
        let totalPrice = productState.basePrice || 0;
        let availableQuantity = productState.product.baseProductQuantity || 0;
        
        // Add price from options
        Object.values(productState.selectedOptions).forEach(option => {
            totalPrice += option.addPrice || 0;
            
            // Track the minimum available quantity from options
            if (option.quantity < availableQuantity) {
                availableQuantity = option.quantity;
            }
        });
        
        // Update quantity input max value
        const quantityInput = document.getElementById('quantity-input');
        if (quantityInput) {
            quantityInput.max = availableQuantity;
            
            // Ensure quantity doesn't exceed available
            if (parseInt(quantityInput.value) > availableQuantity) {
                quantityInput.value = Math.max(1, availableQuantity);
            }
            
            // Update product state quantity
            productState.quantity = parseInt(quantityInput.value) || 1;
        } else {
            productState.quantity = 1;
        }
        
        // Calculate final price with quantity
        totalPrice = totalPrice * productState.quantity;
        productState.totalPrice = totalPrice;
        
        // Update price display
        const priceElement = document.querySelector('.product-info .price');
        if (priceElement) {
            const formattedPrice = new Intl.NumberFormat('vi-VN').format(totalPrice) + ' đ';
            priceElement.textContent = formattedPrice;
        }
        
        // Show availability info
        const stockInfoElement = document.querySelector('.stock-info');
        if (stockInfoElement) {
            const availabilityText = availableQuantity > 10 
                ? 'In stock' 
                : availableQuantity > 0 
                    ? `Only ${availableQuantity} left in stock!` 
                    : 'Out of stock';
            
            stockInfoElement.textContent = availabilityText;
        }
        
        // Update add to cart button state
        const addToCartBtn = document.querySelector('.add-to-cart');
        if (addToCartBtn) {
            addToCartBtn.disabled = availableQuantity <= 0;
        }
    } catch (error) {
        console.error('Error updating product state:', error);
    }
}

// Add to cart function
async function addToCart() {
    // Check if user is logged in (you should replace this with your actual auth check)
    const token = localStorage.getItem('authToken');
    if (!token) {
        alert('Please login to add items to your cart');
        window.location.href = '../io/signin.html';
        return;
    }
    
    try {
        // Prepare request body
        let requestBody = {
            productId: productState.product.productId,
        };
        
        // If no options selected, add base product
        const hasOptions = Object.keys(productState.selectedOptions).length > 0;
        
        if (hasOptions) {
            // Add product with selected options
            requestBody.baseProductQuantity = null;
            requestBody.productOptionValueIdsAndQuantity = [];
            
            // Convert selected options to the required format
            Object.values(productState.selectedOptions).forEach(option => {
                const optionObj = {};
                optionObj[option.optionValueId] = productState.quantity;
                requestBody.productOptionValueIdsAndQuantity.push(optionObj);
            });
        } else {
            // Add base product without options
            requestBody.baseProductQuantity = productState.quantity;
            requestBody.productOptionValueIdsAndQuantity = null;
        }
        
        // Send add to cart request
        const response = await fetch(`${API_BASE_URL}/cart/add`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(requestBody)
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            // Show success message
            showMessage('Product added to cart successfully!', 'success');
            
            // Optionally redirect to cart page
            // setTimeout(() => window.location.href = '../cart/cart.html', 1500);
        } else {
            showMessage('Failed to add product to cart. ' + (data.message || 'Please try again.'), 'error');
        }
    } catch (error) {
        console.error('Error adding product to cart:', error);
        showMessage('An error occurred. Please try again later.', 'error');
    }
}

// Show loading indicator
function showLoading(isLoading) {
    if (isLoading) {
        // Add loading indicator to product section
        const loadingDiv = document.createElement('div');
        loadingDiv.id = 'loading-indicator';
        loadingDiv.innerHTML = '<div class="spinner"></div><p>Loading product...</p>';
        document.querySelector('.product-section').appendChild(loadingDiv);
    } else {
        // Remove loading indicator
        const loadingDiv = document.getElementById('loading-indicator');
        if (loadingDiv) {
            loadingDiv.remove();
        }
    }
}

// Show message toast
function showMessage(message, type) {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<p>${message}</p>`;
    document.body.appendChild(toast);
    
    // Show toast with animation
    setTimeout(() => toast.classList.add('show'), 10);
    
    // Hide toast after delay
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Thay đổi hình ảnh chính khi click vào thumbnail
document.querySelectorAll('.thumbnail').forEach(img => {
    img.addEventListener('click', function() {
        document.getElementById('main-image').src = this.src;
    });
});

// Hiển thị thêm ảnh khi click vào nút "Hiển thị thêm ảnh"
document.getElementById('show-more').addEventListener('click', function() {
    const thumbnailContainer = document.querySelector('.thumbnail-container');
    const hiddenThumbnails = thumbnailContainer.querySelectorAll('.thumbnail[style="display: none;"]');
    
    hiddenThumbnails.forEach(thumbnail => {
        thumbnail.style.display = '';
    });
    
    // Ẩn nút "Hiển thị thêm ảnh" sau khi thêm ảnh
    this.style.display = 'none';
});

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

function performSearch() {
    const query = searchInput.value.trim();
    if (!query) return;

    // Simulate search results (replace with actual search logic)
    const results = Array.from({ length: 15 }, (_, i) => `Product ${i + 1}`);
    searchResultsList.innerHTML = results
        .slice(0, 10)
        .map(result => `<li>${result}</li>`)
        .join("");

    if (results.length > 10) {
        document.getElementById("show-all-results-btn").style.display = "block";
    } else {
        document.getElementById("show-all-results-btn").style.display = "none";
    }
}

function showAllResults() {
    alert("Redirecting to all results page...");
    // Implement redirection logic here
}

// Add to cart functionality
document.querySelector('.add-to-cart').addEventListener('click', function () {
    const productName = document.querySelector('.product-info h1').textContent;
    const productPrice = document.querySelector('.product-info .price').textContent;

    // Simulate adding to cart (replace with actual logic)
    alert(`Added "${productName}" to cart for ${productPrice}.`);

    // Optionally, update cart UI or redirect to cart page
});
