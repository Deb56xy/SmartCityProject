const BASE_URL = "http://localhost:8080";
const token = localStorage.getItem("token");

let complaints = [];
let currentPage = 0;
const pageSize = 5;
let selectedComplaintId = null;

document.addEventListener("DOMContentLoaded", () => {
    loadUserProfile();
    loadFilters();
    loadDashboard();
    loadComplaints(0);
    loadAnalytics();

    search.addEventListener("input", () => loadComplaints(0));
    statusFilter.addEventListener("change", () => loadComplaints(0));
    priorityFilter.addEventListener("change", () => loadComplaints(0));
});

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

function getInitials(name) {
    if (!name) return "--";
    return name
        .trim()
        .split(/\s+/)
        .map(w => w[0])
        .join("")
        .toUpperCase();
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

function loadFilters() {

    fetch(`${BASE_URL}/api/complaints/status`)
        .then(res => {

            return res.json();
        })
        .then(status => {
            if (!status) return;
            status.forEach(s => {
                statusFilter.innerHTML += `<option value="${s}">${s}</option>`;
            });
        });

    fetch(`${BASE_URL}/api/complaints/priorities`)
        .then(res => {

            return res.json();
        })
        .then(priorities => {
            if (!priorities) return;
            priorities.forEach(p => {
                priorityFilter.innerHTML += `<option value="${p}">${p}</option>`;
            });
        });

    search.addEventListener("input", () => loadComplaints(0));
    statusFilter.addEventListener("change", () => loadComplaints(0));
    priorityFilter.addEventListener("change", () => loadComplaints(0));
}

function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}

function loadDashboard() {
    fetch(`${BASE_URL}/api/complaints/stats`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + localStorage.getItem("token")
        }
    })
        .then(r => r.json())
        .then(s => {
            stats.innerHTML = `
        <div class="stat-card">Total <span>${s.total}</span></div>
        <div class="stat-card">New <span>${s.new}</span></div>
        <div class="stat-card">Under Review <span>${s.underReview}</span></div>
        <div class="stat-card">In Progress <span>${s.inProgress}</span></div>
        <div class="stat-card">Resolved <span>${s.resolved}</span></div>
        <div class="stat-card">Overdue <span>${s.overdue}</span></div>
      `;
        });
}

function loadComplaints(page) {
    currentPage = page;

    const params = new URLSearchParams({
        page,
        size: pageSize,
        search: search.value,
        status: statusFilter.value,
        priority: priorityFilter.value
    });

    fetch(`${BASE_URL}/api/complaints/search?${params}`, {
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
                throw new Error("Failed to load complaints");
            }
            return res.json();
        })
        .then(data => {
            renderTable(data.content);
            renderPagination(data.totalPages);
        });
}

function renderTable(list) {
    complaints = list;
    complaintTable.innerHTML = "";

    if (list.length === 0) {
        complaintTable.innerHTML = `<tr><td colspan="10">No complaints found</td></tr>`;
        return;
    }

    list.forEach(c => {
        const sla = computeSla(c.slaDeadline, c.status);
        complaintTable.innerHTML += `
      <tr>
        <td>#${c.id}</td>
        <td>${c.title}</td>
        <td>${c.category}</td>
        <td>${c.ward}</td>
        <td><span class="priority ${c.priority}">${c.priority}</span></td>
        <td><span class="status ${c.status}">${c.status}</span></td>
        <td>${c.assignedOfficer ?? "-"}</td>
        <td>${formatDate(c.createdDate)}</td>
        <td>
            <span class="sla-badge" data-sla="${sla.slaStatus}">${sla.slaText}</span>
        </td>
        <td class="action-cell">
            <button class="btn-view" onclick="openComplaintModal(${c.id})">View</button>
            ${(c.status !== "RESOLVED" && c.status !== "REJECTED")
            ? `<button class="btn-assign" onclick="assignComplaint(${c.id})">Assign</button>`
            : ""}
        </td>
      </tr>
    `;
    });
}

function assignComplaint(id) {
  selectedComplaintId = id;
  document.getElementById("authoritySearch").value = "";
  document.getElementById("authorityList").innerHTML = "";
  document.getElementById("assignModal").style.display = "flex";
}

function closeAssignModal() {
  document.getElementById("assignModal").style.display = "none";
}

