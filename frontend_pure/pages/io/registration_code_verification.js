document.querySelector(".verifyBtn").addEventListener("click", async function () {
    const verificationCode = document.querySelector(".regs-code").value;
    const errorMessageElement = document.querySelector(".error-message");

    // Clear any previous error message
    if (errorMessageElement) {
        errorMessageElement.textContent = "";
    }

    try {
        const response = await fetch("http://127.0.0.1:8080/api/auth/verify-new-acct", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ verificationCode }),
        });

        const data = await response.json();

        if (data.code === 1000 && data.result === "valid") {
            localStorage.removeItem("email");
            localStorage.removeItem("resendCooldown");
            alert("Verification successful! You can now log in.");
            window.location.href = "../io/userinfo.html";
        } else if (data.code === 1013) {
            if (errorMessageElement) {
                errorMessageElement.textContent = "Verification code not correct or expired.";
            }
        } else {
            if (errorMessageElement) {
                errorMessageElement.textContent = data.message || "Verification failed. Please try again.";
            }
        }
    } catch (error) {
        console.error("Error during verification:", error);
        if (errorMessageElement) {
            errorMessageElement.textContent = "An error occurred. Please try again later.";
        }
    }
});

let resendCooldown = localStorage.getItem("resendCooldown");
if (resendCooldown) {
    const remainingTime = Math.max(0, resendCooldown - Date.now());
    if (remainingTime > 0) {
        disableResendButton(remainingTime);
    }
}

document.querySelector(".resendBtn").addEventListener("click", async function () {
    const email = localStorage.getItem("email"); // Retrieve email from localStorage
    if (!email) {
        alert("Email not found. Please sign in / sign up again.");
        return;
    }

    const resendButton = this;
    if (resendButton.disabled) {
        return;
    }

    try {
        const response = await fetch("http://127.0.0.1:8080/api/auth/re-send-verification-code", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ email }),
        });

        const data = await response.json();

        if (data.code === 1000 && data.result === "Resend successfully") {
            alert("A new verification code has been sent to your email.");
            startCooldown();
        } else {
            alert(data.message || "Failed to resend verification code.");
        }
    } catch (error) {
        console.error("Error during resend:", error);
        alert("An error occurred. Please try again later.");
    }
});

function startCooldown() {
    const cooldownTime = 60000; // 1 minute in milliseconds
    const cooldownEnd = Date.now() + cooldownTime;
    localStorage.setItem("resendCooldown", cooldownEnd);
    disableResendButton(cooldownTime);
}

function disableResendButton(duration) {
    const resendButton = document.querySelector(".resendBtn");
    resendButton.disabled = true;
    const interval = setInterval(() => {
        const remainingTime = Math.max(0, localStorage.getItem("resendCooldown") - Date.now());
        if (remainingTime <= 0) {
            clearInterval(interval);
            resendButton.disabled = false;
            resendButton.textContent = "Resend Code";
            localStorage.removeItem("resendCooldown");
        } else {
            resendButton.textContent = `Resend Code (${Math.ceil(remainingTime / 1000)}s)`;
        }
    }, 1000);
}
