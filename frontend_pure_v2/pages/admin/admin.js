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






