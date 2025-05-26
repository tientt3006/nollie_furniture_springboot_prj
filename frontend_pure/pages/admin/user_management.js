// API base URL
const API_BASE_URL = 'http://localhost:8080/api';

// State management for users and filters
let state = {
    users: [],
    pagination: {
        currentPage: 0,
        pageSize: 10,
        totalElements: 0,
        totalPages: 0
    },
    filters: {
        userId: '',
        search: '',
        isActive: null,
        sortBy: 'id',
        sortDirection: 'asc'
    }
};

// DOM elements
document.addEventListener('DOMContentLoaded', function() {
    // Setup event listeners
    document.getElementById("search-button").addEventListener("click", function () {
        state.filters.userId = document.getElementById("search-id-input").value.trim();
        state.filters.search = document.getElementById("search-name-input").value.trim();
        state.filters.sortBy = document.getElementById("sort-by").value;
        state.filters.sortDirection = document.getElementById("sort-order").value;
        state.pagination.currentPage = 0; // Reset to first page when searching
        loadUsers();
    });

    // Add reset button functionality
    document.getElementById("reset-filters").addEventListener("click", function() {
        // Clear all input fields
        document.getElementById("search-id-input").value = '';
        document.getElementById("search-name-input").value = '';
        document.getElementById("sort-by").value = 'id';
        document.getElementById("sort-order").value = 'asc';
        
        // Reset filter state
        state.filters.userId = '';
        state.filters.search = '';
        state.filters.sortBy = 'id';
        state.filters.sortDirection = 'asc';
        state.pagination.currentPage = 0; // Reset to first page
        
        // Reload users with reset filters
        loadUsers();
    });

    document.getElementById("page-size").addEventListener("change", function() {
        state.pagination.pageSize = parseInt(this.value);
        state.pagination.currentPage = 0; // Reset to first page when changing page size
        loadUsers();
    });

    document.getElementById("prev-page").addEventListener("click", function() {
        if (state.pagination.currentPage > 0) {
            state.pagination.currentPage--;
            loadUsers();
        }
    });

    document.getElementById("next-page").addEventListener("click", function() {
        if (state.pagination.currentPage < state.pagination.totalPages - 1) {
            state.pagination.currentPage++;
            loadUsers();
        }
    });

    document.getElementById("page-number").addEventListener("change", function() {
        const pageNumber = parseInt(this.value) - 1; // Convert from 1-based to 0-based
        if (pageNumber >= 0 && pageNumber < state.pagination.totalPages) {
            state.pagination.currentPage = pageNumber;
            loadUsers();
        } else {
            this.value = state.pagination.currentPage + 1; // Reset to current page if invalid
        }
    });

    // Initial load
    loadUsers();
});

