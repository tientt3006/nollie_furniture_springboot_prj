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

// Add scroll event listener to toggle header background
window.addEventListener("scroll", () => {
    const header = document.querySelector(".header");
    if (window.scrollY > 50) {
        header.classList.add("scrolled");
    } else {
        header.classList.remove("scrolled");
    }
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


function showMap(location, button) {
    const maps = {
        hanoi: "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3724.856178740812!2d105.7984443154026!3d21.0031179939634!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3135ab8d1f1f1f1f%3A0x0!2zSGFOb2kgQWRkcmVzcw!5e0!3m2!1sen!2s!4v1234567890",
        hcm: "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.1234567890!2d106.7984443154026!3d10.0031179939634!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3135ab8d1f1f1f1f%3A0x0!2zSG9DaU1pbmggQWRkcmVzcw!5e0!3m2!1sen!2s!4v1234567890",
        danang: "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.1234567890!2d108.7984443154026!3d16.0031179939634!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3135ab8d1f1f1f1f%3A0x0!2zRGFOYW5nIEFkZHJlc3M!5e0!3m2!1sen!2s!4v1234567890"
    };
    document.getElementById('map').src = maps[location];

    // Update active button
    const buttons = document.querySelectorAll('.location-btn');
    buttons.forEach(btn => btn.classList.remove('active'));
    button.classList.add('active');
}
