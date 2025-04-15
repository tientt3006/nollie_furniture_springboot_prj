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

collectionMenu.addEventListener("click", (event) => {
    event.preventDefault();
    document.getElementById('sidebar-collection').classList.toggle('show');
    document.getElementById('sidebar-fur').classList.remove('show');
    document.getElementById('sidebar-care').classList.remove('show');
});
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
