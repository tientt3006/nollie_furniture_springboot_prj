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
