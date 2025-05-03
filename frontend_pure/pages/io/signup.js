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

document.getElementById('signup-form').addEventListener('submit', async function (event) {
    event.preventDefault(); // Prevent form submission

    const fullName = document.getElementById('full-name').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();
    const reenterPassword = document.getElementById('reenter-password').value.trim();
    const messageContainer = document.getElementById('message-container');

    // Clear previous messages
    messageContainer.textContent = '';
    messageContainer.className = 'message-container';

    if (password !== reenterPassword) {
        messageContainer.textContent = 'Passwords do not match.';
        messageContainer.classList.add('error');
        return;
    }

    try {
        const response = await fetch('http://127.0.0.1:8080/api/user', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullName, email, password })
        });

        const data = await response.json();

        if (data.code === 1002) {
            messageContainer.textContent = 'User already exists.';
            messageContainer.classList.add('error');
        } else if (data.code === 1000) {
            window.location.href = '../io/userinfo.html'; // Redirect on success
        } else if (data.code === 1004) {
            messageContainer.textContent = 'Password must be at least 4 characters and max of 50 characters.';
            messageContainer.classList.add('error');
        } else if (data.code === 1009) {
            messageContainer.textContent = 'Your name should not be blank.';
            messageContainer.classList.add('error');
        } else {
            messageContainer.textContent = 'An unexpected error occurred.';
            messageContainer.classList.add('error');
        }
    } catch (error) {
        messageContainer.textContent = 'Failed to connect to the server.';
        messageContainer.classList.add('error');
    }
});