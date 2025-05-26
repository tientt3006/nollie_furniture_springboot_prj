// API base URL - replace with your actual API base URL
const API_BASE_URL = 'http://localhost:8080/api';

// State management for products and filters
let state = {
    products: [],
    categories: [],
    options: [],
    pagination: {
        currentPage: 1,
        pageSize: 10,
        totalItems: 0,
        totalPages: 0
    },
    filters: {
        searchId: '',
        searchName: '',
        categoryId: '',
        optionFilters: {},
        sortBy: 'id',
        sortOrder: 'desc'
    }
};

// DOM elements
const productTableBody = document.getElementById('product-table-body');
const paginationInfo = document.getElementById('pagination-info');
const pageDisplay = document.getElementById('page-display');
const loadingSpinner = document.getElementById('loading-spinner');
const searchIdInput = document.getElementById('search-id-input');
const searchInput = document.getElementById('search-input');
const categoryFilter = document.getElementById('category-filter');
const sortBySelect = document.getElementById('sort-by');
const sortOrderSelect = document.getElementById('sort-order');
const pageSizeSelect = document.getElementById('page-size');
const optionFiltersContainer = document.getElementById('option-filters');
const productDetailsModal = document.getElementById('product-details-modal');
const productDetailsContent = document.getElementById('product-details-content');

// Initialize the page when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    loadCategories();
    loadOptions();
    setupEventListeners();
    loadProducts();
});

// Load categories for the filter dropdown
async function loadCategories() {
    try {
        const response = await fetch(`${API_BASE_URL}/category`);
        const data = await response.json();
        
        if (data.code === 1000) {
            state.categories = data.result;
            populateCategoryFilter();
        } else {
            console.error('Failed to fetch categories:', data);
            showToast('Failed to load categories. Please try again.');
        }
    } catch (error) {
        console.error('Error fetching categories:', error);
        showToast('An error occurred while loading categories.');
    }
}

// Load options for advanced filtering
async function loadOptions() {
    try {
        const response = await fetch(`${API_BASE_URL}/option/all`);
        const data = await response.json();
        
        if (data.code === 1000) {
            state.options = data.result;
            populateOptionFilters();
        } else {
            console.error('Failed to fetch options:', data);
            showToast('Failed to load options for filtering. Please try again.');
        }
    } catch (error) {
        console.error('Error fetching options:', error);
        showToast('An error occurred while loading filter options.');
    }
}

// Populate the category filter dropdown
function populateCategoryFilter() {
    categoryFilter.innerHTML = '<option value="">All Categories</option>';
    
    state.categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.name;
        option.textContent = category.name;
        categoryFilter.appendChild(option);
    });
}

// Populate option filters for advanced filtering
function populateOptionFilters() {
    optionFiltersContainer.innerHTML = '';
    
    state.options.forEach(option => {
        const optionGroup = document.createElement('div');
        optionGroup.className = 'input-group mb-2 me-2';
        optionGroup.style.maxWidth = '300px';
        
        optionGroup.innerHTML = `
            <span class="input-group-text">${option.name}</span>
            <select class="form-select option-filter" data-option-id="${option.id}">
                <option value="">Any</option>
                ${option.values.map(value => `<option value="${value.id}">${value.name}</option>`).join('')}
            </select>
        `;
        
        optionFiltersContainer.appendChild(optionGroup);
    });
}

// Load products with current filters and pagination
async function loadProducts() {
    showLoading(true);
    
    try {
        let searchTerm = '';
        if (state.filters.searchId) {
            searchTerm = state.filters.searchId;
        } else if (state.filters.searchName) {
            searchTerm = state.filters.searchName;
        }
        
        // Build the URL with query parameters
        const queryParams = new URLSearchParams({
            page: state.pagination.currentPage,
            size: state.pagination.pageSize,
            sortBy: state.filters.sortBy,
            sortOrder: state.filters.sortOrder
        });
        
        if (state.filters.categoryId) {
            queryParams.append('category', state.filters.categoryId);
        }
        
        if (searchTerm) {
            queryParams.append('search', searchTerm);
        }
        
        // Add option filters if any are active
        for (const [optionId, valueId] of Object.entries(state.filters.optionFilters)) {
            if (valueId) {
                queryParams.append(`option${optionId}`, valueId);
            }
        }
        
        const response = await fetch(`${API_BASE_URL}/product/?${queryParams.toString()}`);
        const data = await response.json();
        
        if (data.code === 1000) {
            state.products = data.result.products;
            state.pagination.currentPage = data.result.currentPage;
            state.pagination.pageSize = data.result.pageSize;
            state.pagination.totalItems = data.result.totalItems;
            state.pagination.totalPages = data.result.totalPages;
            
            renderProducts();
            updatePagination();
        } else {
            console.error('Failed to fetch products:', data);
            showToast('Failed to load products. Please try again.');
            productTableBody.innerHTML = `<tr><td colspan="8" class="text-center">Failed to load products. Please try again.</td></tr>`;
        }
    } catch (error) {
        console.error('Error fetching products:', error);
        showToast('An error occurred while loading products.');
        productTableBody.innerHTML = `<tr><td colspan="8" class="text-center">An error occurred while loading products.</td></tr>`;
    } finally {
        showLoading(false);
    }
}

