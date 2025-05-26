// API base URL - replace with your actual API base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Global variables
let productData = null;
let categories = [];
let options = [];
let imagesToDelete = [];

// DOM elements
const productLoadingDiv = document.getElementById('product-loading');
const productContentDiv = document.getElementById('product-content');
const optionsContainer = document.getElementById('optionsContainer');
const noOptionsMessage = document.getElementById('noOptionsMessage');
const baseImagePreview = document.getElementById('baseImagePreview');
const otherImagesPreview = document.getElementById('otherImagesPreview');
const imagesToDeleteDiv = document.getElementById('imagesToDelete');
const newBaseImage = document.getElementById('newBaseImage');
const newOtherImages = document.getElementById('newOtherImages');

// Get product ID from URL query parameter
const urlParams = new URLSearchParams(window.location.search);
const productId = urlParams.get('id');

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Check if product ID is provided
    if (!productId) {
        showError('Product ID is missing. Please return to product list and try again.');
        return;
    }
    
    loadCategories();
    loadOptions();
    loadProduct(productId);
    setupEventListeners();
});

// Load product data from API
async function loadProduct(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/product/${id}`);
        const data = await response.json();
        
        if (data.code === 1000) {
            productData = data.result;
            populateProductData();
            showProductContent();
        } else {
            showError('Failed to load product details: ' + (data.message || 'Unknown error'));
        }
    } catch (error) {
        console.error('Error fetching product details:', error);
        showError('An error occurred while loading product details');
    }
}

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
        }
    } catch (error) {
        console.error('Error fetching categories:', error);
    }
}

// Load options from API
async function loadOptions() {
    try {
        const response = await fetch(`${API_BASE_URL}/option/all`);
        const data = await response.json();
        
        if (data.code === 1000) {
            options = data.result;
            populateOptionsDropdown();
        } else {
            console.error('Failed to fetch options:', data);
        }
    } catch (error) {
        console.error('Error fetching options:', error);
    }
}

// Populate category dropdown with fetched categories
function populateCategoryDropdown() {
    const productCategory = document.getElementById('productCategory');
    productCategory.innerHTML = '<option value="" disabled>Select a category</option>';
    
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = category.name;
        productCategory.appendChild(option);
    });
}

// Populate options dropdown for the add option modal
function populateOptionsDropdown() {
    const newOptionSelect = document.getElementById('newOptionSelect');
    newOptionSelect.innerHTML = '<option value="" disabled selected>Select an option</option>';
    
    if (productData && productData.productOptionResponseList) {
        const existingOptionIds = productData.productOptionResponseList.map(opt => opt.optionId);
        
        options.forEach(option => {
            // Only add options that aren't already added to the product
            if (!existingOptionIds.includes(option.id)) {
                const optElement = document.createElement('option');
                optElement.value = option.id;
                optElement.textContent = option.name;
                newOptionSelect.appendChild(optElement);
            }
        });
    }
}

// Populate form with product data
function populateProductData() {
    // Set product ID and basic information
    document.getElementById('productId').textContent = productData.productId;
    document.getElementById('productName').value = productData.name;
    document.getElementById('productCategory').value = productData.category ? productData.category.id : '';
    document.getElementById('basePrice').value = productData.basePrice;
    document.getElementById('baseProductQuantity').value = productData.baseProductQuantity;
    document.getElementById('width').value = productData.width || '';
    document.getElementById('length').value = productData.length || '';
    document.getElementById('height').value = productData.height || '';
    document.getElementById('description').value = productData.description || '';
    
    // Display base image
    if (productData.baseImageUrl) {
        const baseImgId = Object.keys(productData.baseImageUrl)[0];
        const baseImgUrl = productData.baseImageUrl[baseImgId];
        
        document.getElementById('baseImageId').value = baseImgId;
        
        const imgElement = document.createElement('img');
        imgElement.src = baseImgUrl;
        imgElement.alt = 'Base Product Image';
        imgElement.className = 'preview-image';
        imgElement.style.maxWidth = '200px';
        imgElement.style.maxHeight = '200px';
        
        baseImagePreview.innerHTML = '';
        baseImagePreview.appendChild(imgElement);
    }
    
    // Display other images
    if (productData.otherImageUrl && productData.otherImageUrl.length > 0) {
        otherImagesPreview.innerHTML = '';
        
        productData.otherImageUrl.forEach(imageObj => {
            const imgId = Object.keys(imageObj)[0];
            const imgUrl = imageObj[imgId];
            
            const imageContainer = document.createElement('div');
            imageContainer.className = 'image-item';
            imageContainer.setAttribute('data-image-id', imgId);
            
            const imgElement = document.createElement('img');
            imgElement.src = imgUrl;
            imgElement.alt = 'Product Image';
            imgElement.className = 'preview-image';
            
            const deleteBtn = document.createElement('span');
            deleteBtn.className = 'delete-image';
            deleteBtn.innerHTML = '&times;';
            deleteBtn.addEventListener('click', () => markImageForDeletion(imgId, imageContainer));
            
            imageContainer.appendChild(imgElement);
            imageContainer.appendChild(deleteBtn);
            otherImagesPreview.appendChild(imageContainer);
        });
    }
    
    // Display product options and their values
    displayProductOptions();
}

// Display product options and their values
function displayProductOptions() {
    // Clear options container
    optionsContainer.innerHTML = '';
    
    if (!productData.productOptionResponseList || productData.productOptionResponseList.length === 0) {
        noOptionsMessage.style.display = 'block';
        return;
    }
    
    noOptionsMessage.style.display = 'none';
    
    // Iterate through each option
    productData.productOptionResponseList.forEach(option => {
        const optionDiv = document.createElement('div');
        optionDiv.className = 'option-container';
        optionDiv.id = `option-${option.productOptionId}`;
        
        // Create option header with delete button
        const optionHeader = document.createElement('div');
        optionHeader.className = 'd-flex justify-content-between align-items-center mb-3';
        
        const optionTitle = document.createElement('h5');
        optionTitle.textContent = option.optionName;
        
        const optionActions = document.createElement('div');
        
        const addValueBtn = document.createElement('button');
        addValueBtn.type = 'button';
        addValueBtn.className = 'btn btn-sm btn-outline-success me-2';
        addValueBtn.textContent = 'Add Value';
        addValueBtn.addEventListener('click', () => openAddValueModal(option.optionId, option.productOptionId));
        
        const deleteOptionBtn = document.createElement('button');
        deleteOptionBtn.type = 'button';
        deleteOptionBtn.className = 'btn btn-sm btn-outline-danger';
        deleteOptionBtn.textContent = 'Delete Option';
        deleteOptionBtn.addEventListener('click', () => deleteOption(option.productOptionId));
        
        optionActions.appendChild(addValueBtn);
        optionActions.appendChild(deleteOptionBtn);
        
        optionHeader.appendChild(optionTitle);
        optionHeader.appendChild(optionActions);
        
        optionDiv.appendChild(optionHeader);
        
        // Create container for option values
        const valuesContainer = document.createElement('div');
        valuesContainer.className = 'option-values-container';
        
        // Add each option value
        if (option.productOptionValueResponseList && option.productOptionValueResponseList.length > 0) {
            option.productOptionValueResponseList.forEach(value => {
                const valueDiv = createOptionValueElement(value);
                valuesContainer.appendChild(valueDiv);
            });
        } else {
            const noValuesMsg = document.createElement('p');
            noValuesMsg.className = 'text-muted';
            noValuesMsg.textContent = 'No values added to this option.';
            valuesContainer.appendChild(noValuesMsg);
        }
        
        optionDiv.appendChild(valuesContainer);
        optionsContainer.appendChild(optionDiv);
    });
}

// Create an option value element
function createOptionValueElement(value) {
    const valueDiv = document.createElement('div');
    valueDiv.className = 'option-value-container';
    valueDiv.id = `value-${value.productOptionValueId}`;
    
    // Create value header with delete button
    const valueHeader = document.createElement('div');
    valueHeader.className = 'd-flex justify-content-between align-items-center mb-3';
    
    const valueTitle = document.createElement('h6');
    valueTitle.textContent = value.optionValueName;
    
    const deleteValueBtn = document.createElement('button');
    deleteValueBtn.type = 'button';
    deleteValueBtn.className = 'btn btn-sm btn-outline-danger';
    deleteValueBtn.textContent = 'Delete Value';
    deleteValueBtn.addEventListener('click', () => deleteOptionValue(value.productOptionValueId));
    
    valueHeader.appendChild(valueTitle);
    valueHeader.appendChild(deleteValueBtn);
    
    // Create form for editing value details
    const valueForm = document.createElement('form');
    valueForm.className = 'row g-3';
    valueForm.id = `form-value-${value.productOptionValueId}`;
    
    // Quantity field
    const quantityDiv = document.createElement('div');
    quantityDiv.className = 'col-md-6';
    
    const quantityLabel = document.createElement('label');
    quantityLabel.className = 'form-label';
    quantityLabel.textContent = 'Quantity';
    
    const quantityInput = document.createElement('input');
    quantityInput.type = 'number';
    quantityInput.className = 'form-control';
    quantityInput.name = 'quantity';
    quantityInput.value = value.quantity || 0;
    quantityInput.min = '0';
    quantityInput.required = true;
    
    quantityDiv.appendChild(quantityLabel);
    quantityDiv.appendChild(quantityInput);
    
    // Additional price field
    const priceDiv = document.createElement('div');
    priceDiv.className = 'col-md-6';
    
    const priceLabel = document.createElement('label');
    priceLabel.className = 'form-label';
    priceLabel.textContent = 'Additional Price';
    
    const priceInputGroup = document.createElement('div');
    priceInputGroup.className = 'input-group';
    
    const priceSpan = document.createElement('span');
    priceSpan.className = 'input-group-text';
    priceSpan.textContent = '$';
    
    const priceInput = document.createElement('input');
    priceInput.type = 'number';
    priceInput.className = 'form-control';
    priceInput.name = 'addPrice';
    priceInput.value = value.addPrice || 0;
    priceInput.step = '0.01';
    priceInput.min = '0';
    priceInput.required = true;
    
    priceInputGroup.appendChild(priceSpan);
    priceInputGroup.appendChild(priceInput);
    priceDiv.appendChild(priceLabel);
    priceDiv.appendChild(priceInputGroup);
    
    // Image field
    const imageDiv = document.createElement('div');
    imageDiv.className = 'col-12';
    
    const imageFieldset = document.createElement('fieldset');
    
    const imageLegend = document.createElement('legend');
    imageLegend.className = 'form-label';
    imageLegend.textContent = 'Option Value Image';
    
    const imageRow = document.createElement('div');
    imageRow.className = 'row';
    
    const previewCol = document.createElement('div');
    previewCol.className = 'col-md-6';
    
    const imagePreview = document.createElement('div');
    imagePreview.className = 'image-preview-container';
    
    if (value.productOptionValueImgUrl) {
        const img = document.createElement('img');
        img.src = value.productOptionValueImgUrl;
        img.alt = value.optionValueName;
        img.className = 'preview-image';
        imagePreview.appendChild(img);
    }
    
    previewCol.appendChild(imagePreview);
    
    const uploadCol = document.createElement('div');
    uploadCol.className = 'col-md-6';
    
    const imageInput = document.createElement('input');
    imageInput.type = 'file';
    imageInput.className = 'form-control';
    imageInput.name = 'optionValueImage';
    imageInput.accept = 'image/*';
    
    uploadCol.appendChild(imageInput);
    
    imageRow.appendChild(previewCol);
    imageRow.appendChild(uploadCol);
    
    imageFieldset.appendChild(imageLegend);
    imageFieldset.appendChild(imageRow);
    imageDiv.appendChild(imageFieldset);
    
    // Save button
    const saveDiv = document.createElement('div');
    saveDiv.className = 'col-12';
    
    const saveButton = document.createElement('button');
    saveButton.type = 'button';
    saveButton.className = 'btn btn-primary';
    saveButton.textContent = 'Save Value';
    saveButton.addEventListener('click', () => updateOptionValue(value.productOptionValueId, valueForm));
    
    // Status message
    const statusMessage = document.createElement('div');
    statusMessage.className = 'status-message';
    statusMessage.id = `status-value-${value.productOptionValueId}`;
    
    saveDiv.appendChild(saveButton);
    saveDiv.appendChild(statusMessage);
    
    // Append all elements to form
    valueForm.appendChild(quantityDiv);
    valueForm.appendChild(priceDiv);
    valueForm.appendChild(imageDiv);
    valueForm.appendChild(saveDiv);
    
    // Append header and form to value div
    valueDiv.appendChild(valueHeader);
    valueDiv.appendChild(valueForm);
    
    return valueDiv;
}

// Mark an image for deletion
function markImageForDeletion(imageId, imageContainer) {
    imagesToDelete.push(imageId);
    imageContainer.style.opacity = '0.5';
    
    // Add hidden input to track deleted images
    const hiddenInput = document.createElement('input');
    hiddenInput.type = 'hidden';
    hiddenInput.name = 'deleteImageId';
    hiddenInput.value = imageId;
    hiddenInput.id = `delete-image-${imageId}`;
    imagesToDeleteDiv.appendChild(hiddenInput);
    
    // Add undo button
    const undoBtn = document.createElement('button');
    undoBtn.type = 'button';
    undoBtn.className = 'btn btn-sm btn-outline-secondary mt-1';
    undoBtn.textContent = `Undo delete image #${imageId}`;
    undoBtn.addEventListener('click', () => {
        // Remove from deletion list
        imagesToDelete = imagesToDelete.filter(id => id !== imageId);
        document.getElementById(`delete-image-${imageId}`)?.remove();
        undoBtn.remove();
        imageContainer.style.opacity = '1';
    });
    
    imagesToDeleteDiv.appendChild(undoBtn);
}

