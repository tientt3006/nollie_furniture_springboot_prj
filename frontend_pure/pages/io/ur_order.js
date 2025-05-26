// API configuration
const API_BASE_URL = 'http://localhost:8080/api';

// State management
let currentState = {
    orders: [],
    pagination: {
        currentPage: 0,
        pageSize: 10,
        totalPages: 0,
        totalElements: 0
    },
    filters: {
        orderId: '',
        startDate: '',
        endDate: '',
        paymentMethod: '',
        status: ''
    }
};

// Get auth token from localStorage
function getAuthToken() {
    return localStorage.getItem('authToken');
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Format date
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN');
}

// Load orders with current filters and pagination
async function loadOrders() {
    try {
        const token = getAuthToken();
        if (!token) {
            alert('Please login to view your orders');
            window.location.href = '../io/signin.html';
            return;
        }

        // Build query parameters
        const queryParams = new URLSearchParams({
            page: currentState.pagination.currentPage,
            size: currentState.pagination.pageSize
        });

        // Add filters if they exist
        if (currentState.filters.orderId) {
            queryParams.append('search', currentState.filters.orderId);
        }
        
        if (currentState.filters.startDate) {
            queryParams.append('startDate', currentState.filters.startDate);
        }
        
        if (currentState.filters.endDate) {
            queryParams.append('endDate', currentState.filters.endDate);
        }
        
        if (currentState.filters.paymentMethod) {
            queryParams.append('paymentMethod', currentState.filters.paymentMethod);
        }
        
        if (currentState.filters.status) {
            queryParams.append('status', currentState.filters.status);
        }

        const response = await fetch(`${API_BASE_URL}/order/user/search?${queryParams.toString()}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load orders');
        }

        const data = await response.json();
        if (data.code === 1000) {
            currentState.orders = data.result.content;
            currentState.pagination.currentPage = data.result.number;
            currentState.pagination.pageSize = data.result.size;
            currentState.pagination.totalPages = data.result.totalPages;
            currentState.pagination.totalElements = data.result.totalElements;
            
            renderOrders();
            updatePagination();
        } else {
            throw new Error('Failed to load orders');
        }
    } catch (error) {
        console.error('Error loading orders:', error);
        alert(error.message || 'Failed to load orders');
        // Show empty state
        document.getElementById('order-list').innerHTML = '<tr><td colspan="7" style="text-align: center;">Failed to load orders</td></tr>';
    }
}

// Render orders to the table
function renderOrders() {
    const tbody = document.getElementById('order-list');
    tbody.innerHTML = '';

    if (currentState.orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">No orders found</td></tr>';
        return;
    }

    const statusMap = {
        'ORDER_SUCCESSFUL': 'Order Success',
        'ON_DELIVERY': 'On Delivery',
        'RECEIVED': 'Received',
        'CANCELED': 'Canceled'
    };

    const paymentMethodMap = {
        'CASH_ON_DELIVERY': 'COD',
        'BANK_TRANSFER': 'Bank Transfer'
    };

    // Define cancellable statuses
    const cancellableStatuses = ['ORDER_SUCCESSFUL', 'ON_DELIVERY'];

    currentState.orders.forEach(order => {
        const row = document.createElement('tr');
        const canCancel = cancellableStatuses.includes(order.status);
        
        // Create action buttons based on order status
        let actionButtons = `<button onclick="viewOrderDetail(${order.orderId})">Detail</button>`;
        
        if (canCancel) {
            actionButtons += `<button onclick="cancelOrder(${order.orderId})" style="background-color: #dc3545; margin-left: 5px;">Cancel</button>`;
        }
        
        actionButtons += `<button onclick="reorderOrder(${order.orderId})" style="margin-left: 5px;">Re-Order</button>`;
        
        row.innerHTML = `
            <td>${order.orderId}</td>
            <td>${formatDate(order.orderDate)}</td>
            <td>${order.itemCount}</td>
            <td>${formatCurrency(order.total)}</td>
            <td>${paymentMethodMap[order.paymentMethod] || order.paymentMethod}</td>
            <td>${statusMap[order.status] || order.status}</td>
            <td style="display: flex; gap: 0px; align-items: center;">
                ${actionButtons}
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Update pagination display and controls
function updatePagination() {
    const { currentPage, totalPages, totalElements, pageSize } = currentState.pagination;
    
    document.getElementById('current-page').value = currentPage + 1; // Display 1-based page numbers
    document.getElementById('current-page').max = totalPages;
    document.getElementById('total-pages').textContent = totalPages;
    
    // Update page size selector
    document.getElementById('page-size').value = pageSize;
    
    // Enable/disable navigation buttons
    const prevBtn = document.querySelector('button[onclick="prevPage()"]');
    const nextBtn = document.querySelector('button[onclick="nextPage()"]');
    
    if (prevBtn) prevBtn.disabled = currentPage === 0;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages - 1;
}

// Navigation functions
function prevPage() {
    if (currentState.pagination.currentPage > 0) {
        currentState.pagination.currentPage--;
        loadOrders();
    }
}

function nextPage() {
    if (currentState.pagination.currentPage < currentState.pagination.totalPages - 1) {
        currentState.pagination.currentPage++;
        loadOrders();
    }
}

function goToPage(page) {
    const pageNumber = parseInt(page) - 1; // Convert to 0-based
    if (pageNumber >= 0 && pageNumber < currentState.pagination.totalPages) {
        currentState.pagination.currentPage = pageNumber;
        loadOrders();
    } else {
        alert('Invalid page number');
        document.getElementById('current-page').value = currentState.pagination.currentPage + 1;
    }
}

function changePageSize(size) {
    currentState.pagination.pageSize = parseInt(size);
    currentState.pagination.currentPage = 0; // Reset to first page
    loadOrders();
}

// Filter functions
function applyFilters() {
    // Get filter values
    currentState.filters.orderId = document.getElementById('order-id').value.trim();
    currentState.filters.startDate = document.getElementById('from-date').value;
    currentState.filters.endDate = document.getElementById('to-date').value;
    currentState.filters.paymentMethod = document.getElementById('payment-method').value;
    currentState.filters.status = document.getElementById('order-status').value;
    
    // Convert dates to ISO format if provided
    if (currentState.filters.startDate) {
        currentState.filters.startDate = new Date(currentState.filters.startDate).toISOString();
    }
    if (currentState.filters.endDate) {
        // Set end date to end of day
        const endDate = new Date(currentState.filters.endDate);
        endDate.setHours(23, 59, 59, 999);
        currentState.filters.endDate = endDate.toISOString();
    }
    
    // Reset to first page when applying filters
    currentState.pagination.currentPage = 0;
    loadOrders();
}

function clearFilters() {
    // Clear form inputs
    document.getElementById('order-id').value = '';
    document.getElementById('from-date').value = '';
    document.getElementById('to-date').value = '';
    document.getElementById('payment-method').value = '';
    document.getElementById('order-status').value = '';
    
    // Clear state filters
    currentState.filters = {
        orderId: '',
        startDate: '',
        endDate: '',
        paymentMethod: '',
        status: ''
    };
    
    // Reset to first page
    currentState.pagination.currentPage = 0;
    loadOrders();
}

// Action functions
function viewOrderDetail(orderId) {
    window.location.href = `order_detail.html?orderId=${orderId}`;
}

async function cancelOrder(orderId) {
    // Show confirmation dialog
    const confirmCancel = confirm('Are you sure you want to cancel this order? This action cannot be undone.');
    if (!confirmCancel) {
        return;
    }

    try {
        const token = getAuthToken();
        if (!token) {
            alert('Please login to cancel order');
            return;
        }

        const response = await fetch(`${API_BASE_URL}/order/${orderId}/cancel`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to cancel order');
        }

        const data = await response.json();
        if (data.code === 1000) {
            const result = data.result;
            let message = `${result.status}\n`;
            if (result.message) {
                message += `${result.message}\n`;
            }
            if (result.refundPercentage) {
                message += `Refund: ${result.refundPercentage}`;
            }
            
            alert(message);
            // Reload orders to update the status
            loadOrders();
        } else {
            throw new Error(data.message || 'Failed to cancel order');
        }
    } catch (error) {
        console.error('Cancel order error:', error);
        alert(error.message || 'Failed to cancel order. Please try again.');
    }
}

async function reorderOrder(orderId) {
    try {
        const token = getAuthToken();
        if (!token) {
            alert('Please login to reorder');
            return;
        }

        const response = await fetch(`${API_BASE_URL}/order/${orderId}/reorder`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to reorder');
        }

        const data = await response.json();
        if (data.code === 1000) {
            alert(data.result.status || 'Items added to cart successfully!');
            // Redirect to checkout page
            window.location.href = '../checkout/checkout.html';
        } else {
            throw new Error('Reorder failed');
        }
    } catch (error) {
        console.error('Reorder error:', error);
        alert(error.message || 'Failed to reorder. Please try again.');
    }
}

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    // Set default page size
    document.getElementById('page-size').value = currentState.pagination.pageSize;
    
    // Load initial orders
    loadOrders();
});
