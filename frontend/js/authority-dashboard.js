/***********************
 * GLOBAL VARIABLES
 ***********************/
const BASE_URL = "http://localhost:8080";
const token = localStorage.getItem("token");

let ongoingComplaints = [];
let completedComplaints = [];
let currentComplaintId = null;
let originalValues = {};
let currentOngoingPage = 0;
let currentCompletedPage = 0;
const pageSize = 5;

/***********************
 * PAGE LOAD
 ***********************/
document.addEventListener("DOMContentLoaded", () => {
    if (!token) {
        logout();
        return;
    }

    loadUserProfile();
    getComplaints(0);
});

/***********************
 * USER PROFILE
 ***********************/
function loadUserProfile() {
    fetch(`${BASE_URL}/api/auth/profile`, {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + token
        }
    })
        .then(res => {
            if (res.status === 401) {
                logout();
                return;
            }
            if (!res.ok) throw new Error("Failed to load profile");
            return res.json();
        })
        .then(user => {
            if (!user) return;

            document.getElementById("userAvatar").innerText = getInitials(user.name);
            document.getElementById("pName").innerText = user.name;
            document.getElementById("pEmail").innerText = user.email;
            document.getElementById("pPhone").innerText = user.phone || "-";
            document.getElementById("pLocation").innerText = user.location || "-";
            document.getElementById("pDistrict").innerText = user.district || "-";
            document.getElementById("pState").innerText = user.state || "-";
        })
        .catch(err => console.error(err));
}

function getInitials(name) {
    if (!name) return "--";
    return name.trim().split(/\s+/).map(w => w[0]).join("").toUpperCase();
}

function logout() {
    localStorage.clear();
    window.location.href = "index.html";
}

/***********************
 * PROFILE DROPDOWN
 ***********************/
function toggleProfile() {
    const card = document.getElementById("profileCard");
    card.style.display = card.style.display === "block" ? "none" : "block";
}

document.addEventListener("click", function (e) {
    if (!e.target.closest(".profile-wrapper")) {
        document.getElementById("profileCard").style.display = "none";
    }
});

/***********************
 * LOAD COMPLAINTS
 ***********************/
function getComplaints(page) {

    loadOngoingComplaints(page);
    loadCompletedComplaints(page);
}

function loadOngoingComplaints(page) {

    currentOngoingPage = page;
    const params = new URLSearchParams({
        page,
        size: pageSize
    });

    fetch(`${BASE_URL}/api/complaints/authority/ongoing?${params}`, {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + token
        }
    })
    .then(res => {
        if (res.status === 401) {
            logout();
            return;
        }
        if (!res.ok) throw new Error("Failed to fetch complaints");
        return res.json();
    })
    .then(data => {
        renderOngoingTable(data.content);
        renderPagination(data.totalPages);
    })
    .catch(err => console.error(err));
}

function loadCompletedComplaints(page) {

    currentCompletedPage = page;
    const params = new URLSearchParams({
        page,
        size: pageSize
    });

    fetch(`${BASE_URL}/api/complaints/authority/completed?${params}`, {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + token
        }
    })
    .then(res => {
        if (res.status === 401) {
            logout();
            return;
        }
        if (!res.ok) throw new Error("Failed to fetch complaints");
        return res.json();
    })
    .then(data => {
        renderCompletedTable(data.content);
        renderCompletedPagination(data.totalPages);
    })
    .catch(err => console.error(err));
}

/***********************
 * RENDER TABLE
 ***********************/
function renderOngoingTable(list) {
    ongoingComplaints = list;
    complaintTableBody.innerHTML = "";

    if (list.length === 0) {
        complaintTableBody.innerHTML = `<tr><td colspan="10">No complaints found</td></tr>`;
        return;
    }

    list.forEach(c => {
        const sla = computeSla(c.slaDeadline, c.status);
        complaintTableBody.innerHTML += `
      <tr>
        <td>#${c.id}</td>
        <td>${c.title}</td>
        <td>
            <span class="sla-badge" data-sla="${sla.slaStatus}">${sla.slaText}</span>
        </td>
        <td><span class="status ${c.status}">${c.status}</span></td>
        <td class="action-cell">
            <button class="btn-view" onclick="openModal(${c.id})">View</button>
        </td>
      </tr>
    `;
    });
}

