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

// Function to handle the "Edit" button click event
async function toggleEdit() {
    const inputs = document.querySelectorAll("input");
    const editButton = document.querySelector(".buttons button:first-child");
    const cancelButton = document.getElementById("cancel-edit");
    const sendButton = document.getElementById("send-code");
    const newEmailGroup = document.querySelector(".new-email-group");
    const newEmailInput = document.getElementById("new-email");
    const emailCodeInput = document.getElementById("email-code");
    const emailInput = document.querySelector("input[type='email']");
    const errorMessage = document.getElementById("error-message");

    if (editButton.textContent === "Edit") {
        // Chuyển sang chế độ chỉnh sửa
        inputs.forEach(input => input.disabled = false);
        newEmailInput.disabled = false;
        emailCodeInput.disabled = false;
        sendButton.disabled = false;
        newEmailGroup.style.display = "flex"; // Hiển thị nhóm new-email-group
        editButton.textContent = "Save";
        cancelButton.style.display = "inline-block"; // Hiển thị nút Cancel Edit
    } else {
        // Save user information
        const fullName = document.querySelector(".user-fullname").value;
        const email = emailInput.value;
        const phone = document.querySelector(".user-phone").value;
        const address = document.querySelector(".user-address").value;

        const token = localStorage.getItem("authToken");
        if (!token) {
            const errorMessage = document.getElementById("error-message");
            if (errorMessage) {
                errorMessage.textContent = "You need to log in.";
            }
            window.location.href = "../io/signin.html";
            return;
        }

        try {
            const response = await fetch("http://127.0.0.1:8080/api/user/update-info", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify({ fullName, email, phone, address }),
            });

            const data = await response.json();

            if (data.code === 1000) {
                // Update successful, disable inputs
                inputs.forEach(input => input.disabled = true);
                newEmailInput.disabled = true;
                emailCodeInput.disabled = true;
                sendButton.disabled = true;
                newEmailGroup.style.display = "none";
                editButton.textContent = "Edit";
                cancelButton.style.display = "none"; // Ẩn nút Cancel Edit sau khi lưu
                if (errorMessage) errorMessage.textContent = ""; // Clear any previous error
            } else if (data.code === 1011) {
                // Duplicate email or phone
                if (!errorMessage) {
                    const errorDiv = document.createElement("div");
                    errorDiv.id = "error-message";
                    errorDiv.style.color = "red";
                    errorDiv.textContent = data.message;
                    document.querySelector(".userinfo-main").appendChild(errorDiv);
                } else {
                    errorMessage.textContent = data.message;
                }
            } else if (data.code === 1006) {
                // Token expired
                window.location.href = "../io/signin.html";
            } else if (data.code === 1010) {
                // Email is blank
                if (!errorMessage) {
                    const errorDiv = document.createElement("div");
                    errorDiv.id = "error-message";
                    errorDiv.style.color = "red";
                    errorDiv.textContent = data.message;
                    document.querySelector(".userinfo-main").appendChild(errorDiv);
                } else {
                    errorMessage.textContent = data.message;
                }
            } else if (data.code === 1009) {
                // Name is blank
                if (!errorMessage) {
                    const errorDiv = document.createElement("div");
                    errorDiv.id = "error-message";
                    errorDiv.style.color = "red";
                    errorDiv.textContent = data.message;
                    document.querySelector(".userinfo-main").appendChild(errorDiv);
                } else {
                    errorMessage.textContent = data.message;
                }
            } else {
                console.error("Unexpected response:", data);
            }
        } catch (error) {
            console.error("Error updating user info:", error);
        }

        // Handle password change
        const oldPassword = document.querySelector(".old-password").value;
        const newPassword = document.querySelector(".new-password").value;
        const reNewPassword = document.querySelector(".renew-password").value;
        const passwordErrorMessage = document.getElementById("password-error-message");

        if(oldPassword || newPassword || reNewPassword){
            if (!oldPassword || !newPassword || !reNewPassword) {
                if (passwordErrorMessage) {
                    passwordErrorMessage.textContent = "Please fill in all password fields.";
                }
                return;
            }
        
            if (newPassword !== reNewPassword) {
                if (passwordErrorMessage) {
                    passwordErrorMessage.textContent = "New password and re-entered password do not match.";
                }
                return;
            }
    
            try {
                const response = await fetch("http://127.0.0.1:8080/api/user/change-password", {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${token}`,
                    },
                    body: JSON.stringify({ oldPassword, newPassword }),
                });
        
                const data = await response.json();
        
                if (data.code === 1000) {
                    if (passwordErrorMessage) {
                        passwordErrorMessage.style.color = "green";
                        passwordErrorMessage.textContent = "Password changed successfully.";
                    }
                    document.querySelector(".old-password").value = "";
                    document.querySelector(".new-password").value = "";
                    document.querySelector(".renew-password").value = "";
                    return;
                } else if (data.code === 1012) {
                    // Old password not correct
                    if (!passwordErrorMessage) {
                        const errorDiv = document.createElement("div");
                        errorDiv.id = "password-error-message";
                        errorDiv.style.color = "red";
                        errorDiv.textContent = data.message;
                        document.querySelector(".userinfo-main").appendChild(errorDiv);
                    } else {
                        passwordErrorMessage.textContent = data.message;
                    }
                } else {
                    if (passwordErrorMessage) {
                        passwordErrorMessage.textContent = "Failed to change password: " + (data.message || "Unknown error");
                    }
                    return;
                }
            } catch (error) {
                console.error("Error changing password:", error);
                if (passwordErrorMessage) {
                    passwordErrorMessage.textContent = "An error occurred. Please try again later.";
                }
            }
        }
        
    }
}

// Function to handle the "Cancel Edit" button click event
function cancelEdit() {
    window.location.reload(); // Reload the page to discard changes and show original data
}

//Xử lý nút Send
document.getElementById("send-code").addEventListener("click", function () {
    const newEmail = document.getElementById("new-email").value;
    if (!newEmail) {
        const errorMessage = document.getElementById("error-message");
        if (errorMessage) {
            errorMessage.textContent = "Please enter a new email to use send-code.";
        }
        return;
    }
    console.log("Sending code to old email:");
    const emailMessage = document.getElementById("error-message");
    if (emailMessage) {
        emailMessage.style.color = "green";
        emailMessage.textContent = "A code has been sent to your old email.";
    }
});


// Function to fetch and display user information
async function fetchUserInfo() {
    const token = localStorage.getItem("authToken");

    if (!token) {
        const errorMessage = document.getElementById("error-message");
        if (errorMessage) {
            errorMessage.textContent = "You need to log in.";
        }
        window.location.href = "../io/signin.html";
        return;
    }

    try {
        const response = await fetch("http://127.0.0.1:8080/api/user/my-info", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
            },
        });

        const data = await response.json();

        if (data.code === 1000) {
            const userInfo = data.result;
            document.querySelector(".user-fullname").value = userInfo.fullName;
            document.querySelector(".user-email-header").textContent = userInfo.email;
            document.querySelector(".user-email").value = userInfo.email;
            document.querySelector(".user-phone").value = userInfo.phone;
            document.querySelector(".user-address").value = userInfo.address;
        } else {
            const errorMessage = document.getElementById("error-message");
            if (errorMessage) {
                errorMessage.textContent = "Failed to fetch user info: " + (data.message || "Unknown error");
            }
            return;
        }
        // Try to refresh the token first
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
            // window.location.href = "../io/userinfo.html";
            return;
        } else {
            // Refresh failed, stay on login page
            const errorMessage = document.getElementById("error-message");
            if (errorMessage) {
                errorMessage.textContent = "Session expired. Please log in again.";
            }
            localStorage.removeItem("authToken");
            window.location.href = "../io/signin.html";
            return;
        }
    } catch (error) {
        console.error("Error fetching user info:", error);
        const errorMessage = document.getElementById("error-message");
        if (errorMessage) {
            errorMessage.textContent = "An error occurred. Please try again later.";
        }
        localStorage.removeItem("authToken");
    }
}

// Call the function to fetch user info on page load
window.addEventListener("DOMContentLoaded", fetchUserInfo);