const BASE_URL = "http://localhost:8080";
const token = localStorage.getItem("token");
let ongoingData = [];
let ongoingPage = 1;
let resolvedData = [];
let resolvedPage = 1;
const PAGE_SIZE = 3;
let deleteComplaintId = null;
let reopenComplaintId = null;

document.addEventListener("DOMContentLoaded", () => {
    loadUserProfile();
    loadComplaintTypes();
    loadMyComplaints();
});

/* TAB SWITCH */
function switchTab(tab) {
    document.getElementById("ongoingTab").classList.remove("active");
    document.getElementById("resolvedTab").classList.remove("active");
    document.querySelectorAll(".tab").forEach(b => b.classList.remove("active"));

    if (tab === "ongoing") {
        document.getElementById("ongoingTab").classList.add("active");
        document.querySelectorAll(".tab")[0].classList.add("active");
    } else {
        document.getElementById("resolvedTab").classList.add("active");
        document.querySelectorAll(".tab")[1].classList.add("active");
    }
}

/* LOAD COMPLAINTS */
function loadMyComplaints() {
    fetch(`${BASE_URL}/api/complaints`, {
        headers: { Authorization: "Bearer " + token }
    })
        .then(r => {
            if (r.status === 401) {
                window.location.href = 'login.html';
                return;
            }
            return r.json();
        })
        .then(data => {
            if (!data) return;
            ongoingData = data.ongoingComplaints || [];
            ongoingPage = 1;
            renderOngoing();
            resolvedData = data.resolvedComplaints || [];
            resolvedPage = 1;
            renderResolved();
        });
}