// Setup event listeners
function setupEventListeners() {
    // Save basic info button
    document.getElementById('saveBasicInfo').addEventListener('click', updateBasicInfo);
    
    // Save images button
    document.getElementById('saveImages').addEventListener('click', updateImages);
    
    // Add option confirmation button
    document.getElementById('confirmAddOption').addEventListener('click', addNewOption);
    
    // Add option value confirmation button
    document.getElementById('confirmAddOptionValue').addEventListener('click', addNewOptionValue);
    
    // Delete product button
    document.getElementById('deleteProduct').addEventListener('click', () => {
        const deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
        deleteModal.show();
    });
    
    // Confirm delete button
    document.getElementById('confirmDelete').addEventListener('click', deleteProduct);
}

// Show product content after loading
function showProductContent() {
    productLoadingDiv.style.display = 'none';
    productContentDiv.style.display = 'block';
}

// Show error message
function showError(message) {
    productLoadingDiv.innerHTML = `
        <div class="alert alert-danger" role="alert">
            ${message}
        </div>
        <div class="text-center">
            <a href="./product_management.html" class="btn btn-outline-secondary">Back to Products</a>
        </div>
    `;
}

// Set status message
function setStatusMessage(elementId, message, isSuccess) {
    const statusElement = document.getElementById(elementId);
    statusElement.textContent = message;
    statusElement.className = `status-message ${isSuccess ? 'success-message' : 'error-message'}`;
    statusElement.style.display = 'block';
    
    // Auto-hide after 3 seconds
    setTimeout(() => {
        statusElement.style.display = 'none';
    }, 3000);
}

