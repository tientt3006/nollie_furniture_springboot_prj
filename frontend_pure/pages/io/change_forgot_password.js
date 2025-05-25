document.addEventListener('DOMContentLoaded', function() {
    // Get DOM elements
    const form = document.getElementById('forgot-password-form');
    const emailInput = document.getElementById('email');
    const verificationCodeInput = document.getElementById('verification-code');
    const newPasswordInput = document.getElementById('new-password');
    const confirmPasswordInput = document.getElementById('confirm-password');
    const sendCodeBtn = document.getElementById('send-code-btn');
    const statusMessage = document.getElementById('status-message');
    const togglePasswordBtns = document.querySelectorAll('.toggle-password');

    // Initialize cooldown variables
    let cooldownTime = 60; // seconds
    let cooldownActive = false;
    let cooldownInterval;

    // Toggle password visibility
    togglePasswordBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.previousElementSibling;
            const icon = this.querySelector('img');
            
            if (input.type === 'password') {
                input.type = 'text';
                icon.src = '../../images/eyeclose.png';
                icon.alt = 'Hide Password';
            } else {
                input.type = 'password';
                icon.src = '../../images/eyeopen.png';
                icon.alt = 'Show Password';
            }
        });
    });

    // Function to validate email format
    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    // Function to display status message
    function showStatusMessage(message, type) {
        statusMessage.textContent = message;
        statusMessage.style.display = 'block';
        statusMessage.className = `alert alert-${type}`;
        
        // Auto hide after 5 seconds for success messages
        if (type === 'success') {
            setTimeout(() => {
                statusMessage.style.display = 'none';
            }, 5000);
        }
    }

    // Function to handle cooldown timer for the send code button
    function startCooldown() {
        cooldownActive = true;
        sendCodeBtn.disabled = true;
        sendCodeBtn.textContent = `Resend (${cooldownTime}s)`;
        
        cooldownInterval = setInterval(() => {
            cooldownTime--;
            sendCodeBtn.textContent = `Resend (${cooldownTime}s)`;
            
            if (cooldownTime <= 0) {
                clearInterval(cooldownInterval);
                cooldownActive = false;
                cooldownTime = 60;
                sendCodeBtn.disabled = false;
                sendCodeBtn.textContent = 'Send Code';
            }
        }, 1000);
    }

    // Event listener for Send Code button
    sendCodeBtn.addEventListener('click', async function() {
        // Validate email
        const email = emailInput.value.trim();
        
        if (!email) {
            showStatusMessage('Please enter your email address.', 'danger');
            return;
        }
        
        if (!isValidEmail(email)) {
            showStatusMessage('Please enter a valid email address.', 'danger');
            return;
        }
        
        try {
            const response = await fetch('http://localhost:8080/api/auth/re-send-verification-code', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email: email })
            });
            
            const data = await response.json();
            
            if (data.code === 1000) {
                showStatusMessage('Verification code sent successfully. Please check your email.', 'success');
                startCooldown();
            } else {
                showStatusMessage(data.message || 'Failed to send verification code. Please try again.', 'danger');
            }
        } catch (error) {
            console.error('Error sending verification code:', error);
            showStatusMessage('Network error. Please try again later.', 'danger');
        }
    });

    // Event listener for form submission
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        // Get form values
        const verificationCode = verificationCodeInput.value.trim();
        const newPassword = newPasswordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        
        // Validate inputs
        if (!verificationCode) {
            showStatusMessage('Please enter the verification code.', 'danger');
            return;
        }
        
        if (!newPassword) {
            showStatusMessage('Please enter a new password.', 'danger');
            return;
        }
        
        if (newPassword !== confirmPassword) {
            showStatusMessage('Passwords do not match.', 'danger');
            return;
        }
        
        if (newPassword.length < 4) {
            showStatusMessage('Password must be at least 4 characters long.', 'danger');
            return;
        }
        
        try {
            const response = await fetch('http://localhost:8080/api/auth/change-forgot-password', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    forgotPasswordRecoveryCode: verificationCode,
                    newPassword: newPassword
                })
            });
            
            const data = await response.json();
            
            if (data.code === 1000) {
                showStatusMessage('Password changed successfully. Redirecting to login page...', 'success');
                // Reset the form
                form.reset();
                
                // Redirect to login page after 3 seconds
                setTimeout(() => {
                    window.location.href = 'signin.html';
                }, 3000);
            } else {
                showStatusMessage(data.message || 'Failed to change password. Please try again.', 'danger');
            }
        } catch (error) {
            console.error('Error changing password:', error);
            showStatusMessage('Network error. Please try again later.', 'danger');
        }
    });
});
