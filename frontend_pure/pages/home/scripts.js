const productList = document.querySelector(".product-list");
const productList2 = document.querySelector(".product-list2");
const prevBtn = document.querySelector(".prev-btn");
const nextBtn = document.querySelector(".next-btn");
const prevBtn2 = document.querySelector(".prev-btn2");
const nextBtn2 = document.querySelector(".next-btn2");

prevBtn.addEventListener("click", () => {
    productList.scrollBy({ left: -370, behavior: "smooth" });
});

nextBtn.addEventListener("click", () => {
    productList.scrollBy({ left: 370, behavior: "smooth" });
});

prevBtn2.addEventListener("click", () => {
    productList2.scrollBy({ left: -370, behavior: "smooth" });
});

nextBtn2.addEventListener("click", () => {
    productList2.scrollBy({ left: 370, behavior: "smooth" });
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

window.addEventListener("scroll", () => {
    if (window.scrollY > 0) {
        header.classList.add("scrolled");
        navLinks.forEach(link => link.style.color = "black");
        menuIcon.src = "../../images/menu.png";
        logo.src = "../../images/logoonly.png";
        logoName.style.color = "black";
        navIcons.forEach((icon, index) => {
            icon.src = `../../images/${["search", "shopping-cart", "user"][index]}.png`;
        });
    } else {
        header.classList.remove("scrolled");
        navLinks.forEach(link => link.style.color = "white");
        menuIcon.src = "../../images/menu_w.png";
        logoName.style.color = "white";
        logo.src = "../../images/logoonly_w.png";
        navIcons.forEach((icon, index) => {
            icon.src = `../../images/${["search_w", "shopping-cart1_w", "user_w"][index]}.png`;
        });
    }
});

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

// Update dropdown menu based on login state
function updateDropdownMenu() {
    const token = localStorage.getItem("authToken");
    if (token) {
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

// Logout functionality
logoutItem.addEventListener("click", async (event) => {
    event.preventDefault();
    const token = localStorage.getItem("authToken");
    if (token) {
        try {
            const response = await fetch("http://127.0.0.1:8080/api/auth/logout", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ token }),
            });
            const data = await response.json();
            if (data.code === 1000) {
                localStorage.removeItem("authToken");
                updateDropdownMenu();
                window.location.href = "../io/signin.html";
            } else {
                console.error("Failed to logout: Invalid response code");
            }
        } catch (error) {
            console.error("Failed to logout:", error);
        }
    }
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


// Function to fetch top-selling products
async function fetchTopSellingProducts() {
    try {
        const response = await fetch('http://localhost:8080/api/product/top-sell/8');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const data = await response.json();
        return data.result || [];
    } catch (error) {
        console.error('Error fetching top-selling products:', error);
        return [];
    }
}

// Function to format price in VND
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price) + ' đ';
}

// Function to generate color circles HTML
function generateColorOptions(productOptions) {
    // Find color options
    const colorOption = productOptions.find(option => 
        option.optionName.toLowerCase().includes('color'));
    
    if (!colorOption) return '';
    
    let colorHTML = '<div class="color-list">';
    
    // Get up to 5 color values
    const colorValues = colorOption.productOptionValueResponseList.slice(0, 5);
    
    colorValues.forEach(color => {
        // Try to extract color from name or use a default
        let colorName = color.optionValueName.toLowerCase();
        let backgroundColor = '#c4c4c4'; // Default color
        
        if (colorName.includes('red')) backgroundColor = '#c13a3a';
        else if (colorName.includes('blue')) backgroundColor = '#3a57c1';
        else if (colorName.includes('green')) backgroundColor = '#3ac144';
        else if (colorName.includes('black')) backgroundColor = '#262626';
        else if (colorName.includes('gray')) backgroundColor = '#5a5a5a';
        else if (colorName.includes('brown')) backgroundColor = '#4d380e';
        
        colorHTML += `<span class="color-circle" style="background-color: ${backgroundColor};"></span>`;
    });
    
    colorHTML += '</div>';
    return colorHTML;
}

// Function to list options
function listProductOptions(productOptions) {
    if (!productOptions || productOptions.length === 0) return '';
    
    let optionsHTML = '<div class="product-options">';
    
    productOptions.forEach(option => {
        const optionName = option.optionName;
        const values = option.productOptionValueResponseList
            .map(value => value.optionValueName)
            .join(', ');
        
        optionsHTML += `<p class="option-text">${optionName}: ${values}</p>`;
    });
    
    optionsHTML += '</div>';
    return optionsHTML;
}

// Function to render top-selling products
async function renderTopSellingProducts() {
    const productListElement = document.getElementById('top-selling-products');
    
    // Show loading indicator
    productListElement.innerHTML = '<div class="loading-indicator">Loading products...</div>';
    
    const products = await fetchTopSellingProducts();
    
    if (products.length === 0) {
        productListElement.innerHTML = '<p>No products found</p>';
        return;
    }
    
    let productsHTML = '';
    
    products.forEach(product => {
        // Get product image URL from baseImageUrl
        const imageId = Object.keys(product.baseImageUrl)[0];
        const imageUrl = product.baseImageUrl[imageId];
        
        // Generate product card HTML
        productsHTML += `
        <div class="product-card">
            <a href="../product/product.html?id=${product.productId}" style="text-decoration: none; color: black;">
                <img src="${imageUrl}" alt="${product.name}">
                <div class="product-info">
                    <h3>${product.name}</h3>
                    <p>${product.category.name}</p>
                    ${listProductOptions(product.productOptionResponseList)}
                    ${generateColorOptions(product.productOptionResponseList)}
                    <p class="price">${formatPrice(product.basePrice)}</p>
                </div>
            </a>
        </div>`;
    });
    
    productListElement.innerHTML = productsHTML;
}

// Function to fetch categories
async function fetchCategories() {
    try {
        const response = await fetch('http://localhost:8080/api/category');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const data = await response.json();
        return data.result || [];
    } catch (error) {
        console.error('Error fetching categories:', error);
        return [];
    }
}

// Function to get category image URL
function getCategoryImageUrl(categoryName) {
    // Map of category names to image filenames
    const categoryImages = {
        'sofa': 'cate1.png',
        'chair': 'cate2.png',
        'table': 'cate3.png',
        'storage': 'cate4.png',
        'lamp': 'cate5.png',
        'accessories': 'cate6.png',
        'rug': 'cate7.png',
        'outdoor': 'cate8.png',
        'bed': 'cate9.png'
    };
    
    // Find matching image name (case insensitive)
    const lowerCaseName = categoryName.toLowerCase();
    for (const [key, value] of Object.entries(categoryImages)) {
        if (lowerCaseName.includes(key)) {
            return `../../images/${value}`;
        }
    }
    
    // Default image if no match is found
    return '../../images/placeholder.png';
}

// Function to render categories
async function renderCategories() {
    const categoryListElement = document.getElementById('category-list');
    
    // Show loading indicator
    categoryListElement.innerHTML = '<div class="loading-indicator">Loading categories...</div>';
    
    const categories = await fetchCategories();
    
    if (categories.length === 0) {
        categoryListElement.innerHTML = '<p>No categories found</p>';
        return;
    }
    
    let categoriesHTML = '';
    
    categories.forEach(category => {
        const imageUrl = category.imgUrl || getCategoryImageUrl(category.name);
        
        // Generate category card HTML
        categoriesHTML += `
        <div class="product-card" style="box-shadow: none;">
            <a href="../all_product/products.html?category=${category.id}" style="text-decoration: none; color: black;">
                <img src="${imageUrl}" alt="${category.name}" style="border-radius: 10px;">
                <div class="product-info" style="background-color: rgb(255, 255, 255);">
                    <h3 style="text-align: center; font-size: 20px; background-color: rgb(255, 255, 255);">${category.name}</h3>
                </div>
            </a>
        </div>`;
    });
    
    categoryListElement.innerHTML = categoriesHTML;
}

// Load products and categories on page load
document.addEventListener('DOMContentLoaded', () => {
    renderTopSellingProducts();
    renderCategories();
});