// Load users from API
async function loadUsers() {
    try {
        // Show loading indicator
        document.getElementById("user-table-body").innerHTML = '<tr><td colspan="5" class="text-center">Loading...</td></tr>';
        
        // Build query parameters
        const queryParams = new URLSearchParams({
            page: state.pagination.currentPage,
            size: state.pagination.pageSize,
            sortBy: state.filters.sortBy,
            sortDirection: state.filters.sortDirection
        });

        // Add filters if they exist
        if (state.filters.userId) queryParams.append('userId', state.filters.userId);
        if (state.filters.search) queryParams.append('search', state.filters.search);
        if (state.filters.isActive !== null) queryParams.append('isActive', state.filters.isActive);

        // Get auth token from localStorage
        const authToken = localStorage.getItem('authToken');
        if (!authToken) {
            alert('You are not logged in. Please log in to access this feature.');
            return;
        }

        // Fetch data from API
        const response = await fetch(`${API_BASE_URL}/user/admin/search?${queryParams.toString()}`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        const data = await response.json();
        if (data.code !== 1000) {
            throw new Error(data.message || 'Failed to load users');
        }

        // Update state with response data
        state.users = data.result.content;
        state.pagination.currentPage = data.result.number;
        state.pagination.totalElements = data.result.totalElements;
        state.pagination.totalPages = data.result.totalPages;

        // Render users and update pagination
        renderUsers();
        renderPagination();
    } catch (error) {
        console.error('Error loading users:', error);
        document.getElementById("user-table-body").innerHTML = 
            `<tr><td colspan="5" class="text-center">Error loading users: ${error.message}</td></tr>`;
    }
}

// Render user data to the table
function renderUsers() {
    const tableBody = document.getElementById("user-table-body");
    tableBody.innerHTML = "";

    if (state.users.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="5" class="text-center">No users found</td></tr>';
        return;
    }

    state.users.forEach(user => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.fullName}</td>
            <td>${user.email}</td>
            <td>${user.phone}</td>
            <td>
                <button class="btn btn-sm btn-info view-details" data-id="${user.id}">Details</button>
            </td>
        `;
        tableBody.appendChild(row);
    });

    // Add event listeners to detail buttons
    document.querySelectorAll(".view-details").forEach(button => {
        button.addEventListener("click", () => showUserDetails(button.getAttribute("data-id")));
    });
}

// Render pagination controls
function renderPagination() {
    const { currentPage, pageSize, totalElements, totalPages } = state.pagination;
    
    // Update showing X-Y of Z text
    const start = totalElements === 0 ? 0 : currentPage * pageSize + 1;
    const end = Math.min((currentPage + 1) * pageSize, totalElements);
    document.getElementById("pagination-info").textContent = `Showing ${start}-${end} of ${totalElements}`;
    
    // Update page number input
    document.getElementById("page-number").value = currentPage + 1;
    document.getElementById("page-number").max = totalPages;
    
    // Enable/disable prev/next buttons
    document.getElementById("prev-page").disabled = currentPage === 0;
    document.getElementById("next-page").disabled = currentPage === totalPages - 1 || totalPages === 0;
    
    // Generate page links
    const paginationLinks = document.getElementById("pagination-links");
    paginationLinks.innerHTML = "";
    
    if (totalPages <= 7) {
        // Show all pages if 7 or fewer
        for (let i = 0; i < totalPages; i++) {
            addPageLink(paginationLinks, i);
        }
    } else {
        // Show first page
        addPageLink(paginationLinks, 0);
        
        if (currentPage < 3) {
            // Near start: show first 5 pages, then ellipsis, then last page
            for (let i = 1; i < 5; i++) {
                addPageLink(paginationLinks, i);
            }
            addEllipsis(paginationLinks);
        } else if (currentPage >= totalPages - 3) {
            // Near end: show ellipsis, then last 5 pages
            addEllipsis(paginationLinks);
            for (let i = totalPages - 5; i < totalPages - 1; i++) {
                addPageLink(paginationLinks, i);
            }
        } else {
            // Middle: show ellipsis, then 2 pages before and after current, then ellipsis
            addEllipsis(paginationLinks);
            for (let i = currentPage - 2; i <= currentPage + 2; i++) {
                addPageLink(paginationLinks, i);
            }
            addEllipsis(paginationLinks);
        }
        
        // Show last page
        addPageLink(paginationLinks, totalPages - 1);
    }
}

// Helper to add a page link to pagination
function addPageLink(container, pageNumber) {
    const link = document.createElement("button");
    link.className = "btn btn-sm " + 
        (pageNumber === state.pagination.currentPage ? "btn-primary" : "btn-outline-secondary");
    link.textContent = pageNumber + 1;
    link.addEventListener("click", () => {
        state.pagination.currentPage = pageNumber;
        loadUsers();
    });
    container.appendChild(link);
}

// Helper to add ellipsis to pagination
function addEllipsis(container) {
    const ellipsis = document.createElement("span");
    ellipsis.className = "mx-1";
    ellipsis.textContent = "...";
    container.appendChild(ellipsis);
}

// Show user details in modal
function showUserDetails(userId) {
    const user = state.users.find(u => u.id == userId);
    if (!user) {
        alert('User not found');
        return;
    }

    // Populate modal with user details
    document.getElementById("detail-user-id").textContent = user.id;
    document.getElementById("detail-user-name").textContent = user.fullName;
    document.getElementById("detail-user-email").textContent = user.email;
    document.getElementById("detail-user-phone").textContent = user.phone;
    document.getElementById("detail-user-role").textContent = user.role;
    document.getElementById("detail-user-status").textContent = user.active ? "Active" : "Inactive";
    document.getElementById("detail-user-address").textContent = user.address || "No address provided";

    // Show modal
    document.getElementById("user-details-modal").style.display = "flex";
}

// Close user details modal
function closeUserDetailsModal() {
    document.getElementById("user-details-modal").style.display = "none";
}

// Add global function for modal close button
window.closeUserDetailsModal = closeUserDetailsModal;