/* ONGOING TABLE */
function renderOngoing() {
    const tbody = document.getElementById("ongoingComplaints");
    tbody.innerHTML = "";

    const start = (ongoingPage - 1) * PAGE_SIZE;
    const end = start + PAGE_SIZE;
    const pageItems = ongoingData.slice(start, end);

    pageItems.forEach(c => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
      <td>${c.title}</td>
      <td>${c.type}</td>
      <td><span class="status ${c.status}">${c.status}</span></td>
      <td>${c.pendingWith}</td>
      <td>
        <button class="btn-view" onclick='openModal(${JSON.stringify(c)})'>View</button>
        <button class="btn-delete" onclick="openDeleteModal(${c.id})">Delete</button>
      </td>
    `;
        tbody.appendChild(tr);
    });
    updatePaginationControls();
}

function updatePaginationControls() {
    const totalPages = Math.ceil(ongoingData.length / PAGE_SIZE);

    const pageContainer = document.getElementById("pageNumbers");
    pageContainer.innerHTML = "";

    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement("button");
        btn.innerText = i;

        if (i === ongoingPage) {
            btn.classList.add("active");
        }

        btn.onclick = () => {
            ongoingPage = i;
            renderOngoing();
        };

        pageContainer.appendChild(btn);
    }

    document.getElementById("prevBtn").disabled = ongoingPage === 1;
    document.getElementById("nextBtn").disabled = ongoingPage === totalPages;
}

function prevPage() {
    if (ongoingPage > 1) {
        ongoingPage--;
        renderOngoing();
    }
}

function nextPage() {
    const totalPages = Math.ceil(ongoingData.length / PAGE_SIZE);
    if (ongoingPage < totalPages) {
        ongoingPage++;
        renderOngoing();
    }
}

/* RESOLVED TABLE */
function renderResolved(list) {
    const tbody = document.getElementById("resolvedComplaints");
    tbody.innerHTML = "";

    const start = (resolvedPage - 1) * PAGE_SIZE;
    const end = start + PAGE_SIZE;
    const pageItems = resolvedData.slice(start, end);

    pageItems.forEach(c => {
        const tr = document.createElement("tr");
        let actionsHtml = `
            <button class="btn-view" onclick='openModal(${JSON.stringify(c)})'>View</button>
        `;

        if (c.status !== "REJECTED") {
            actionsHtml += `
                <button class="btn-delete" onclick="openReopenModal(${c.id})">Reopen</button>
            `;
        }
        tr.innerHTML = `
      <td><b>${c.title}</b></td>
      <td>${c.type}</td>
      <td><span class="status ${c.status}">${c.status}</span></td>
      <td>${c.resolvedBy}</td>
      <td>${c.resolvedInDays}</td>
      <td>${actionsHtml}</td>
    `;
        tbody.appendChild(tr);
    });
    updateResolvedPaginationControls();
}

function updateResolvedPaginationControls() {
    const totalPages = Math.ceil(resolvedData.length / PAGE_SIZE);

    const container = document.getElementById("resolvedPageNumbers");
    container.innerHTML = "";

    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement("button");
        btn.innerText = i;

        if (i === resolvedPage) {
            btn.classList.add("active");
        }

        btn.onclick = () => {
            resolvedPage = i;
            renderResolved();
        };

        container.appendChild(btn);
    }

    document.getElementById("resolvedPrevBtn").disabled = resolvedPage === 1;
    document.getElementById("resolvedNextBtn").disabled = resolvedPage === totalPages;
}

function prevResolvedPage() {
    if (resolvedPage > 1) {
        resolvedPage--;
        renderResolved();
    }
}

function nextResolvedPage() {
    const totalPages = Math.ceil(resolvedData.length / PAGE_SIZE);
    if (resolvedPage < totalPages) {
        resolvedPage++;
        renderResolved();
    }
}

/* Load Complaint Types */
function loadComplaintTypes() {
    fetch(`${BASE_URL}/api/complaints/types`)
        .then(res => {
            if (res.status === 401) {
                window.location.href = "login.html"; return;
            } return res.json();
        })
        .then(types => {
            if (!types) return;
            const select = document.getElementById("complaintType");
            types.forEach(t => {
                const o = document.createElement("option");
                o.value = t;
                o.text = t;
                select.appendChild(o);
            });
        });
}

/* Submit Complaint */
function submitComplaint() {

    const locality = document.getElementById("locality").value.trim();
    const ward = document.getElementById("ward").value.trim();
    const complaintType = document.getElementById("complaintType").value;
    const title = document.getElementById("title").value.trim();
    const description = document.getElementById("description").value.trim();
    const msg = document.getElementById("msg");
    msg.style.color = "red";

    if (!locality) {
        msg.innerText = "Locality is required";
        return;
    } if (!ward) {
        msg.innerText = "Ward is required";
        return;
    } if (!complaintType) {
        msg.innerText = "Please select a valid complaint type";
        return;
    } if (!title) {
        msg.innerText = "Title is required";
        return;
    } if (!description || description.length < 10 || description.length > 1000) {
        msg.innerText = "Description is required and must be atleast 10 characters and maximum 1000 characters";
        return;
    }
    const complaintData = {
        locality: locality,
        ward: ward,
        complaintType: complaintType,
        title: title,
        description: description
    }

    showLoader();

    fetch(`${BASE_URL}/api/complaints`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + localStorage.getItem("token")
        },
        body: JSON.stringify(complaintData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to submit complaint");
            } else if (response.status === 401) {
                window.location.href = "login.html";
                return;
            }
            return response.json();
        })
        .then(data => {
            if (!data) return;
            msg.innerText = "";
            showToast("Complaint registered successfully");
            document.getElementById("locality").value = "";
            document.getElementById("ward").value = "";
            document.getElementById("complaintType").value = "";
            document.getElementById("title").value = "";
            document.getElementById("description").value = "";
            loadMyComplaints();
        })
        .catch(error => {
            msg.style.color = "red";
            msg.innerText = error.message;
        })
        .finally(() => {
            hideLoader();
        });
}

function openModal(c) {
    mTitle.innerText = c.title;
    mType.innerText = c.type || "-";
    mDescription.innerText = c.description;
    mHandledBy.innerText = c.pendingWith || c.resolvedBy || "-";

    mStatus.innerText = c.status || "CLOSED";
    mStatus.className = "status-badge " + (c.status || "CLOSED");

    complaintModal.style.display = "block";
}

function closeComplaintModal() {
    complaintModal.style.display = "none";
}

function deleteComplaint(id) {
    fetch(`${BASE_URL}/api/complaints/${id}`, {
        method: "DELETE",
        headers: { "Authorization": "Bearer " + token }
    }).then(res => {
        if (!res.ok) {
            throw new Error("Failed to reopen complaint");
        }

        showToast("Complaint deleted successfully");
        loadMyComplaints();
    });
}

function showToast(message) {
  const toast = document.getElementById("toast");
  toast.innerText = message;
  toast.style.display = "block";

  setTimeout(() => {
    toast.style.display = "none";
  }, 2500);
}

function openDeleteModal(id) {
    deleteComplaintId = id;
    document.getElementById("deleteConfirmModal").style.display = "flex";
}

function closeDeleteModal() {
    deleteComplaintId = null;
    document.getElementById("deleteConfirmModal").style.display = "none";
}

function confirmDelete() {
    if (!deleteComplaintId) return
    deleteComplaint(deleteComplaintId);
    closeDeleteModal();
}

function reopenComplaint(id) {
    fetch(`${BASE_URL}/api/complaints/${id}/reopen`, {
        method: "POST",
        headers: { "Authorization": "Bearer " + token }
    }).then(res => {
        if (!res.ok) {
            throw new Error("Failed to reopen complaint");
        }

        showToast("Complaint reopened successfully");
        loadMyComplaints();
    });
}

function openReopenModal(id) {
    reopenComplaintId = id;
    document.getElementById("reopenConfirmModal").style.display = "flex";
}

function closeReopenModal() {
    reopenComplaintId = null;
    document.getElementById("reopenConfirmModal").style.display = "none";
}

function confirmReopen() {
    if (!reopenComplaintId) return;

    reopenComplaint(reopenComplaintId);
    closeReopenModal();
}

function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}

function getInitials(name) {
  if (!name) return "--";
  return name
    .trim()
    .split(/\s+/)
    .map(w => w[0])
    .join("")
    .toUpperCase();
}

function loadUserProfile() {
  fetch(`${BASE_URL}/api/auth/profile`, {
    method: "GET",
    headers: {
      "Authorization": "Bearer " + localStorage.getItem("token")
    }
  })
    .then(res => {
      if (res.status === 401) {
        logout();
        return;
      }
      if (!res.ok) {
        throw new Error("Failed to load profile");
      }
      return res.json();
    })
    .then(user => {
      if (!user) return;

      document.getElementById("userAvatar").innerText =
        getInitials(user.name);

      document.getElementById("pName").innerText = user.name;
      document.getElementById("pEmail").innerText = user.email;
      document.getElementById("pPhone").innerText = user.phone || "-";
      document.getElementById("pLocation").innerText = user.location || "-";
      document.getElementById("pDistrict").innerText = user.district || "-";
      document.getElementById("pState").innerText = user.state || "-";
    })
    .catch(err => console.error(err));
}

function toggleProfile() {
  const card = document.getElementById("profileCard");
  card.style.display = card.style.display === "block" ? "none" : "block";
}

document.addEventListener("click", function (e) {
  if (!e.target.closest(".profile-wrapper")) {
    document.getElementById("profileCard").style.display = "none";
  }
});

function showLoader() {
    document.getElementById("loaderOverlay").style.display = "flex";
}

function hideLoader() {
    document.getElementById("loaderOverlay").style.display = "none";
}