// Render products to the table
function renderProducts() {
    productTableBody.innerHTML = '';
    
    if (state.products.length === 0) {
        productTableBody.innerHTML = `<tr><td colspan="8" class="text-center">No products found.</td></tr>`;
        return;
    }
    
    state.products.forEach(product => {
        const baseImageUrl = product.baseImageUrl ? Object.values(product.baseImageUrl)[0] : '../../images/placeholder.png';
        
        const row = document.createElement('tr');
        
        // Calculate total stock
        let totalStock = product.baseProductQuantity || 0;
        if (product.productOptionResponseList) {
            product.productOptionResponseList.forEach(option => {
                option.productOptionValueResponseList.forEach(value => {
                    totalStock += value.quantity || 0;
                });
            });
        }
        
        // Extract option names
        const optionNames = product.productOptionResponseList 
            ? product.productOptionResponseList.map(option => option.optionName).join(', ')
            : 'None';
        
        row.innerHTML = `
            <td>${product.productId}</td>
            <td><img src="${baseImageUrl}" width="50" height="50" alt="${product.name}"></td>
            <td>${product.name}</td>
            <td>${product.category ? product.category.name : 'Unknown'}</td>
            <td>$${product.basePrice.toFixed(2)}</td>
            <td>${totalStock}</td>
            <td>${optionNames}</td>
            <td>
                <button class="btn btn-sm details-product-btn" data-id="${product.productId}">Details</button>
                <button class="btn btn-sm btn-outline-primary edit-product-btn">
                    <a href="./edit_product.html?id=${product.productId}" style="text-decoration: none; color: inherit;">Edit</a>
                </button>
            </td>
        `;
        
        productTableBody.appendChild(row);
    });
    
    // Add event listeners to buttons
    document.querySelectorAll('.details-product-btn').forEach(btn => {
        btn.addEventListener('click', () => showProductDetails(btn.getAttribute('data-id')));
    });
}

// Update pagination display and controls
function updatePagination() {
    const { currentPage, pageSize, totalItems, totalPages } = state.pagination;
    const start = (currentPage - 1) * pageSize + 1;
    const end = Math.min(currentPage * pageSize, totalItems);
    
    paginationInfo.textContent = `Showing ${start}-${end} of ${totalItems}`;
    pageDisplay.textContent = `Page ${currentPage} of ${totalPages}`;
    
    // Enable/disable pagination buttons
    document.getElementById('first-page').disabled = currentPage === 1;
    document.getElementById('prev-page').disabled = currentPage === 1;
    document.getElementById('next-page').disabled = currentPage === totalPages;
    document.getElementById('last-page').disabled = currentPage === totalPages;
}

