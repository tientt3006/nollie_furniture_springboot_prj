// API configuration
const API_BASE_URL = 'http://localhost:8080/api';

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

// Get order ID from URL
function getOrderIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('orderId');
}

// Load order details
async function loadOrderDetails() {
    try {
        const orderId = getOrderIdFromUrl();
        if (!orderId) {
            throw new Error('Order ID not found in URL');
        }

        const token = getAuthToken();
        if (!token) {
            throw new Error('Please login to view order details');
        }

        const response = await fetch(`${API_BASE_URL}/order/${orderId}`, {
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
            displayOrderDetails(data.result);
        } else {
            throw new Error('Failed to load order details');
        }
    } catch (error) {
        console.error('Error loading order:', error);
        alert(error.message || 'Failed to load order details');
    }
}

// Display order details
function displayOrderDetails(orderData) {
    // Update order ID in title
    const titleElement = document.querySelector('h1');
    if (titleElement) {
        titleElement.innerHTML = `Order Detail <span>ID: ${orderData.orderId}</span>`;
    }

    // Display order items
    const cartItemsContainer = document.querySelector('.cart-items');
    if (cartItemsContainer && orderData.items) {
        const itemsHTML = orderData.items.map(item => `
            <div class="cart-item">
                <div class="cart-image">
                    <img src="${item.productImageUrl || '../../images/cart1.png'}" alt="${item.productName}">
                </div>
                <div class="item-info">
                    <div class="item-info-row1">
                        <div>
                            <h2><b>${item.productName}</b></h2>
                            <p>${item.productName}</p>
                            <br>
                            <span style="color: gray; font-size: 12px">Unit price: </span>
                            <p class="price"><b>${formatCurrency(item.itemPrice)}</b></p>
                        </div>
                        <div class="item-actions">
                            <input class="quantity" type="number" value="${item.quantity}" min="1" max="50" disabled>
                        </div>
                    </div>
                    <hr>
                    <div class="item-info-row2">
                        <div>
                            <p><span style="color: gray;">${item.optionName}: </span> ${item.optionValueName}</p>
                            <p><span style="color: gray;">Total: </span> ${formatCurrency(item.totalPrice)}</p>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
        
        cartItemsContainer.innerHTML = itemsHTML;
    }

    // Display order summary
    const orderSummaryContainer = document.querySelector('.order-summary');
    if (orderSummaryContainer) {
        const statusMap = {
            'ORDER_SUCCESSFUL': 'Order Success',
            'ON_THE_WAY': 'On the way',
            'ORDER_FULFILLED': 'Order Fulfilled',
            'CANCELED': 'Canceled'
        };

        const paymentMethodMap = {
            'CASH_ON_DELIVERY': 'COD',
            'BANK_TRANSFER': 'Bank Transfer'
        };

        const summaryHTML = `
            <h2><b>Order summary</b></h2>
            <p>Full Name <span>${orderData.fullName || '-'}</span></p>
            <p>Address <span>${orderData.address || '-'}</span></p>
            <p>Phone <span>${orderData.phone || '-'}</span></p>
            <p>Email <span>${orderData.email || '-'}</span></p>
            <p>Status <span>${statusMap[orderData.status] || orderData.status}</span></p>
            <p>Date order <span>${formatDate(orderData.orderDate)}</span></p>
            <p>Date Fulfilled <span>${formatDate(orderData.receiveDate)}</span></p>
            <p>Status Detail <span>${orderData.statusDetail || '-'}</span></p>
            <p>Payment <span>${paymentMethodMap[orderData.paymentMethod] || orderData.paymentMethod}</span></p>
            <p>Refund <span>${orderData.refund ? formatCurrency(orderData.refund) : '-'}</span></p>
            
            <hr>
            <p class="total">Total price <span>${formatCurrency(orderData.total)}</span></p>
            <p>Incl. tax</p>
            <button class="checkout reorder" onclick="handleReorder()">Re Order</button>
        `;
        
        orderSummaryContainer.innerHTML = summaryHTML;
    }
}

// Handle reorder
async function handleReorder() {
    try {
        const orderId = getOrderIdFromUrl();
        const token = getAuthToken();
        
        if (!orderId || !token) {
            throw new Error('Order ID or authentication token not found');
        }

        const reorderBtn = document.querySelector('.reorder');
        const originalText = reorderBtn.textContent;
        
        // Disable button and show loading state
        reorderBtn.disabled = true;
        reorderBtn.textContent = 'Processing...';

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
        
        // Re-enable button
        const reorderBtn = document.querySelector('.reorder');
        if (reorderBtn) {
            reorderBtn.disabled = false;
            reorderBtn.textContent = 'Re Order';
        }
    }
}

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    loadOrderDetails();
});