// Update basic product information
async function updateBasicInfo() {
    const saveBtn = document.getElementById('saveBasicInfo');
    const statusElement = document.getElementById('basicInfoStatus');
    
    try {
        saveBtn.disabled = true;
        saveBtn.innerHTML = 'Saving...';
        
        const requestData = {
            productId: productData.productId,
            categoryId: parseInt(document.getElementById('productCategory').value),
            name: document.getElementById('productName').value,
            basePrice: parseFloat(document.getElementById('basePrice').value),
            baseProductQuantity: parseInt(document.getElementById('baseProductQuantity').value),
            description: document.getElementById('description').value
        };
        
        // Add optional dimension fields if they have values
        const width = document.getElementById('width').value;
        if (width) requestData.width = parseFloat(width);
        
        const length = document.getElementById('length').value;
        if (length) requestData.length = parseFloat(length);
        
        const height = document.getElementById('height').value;
        if (height) requestData.height = parseFloat(height);
        
        const response = await fetch(`${API_BASE_URL}/product/update-base-info`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
            body: JSON.stringify(requestData)
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            setStatusMessage('basicInfoStatus', 'Basic information updated successfully!', true);
            // Update product data
            productData = data.result;
        } else {
            setStatusMessage('basicInfoStatus', 'Failed to update: ' + (data.message || 'Unknown error'), false);
        }
    } catch (error) {
        console.error('Error updating basic info:', error);
        setStatusMessage('basicInfoStatus', 'An error occurred while updating', false);
    } finally {
        saveBtn.disabled = false;
        saveBtn.innerHTML = 'Save Basic Info';
    }
}