function renderCompletedTable(list) {
    completedComplaints = list;
    completedComplaintTableBody.innerHTML = "";

    if (list.length === 0) {
        completedComplaintTableBody.innerHTML = `<tr><td colspan="10">No complaints found</td></tr>`;
        return;
    }

    list.forEach(c => {
        completedComplaintTableBody.innerHTML += `
      <tr>
        <td>#${c.id}</td>
        <td>${c.title}</td>
        <td><span class="status ${c.status}">${c.status}</span></td>
        <td>${formatDate(c.updatedDate)}</td>
        <td>${calculateTimeDiff(c.updatedDate, c.createdDate)} Days</td>
        <td class="action-cell">
            <button class="btn-view" onclick="openCompletedModal(${c.id})">View</button>
        </td>
      </tr>
    `;
    });
}

function calculateTimeDiff(completedDate, createdDate) {
    const startDate = new Date(createdDate);
    const endDate = new Date(completedDate);
    return Math.floor((endDate - startDate)/86400000);
}

function renderPagination(totalPages) {
    pagination.innerHTML = "";

    const maxVisible = 5; 

    pagination.innerHTML += `
        <span class="page-btn ${currentOngoingPage === 0 ? 'disabled' : ''}"
              onclick="${currentOngoingPage === 0 ? '' : `loadOngoingComplaints(${currentOngoingPage - 1})`}">
            « Prev
        </span>
    `;

    let start = Math.max(0, currentOngoingPage - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages - 1, start + maxVisible - 1);

    start = Math.max(0, end - maxVisible + 1);

    for (let i = start; i <= end; i++) {
        pagination.innerHTML += `
            <span class="page-number ${i === currentOngoingPage ? 'active' : ''}"
                  onclick="loadOngoingComplaints(${i})">
                ${i + 1}
            </span>
        `;
    }

    pagination.innerHTML += `
        <span class="page-btn ${currentOngoingPage === totalPages - 1 ? 'disabled' : ''}"
              onclick="${currentOngoingPage === totalPages - 1 ? '' : `loadOngoingComplaints(${currentOngoingPage + 1})`}">
            Next »
        </span>
    `;
}


function renderCompletedPagination(totalPages) {
    completedPagination.innerHTML = "";

    for (let i = 0; i < totalPages; i++) {
        completedPagination.innerHTML += `
      <span class="${i === currentCompletedPage ? 'active' : ''}"
            onclick="loadCompletedComplaints(${i})">${i + 1}</span>`;
    }
}

function computeSla(deadline, complaintStatus) {

    if (complaintStatus === "" || complaintStatus === "REJECTED") {
        return {
            slaStatus: "MET",
            slaText: complaintStatus === "RESOLVED"
                ? "Met on time"
                : "Met"
        };
    }

    const now = new Date();
    const due = new Date(deadline);
    const diffMs = due - now;

    if (diffMs <= 0) {
        const overdueHours = Math.abs(Math.floor(diffMs / (1000 * 60 * 60)));
        const overdueDays = Math.floor(overdueHours / 24);

        return {
            slaStatus: "OVERDUE",
            slaText: overdueDays > 0
                ? `${overdueDays} day overdue`
                : `${overdueHours} hours overdue`
        };
    }

    const totalMinutes = Math.floor(diffMs / (1000 * 60));
    const days = Math.floor(totalMinutes / (60 * 24));
    const hours = Math.floor((totalMinutes % (60 * 24)) / 60);
    const minutes = totalMinutes % 60;

    if (days >= 2) {
        return {
            slaStatus: "ON_TRACK",
            slaText: `${days} days left | ${hours}h ${minutes}m`
        };
    }

    if (days >= 1) {
        return {
            slaStatus: "WARNING",
            slaText: `${hours + 24} hours left | ${minutes}m`
        };
    }

    return {
        slaStatus: "CRITICAL",
        slaText: `${hours} hours left | ${minutes}m`
    };
}

/***********************
 * OPEN MODAL
 ***********************/