function searchAuthority() {
  const q = authoritySearch.value;
  if (q.length < 2) return;

  const complaint = complaints.find(c => c.id === selectedComplaintId);

  fetch(
    `${BASE_URL}/api/complaints/search/authority?complaintId=${complaint.id}&q=${q}`,
    {
      headers: {
        "Authorization": "Bearer " + localStorage.getItem("token")
      }
    }
  )
    .then(res => res.json())
    .then(list => {
      authorityList.innerHTML = "";
      list.forEach(a => {
        authorityList.innerHTML += `
          <li>
            <span>${a.name}</span>
            <button class="assign-btn"
              onclick="confirmAssign(${a.id})">
              Assign
            </button>
          </li>
        `;
      });
    });
}

function confirmAssign(authorityId) {
  fetch(
    `${BASE_URL}/api/complaints/${selectedComplaintId}/assign?authorityId=${authorityId}`,
    {
      method: "POST",
      headers: {
        "Authorization": "Bearer " + localStorage.getItem("token")
      }
    }
  ).then(() => {
    closeAssignModal();
    loadComplaints(currentPage);
  });
}

function computeSla(deadline, complaintStatus) {

    if (complaintStatus === "RESOLVED" || complaintStatus === "REJECTED") {
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

function renderPagination(totalPages) {
    pagination.innerHTML = "";

    const maxVisible = 5; 

    pagination.innerHTML += `
        <span class="page-btn ${currentPage === 0 ? 'disabled' : ''}"
              onclick="${currentPage === 0 ? '' : `loadComplaints(${currentPage - 1})`}">
            « Prev
        </span>
    `;

    let start = Math.max(0, currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages - 1, start + maxVisible - 1);

    start = Math.max(0, end - maxVisible + 1);

    for (let i = start; i <= end; i++) {
        pagination.innerHTML += `
            <span class="page-number ${i === currentPage ? 'active' : ''}"
                  onclick="loadComplaints(${i})">
                ${i + 1}
            </span>
        `;
    }

    pagination.innerHTML += `
        <span class="page-btn ${currentPage === totalPages - 1 ? 'disabled' : ''}"
              onclick="${currentPage === totalPages - 1 ? '' : `loadComplaints(${currentPage + 1})`}">
            Next »
        </span>
    `;
}

function formatDate(dateStr) {
    const d = new Date(dateStr);
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}-${month}-${year}`;
}

function openComplaintModal(id) {
    const complaint = complaints.find(c => c.id === id);

    if (!complaint) {
        alert("Complaint not found");
        return;
    }

    // Populate modal fields
    document.getElementById("mCreatedBy").textContent =
        complaint.createdBy || "-";

    document.getElementById("mDescription").textContent =
        complaint.description || "-";

    document.getElementById("mWard").textContent =
        complaint.ward ?? "-";

    document.getElementById("mLocation").textContent =
        complaint.location || "-";

    document.getElementById("complaintModal").style.display = "flex";
}

function closeComplaintModal() {
    document.getElementById("complaintModal").style.display = "none";
}

function loadAnalytics() {
    fetch(`${BASE_URL}/api/complaints/analytics`, {
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token")
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Failed to fetch analytics");
        } else if (response.status === 401) {
            window.location.href = "login.html";
            return;
        }
        return response.json();
    })
    .then(data => {
        renderStatusChart(data.byStatus);
        renderTypeChart(data.byType);
        renderTopOfficers(data.topOfficers);
    });
}

function renderStatusChart(data) {
  new Chart(document.getElementById("statusChart"), {
    type: "bar",
    data: {
      labels: Object.keys(data),
      datasets: [{
        data: Object.values(data),
        backgroundColor: [
          "#facc15", // NEW
          "#fb923c", // UNDER_REVIEW
          "#3b82f6", // IN_PROGRESS
          "#22c55e", // RESOLVED
          "#ef4444"  // OVERDUE
        ],
        borderRadius: 6
      }]
    },
    options: {
      plugins: { legend: { display: false } },
      scales: {
        y: { beginAtZero: true }
      }
    }
  });
}

function renderTypeChart(data) {
  new Chart(document.getElementById("typeChart"), {
    type: "pie",
    data: {
      labels: Object.keys(data),
      datasets: [{
        data: Object.values(data),
        backgroundColor: [
          "#16a34a", // Garbage
          "#facc15", // Water
          "#3b82f6", // Wiring
          "#86efac"  // Other
        ]
      }]
    },
    options: {
      plugins: { legend: { position: "left" } }
    }
  });
}

function renderTopOfficers(list) {
  const ul = document.getElementById("topOfficers");
  ul.innerHTML = "";

  list.forEach(o => {
    ul.innerHTML += `
      <li>
        <span>${o.name}</span>
        <span class="count">${o.count}</span>
      </li>
    `;
  });
}