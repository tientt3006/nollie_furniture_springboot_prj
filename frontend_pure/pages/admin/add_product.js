// API base URL - replace with your actual API base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Global variables
let categories = [];
let options = [];
let optionCounter = 0;

// Cache DOM elements
const form = document.getElementById('addProductForm');
const productCategory = document.getElementById('productCategory');
const baseProductImage = document.getElementById('baseProductImage');
const baseImagePreview = document.getElementById('baseImagePreview');
const otherProductImages = document.getElementById('otherProductImages');
const otherImagesPreview = document.getElementById('otherImagesPreview');
const addOptionBtn = document.getElementById('addOptionBtn');
const optionsContainer = document.getElementById('optionsContainer');
const productDetailsModal = document.getElementById('product-details-modal');
const productDetailsContent = document.getElementById('product-details-content');
const detailsCloseBtn = document.getElementById('details-close-btn');
const addAnotherBtn = document.getElementById('add-another-btn');
const goToProductsBtn = document.getElementById('go-to-products-btn');

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    loadCategories();
    loadOptions();
    setupEventListeners();
});

// Load categories from API
async function loadCategories() {
    try {
        const response = await fetch(`${API_BASE_URL}/category`);
        const data = await response.json();
        
        if (data.code === 1000) {
            categories = data.result;
            populateCategoryDropdown();
        } else {
            console.error('Failed to fetch categories:', data);
            showToast('Failed to load categories');
        }
    } catch (error) {
        console.error('Error fetching categories:', error);
        showToast('An error occurred while loading categories');
    }
}

// Load options from API
async function loadOptions() {
    try {
        const response = await fetch(`${API_BASE_URL}/option/all`);
        const data = await response.json();
        
        if (data.code === 1000) {
            options = data.result;
        } else {
            console.error('Failed to fetch options:', data);
            showToast('Failed to load options');
        }
    } catch (error) {
        console.error('Error fetching options:', error);
        showToast('An error occurred while loading options');
    }
}

// Populate category dropdown with fetched categories
function populateCategoryDropdown() {
    productCategory.innerHTML = '<option value="" disabled selected>Select a category</option>';
    
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = category.name;
        productCategory.appendChild(option);
    });
}

// Setup all event listeners
function setupEventListeners() {
    // Form submission
    form.addEventListener('submit', handleFormSubmit);
    
    // Add option button
    addOptionBtn.addEventListener('click', addOption);
    
    // Image preview for base image
    baseProductImage.addEventListener('change', function() {
        previewImage(this, baseImagePreview);
    });
    
    // Image preview for other images
    otherProductImages.addEventListener('change', function() {
        previewMultipleImages(this, otherImagesPreview);
    });
    
    // Modal close button
    detailsCloseBtn.addEventListener('click', closeProductDetailsModal);
    
    // Add another product button
    addAnotherBtn.addEventListener('click', () => {
        closeProductDetailsModal();
        form.reset();
        clearImagePreviews();
        clearOptions();
    });
    
    // Go to products button
    goToProductsBtn.addEventListener('click', () => {
        window.location.href = 'product_management.html';
    });
}

// Preview a single image
function previewImage(input, previewContainer) {
    previewContainer.innerHTML = '';
    
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        
        reader.onload = function(e) {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'preview-image';
            previewContainer.appendChild(img);
        }
        
        reader.readAsDataURL(input.files[0]);
    }
}

// Preview multiple images
function previewMultipleImages(input, previewContainer) {
    previewContainer.innerHTML = '';
    
    if (input.files) {
        Array.from(input.files).forEach(file => {
            const reader = new FileReader();
            
            reader.onload = function(e) {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.className = 'preview-image';
                previewContainer.appendChild(img);
            }
            
            reader.readAsDataURL(file);
        });
    }
}

// Clear all image previews
function clearImagePreviews() {
    baseImagePreview.innerHTML = '';
    otherImagesPreview.innerHTML = '';
}

// Clear all options
function clearOptions() {
    optionsContainer.innerHTML = '';
    optionCounter = 0;
}