function openModal(id) {
    const c = ongoingComplaints.find(x => x.id === id);
    if (!c) return;

    currentComplaintId = id;

    Promise.all([
        fetch(`${BASE_URL}/api/complaints/status`).then(r => r.json()),
        fetch(`${BASE_URL}/api/complaints/priorities`).then(r => r.json()),
        fetch(`${BASE_URL}/api/complaints/types`).then(r => r.json())
    ])
        .then(([statusList, priorityList, categoryList]) => {

            document.getElementById("desc").textContent = c.description;
            document.getElementById("createdBy").textContent = c.createdBy;
            document.getElementById("createdOn").textContent = formatDate(c.createdDate);
            document.getElementById("ward").textContent = c.ward;
            document.getElementById("location").textContent = c.location;

            loadDropdown("statusSelect", statusList, c.status);
            loadDropdown("prioritySelect", priorityList, c.priority);
            loadDropdown("categorySelect", categoryList, c.category);

            applyStatusColor(c.status);
            applyPriorityColor(c.priority);

            originalValues = {
                status: c.status,
                priority: c.priority,
                category: c.category
            };
            toggleSaveButton();
            document.getElementById("complaintModal").style.display = "flex";
        });
}

function openCompletedModal(id) {
    const c = completedComplaints.find(x => x.id === id);
    if (!c) return;

    document.getElementById("cDesc").textContent = c.description;
    document.getElementById("cCreatedBy").textContent = c.createdBy;
    document.getElementById("cCreatedOn").textContent = formatDate(c.createdDate);
    document.getElementById("cWard").textContent = c.ward;
    document.getElementById("cLocation").textContent = c.location;
    document.getElementById("cCategory").textContent = c.category;

    const el = document.getElementById("cPriority");
    el.className = "priority";
    el.classList.add(c.priority);

    el.textContent = c.priority;
    document.getElementById("completedComplaintModal").style.display = "flex";
}

function formatDate(dateStr) {
    const d = new Date(dateStr);
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}-${month}-${year}`;
}

function loadDropdown(selectId, options, selectedValue) {
    const select = document.getElementById(selectId);
    select.innerHTML = "";

    options.forEach(value => {
        const option = document.createElement("option");
        option.value = value;
        option.textContent = "  " + value.replace("_", " ");
        select.appendChild(option);
    });
    select.value = selectedValue;
}

function applyStatusColor(value) {
    const el = document.getElementById("statusWrap");
    el.className = `meta-value status ${value}`;
}

function applyPriorityColor(value) {
    const el = document.getElementById("priorityWrap");
    el.className = `completed-meta-value priority ${value}`;
}

statusSelect.addEventListener("change", e => {
    applyStatusColor(e.target.value);
});

prioritySelect.addEventListener("change", e => {
    applyPriorityColor(e.target.value);
});

["statusSelect", "prioritySelect", "categorySelect"].forEach(id => {
    document.getElementById(id).addEventListener("change", toggleSaveButton);
});

function toggleSaveButton() {
    const changed =
        statusSelect.value !== originalValues.status ||
        prioritySelect.value !== originalValues.priority ||
        categorySelect.value !== originalValues.category;

    document.getElementById("saveBtn").disabled = !changed;
}

/***********************
 * CLOSE MODAL
 ***********************/
function closeModal() {
    document.getElementById("complaintModal").style.display = "none";
    document.getElementById("completedComplaintModal").style.display = "none";
    currentComplaintId = null;
}

function updateComplaint() {
    const updatedData = {
        status: statusSelect.value,
        priority: prioritySelect.value,
        category: categorySelect.value
    };

    const changed =
        updatedData.status !== originalValues.status ||
        updatedData.priority !== originalValues.priority ||
        updatedData.category !== originalValues.category;

    if (!changed) return;

    fetch(`${BASE_URL}/api/complaints/update/${currentComplaintId}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify(updatedData)
    })
    .then(res => {
        if (res.status === 401) {
            logout();
            return;
        }
        if (!res.ok) throw new Error("Update failed");
        return res.text();
    })
    .then(() => {
        closeModal();
        getComplaints(0);
    })
    .catch(err => console.error(err));
}
