
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





