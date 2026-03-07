const BASE_URL = "http://localhost:8080";
const email = localStorage.getItem("email");

function verifyOtp() {
  const otp = document.getElementById("otp").value;
  const msg = document.getElementById("msg");
  const userId = localStorage.getItem("userId");

  msg.innerText = "";

  if (!otp || otp.length !== 6) {
    msg.innerText = "Please enter a valid 6-digit OTP.";
    return;
  }

  fetch(`${BASE_URL}/api/auth/verify-email-otp`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      userId: userId,
      otp: otp
    })
  })
    .then(res => {
      if (!res.ok) {
        return res.text().then(t => {
          throw new Error(t || "Invalid or expired OTP");
        });
      }
      return res.text();
    })
    .then(() => {
      alert("Email verified successfully. Please login.");
      localStorage.removeItem("userId");
      window.location.href = "login.html";
    })
    .catch(err => {
      msg.innerText = err.message;
    });
}

function resendOtp() {
  const msg = document.getElementById("msg");
  msg.innerText = "";

  fetch(`${BASE_URL}/api/auth/resend-email-otp-by-email`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      email: email
    })
  })
    .then(res => {
      if (!res.ok) throw new Error("Unable to resend OTP");
      alert("A new OTP has been sent to your email.");
    })
    .catch(err => {
      msg.innerText = err.message;
    });
}

function sendOtp() {
  const email = document.getElementById("email").value;
  const msg = document.getElementById("msg");
  const otpSection = document.getElementById("otpSection");

  msg.innerText = "";

  if (!email) {
    msg.innerText = "Please enter your registered email address.";
    return;
  }

  fetch(`${BASE_URL}/api/auth/resend-email-otp-by-email`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email })
  })
    .then(res => {
      if (!res.ok) {
        return res.text().then(t => {
          throw new Error(t || "Unable to send OTP");
        });
      }
      return res.json();
    })
    .then(data => {
      // backend returns userId
      localStorage.setItem("userId", data.userId);
      otpSection.style.display = "block";
      msg.innerText = "OTP sent to your email.";
      msg.style.color = "green";
    })
    .catch(err => {
      msg.innerText = err.message;
      msg.style.color = "red";
    });
}