// Update product images
async function updateImages() {
    const saveBtn = document.getElementById('saveImages');
    const statusElement = document.getElementById('imagesStatus');
    
    try {
        saveBtn.disabled = true;
        saveBtn.innerHTML = 'Saving...';
        
        const formData = new FormData();
        
        // Add base image ID
        const baseImgId = document.getElementById('baseImageId').value;
        if (baseImgId) {
            formData.append('baseProdImgId', baseImgId);
        }
        
        // Add new base image if provided
        const newBaseImageFile = document.getElementById('newBaseImage').files[0];
        if (newBaseImageFile) {
            formData.append('newBaseProdImg', newBaseImageFile);
        }
        
        // Add images to delete
        if (imagesToDelete.length > 0) {
            formData.append('otherProdImgIdsForDelete', imagesToDelete.join(','));
        }
        
        // Add new other images if provided
        const newOtherImageFiles = document.getElementById('newOtherImages').files;
        if (newOtherImageFiles.length > 0) {
            for (let i = 0; i < newOtherImageFiles.length; i++) {
                formData.append('newOtherProdImgList', newOtherImageFiles[i]);
            }
        }
        
        const response = await fetch(`${API_BASE_URL}/product/update-img`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
            body: formData
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            setStatusMessage('imagesStatus', 'Product images updated successfully!', true);
            // Update product data and refresh the image displays
            productData = data.result;
            
            // Reset image states
            imagesToDelete = [];
            imagesToDeleteDiv.innerHTML = '';
            document.getElementById('newBaseImage').value = '';
            document.getElementById('newOtherImages').value = '';
            
            // Refresh image displays
            if (productData.baseImageUrl) {
                const baseImgId = Object.keys(productData.baseImageUrl)[0];
                const baseImgUrl = productData.baseImageUrl[baseImgId];
                
                document.getElementById('baseImageId').value = baseImgId;
                
                const imgElement = document.createElement('img');
                imgElement.src = baseImgUrl;
                imgElement.alt = 'Base Product Image';
                imgElement.className = 'preview-image';
                imgElement.style.maxWidth = '200px';
                imgElement.style.maxHeight = '200px';
                
                baseImagePreview.innerHTML = '';
                baseImagePreview.appendChild(imgElement);
            }
            
            // Display other images
            if (productData.otherImageUrl && productData.otherImageUrl.length > 0) {
                otherImagesPreview.innerHTML = '';
                
                productData.otherImageUrl.forEach(imageObj => {
                    const imgId = Object.keys(imageObj)[0];
                    const imgUrl = imageObj[imgId];
                    
                    const imageContainer = document.createElement('div');
                    imageContainer.className = 'image-item';
                    imageContainer.setAttribute('data-image-id', imgId);
                    
                    const imgElement = document.createElement('img');
                    imgElement.src = imgUrl;
                    imgElement.alt = 'Product Image';
                    imgElement.className = 'preview-image';
                    
                    const deleteBtn = document.createElement('span');
                    deleteBtn.className = 'delete-image';
                    deleteBtn.innerHTML = '&times;';
                    deleteBtn.addEventListener('click', () => markImageForDeletion(imgId, imageContainer));
                    
                    imageContainer.appendChild(imgElement);
                    imageContainer.appendChild(deleteBtn);
                    otherImagesPreview.appendChild(imageContainer);
                });
            } else {
                otherImagesPreview.innerHTML = '<p>No additional images</p>';
            }
        } else {
            setStatusMessage('imagesStatus', 'Failed to update images: ' + (data.message || 'Unknown error'), false);
        }
    } catch (error) {
        console.error('Error updating images:', error);
        setStatusMessage('imagesStatus', 'An error occurred while updating images', false);
    } finally {
        saveBtn.disabled = false;
        saveBtn.innerHTML = 'Save Image Changes';
    }
}