// Add a new option
function addOption() {
    // Create a select element with all available options
    let optionSelectHtml = '<option value="" disabled selected>Select an option</option>';
    options.forEach(option => {
        optionSelectHtml += `<option value="${option.id}">${option.name}</option>`;
    });
    
    const optionId = 'option_' + optionCounter++;
    
    const optionHtml = `
        <div class="option-container" id="${optionId}">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div class="col-md-6">
                    <label class="form-label">Option</label>
                    <select class="form-select option-select" onchange="loadOptionValues(this, '${optionId}')">
                        ${optionSelectHtml}
                    </select>
                </div>
                <button type="button" class="btn btn-sm btn-outline-danger" onclick="removeOption('${optionId}')">
                    Remove Option
                </button>
            </div>
            <div class="option-values-container" id="${optionId}_values">
                <!-- Option values will be added dynamically -->
            </div>
            <button type="button" class="btn btn-sm btn-outline-secondary mt-2 add-value-btn" style="display:none;" 
                id="${optionId}_addValueBtn" onclick="addOptionValue('${optionId}')">
                Add Option Value
            </button>
        </div>
    `;
    
    optionsContainer.insertAdjacentHTML('beforeend', optionHtml);
}

// Remove an option
function removeOption(optionId) {
    document.getElementById(optionId).remove();
}

// Load option values based on selected option
function loadOptionValues(selectElement, optionId) {
    const optionValueId = parseInt(selectElement.value);
    const option = options.find(opt => opt.id === optionValueId);
    
    if (option) {
        // Show add value button
        document.getElementById(`${optionId}_addValueBtn`).style.display = 'block';
        
        // Store option values in the select element for future reference
        selectElement.setAttribute('data-values', JSON.stringify(option.values));
    }
}

// Add an option value for a specific option
function addOptionValue(optionId) {
    const optionSelect = document.querySelector(`#${optionId} .option-select`);
    const optionValueId = parseInt(optionSelect.value);
    const optionValuesContainer = document.getElementById(`${optionId}_values`);
    
    // Get stored values
    const optionValues = JSON.parse(optionSelect.getAttribute('data-values') || '[]');
    
    if (optionValues.length === 0) {
        showToast('No option values available');
        return;
    }
    
    // Generate unique ID for this option value
    const valueInstanceId = `${optionId}_value_${Date.now()}`;
    
    // Create options for value dropdown
    let valueOptionsHtml = '<option value="" disabled selected>Select a value</option>';
    optionValues.forEach(value => {
        valueOptionsHtml += `<option value="${value.id}">${value.name}</option>`;
    });
    
    const optionValueHtml = `
        <div class="option-value-container" id="${valueInstanceId}">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h6 class="card-subtitle mb-0">Option Value</h6>
                <button type="button" class="btn btn-sm btn-outline-danger" onclick="removeOptionValue('${valueInstanceId}')">
                    Remove Value
                </button>
            </div>
            <div class="row">
                <div class="col-md-6 mb-3">
                    <label class="form-label">Select Value</label>
                    <select class="form-select option-value-select" name="optionValueId">
                        ${valueOptionsHtml}
                    </select>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label">Quantity</label>
                    <input type="number" class="form-control" name="quantity" required>
                </div>
            </div>
            <div class="row">
                <div class="col-md-6 mb-3">
                    <label class="form-label">Additional Price</label>
                    <div class="input-group">
                        <span class="input-group-text">$</span>
                        <input type="number" step="0.01" class="form-control" name="addPrice" required>
                    </div>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label">Option Value Image</label>
                    <input type="file" class="form-control" name="productOptionValueImage" accept="image/*">
                </div>
            </div>
        </div>
    `;
    
    optionValuesContainer.insertAdjacentHTML('beforeend', optionValueHtml);
}

// Remove an option value
function removeOptionValue(valueInstanceId) {
    document.getElementById(valueInstanceId).remove();
}