// Show product details in modal
function showProductDetails(productId) {
    const product = state.products.find(p => p.productId == productId);
    
    if (!product) {
        showToast('Product not found');
        return;
    }
    
    const baseImageUrl = product.baseImageUrl ? Object.values(product.baseImageUrl)[0] : '../../images/placeholder.png';
    
    let optionsHtml = '';
    if (product.productOptionResponseList && product.productOptionResponseList.length > 0) {
        product.productOptionResponseList.forEach(option => {
            optionsHtml += `<h6>${option.optionName}</h6><ul class="list-group mb-3">`;
            
            option.productOptionValueResponseList.forEach(value => {
                const valueImgUrl = value.productOptionValueImgUrl || value.optionValueImgUrl || null;
                
                optionsHtml += `
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        ${value.optionValueName}
                        <div>
                            ${valueImgUrl ? `<img src="${valueImgUrl}" height="30" alt="${value.optionValueName}" class="me-2">` : ''}
                            <span class="badge bg-primary me-1">Qty: ${value.quantity}</span>
                            <span class="badge bg-success">+$${value.addPrice.toFixed(2)}</span>
                        </div>
                    </li>
                `;
            });
            
            optionsHtml += '</ul>';
        });
    } else {
        optionsHtml = '<p>No options available for this product.</p>';
    }
    
    // Display other images if available
    let otherImagesHtml = '';
    if (product.otherImageUrl && product.otherImageUrl.length > 0) {
        otherImagesHtml = '<div class="row mt-3">';
        product.otherImageUrl.forEach(imgObj => {
            const imgUrl = Object.values(imgObj)[0];
            otherImagesHtml += `
                <div class="col-4 mb-2">
                    <img src="${imgUrl}" class="img-thumbnail" alt="${product.name}">
                </div>
            `;
        });
        otherImagesHtml += '</div>';
    }
    
    const dimensions = `
        <p><strong>Dimensions:</strong> 
            ${product.width ? `W: ${product.width}cm` : ''} 
            ${product.length ? `L: ${product.length}cm` : ''} 
            ${product.height ? `H: ${product.height}cm` : ''}
        </p>
    `;
    
    productDetailsContent.innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <img src="${baseImageUrl}" class="product-image" alt="${product.name}">
            </div>
            <div class="col-md-6">
                <h4>${product.name}</h4>
                <p><strong>ID:</strong> ${product.productId}</p>
                <p><strong>Category:</strong> ${product.category ? product.category.name : 'Unknown'}</p>
                <p><strong>Base Price:</strong> $${product.basePrice.toFixed(2)}</p>
                <p><strong>Base Stock:</strong> ${product.baseProductQuantity || 0}</p>
                ${dimensions}
                <p><strong>Description:</strong> ${product.description || 'No description available.'}</p>
            </div>
        </div>
        
        <div class="mt-4">
            <h5>Product Options</h5>
            ${optionsHtml}
        </div>
        
        ${otherImagesHtml ? `<h5>Additional Images</h5>${otherImagesHtml}` : ''}
    `;
    
    // Show the modal
    productDetailsModal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

// Setup all event listeners
function setupEventListeners() {
    // Search button click
    document.getElementById('search-button').addEventListener('click', () => {
        state.filters.searchId = searchIdInput.value.trim();
        state.filters.searchName = searchInput.value.trim();
        state.filters.categoryId = categoryFilter.value;
        state.filters.sortBy = sortBySelect.value;
        state.filters.sortOrder = sortOrderSelect.value;
        
        // Collect option filters
        state.filters.optionFilters = {};
        document.querySelectorAll('.option-filter').forEach(select => {
            const optionId = select.getAttribute('data-option-id');
            const valueId = select.value;
            if (valueId) {
                state.filters.optionFilters[optionId] = valueId;
            }
        });
        
        state.pagination.currentPage = 1;
        loadProducts();
    });
    
    // Reset filters
    document.getElementById('reset-filters').addEventListener('click', () => {
        searchIdInput.value = '';
        searchInput.value = '';
        categoryFilter.value = '';
        sortBySelect.value = 'id';
        sortOrderSelect.value = 'desc';
        
        // Reset option filters
        document.querySelectorAll('.option-filter').forEach(select => {
            select.value = '';
        });
        
        // Clear state
        state.filters.searchId = '';
        state.filters.searchName = '';
        state.filters.categoryId = '';
        state.filters.sortBy = 'id';
        state.filters.sortOrder = 'desc';
        state.filters.optionFilters = {};
        state.pagination.currentPage = 1;
        
        loadProducts();
    });
    
    // Pagination controls
    document.getElementById('first-page').addEventListener('click', () => {
        if (state.pagination.currentPage > 1) {
            state.pagination.currentPage = 1;
            loadProducts();
        }
    });
    
    document.getElementById('prev-page').addEventListener('click', () => {
        if (state.pagination.currentPage > 1) {
            state.pagination.currentPage--;
            loadProducts();
        }
    });
    
    document.getElementById('next-page').addEventListener('click', () => {
        if (state.pagination.currentPage < state.pagination.totalPages) {
            state.pagination.currentPage++;
            loadProducts();
        }
    });
    
    document.getElementById('last-page').addEventListener('click', () => {
        if (state.pagination.currentPage < state.pagination.totalPages) {
            state.pagination.currentPage = state.pagination.totalPages;
            loadProducts();
        }
    });
    
    // Page size change
    pageSizeSelect.addEventListener('change', () => {
        state.pagination.pageSize = parseInt(pageSizeSelect.value);
        state.pagination.currentPage = 1; // Reset to first page when changing page size
        loadProducts();
    });
    
    // Details modal close buttons
    document.getElementById('details-close-btn').addEventListener('click', closeProductDetailsModal);
    document.getElementById('details-close-btn-footer').addEventListener('click', closeProductDetailsModal);
}

// Close product details modal
function closeProductDetailsModal() {
    productDetailsModal.style.display = 'none';
    document.body.style.overflow = 'auto';
}

// Show loading spinner
function showLoading(show) {
    loadingSpinner.style.display = show ? 'block' : 'none';
}

// Simple toast message implementation
function showToast(message) {
    // Could implement a more sophisticated toast system here
    alert(message);
}