// Add a new option to the product
async function addNewOption() {
    const saveBtn = document.getElementById('confirmAddOption');
    const statusElement = document.getElementById('addOptionStatus');
    
    try {
        const optionId = document.getElementById('newOptionSelect').value;
        
        if (!optionId) {
            setStatusMessage('addOptionStatus', 'Please select an option', false);
            return;
        }
        
        saveBtn.disabled = true;
        saveBtn.textContent = 'Adding...';
        
        const requestData = {
            productId: productData.productId,
            optionId: parseInt(optionId)
        };
        
        const response = await fetch(`${API_BASE_URL}/product/add-option`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
            body: JSON.stringify(requestData)
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            // Update product data
            productData = data.result;
            
            // Close modal
            bootstrap.Modal.getInstance(document.getElementById('addOptionModal')).hide();
            
            // Refresh options display
            displayProductOptions();
            
            // Update options dropdown (remove the one we just added)
            populateOptionsDropdown();
        } else {
            setStatusMessage('addOptionStatus', 'Failed to add option: ' + (data.message || 'Unknown error'), false);
        }
    } catch (error) {
        console.error('Error adding option:', error);
        setStatusMessage('addOptionStatus', 'An error occurred while adding option', false);
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = 'Add';
    }
}

// Open add option value modal
function openAddValueModal(optionId, productOptionId) {
    // Set the current product option ID
    document.getElementById('currentProductOptionId').value = productOptionId;
    
    // Find the option in the options list
    const option = options.find(opt => opt.id === optionId);
    
    if (option && option.values) {
        // Get existing option values for this product option
        const existingValueIds = [];
        if (productData.productOptionResponseList) {
            const productOption = productData.productOptionResponseList.find(po => po.productOptionId === productOptionId);
            if (productOption && productOption.productOptionValueResponseList) {
                existingValueIds.push(...productOption.productOptionValueResponseList.map(pov => pov.optionValueId));
            }
        }
        
        // Populate option values dropdown
        const optionValueSelect = document.getElementById('optionValueSelect');
        optionValueSelect.innerHTML = '<option value="" disabled selected>Select a value</option>';
        
        option.values.forEach(value => {
            // Only add values that aren't already added to this option
            if (!existingValueIds.includes(value.id)) {
                const optElement = document.createElement('option');
                optElement.value = value.id;
                optElement.textContent = value.name;
                optionValueSelect.appendChild(optElement);
            }
        });
        
        // Reset form values
        document.getElementById('optionValueQuantity').value = '0';
        document.getElementById('optionValueAddPrice').value = '0';
        document.getElementById('optionValueImage').value = '';
        document.getElementById('addOptionValueStatus').className = 'status-message';
        document.getElementById('addOptionValueStatus').style.display = 'none';
        
        // Set modal title
        document.getElementById('addOptionValueModalLabel').textContent = `Add Value to "${option.name}"`;
        
        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('addOptionValueModal'));
        modal.show();
    }
}

