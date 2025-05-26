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
        userId: '',
        search: '',
        startDate: '',
        endDate: '',
        paymentMethod: '',
        status: '',
        sortBy: 'orderDate',
        sortDirection: 'desc'
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
    return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', { 
        hour: '2-digit', 
        minute: '2-digit' 
    });
}

// Load orders with current filters and pagination
async function loadOrders() {
    try {
        const token = getAuthToken();
        if (!token) {
            alert('Please login to access admin features');
            window.location.href = '../io/signin.html';
            return;
        }

        // Build query parameters
        const queryParams = new URLSearchParams({
            page: currentState.pagination.currentPage,
            size: currentState.pagination.pageSize,
            sortBy: currentState.filters.sortBy,
            sortDirection: currentState.filters.sortDirection
        });

        // Add filters if they exist
        Object.entries(currentState.filters).forEach(([key, value]) => {
            if (value && key !== 'sortBy' && key !== 'sortDirection') {
                queryParams.append(key, value);
            }
        });

        const response = await fetch(`${API_BASE_URL}/order/admin/search?${queryParams.toString()}`, {
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
            throw new Error(data.message || 'Failed to load orders');
        }
    } catch (error) {
        console.error('Error loading orders:', error);
        alert(error.message || 'Failed to load orders');
        document.getElementById('order-table-body').innerHTML = 
            '<tr><td colspan="9" style="text-align: center;">Failed to load orders</td></tr>';
    }
}

// Render orders to the table
function renderOrders() {
    const tbody = document.getElementById('order-table-body');
    tbody.innerHTML = '';

    if (currentState.orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align: center;">No orders found</td></tr>';
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

    const statusColors = {
        'ORDER_SUCCESSFUL': 'warning',
        'ON_DELIVERY': 'info',
        'RECEIVED': 'success',
        'CANCELED': 'danger'
    };

    currentState.orders.forEach(order => {
        const row = document.createElement('tr');
        
        // Determine which action buttons to show
        const canUpdateStatus = order.status !== 'RECEIVED' && order.status !== 'CANCELED';
        const canCancel = order.status === 'ORDER_SUCCESSFUL' || order.status === 'ON_DELIVERY';
        
        let actionButtons = `<button class="btn btn-sm btn-primary me-1" onclick="viewOrderDetail(${order.orderId})">Detail</button>`;
        
        if (canUpdateStatus) {
            actionButtons += `<button class="btn btn-sm btn-warning me-1" onclick="updateOrderStatus(${order.orderId})">Update Status</button>`;
        }
        
        if (canCancel) {
            actionButtons += `<button class="btn btn-sm btn-danger" onclick="cancelOrder(${order.orderId})">Cancel</button>`;
        }
        
        row.innerHTML = `
            <td>${order.orderId}</td>
            <td>
                <div><strong>${order.fullName}</strong></div>
                <small class="text-muted">${order.email || '-'}</small>
            </td>
            <td>
                <div>${order.phone}</div>
                <small class="text-muted">${order.address}</small>
            </td>
            <td>${formatDate(order.orderDate)}</td>
            <td>${order.itemCount}</td>
            <td>${formatCurrency(order.total)}</td>
            <td>${paymentMethodMap[order.paymentMethod] || order.paymentMethod}</td>
            <td><span class="badge bg-${statusColors[order.status]}">${statusMap[order.status] || order.status}</span></td>
            <td>${actionButtons}</td>
        `;
        tbody.appendChild(row);
    });
}

// Update pagination display and controls
function updatePagination() {
    const { currentPage, totalPages, totalElements, pageSize } = currentState.pagination;
    
    const start = currentPage * pageSize + 1;
    const end = Math.min((currentPage + 1) * pageSize, totalElements);
    
    document.getElementById('pagination-info').textContent = 
        `Showing ${totalElements > 0 ? start : 0}-${end} of ${totalElements}`;
    document.getElementById('page-input').value = currentPage + 1;
    document.getElementById('page-input').max = totalPages;
    document.getElementById('total-pages').textContent = totalPages;
    
    document.getElementById('page-size').value = pageSize;
    
    const prevBtn = document.getElementById('prev-page');
    const nextBtn = document.getElementById('next-page');
    
    prevBtn.disabled = currentPage === 0;
    nextBtn.disabled = currentPage >= totalPages - 1;
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
    const pageNumber = parseInt(page) - 1;
    if (pageNumber >= 0 && pageNumber < currentState.pagination.totalPages) {
        currentState.pagination.currentPage = pageNumber;
        loadOrders();
    } else {
        alert('Invalid page number');
        document.getElementById('page-input').value = currentState.pagination.currentPage + 1;
    }
}

function changePageSize(size) {
    currentState.pagination.pageSize = parseInt(size);
    currentState.pagination.currentPage = 0;
    loadOrders();
}

// Filter functions
function applyFilters() {
    currentState.filters.orderId = document.getElementById('filter-order-id').value.trim();
    currentState.filters.userId = document.getElementById('filter-user-id').value.trim();
    currentState.filters.search = document.getElementById('filter-search').value.trim();
    currentState.filters.paymentMethod = document.getElementById('filter-payment-method').value;
    currentState.filters.status = document.getElementById('filter-status').value;
    currentState.filters.sortBy = document.getElementById('sort-by').value;
    currentState.filters.sortDirection = document.getElementById('sort-direction').value;
    
    const startDate = document.getElementById('filter-start-date').value;
    const endDate = document.getElementById('filter-end-date').value;
    
    if (startDate) {
        currentState.filters.startDate = new Date(startDate).toISOString();
    } else {
        currentState.filters.startDate = '';
    }
    
    if (endDate) {
        const endDateTime = new Date(endDate);
        endDateTime.setHours(23, 59, 59, 999);
        currentState.filters.endDate = endDateTime.toISOString();
    } else {
        currentState.filters.endDate = '';
    }
    
    currentState.pagination.currentPage = 0;
    loadOrders();
}

function clearFilters() {
    document.getElementById('filter-order-id').value = '';
    document.getElementById('filter-user-id').value = '';
    document.getElementById('filter-search').value = '';
    document.getElementById('filter-start-date').value = '';
    document.getElementById('filter-end-date').value = '';
    document.getElementById('filter-payment-method').value = '';
    document.getElementById('filter-status').value = '';
    document.getElementById('sort-by').value = 'orderDate';
    document.getElementById('sort-direction').value = 'desc';
    
    currentState.filters = {
        orderId: '',
        userId: '',
        search: '',
        startDate: '',
        endDate: '',
        paymentMethod: '',
        status: '',
        sortBy: 'orderDate',
        sortDirection: 'desc'
    };
    
    currentState.pagination.currentPage = 0;
    loadOrders();
}

// Action functions
async function viewOrderDetail(orderId) {
    try {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/order/admin/${orderId}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load order details');
        }

        const data = await response.json();
        if (data.code === 1000) {
            const order = data.result;
            displayOrderDetailModal(order);
        } else {
            throw new Error(data.message || 'Failed to load order details');
        }
    } catch (error) {
        console.error('Error loading order details:', error);
        alert(error.message || 'Failed to load order details');
    }
}

function displayOrderDetailModal(order) {
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

    let itemsHtml = '';
    order.items.forEach(item => {
        itemsHtml += `
            <div class="row border-bottom pb-2 mb-2">
                <div class="col-2">
                    <img src="${item.productImageUrl}" alt="${item.productName}" class="img-fluid" style="max-height: 80px;">
                </div>
                <div class="col-6">
                    <h6>${item.productName}</h6>
                    <small>${item.optionName}: ${item.optionValueName}</small>
                </div>
                <div class="col-2 text-center">
                    <strong>${item.quantity}</strong>
                </div>
                <div class="col-2 text-end">
                    <div>${formatCurrency(item.itemPrice)}</div>
                    <strong>${formatCurrency(item.totalPrice)}</strong>
                </div>
            </div>
        `;
    });

    const content = `
        <div class="row">
            <div class="col-md-6">
                <h6>Order Information</h6>
                <p><strong>Order ID:</strong> ${order.orderId}</p>
                <p><strong>Status:</strong> <span class="badge bg-info">${statusMap[order.status]}</span></p>
                <p><strong>Order Date:</strong> ${formatDate(order.orderDate)}</p>
                ${order.startDeliveryDate ? `<p><strong>Delivery Start:</strong> ${formatDate(order.startDeliveryDate)}</p>` : ''}
                ${order.receiveDate ? `<p><strong>Received:</strong> ${formatDate(order.receiveDate)}</p>` : ''}
                ${order.cancelDate ? `<p><strong>Canceled:</strong> ${formatDate(order.cancelDate)}</p>` : ''}
                <p><strong>Payment:</strong> ${paymentMethodMap[order.paymentMethod]}</p>
                <p><strong>Total:</strong> ${formatCurrency(order.total)}</p>
                ${order.refund ? `<p><strong>Refund:</strong> ${(order.refund * 100).toFixed(0)}%</p>` : ''}
            </div>
            <div class="col-md-6">
                <h6>Customer Information</h6>
                <p><strong>Name:</strong> ${order.fullName}</p>
                <p><strong>Email:</strong> ${order.email}</p>
                <p><strong>Phone:</strong> ${order.phone}</p>
                <p><strong>Address:</strong> ${order.address}</p>
                ${order.statusDetail ? `<p><strong>Status Detail:</strong> ${order.statusDetail}</p>` : ''}
            </div>
        </div>
        <hr>
        <h6>Order Items</h6>
        <div class="row fw-bold border-bottom pb-1 mb-2">
            <div class="col-2">Image</div>
            <div class="col-6">Product</div>
            <div class="col-2 text-center">Qty</div>
            <div class="col-2 text-end">Price</div>
        </div>
        ${itemsHtml}
    `;

    document.getElementById('order-detail-content').innerHTML = content;
    const modal = new bootstrap.Modal(document.getElementById('order-detail-modal'));
    modal.show();
}

async function updateOrderStatus(orderId) {
    if (!confirm('Are you sure you want to advance this order to the next status?')) {
        return;
    }

    try {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/order/admin/${orderId}/advance-status`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to update order status');
        }

        const data = await response.json();
        if (data.code === 1000) {
            const result = data.result;
            let message = result.message;
            
            if (result.status === 'success') {
                message += `\nNew Status: ${result.newStatus}`;
                if (result.startDeliveryDate) {
                    message += `\nDelivery Start: ${formatDate(result.startDeliveryDate)}`;
                }
                if (result.receiveDate) {
                    message += `\nReceive Date: ${formatDate(result.receiveDate)}`;
                }
            }
            
            alert(message);
            loadOrders(); // Reload to update the table
        } else {
            throw new Error(data.message || 'Failed to update order status');
        }
    } catch (error) {
        console.error('Update status error:', error);
        alert(error.message || 'Failed to update order status');
    }
}

async function cancelOrder(orderId) {
    if (!confirm('Are you sure you want to cancel this order? This action cannot be undone.')) {
        return;
    }

    try {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/order/admin/${orderId}/cancel`, {
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
            let message = result.message;
            
            if (result.refund) {
                message += `\nRefund: ${result.refund}`;
            }
            if (result.cancelDate) {
                message += `\nCancel Date: ${formatDate(result.cancelDate)}`;
            }
            if (result.statusDetail) {
                message += `\nDetail: ${result.statusDetail}`;
            }
            
            alert(message);
            loadOrders(); // Reload to update the table
        } else {
            throw new Error(data.message || 'Failed to cancel order');
        }
    } catch (error) {
        console.error('Cancel order error:', error);
        alert(error.message || 'Failed to cancel order');
    }
}

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    // Set up event listeners
    document.getElementById('search-button').addEventListener('click', applyFilters);
    document.getElementById('reset-filter').addEventListener('click', clearFilters);
    document.getElementById('page-size').addEventListener('change', (e) => changePageSize(e.target.value));
    document.getElementById('prev-page').addEventListener('click', prevPage);
    document.getElementById('next-page').addEventListener('click', nextPage);
    document.getElementById('page-input').addEventListener('change', (e) => goToPage(e.target.value));
    
    // Load initial orders
    loadOrders();
});
