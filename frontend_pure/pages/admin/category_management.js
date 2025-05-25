document.addEventListener('DOMContentLoaded', function() {
    fetchCategories();
    setupEventListeners();
    setupImagePreview();
});

// Global variable to store categories
let categories = [];

// Fetch all categories from the API
async function fetchCategories() {
    try {
        const response = await fetch('http://localhost:8080/api/category');
        const data = await response.json();
        
        if (data.code === 1000) {
            categories = data.result;
            renderCategories(categories);
        } else {
            showAlert('Failed to load categories', 'danger');
        }
    } catch (error) {
        console.error('Error fetching categories:', error);
        showAlert('Error loading categories. Please try again later.', 'danger');
    }
}

// Render categories to the table
function renderCategories(categoryList) {
    const tableBody = document.getElementById('category-table-body');
    tableBody.innerHTML = '';
    
    categoryList.forEach(category => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${category.id}</td>
            <td>${category.name}</td>
            <td>${category.imgUrl ? `<img src="${category.imgUrl}" width="50" height="50" style="object-fit: cover;">` : 'No image'}</td>
            <td class="action-icons">
                <button class="btn btn-sm btn-outline-primary edit-category-btn" data-id="${category.id}">Edit</button>
                <button class="btn btn-sm btn-outline-danger delete-category-btn" data-id="${category.id}">Delete</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
    
    // Add event listeners to the new buttons
    document.querySelectorAll('.edit-category-btn').forEach(button => {
        button.addEventListener('click', () => openEditModal(button.dataset.id));
    });
    
    document.querySelectorAll('.delete-category-btn').forEach(button => {
        button.addEventListener('click', () => openDeleteModal(button.dataset.id));
    });
}

// Set up event listeners
function setupEventListeners() {
    // Add category button
    document.getElementById('add-category-btn').addEventListener('click', () => {
        openModal('add-category-modal');
    });
    
    // Add category form submission
    document.getElementById('add-category-form').addEventListener('submit', function(event) {
        event.preventDefault();
        addCategory();
    });
    
    // Edit category form submission
    document.getElementById('edit-category-form').addEventListener('submit', function(event) {
        event.preventDefault();
        updateCategory();
    });
    
    // Confirm delete button
    document.getElementById('confirm-delete-btn').addEventListener('click', deleteCategory);
    
    // Search button
    document.getElementById('search-button').addEventListener('click', searchCategories);
    
    // Search inputs (enable real-time search)
    document.getElementById('search-input').addEventListener('input', searchCategories);
    document.getElementById('search-id-input').addEventListener('input', searchCategories);
}

// Setup image preview functionality
function setupImagePreview() {
    // Preview for add category
    document.getElementById('category-image').addEventListener('change', function() {
        previewImage(this, 'add-image-preview');
    });
    
    // Preview for edit category
    document.getElementById('edit-category-image').addEventListener('change', function() {
        document.getElementById('current-image-preview').style.display = 'none';
        previewImage(this, 'edit-image-preview');
    });
}

// Preview uploaded image
function previewImage(input, previewId) {
    const preview = document.getElementById(previewId);
    
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        
        reader.onload = function(e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
        }
        
        reader.readAsDataURL(input.files[0]);
    } else {
        preview.style.display = 'none';
    }
}

// Open modal with overlay effect
function openModal(modalId) {
    document.getElementById(modalId).style.display = 'flex';
}

// Close modal
function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
    
    // Reset forms
    if (modalId === 'add-category-modal') {
        document.getElementById('add-category-form').reset();
        document.getElementById('add-image-preview').style.display = 'none';
    } else if (modalId === 'edit-category-modal') {
        document.getElementById('edit-category-form').reset();
        document.getElementById('edit-image-preview').style.display = 'none';
    }
}

// Open edit modal and populate with category data
function openEditModal(categoryId) {
    const category = categories.find(cat => cat.id == categoryId);
    if (category) {
        document.getElementById('edit-category-id').value = category.id;
        document.getElementById('edit-category-name').value = category.name;
        
        // Display current image if available
        const currentImagePreview = document.getElementById('current-image-preview');
        if (category.imgUrl) {
            currentImagePreview.src = category.imgUrl;
            currentImagePreview.style.display = 'block';
        } else {
            currentImagePreview.style.display = 'none';
        }
        
        // Hide new image preview
        document.getElementById('edit-image-preview').style.display = 'none';
        
        openModal('edit-category-modal');
    }
}

