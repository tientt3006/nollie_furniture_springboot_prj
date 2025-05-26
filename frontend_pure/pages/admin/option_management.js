// API base URL - replace with your actual API base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Store all options data
let allOptions = [];

// DOM elements
const searchInput = document.getElementById('search-input');
const optionTableBody = document.getElementById('option-table-body');

// Modal elements
const detailsModal = document.getElementById('details-modal');
const addOptionModal = document.getElementById('add-option-modal');
const editOptionModal = document.getElementById('edit-option-modal');
const deleteConfirmModal = document.getElementById('delete-confirm-modal');

// Initialize the page when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    fetchAllOptions();
    setupEventListeners();
});

// Fetch all options from the API
async function fetchAllOptions() {
    try {
        const response = await fetch(`${API_BASE_URL}/option/all`);
        const data = await response.json();
        
        if (data.code === 1000) {
            allOptions = data.result;
            renderOptions(allOptions);
        } else {
            console.error('Failed to fetch options:', data);
            alert('Failed to load options. Please try again.');
        }
    } catch (error) {
        console.error('Error fetching options:', error);
        alert('An error occurred while loading options.');
    }
}

// Render options to the table
function renderOptions(options) {
    optionTableBody.innerHTML = '';
    
    options.forEach(option => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${option.id}</td>
            <td>${option.name}</td>
            <td>${option.values.length}</td>
            <td>
                <button class="btn btn-sm btn-primary view-btn" data-id="${option.id}">Details</button>
                <button class="btn btn-sm btn-warning edit-btn" data-id="${option.id}">Edit</button>
                <button class="btn btn-sm btn-danger delete-btn" data-id="${option.id}">Delete</button>
            </td>
        `;
        optionTableBody.appendChild(row);
    });
    
    // Add event listeners to buttons
    document.querySelectorAll('.view-btn').forEach(btn => {
        btn.addEventListener('click', () => viewOptionDetails(btn.dataset.id));
    });
    
    document.querySelectorAll('.edit-btn').forEach(btn => {
        btn.addEventListener('click', () => showEditModal(btn.dataset.id));
    });
    
    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', () => showDeleteModal(btn.dataset.id));
    });
}

// Setup event listeners
function setupEventListeners() {
    // Search functionality
    searchInput.addEventListener('input', filterOptions);
    
    // Add option button
    document.getElementById('add-option-btn').addEventListener('click', showAddModal);
    
    // Details modal close buttons
    document.getElementById('details-close-btn').addEventListener('click', () => hideModal(detailsModal));
    document.getElementById('details-close-btn-footer').addEventListener('click', () => hideModal(detailsModal));
    
    // Add option modal
    document.getElementById('add-option-close-btn').addEventListener('click', () => hideModal(addOptionModal));
    document.getElementById('add-option-cancel-btn').addEventListener('click', () => hideModal(addOptionModal));
    document.getElementById('add-option-save-btn').addEventListener('click', saveNewOption);
    document.getElementById('add-value-btn').addEventListener('click', addValueField);
    
    // Edit option modal
    document.getElementById('edit-option-close-btn').addEventListener('click', () => hideModal(editOptionModal));
    document.getElementById('edit-option-cancel-btn').addEventListener('click', () => hideModal(editOptionModal));
    document.getElementById('edit-option-save-btn').addEventListener('click', saveEditOption);
    document.getElementById('edit-add-value-btn').addEventListener('click', addEditValueField);
    
    // Delete confirmation modal
    document.getElementById('delete-close-btn').addEventListener('click', () => hideModal(deleteConfirmModal));
    document.getElementById('delete-cancel-btn').addEventListener('click', () => hideModal(deleteConfirmModal));
    document.getElementById('delete-confirm-btn').addEventListener('click', deleteOption);
}

// Filter options based on search input
function filterOptions() {
    const searchTerm = searchInput.value.trim().toLowerCase();
    const filteredOptions = allOptions.filter(option => 
        option.name.toLowerCase().includes(searchTerm)
    );
    renderOptions(filteredOptions);
}

// View option details
async function viewOptionDetails(optionId) {
    try {
        const response = await fetch(`${API_BASE_URL}/option/${optionId}`);
        const data = await response.json();
        
        if (data.code === 1000) {
            const option = data.result;
            const detailsContent = document.getElementById('details-content');
            
            let valuesHtml = '<ul class="list-group">';
            option.values.forEach(value => {
                let valueContent = `<li class="list-group-item d-flex justify-content-between align-items-center">
                    <span>${value.name}</span>`;
                
                if (value.imgUrl) {
                    valueContent += `<img src="${value.imgUrl}" alt="${value.name}" height="50">`;
                }
                
                valueContent += '</li>';
                valuesHtml += valueContent;
            });
            valuesHtml += '</ul>';
            
            detailsContent.innerHTML = `
                <h4>${option.name}</h4>
                <p>ID: ${option.id}</p>
                <p>Values (${option.values.length}):</p>
                ${valuesHtml}
            `;
            
            showModal(detailsModal);
        } else {
            console.error('Failed to fetch option details:', data);
            alert('Failed to load option details. Please try again.');
        }
    } catch (error) {
        console.error('Error fetching option details:', error);
        alert('An error occurred while loading option details.');
    }
}

// Show Add Option Modal
function showAddModal() {
    // Clear previous inputs
    document.getElementById('new-option-name').value = '';
    document.getElementById('new-values-container').innerHTML = `
        <h6>Values</h6>
        <div class="value-input-container">
            <input type="text" class="form-control me-2" placeholder="Value name">
            <input type="file" class="form-control" accept="image/*">
            <button class="btn btn-danger ms-2 remove-value-btn">Remove</button>
        </div>
    `;
    
    // Add event listener to remove button
    document.querySelectorAll('.remove-value-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            this.closest('.value-input-container').remove();
        });
    });
    
    showModal(addOptionModal);
}

// Add a new value field to add option modal
function addValueField() {
    const valueContainer = document.createElement('div');
    valueContainer.className = 'value-input-container';
    valueContainer.innerHTML = `
        <input type="text" class="form-control me-2" placeholder="Value name">
        <input type="file" class="form-control" accept="image/*">
        <button class="btn btn-danger ms-2 remove-value-btn">Remove</button>
    `;
    
    document.getElementById('new-values-container').appendChild(valueContainer);
    
    valueContainer.querySelector('.remove-value-btn').addEventListener('click', function() {
        valueContainer.remove();
    });
}

// Save a new option
async function saveNewOption() {
    const optionName = document.getElementById('new-option-name').value.trim();
    if (!optionName) {
        alert('Please enter an option name.');
        return;
    }
    
    const valueContainers = document.querySelectorAll('#new-values-container .value-input-container');
    if (valueContainers.length === 0) {
        alert('Please add at least one value.');
        return;
    }
    
    // Create form data
    const formData = new FormData();
    formData.append('name', optionName);
    
    // Add values and images
    let hasEmptyValue = false;
    valueContainers.forEach(container => {
        const valueName = container.querySelector('input[type="text"]').value.trim();
        const imageFile = container.querySelector('input[type="file"]').files[0];
        
        if (!valueName) {
            hasEmptyValue = true;
            return;
        }
        
        formData.append('values', valueName);
        if (imageFile) {
            formData.append('images', imageFile);
        } else {
            formData.append('images', new File([], "empty"));
        }
    });
    
    if (hasEmptyValue) {
        alert('All value names must be filled in.');
        return;
    }
    
    try {
        const token = localStorage.getItem('authToken'); // Assuming you store your token in localStorage
        const response = await fetch(`${API_BASE_URL}/option/create-with-images`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
            body: formData
        });
        
        const data = await response.json();
        if (data.code === 1000) {
            alert('Option created successfully!');
            hideModal(addOptionModal);
            fetchAllOptions(); // Refresh the options list
        } else {
            alert(`Failed to create option: ${data.message || 'Unknown error'}`);
        }
    } catch (error) {
        console.error('Error creating option:', error);
        alert('An error occurred while creating the option.');
    }
}

// Show Edit Option Modal
async function showEditModal(optionId) {
    try {
        const response = await fetch(`${API_BASE_URL}/option/${optionId}`);
        const data = await response.json();
        
        if (data.code === 1000) {
            const option = data.result;
            
            document.getElementById('edit-option-id').value = option.id;
            document.getElementById('edit-option-name').value = option.name;
            
            // Display existing values
            const existingValuesContainer = document.getElementById('existing-values-container');
            existingValuesContainer.innerHTML = '<h6>Current Values</h6>';
            
            option.values.forEach(value => {
                const valueRow = document.createElement('div');
                valueRow.className = 'value-row';
                valueRow.dataset.id = value.id;
                
                valueRow.innerHTML = `
                    <span class="me-2">${value.name}</span>
                    ${value.imgUrl ? `<img src="${value.imgUrl}" alt="${value.name}" height="40" class="me-2">` : ''}
                    <button class="btn btn-sm btn-danger toggle-delete-btn" data-id="${value.id}">Delete</button>
                `;
                
                existingValuesContainer.appendChild(valueRow);
            });
            
            // Add event listeners to toggle delete buttons
            document.querySelectorAll('.toggle-delete-btn').forEach(btn => {
                btn.addEventListener('click', function() {
                    const row = this.closest('.value-row');
                    if (row.classList.contains('deleted')) {
                        row.classList.remove('deleted');
                        this.textContent = 'Delete';
                    } else {
                        row.classList.add('deleted');
                        this.textContent = 'Undo';
                    }
                });
            });
            
            // Clear new values container
            document.getElementById('new-edit-values-container').innerHTML = '<h6>Add New Values</h6>';
            
            showModal(editOptionModal);
        } else {
            console.error('Failed to fetch option details for edit:', data);
            alert('Failed to load option details. Please try again.');
        }
    } catch (error) {
        console.error('Error fetching option details for edit:', error);
        alert('An error occurred while loading option details.');
    }
}

// Add a new value field to edit option modal
function addEditValueField() {
    const valueContainer = document.createElement('div');
    valueContainer.className = 'value-input-container';
    valueContainer.innerHTML = `
        <input type="text" class="form-control me-2" placeholder="Value name">
        <input type="file" class="form-control" accept="image/*">
        <button class="btn btn-danger ms-2 remove-value-btn">Remove</button>
    `;
    
    document.getElementById('new-edit-values-container').appendChild(valueContainer);
    
    valueContainer.querySelector('.remove-value-btn').addEventListener('click', function() {
        valueContainer.remove();
    });
}

// Save edited option
async function saveEditOption() {
    const optionId = document.getElementById('edit-option-id').value;
    const optionName = document.getElementById('edit-option-name').value.trim();
    
    if (!optionName) {
        alert('Please enter an option name.');
        return;
    }
    
    // Get deleted value IDs
    const deletedValueIds = [];
    document.querySelectorAll('#existing-values-container .value-row.deleted').forEach(row => {
        deletedValueIds.push(row.dataset.id);
    });
    
    // Get new values and images
    const newValueContainers = document.querySelectorAll('#new-edit-values-container .value-input-container');
    const newValues = [];
    const newImages = [];
    
    newValueContainers.forEach(container => {
        const valueName = container.querySelector('input[type="text"]').value.trim();
        const imageFile = container.querySelector('input[type="file"]').files[0];
        
        if (valueName) {
            newValues.push(valueName);
            if (imageFile) {
                newImages.push(imageFile);
            } else {
                newImages.push(new File([], "empty"));
            }
        }
    });
    
    // Create form data
    const formData = new FormData();
    formData.append('id', optionId);
    formData.append('name', optionName);
    
    // Add value IDs to delete
    deletedValueIds.forEach(id => {
        formData.append('valueIdsForDelete', id);
    });
    
    // Add new values
    newValues.forEach(value => {
        formData.append('newValuesForAdd', value);
    });
    
    // Add images for new values
    newImages.forEach(image => {
        formData.append('images', image);
    });
    
    try {
        const token = localStorage.getItem('authToken'); // Assuming you store your token in localStorage
        const response = await fetch(`${API_BASE_URL}/option/update-with-images`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
            body: formData
        });
        
        const data = await response.json();
        if (data.code === 1000) {
            alert('Option updated successfully!');
            hideModal(editOptionModal);
            fetchAllOptions(); // Refresh the options list
        } else {
            alert(`Failed to update option: ${data.message || 'Unknown error'}`);
        }
    } catch (error) {
        console.error('Error updating option:', error);
        alert('An error occurred while updating the option.');
    }
}

// Show Delete Confirmation Modal
function showDeleteModal(optionId) {
    document.getElementById('delete-option-id').value = optionId;
    showModal(deleteConfirmModal);
}

// Delete an option
async function deleteOption() {
    const optionId = document.getElementById('delete-option-id').value;
    
    try {
        const token = localStorage.getItem('authToken'); // Assuming you store your token in localStorage
        const response = await fetch(`${API_BASE_URL}/option/${optionId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
            }
        });
        
        if (response.ok) {
            alert('Option deleted successfully!');
            hideModal(deleteConfirmModal);
            fetchAllOptions(); // Refresh the options list
        } else {
            const data = await response.json();
            alert(`Failed to delete option: ${data.message || 'Unknown error'}`);
        }
    } catch (error) {
        console.error('Error deleting option:', error);
        alert('An error occurred while deleting the option.');
    }
}

// Utility functions for modals
function showModal(modal) {
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function hideModal(modal) {
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
}
