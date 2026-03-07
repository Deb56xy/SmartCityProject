const BASE_URL = "http://localhost:8080";

function forgotPassword() {
  const email = document.getElementById("email").value;
  const msg = document.getElementById("msg");

  msg.innerText = "";

  if (!email) {
    msg.innerText = "Please enter your registered email address.";
    return;
  }

  fetch(`${BASE_URL}/api/auth/password/forgot`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ email })
  })
    .then(res => {
      if (!res.ok) throw new Error("Email not found");
      return res.json();
    })
    .then(data => {
      localStorage.setItem("resetUserId", data.userId);
      window.location.href = "reset-password.html";
    })
    .catch(err => {
      msg.innerText = err.message;
    });
}

function resetPassword() {
  const otp = document.getElementById("otp").value;
  const newPassword = document.getElementById("newPassword").value;
  const msg = document.getElementById("msg");

  const userId = localStorage.getItem("resetUserId");

  msg.innerText = "";

  if (!otp || !newPassword) {
    msg.innerText = "Please enter OTP and new password.";
    return;
  }

  fetch("http://localhost:8080/api/auth/password/reset", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      userId: userId,
      otp: otp,
      newPassword: newPassword
    })
  })
    .then(res => {
      if (!res.ok) throw new Error("Invalid or expired OTP");
      return res.text();
    })
    .then(() => {
      alert("Password reset successful. Please login.");
      localStorage.removeItem("resetUserId");
      window.location.href = "login.html";
    })
    .catch(err => {
      msg.innerText = err.message;
    });
}