// Open delete confirmation modal
function openDeleteModal(categoryId) {
    document.getElementById('delete-category-id').value = categoryId;
    openModal('delete-category-modal');
}

// Add new category with image
async function addCategory() {
    const categoryName = document.getElementById('category-name').value.trim();
    const categoryImage = document.getElementById('category-image').files[0];
    
    if (!categoryName) {
        showAlert('Category name cannot be empty', 'warning');
        return;
    }
    
    // Create FormData object for multipart/form-data
    const formData = new FormData();
    formData.append('name', categoryName);
    
    if (categoryImage) {
        formData.append('image', categoryImage);
    }
    
    try {
        const response = await fetch('http://localhost:8080/api/category/create/with-image', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + getToken()
            },
            body: formData
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            showAlert('Category added successfully', 'success');
            document.getElementById('add-category-form').reset();
            document.getElementById('add-image-preview').style.display = 'none';
            closeModal('add-category-modal');
            fetchCategories(); // Refresh the list
        } else {
            showAlert('Failed to add category: ' + (data.message || 'Unknown error'), 'danger');
        }
    } catch (error) {
        console.error('Error adding category:', error);
        showAlert('Error adding category. Please try again.', 'danger');
    }
}

// Update existing category with image
async function updateCategory() {
    const categoryId = document.getElementById('edit-category-id').value;
    const categoryName = document.getElementById('edit-category-name').value.trim();
    const categoryImage = document.getElementById('edit-category-image').files[0];
    
    if (!categoryName) {
        showAlert('Category name cannot be empty', 'warning');
        return;
    }
    
    // Create FormData object for multipart/form-data
    const formData = new FormData();
    formData.append('name', categoryName);
    
    if (categoryImage) {
        formData.append('image', categoryImage);
    }
    
    try {
        const response = await fetch(`http://localhost:8080/api/category/with-image/${categoryId}`, {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + getToken()
            },
            body: formData
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            showAlert('Category updated successfully', 'success');
            closeModal('edit-category-modal');
            fetchCategories(); // Refresh the list
        } else {
            showAlert('Failed to update category: ' + (data.message || 'Unknown error'), 'danger');
        }
    } catch (error) {
        console.error('Error updating category:', error);
        showAlert('Error updating category. Please try again.', 'danger');
    }
}

// Delete category
async function deleteCategory() {
    const categoryId = document.getElementById('delete-category-id').value;
    
    try {
        const response = await fetch(`http://localhost:8080/api/category/${categoryId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + getToken()
            }
        });
        
        const data = await response.json();
        
        if (data.code === 1000) {
            showAlert('Category deleted successfully', 'success');
            closeModal('delete-category-modal');
            fetchCategories(); // Refresh the list
        } else {
            showAlert('Failed to delete category', 'danger');
        }
    } catch (error) {
        console.error('Error deleting category:', error);
        showAlert('Error deleting category. Please try again.', 'danger');
    }
}

// Search categories function
function searchCategories() {
    const searchTerm = document.getElementById('search-input').value.toLowerCase().trim();
    const searchId = document.getElementById('search-id-input').value.trim();
    
    let filteredCategories = categories;
    
    // Filter by ID if provided
    if (searchId) {
        filteredCategories = filteredCategories.filter(category => 
            category.id.toString() === searchId
        );
    }
    
    // Filter by name if provided
    if (searchTerm) {
        filteredCategories = filteredCategories.filter(category => 
            category.name.toLowerCase().includes(searchTerm)
        );
    }
    
    renderCategories(filteredCategories);
}

// Helper function to show alerts
function showAlert(message, type) {
    // Create alert element
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.role = 'alert';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    // Insert it at the top of the container
    const container = document.querySelector('.container');
    container.insertBefore(alertDiv, container.firstChild);
    
    // Auto dismiss after 3 seconds
    setTimeout(() => {
        alertDiv.classList.remove('show');
        setTimeout(() => alertDiv.remove(), 150);
    }, 3000);
}

// Helper function to get auth token
function getToken() {
    // Implement your token retrieval logic here
    // For example, from localStorage:
    return localStorage.getItem('authToken') || '';
}
