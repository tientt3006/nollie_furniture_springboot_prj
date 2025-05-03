
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
});// Function to handle the "Edit" button click event
function toggleEdit() {
    const inputs = document.querySelectorAll("input");
    const editButton = document.querySelector(".buttons button:first-child");
    const sendButton = document.getElementById("send-code");
    const newEmailGroup = document.querySelector(".new-email-group");
    const newEmailInput = document.getElementById("new-email");
    const emailCodeInput = document.getElementById("email-code");
    const emailInput = document.querySelector("input[type='email']");

    if (editButton.textContent === "Edit") {
        // Chuyển sang chế độ chỉnh sửa
        inputs.forEach(input => input.disabled = false);
        newEmailInput.disabled = false;
        emailCodeInput.disabled = false;
        sendButton.disabled = false;
        newEmailGroup.style.display = "flex"; // Hiển thị nhóm new-email-group
        editButton.textContent = "Save";
    } else {
        // Lưu thông tin
        const newEmail = newEmailInput.value;
        const currentEmail = emailInput.value;

        if (newEmail && newEmail !== currentEmail) {
            const emailCode = emailCodeInput.value;
            if (!emailCode) {
                alert("Please enter the code sent to your current email.");
                return;
            }
            // Gửi API để xác nhận email mới
            console.log("Sending API request to update email...");
        }

        // Gửi API để lưu thông tin khác
        console.log("Saving user information...");
        inputs.forEach(input => input.disabled = true);
        newEmailInput.disabled = true;
        emailCodeInput.disabled = true;
        sendButton.disabled = true;
        newEmailGroup.style.display = "none"; // Ẩn nhóm new-email-group
        editButton.textContent = "Edit";
    }
}

// Xử lý nút Send
// document.getElementById("send-code").addEventListener("click", function () {
//     const newEmail = document.getElementById("new-email").value;
//     if (!newEmail) {
//         alert("Please enter a new email to send the code.");
//         return;
//     }
//     console.log("Sending code to new email:", newEmail);
//     alert("A code has been sent to your new email.");
// });


// Function to fetch and display user information
async function fetchUserInfo() {
    const token = localStorage.getItem("authToken");

    if (!token) {
        alert("You need to log in.");
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
            document.querySelector(".user-email").value = userInfo.email;
            document.querySelector(".user-phone").value = userInfo.phone;
            document.querySelector(".user-address").value = userInfo.address;
        } else {
            alert("Failed to fetch user info: " + (data.message || "Unknown error"));
        }
    } catch (error) {
        console.error("Error fetching user info:", error);
        alert("An error occurred. Please try again later.");
    }
}

// Call the function to fetch user info on page load
window.addEventListener("DOMContentLoaded", fetchUserInfo);