// Handle form submission
async function handleFormSubmit(event) {
    event.preventDefault();
    
    // Create FormData object to handle file uploads
    const formData = new FormData();
    
    // Add basic product information
    formData.append('name', document.getElementById('productName').value);
    formData.append('basePrice', document.getElementById('basePrice').value);
    formData.append('baseProductQuantity', document.getElementById('baseProductQuantity').value);
    formData.append('categoryId', document.getElementById('productCategory').value);
    
    // Add optional fields if they have values
    const width = document.getElementById('width').value;
    if (width) formData.append('width', width);
    
    const length = document.getElementById('length').value;
    if (length) formData.append('length', length);
    
    const height = document.getElementById('height').value;
    if (height) formData.append('height', height);
    
    const description = document.getElementById('description').value;
    if (description) formData.append('description', description);
    
    // Add base product image
    const baseImageFile = document.getElementById('baseProductImage').files[0];
    if (baseImageFile) {
        formData.append('baseProductImage', baseImageFile);
    }
    
    // Add other product images
    const otherImages = document.getElementById('otherProductImages').files;
    if (otherImages.length > 0) {
        for (let i = 0; i < otherImages.length; i++) {
            formData.append('otherProductImages', otherImages[i]);
        }
    }
    
    // Process options and option values
    const optionContainers = document.querySelectorAll('.option-container');
    
    optionContainers.forEach((optionContainer, optionIndex) => {
        const optionId = optionContainer.querySelector('.option-select').value;
        
        // Only process options that have a selected option ID
        if (optionId) {
            formData.append(`productOptionCreateRequestList[${optionIndex}].optionId`, optionId);
            
            // Process all option values for this option
            const optionValueContainers = optionContainer.querySelectorAll('.option-value-container');
            
            optionValueContainers.forEach((valueContainer, valueIndex) => {
                const valueId = valueContainer.querySelector('.option-value-select').value;
                const quantity = valueContainer.querySelector('input[name="quantity"]').value;
                const addPrice = valueContainer.querySelector('input[name="addPrice"]').value;
                const valueImage = valueContainer.querySelector('input[name="productOptionValueImage"]').files[0];
                
                // Only process option values that have a selected value ID
                if (valueId) {
                    formData.append(`productOptionCreateRequestList[${optionIndex}].productOptionValueCreateRequestList[${valueIndex}].optionValueId`, valueId);
                    formData.append(`productOptionCreateRequestList[${optionIndex}].productOptionValueCreateRequestList[${valueIndex}].quantity`, quantity);
                    formData.append(`productOptionCreateRequestList[${optionIndex}].productOptionValueCreateRequestList[${valueIndex}].addPrice`, addPrice);
                    
                    if (valueImage) {
                        formData.append(`productOptionCreateRequestList[${optionIndex}].productOptionValueCreateRequestList[${valueIndex}].productOptionValueImages`, valueImage);
                    }
                }
            });
        }
    });
    
    try {
        // Disable submit button to prevent multiple submissions
        document.getElementById('submitBtn').disabled = true;
        document.getElementById('submitBtn').innerHTML = 'Saving...';
        
        // Submit the form data to the API
        const response = await fetch(`${API_BASE_URL}/product/create`, {
            method: 'POST',
            body: formData,
            // Add any required authentication headers
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            }
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            // Show success message and display product details
            showProductDetails(data.result);
        } else {
            console.error('Failed to create product:', data);
            showToast('Failed to create product: ' + (data.message || 'Unknown error'));
        }
    } catch (error) {
        console.error('Error creating product:', error);
        showToast('An error occurred while creating the product');
    } finally {
        // Re-enable submit button
        document.getElementById('submitBtn').disabled = false;
        document.getElementById('submitBtn').innerHTML = 'Save Product';
    }
}

// Show product details in modal
function showProductDetails(product) {
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
        <div class="alert alert-success">
            Product has been created successfully with ID: ${product.productId}
        </div>
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

// Close product details modal
function closeProductDetailsModal() {
    productDetailsModal.style.display = 'none';
    document.body.style.overflow = 'auto';
}

// Simple toast message implementation
function showToast(message) {
    // Could implement a more sophisticated toast system here
    alert(message);
}