// Add a new option value to a product option
async function addNewOptionValue() {
    const saveBtn = document.getElementById('confirmAddOptionValue');
    const statusElement = document.getElementById('addOptionValueStatus');
    
    try {
        const productOptionId = document.getElementById('currentProductOptionId').value;
        const optionValueId = document.getElementById('optionValueSelect').value;
        const quantity = document.getElementById('optionValueQuantity').value;
        const addPrice = document.getElementById('optionValueAddPrice').value;
        const imageFile = document.getElementById('optionValueImage').files[0];
        
        if (!optionValueId) {
            setStatusMessage('addOptionValueStatus', 'Please select an option value', false);
            return;
        }
        
        if (!quantity || parseInt(quantity) < 0) {
            setStatusMessage('addOptionValueStatus', 'Quantity must be a non-negative number', false);
            return;
        }
        
        if (!addPrice || parseFloat(addPrice) < 0) {
            setStatusMessage('addOptionValueStatus', 'Additional price must be a non-negative number', false);
            return;
        }
        
        saveBtn.disabled = true;
        saveBtn.textContent = 'Adding...';
        
        const formData = new FormData();
        formData.append('productOptionId', productOptionId);
        formData.append('optionValueId', optionValueId);
        formData.append('quantity', quantity);
        formData.append('addPrice', addPrice);
        
        if (imageFile) {
            formData.append('ProdOptValImg', imageFile);
        }
        
        const response = await fetch(`${API_BASE_URL}/product/add-opt-val`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
            body: formData
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            // Update product data
            productData = data.result;
            
            // Close modal
            bootstrap.Modal.getInstance(document.getElementById('addOptionValueModal')).hide();
            
            // Refresh options display
            displayProductOptions();
        } else {
            setStatusMessage('addOptionValueStatus', 'Failed to add option value: ' + (data.message || 'Unknown error'), false);
        }
    } catch (error) {
        console.error('Error adding option value:', error);
        setStatusMessage('addOptionValueStatus', 'An error occurred while adding option value', false);
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = 'Add';
    }
}

