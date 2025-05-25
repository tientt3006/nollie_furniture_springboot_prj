// Function to handle the hide or show password functionality
document.querySelectorAll('.toggle-password').forEach(button => {
    button.addEventListener('click', function () {
        const passwordInput = this.previousElementSibling; // Lấy input liền trước nút
        const toggleIcon = this.querySelector('.toggle-icon'); // Lấy icon trong nút
        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            toggleIcon.src = '../../images/eyeclose.png'; // Đổi icon thành "ẩn"
        } else {
            passwordInput.type = 'password';
            toggleIcon.src = '../../images/eyeopen.png'; // Đổi icon thành "hiện"
        }
    });
});

// Function to check token validity and handle redirection
async function checkTokenAndRedirect() {
    const token = localStorage.getItem("authToken");
    const previousPage = document.referrer; // Get the previous page URL

    if (!token) {
        return; // No token, stay on the login page
    }

    try {
        // Introspect the token
        const introspectResponse = await fetch("http://127.0.0.1:8080/api/auth/introspect", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ token }),
        });

        const introspectData = await introspectResponse.json();

        if (introspectData.code === 1000 && introspectData.result.valid) {
            // Token is valid, redirect to the previous page or user info page
            window.location.href = previousPage || "../io/userinfo.html";
            return;
        }

        // If token is invalid, try refreshing it
        const refreshResponse = await fetch("http://127.0.0.1:8080/api/auth/refresh", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ token }),
        });

        const refreshData = await refreshResponse.json();

        if (refreshData.code === 1000 && refreshData.result.authenticated) {
            // Refresh successful, save new token and redirect
            localStorage.setItem("authToken", refreshData.result.token);
            window.location.href = previousPage || "../io/userinfo.html";
        } else {
            // Refresh failed, stay on login page
            localStorage.removeItem("authToken");
        }
    } catch (error) {
        console.error("Error during token validation:", error);
        localStorage.removeItem("authToken"); // Clear invalid token
    }
}

// Call the function on page load
checkTokenAndRedirect();

async function handleLogin(event) {
    event.preventDefault(); // Prevent form submission

    const email = document.querySelector("input[type='email']").value;
    const password = document.querySelector(".password-input").value;
    const errorMessageElement = document.querySelector(".error-message"); // Select error message container
    const previousPage = document.referrer; // Get the previous page URL

    // Clear any previous error message
    if (errorMessageElement) {
        errorMessageElement.textContent = "";
    }

    try {
        const response = await fetch("http://127.0.0.1:8080/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ email, password }),
        });

        const data = await response.json();

        if (data.code === 1000 && data.result.authenticated) {
            localStorage.setItem("authToken", data.result.token);
            console.log("Auth Token:", data.result.token); // Log the token to the console
            window.location.href = previousPage || "../io/userinfo.html"; // Redirect to the previous page or user info page
        } else if (data.code === 1014 && data.message === "Account not active") { 
            if (errorMessageElement) {
                errorMessageElement.textContent = "Account not acctivated.";
            }
            localStorage.setItem("email", email);
            window.location.href = "../io/registration_code_verification.html";
        } else {
            if (errorMessageElement) {
                errorMessageElement.textContent = "Wrong email or password.";
            }
        }
    } catch (error) {
        console.error("Error during login:", error);
        if (errorMessageElement) {
            errorMessageElement.textContent = "An error occurred. Please try again later.";
        }
    }
}

// Function to send authenticated requests
async function sendAuthenticatedRequest(url, options = {}) {
    const token = localStorage.getItem("authToken");

    if (!token) {
        window.location.href = "../io/signin.html";
        return;
    }

    options.headers = {
        ...(options.headers || {}),
        Authorization: `Bearer ${token}`,
    };

    let response = await fetch(url, options);

    if (response.status === 401) { // Token might be expired
        const refreshResponse = await fetch("http://127.0.0.1:8080/api/auth/refresh", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ token }),
        });

        const refreshData = await refreshResponse.json();

        if (refreshData.code === 1000 && refreshData.result.authenticated) {
            localStorage.setItem("authToken", refreshData.result.token);
            options.headers.Authorization = `Bearer ${refreshData.result.token}`;
            response = await fetch(url, options); // Retry the original request
        } else {
            alert("Session expired. Please log in again.");
            localStorage.removeItem("authToken");
            window.location.href = "../io/signin.html";
            return;
        }
    }

    return response;
}

// Attach event listener to the sign-in button
document.querySelector(".signinBtn").addEventListener("click", handleLogin);