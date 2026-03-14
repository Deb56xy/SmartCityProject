const BASE_URL = "http://localhost:8080";

function login() {
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    fetch(`${BASE_URL}/api/auth/login`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({email, password})
    })
    .then(res => {
        if(!res.ok) throw new Error("invalid credentials");
        return res.json();
    })
    .then(data => {
        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role);

        if (data.role === "CITIZEN") {
            window.location.href = "citizen-dashboard.html";
        } else if (data.role === "AUTHORITY") {
            window.location.href = "authority-dashboard.html";
        } else if (data.role === "ADMIN") {
          window.location.href = "admin-dashboard.html";
        }
    })
    .catch(err => {
        document.getElementById("msg").innerText = err.message;
    })
}

function logout() {
    localStorage.clear();
    window.location.href = "index.html";
}

function signup() {
  const msg = document.getElementById("msg");
  msg.innerText = "";

  const data = {
    name: document.getElementById("name").value,
    email: document.getElementById("email").value,
    phoneNumber: document.getElementById("phoneNumber").value,
    state: document.getElementById("state").value,
    district: document.getElementById("district").value,
    location: document.getElementById("location").value,
    pinCode: document.getElementById("pinCode").value,
    password: document.getElementById("password").value
  };

  for (let key in data) {
    if (!data[key]) {
      msg.innerText = "Please fill all required fields.";
      return;
    }
  }

  fetch(`${BASE_URL}/api/auth/signup`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  })
    .then(res => {
      if (!res.ok) {
        return res.text().then(t => {
          throw new Error(t || "Signup failed");
        });
      }
      return res.json();
    })
    .then(res => {
      localStorage.setItem("userId", res.userId);
      localStorage.setItem("email", res.email);
      window.location.href = "verify-email.html";
    })
    .catch(err => {
      msg.innerText = err.message;
    });
}