// Update an existing option value
async function updateOptionValue(optionValueId, form) {
    try {
        // Get form elements
        const quantityInput = form.querySelector('input[name="quantity"]');
        const addPriceInput = form.querySelector('input[name="addPrice"]');
        const imageInput = form.querySelector('input[name="optionValueImage"]');
        const statusId = `status-value-${optionValueId}`;
        
        // Validate inputs
        if (!quantityInput.value || parseInt(quantityInput.value) < 0) {
            setStatusMessage(statusId, 'Quantity must be a non-negative number', false);
            return;
        }
        
        if (!addPriceInput.value || parseFloat(addPriceInput.value) < 0) {
            setStatusMessage(statusId, 'Additional price must be a non-negative number', false);
            return;
        }
        
        // Create form data
        const formData = new FormData();
        formData.append('prodOptValId', optionValueId);
        formData.append('quantity', quantityInput.value);
        formData.append('addPrice', addPriceInput.value);
        
        // Add image if provided
        if (imageInput.files[0]) {
            formData.append('newProdOptValImg', imageInput.files[0]);
        }
        
        // Send request
        const response = await fetch(`${API_BASE_URL}/product/update-opt-val`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
            body: formData
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            // Update product data
            productData = data.result;
            setStatusMessage(statusId, 'Value updated successfully!', true);
            
            // Refresh the specific option value
            const optionValue = findOptionValueById(optionValueId);
            if (optionValue) {
                const valueContainer = document.getElementById(`value-${optionValueId}`);
                if (valueContainer) {
                    const parent = valueContainer.parentNode;
                    const newValueElement = createOptionValueElement(optionValue);
                    parent.replaceChild(newValueElement, valueContainer);
                }
            }
        } else {
            setStatusMessage(statusId, 'Failed to update: ' + (data.message || 'Unknown error'), false);
        }
    } catch (error) {
        console.error('Error updating option value:', error);
        setStatusMessage(`status-value-${optionValueId}`, 'An error occurred while updating', false);
    }
}

// Delete an option value
async function deleteOptionValue(optionValueId) {
    if (!confirm('Are you sure you want to delete this option value?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/product/delete-opt-val/${optionValueId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            }
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            // Update product data
            productData = data.result;
            
            // Refresh options display
            displayProductOptions();
        } else {
            alert('Failed to delete option value: ' + (data.message || 'Unknown error'));
        }
    } catch (error) {
        console.error('Error deleting option value:', error);
        alert('An error occurred while deleting option value');
    }
}

// Delete an option
async function deleteOption(productOptionId) {
    if (!confirm('Are you sure you want to delete this option and all its values?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/product/delete-opt/${productOptionId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            }
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            // Update product data
            productData = data.result;
            
            // Refresh options display
            displayProductOptions();
            
            // Update options dropdown (add back the one we just removed)
            populateOptionsDropdown();
        } else {
            alert('Failed to delete option: ' + (data.message || 'Unknown error'));
        }
    } catch (error) {
        console.error('Error deleting option:', error);
        alert('An error occurred while deleting option');
    }
}

// Delete the entire product
async function deleteProduct() {
    try {
        const response = await fetch(`${API_BASE_URL}/product/delete/${productData.productId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            }
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            alert('Product deleted successfully!');
            window.location.href = 'product_management.html';
        } else {
            alert('Failed to delete product: ' + (data.message || 'Unknown error'));
        }
    } catch (error) {
        console.error('Error deleting product:', error);
        alert('An error occurred while deleting product');
    }
}

// Helper function to find an option value by ID
function findOptionValueById(optionValueId) {
    for (const option of productData.productOptionResponseList || []) {
        for (const value of option.productOptionValueResponseList || []) {
            if (value.productOptionValueId === parseInt(optionValueId)) {
                return value;
            }
        }
    }
    return null;
}
