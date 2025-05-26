// API configuration
const API_BASE_URL = 'http://localhost:8080/api';
document.addEventListener("DOMContentLoaded", function() {
    // API base URL
    const API_BASE_URL = 'http://localhost:8080/api';
    
    // DOM elements
    const productList = document.getElementById("productList");
    const showingCount = document.getElementById("showingCount");
    const loadMoreBtn = document.getElementById("loadMore");
    const paginationContainer = document.querySelector(".pagination");
    const searchInput = document.getElementById("search-input");
    const categoryFilter = document.getElementById("sidebar-category");
    const colorFilter = document.getElementById("sidebar-color");
    const materialFilter = document.getElementById("sidebar-material");
    const priceMinInput = document.getElementById("price-min-input");
    const priceMaxInput = document.getElementById("price-max-input");
    const heightMinInput = document.getElementById("dimension-height-min-input");
    const heightMaxInput = document.getElementById("dimension-height-max-input");
    const widthMinInput = document.getElementById("dimension-width-min-input");
    const widthMaxInput = document.getElementById("dimension-width-max-input");
    const lengthMinInput = document.getElementById("dimension-length-min-input");
    const lengthMaxInput = document.getElementById("dimension-length-max-input");
    const pageSizeSelector = document.getElementById("page-size-selector");
    const jumpToPageInput = document.getElementById("jump-to-page");
    const jumpToPageBtn = document.getElementById("jump-btn");
    
    // State management
    let state = {
        filters: {
            search: '',
            categories: '',
            color1s: '',
            materials: '',
            minPrice: '',
            maxPrice: '',
            minHeight: '',
            maxHeight: '',
            minWidth: '',
            maxWidth: '',
            minLength: '',
            maxLength: '',
            sortBy: 'id',
            sortOrder: 'desc'
        },
        pagination: {
            currentPage: 1,
            pageSize: 10,
            totalItems: 0,
            totalPages: 0
        },
        categories: [],
        options: []
    };
    
    // Initialize the page
    init();
    
    // Main initialization function
    async function init() {
        // Set default page size
        if (pageSizeSelector) {
            pageSizeSelector.value = state.pagination.pageSize;
        }
        
        // Load initial data
        await Promise.all([
            loadCategories(),
            loadOptions()
        ]);
        
        // Parse URL parameters if any
        parseUrlParams();
        
        // Fetch products with current filters
        loadProducts();
        
        // Set up event listeners
        setupEventListeners();
    }
    
    // Parse URL parameters and update state
    function parseUrlParams() {
        const urlParams = new URLSearchParams(window.location.search);
        
        // Update filters from URL params
        if (urlParams.has('search')) state.filters.search = urlParams.get('search');
        if (urlParams.has('categories')) state.filters.categories = urlParams.get('categories');
        if (urlParams.has('color1s')) state.filters.color1s = urlParams.get('color1s');
        if (urlParams.has('materials')) state.filters.materials = urlParams.get('materials');
        if (urlParams.has('minPrice')) state.filters.minPrice = urlParams.get('minPrice');
        if (urlParams.has('maxPrice')) state.filters.maxPrice = urlParams.get('maxPrice');
        if (urlParams.has('minHeight')) state.filters.minHeight = urlParams.get('minHeight');
        if (urlParams.has('maxHeight')) state.filters.maxHeight = urlParams.get('maxHeight');
        if (urlParams.has('minWidth')) state.filters.minWidth = urlParams.get('minWidth');
        if (urlParams.has('maxWidth')) state.filters.maxWidth = urlParams.get('maxWidth');
        if (urlParams.has('minLength')) state.filters.minLength = urlParams.get('minLength');
        if (urlParams.has('maxLength')) state.filters.maxLength = urlParams.get('maxLength');
        if (urlParams.has('sortBy')) state.filters.sortBy = urlParams.get('sortBy');
        if (urlParams.has('sortOrder')) state.filters.sortOrder = urlParams.get('sortOrder');
        
        // Update pagination from URL params
        if (urlParams.has('page')) state.pagination.currentPage = parseInt(urlParams.get('page'));
        if (urlParams.has('size')) state.pagination.pageSize = parseInt(urlParams.get('size'));
        
        // Update UI to reflect the filters
        updateFiltersUI();
    }
    
    // Update UI based on the current filters
    function updateFiltersUI() {
        // Set search input
        if (searchInput) searchInput.value = state.filters.search;
        
        // Set category radio
        if (state.filters.categories) {
            const categoryRadio = document.querySelector(`#category-${state.filters.categories}`);
            if (categoryRadio) categoryRadio.checked = true;
        }
        
        // Set color radio
        if (state.filters.color1s) {
            const colorRadio = document.querySelector(`#color-${state.filters.color1s}`);
            if (colorRadio) colorRadio.checked = true;
        }
        
        // Set material radio
        if (state.filters.materials) {
            const materialRadio = document.querySelector(`#material-${state.filters.materials}`);
            if (materialRadio) materialRadio.checked = true;
        }
        
        // Set price inputs
        if (state.filters.minPrice) priceMinInput.value = state.filters.minPrice;
        if (state.filters.maxPrice) priceMaxInput.value = state.filters.maxPrice;
        
        // Set dimension inputs
        if (state.filters.minHeight) heightMinInput.value = state.filters.minHeight;
        if (state.filters.maxHeight) heightMaxInput.value = state.filters.maxHeight;
        if (state.filters.minWidth) widthMinInput.value = state.filters.minWidth;
        if (state.filters.maxWidth) widthMaxInput.value = state.filters.maxWidth;
        if (state.filters.minLength) lengthMinInput.value = state.filters.minLength;
        if (state.filters.maxLength) lengthMaxInput.value = state.filters.maxLength;
        
        // Set sort radios
        const sortByRadio = document.querySelector(`#sort-${state.filters.sortBy}`);
        const sortOrderRadio = document.querySelector(`#order-${state.filters.sortOrder}`);
        if (sortByRadio) sortByRadio.checked = true;
        if (sortOrderRadio) sortOrderRadio.checked = true;
        
        // Update page size selector
        if (pageSizeSelector) pageSizeSelector.value = state.pagination.pageSize;
    }
    
    // Load categories from API
    async function loadCategories() {
        try {
            const response = await fetch(`${API_BASE_URL}/category`);
            const data = await response.json();
            
            if (data.code === 1000) {
                state.categories = data.result;
                populateCategoryFilters();
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
                state.options = data.result;
                populateOptionsFilters();
            } else {
                console.error('Failed to fetch options:', data);
            }
        } catch (error) {
            console.error('Error fetching options:', error);
        }
    }
    
    // Populate category filters
    function populateCategoryFilters() {
        const categoryList = document.querySelector('#sidebar-category ul');
        if (!categoryList) return;
        
        categoryList.innerHTML = '';
        
        state.categories.forEach(category => {
            const li = document.createElement('li');
            li.innerHTML = `
                <input type="radio" id="category-${category.id}" name="category-option" value="${category.id}">
                <label for="category-${category.id}">${category.name}</label>
            `;
            categoryList.appendChild(li);
        });
    }
    
    // Populate color and material option filters
    function populateOptionsFilters() {
        // Find color and material options
        const colorOption = state.options.find(option => option.name.toLowerCase() === 'color1');
        const materialOption = state.options.find(option => option.name.toLowerCase() === 'material');
        
        // Populate color filters
        if (colorOption) {
            const colorList = document.querySelector('#sidebar-color ul');
            if (colorList) {
                colorList.innerHTML = '';
                
                colorOption.values.forEach(color => {
                    const li = document.createElement('li');
                    let backgroundColor = '#c4c4c4'; // Default color
                    
                    // Try to extract color from name
                    const colorName = color.name.toLowerCase();
                    if (colorName.includes('red')) backgroundColor = '#c13a3a';
                    else if (colorName.includes('blue')) backgroundColor = '#3a57c1';
                    else if (colorName.includes('green')) backgroundColor = '#3ac144';
                    else if (colorName.includes('black')) backgroundColor = '#262626';
                    else if (colorName.includes('gray')) backgroundColor = '#5a5a5a';
                    else if (colorName.includes('brown')) backgroundColor = '#4d380e';
                    else if (colorName.includes('white')) backgroundColor = '#ffffff';
                    
                    li.innerHTML = `
                        <input type="radio" id="color-${color.id}" name="color-option" value="${color.id}">
                        <label for="color-${color.id}">
                            <span class="color-circle" style="background-color: ${backgroundColor};"></span> ${color.name}
                        </label>
                    `;
                    colorList.appendChild(li);
                });
            }
        }
        
        // Populate material filters
        if (materialOption) {
            const materialList = document.querySelector('#sidebar-material ul');
            if (materialList) {
                materialList.innerHTML = '';
                
                materialOption.values.forEach(material => {
                    const li = document.createElement('li');
                    li.innerHTML = `
                        <input type="radio" id="material-${material.id}" name="material-option" value="${material.id}">
                        <label for="material-${material.id}">${material.name}</label>
                    `;
                    materialList.appendChild(li);
                });
            }
        }
    }
    
    // Load products based on current filters and pagination
    async function loadProducts() {
        showLoading(true);
        
        try {
            // Build query parameters
            const queryParams = new URLSearchParams({
                page: state.pagination.currentPage,
                size: state.pagination.pageSize,
                sortBy: state.filters.sortBy,
                sortOrder: state.filters.sortOrder
            });
            
            // Add filters if they have values
            if (state.filters.search) queryParams.append('search', state.filters.search);
            if (state.filters.categories) queryParams.append('categories', state.filters.categories);
            if (state.filters.color1s) queryParams.append('color1s', state.filters.color1s);
            if (state.filters.materials) queryParams.append('materials', state.filters.materials);
            if (state.filters.minPrice) queryParams.append('minPrice', state.filters.minPrice);
            if (state.filters.maxPrice) queryParams.append('maxPrice', state.filters.maxPrice);
            if (state.filters.minHeight) queryParams.append('minHeight', state.filters.minHeight);
            if (state.filters.maxHeight) queryParams.append('maxHeight', state.filters.maxHeight);
            if (state.filters.minWidth) queryParams.append('minWidth', state.filters.minWidth);
            if (state.filters.maxWidth) queryParams.append('maxWidth', state.filters.maxWidth);
            if (state.filters.minLength) queryParams.append('minLength', state.filters.minLength);
            if (state.filters.maxLength) queryParams.append('maxLength', state.filters.maxLength);
            
            // Make API call
            const response = await fetch(`${API_BASE_URL}/product/page/customer?${queryParams.toString()}`);
            const data = await response.json();
            
            if (data.code === 1000) {
                // Update state with pagination info
                state.pagination.currentPage = data.result.currentPage;
                state.pagination.pageSize = data.result.pageSize;
                state.pagination.totalItems = data.result.totalItems;
                state.pagination.totalPages = data.result.totalPages;
                
                // Render products
                renderProducts(data.result.products);
                
                // Update pagination UI
                updatePagination();
                
                // Update showing count
                updateShowingCount();
                
                // Update URL with current filters and pagination
                updateUrl();
            } else {
                console.error('Failed to fetch products:', data);
                productList.innerHTML = '<div class="error-message">Failed to load products. Please try again.</div>';
            }
        } catch (error) {
            console.error('Error fetching products:', error);
            productList.innerHTML = '<div class="error-message">An error occurred while loading products.</div>';
        } finally {
            showLoading(false);
        }
    }
    
    // Render products to the product list
    function renderProducts(products) {
        productList.innerHTML = '';
        
        if (!products || products.length === 0) {
            productList.innerHTML = '<div class="no-products">No products found matching your filters. Try adjusting your search criteria.</div>';
            return;
        }
        
        products.forEach(product => {
            // Get the base image URL from the object
            let baseImageUrl = '../../images/placeholder.png';
            if (product.baseImageUrl) {
                const imageId = Object.keys(product.baseImageUrl)[0];
                baseImageUrl = product.baseImageUrl[imageId];
            }
            
            // Create product card
            const card = document.createElement('div');
            card.className = 'product-card';
            
            // Format price in VND
            const formattedPrice = new Intl.NumberFormat('vi-VN').format(product.basePrice) + ' đ';
            
            // Generate color options HTML if available
            const colorOptionsHtml = generateColorOptions(product.productOptionResponseList);
            
            card.innerHTML = `
                <a href="../product/product.html?id=${product.productId}" class="product-link">
                    <img src="${baseImageUrl}" alt="${product.name}">
                </a>
                <div class="product-info">
                    <h3>${product.name}</h3>
                    <p>${product.category ? product.category.name : ''}</p>
                    ${colorOptionsHtml}
                    <p class="price">${formattedPrice}</p>
                </div>
            `;
            
            productList.appendChild(card);
        });
    }
    
    // Generate color options HTML for product card
    function generateColorOptions(productOptions) {
        if (!productOptions || productOptions.length === 0) return '';
        
        // Find color options
        const colorOption = productOptions.find(option => 
            option.optionName.toLowerCase().includes('color'));
        
        if (!colorOption) return '';
        
        let colorHTML = '<div class="color-list">';
        
        // Get up to 5 color values
        const colorValues = colorOption.productOptionValueResponseList.slice(0, 5);
        
        colorValues.forEach(color => {
            // Try to extract color from name or use a default
            let colorName = color.optionValueName.toLowerCase();
            let backgroundColor = '#c4c4c4'; // Default color
            
            if (colorName.includes('red')) backgroundColor = '#c13a3a';
            else if (colorName.includes('blue')) backgroundColor = '#3a57c1';
            else if (colorName.includes('green')) backgroundColor = '#3ac144';
            else if (colorName.includes('black')) backgroundColor = '#262626';
            else if (colorName.includes('gray')) backgroundColor = '#5a5a5a';
            else if (colorName.includes('brown')) backgroundColor = '#4d380e';
            else if (colorName.includes('white')) backgroundColor = '#ffffff; border: 1px solid #ccc';
            
            colorHTML += `<span class="color-circle" style="background-color: ${backgroundColor};"></span>`;
        });
        
        colorHTML += '</div>';
        return colorHTML;
    }
    
    // Update the showing count text
    function updateShowingCount() {
        const start = (state.pagination.currentPage - 1) * state.pagination.pageSize + 1;
        const end = Math.min(state.pagination.currentPage * state.pagination.pageSize, state.pagination.totalItems);
        showingCount.textContent = `Showing ${start}-${end} of ${state.pagination.totalItems} products`;
        
        // Hide load more button if we're on the last page
        if (loadMoreBtn) {
            loadMoreBtn.style.display = state.pagination.currentPage < state.pagination.totalPages ? 'block' : 'none';
        }
    }
    
    // Update pagination controls
    function updatePagination() {
        paginationContainer.innerHTML = '';
        
        // Only show pagination if there's more than one page
        if (state.pagination.totalPages <= 1) return;
        
        // Previous page button
        const prevItem = document.createElement('li');
        prevItem.className = 'page-item';
        prevItem.innerHTML = `<a href="#" class="page-link" ${state.pagination.currentPage === 1 ? 'disabled' : ''}>«</a>`;
        prevItem.addEventListener('click', (e) => {
            e.preventDefault();
            if (state.pagination.currentPage > 1) {
                state.pagination.currentPage--;
                loadProducts();
            }
        });
        paginationContainer.appendChild(prevItem);
        
        // Calculate which page numbers to show
        let startPage = Math.max(1, state.pagination.currentPage - 2);
        let endPage = Math.min(state.pagination.totalPages, startPage + 4);
        
        // Adjust if we're near the end
        if (endPage - startPage < 4 && startPage > 1) {
            startPage = Math.max(1, endPage - 4);
        }
        
        // Page number buttons
        for (let i = startPage; i <= endPage; i++) {
            const pageItem = document.createElement('li');
            pageItem.className = `page-item ${i === state.pagination.currentPage ? 'active' : ''}`;
            pageItem.innerHTML = `<a href="#" class="page-link">${i}</a>`;
            pageItem.addEventListener('click', (e) => {
                e.preventDefault();
                state.pagination.currentPage = i;
                loadProducts();
            });
            paginationContainer.appendChild(pageItem);
        }
        
        // Next page button
        const nextItem = document.createElement('li');
        nextItem.className = 'page-item';
        nextItem.innerHTML = `<a href="#" class="page-link" ${state.pagination.currentPage === state.pagination.totalPages ? 'disabled' : ''}>»</a>`;
        nextItem.addEventListener('click', (e) => {
            e.preventDefault();
            if (state.pagination.currentPage < state.pagination.totalPages) {
                state.pagination.currentPage++;
                loadProducts();
            }
        });
        paginationContainer.appendChild(nextItem);
    }
    
    // Update URL with current filters and pagination
    function updateUrl() {
        const queryParams = new URLSearchParams();
        
        // Add pagination parameters
        queryParams.set('page', state.pagination.currentPage);
        queryParams.set('size', state.pagination.pageSize);
        
        // Add filter parameters if they have values
        if (state.filters.search) queryParams.set('search', state.filters.search);
        if (state.filters.categories) queryParams.set('categories', state.filters.categories);
        if (state.filters.color1s) queryParams.set('color1s', state.filters.color1s);
        if (state.filters.materials) queryParams.set('materials', state.filters.materials);
        if (state.filters.minPrice) queryParams.set('minPrice', state.filters.minPrice);
        if (state.filters.maxPrice) queryParams.set('maxPrice', state.filters.maxPrice);
        if (state.filters.minHeight) queryParams.set('minHeight', state.filters.minHeight);
        if (state.filters.maxHeight) queryParams.set('maxHeight', state.filters.maxHeight);
        if (state.filters.minWidth) queryParams.set('minWidth', state.filters.minWidth);
        if (state.filters.maxWidth) queryParams.set('maxWidth', state.filters.maxWidth);
        if (state.filters.minLength) queryParams.set('minLength', state.filters.minLength);
        if (state.filters.maxLength) queryParams.set('maxLength', state.filters.maxLength);
        if (state.filters.sortBy !== 'id') queryParams.set('sortBy', state.filters.sortBy);
        if (state.filters.sortOrder !== 'desc') queryParams.set('sortOrder', state.filters.sortOrder);
        
        // Update URL without reloading the page
        const newUrl = `${window.location.pathname}?${queryParams.toString()}`;
        window.history.pushState({ path: newUrl }, '', newUrl);
    }
    
    // Show/hide loading indicator
    function showLoading(isLoading) {
        // If you have a loading indicator, update its visibility here
        if (isLoading) {
            productList.classList.add('loading');
        } else {
            productList.classList.remove('loading');
        }
    }
    
    // Set up event listeners
    function setupEventListeners() {
        // Filter show button click
        document.getElementById('filter-show').addEventListener('click', function() {
            // Get selected category
            const selectedCategory = document.querySelector('input[name="category-option"]:checked');
            if (selectedCategory) {
                state.filters.categories = selectedCategory.value;
            } else {
                state.filters.categories = '';
            }
            
            // Get selected color
            const selectedColor = document.querySelector('input[name="color-option"]:checked');
            if (selectedColor) {
                state.filters.color1s = selectedColor.value;
            } else {
                state.filters.color1s = '';
            }
            
            // Get selected material
            const selectedMaterial = document.querySelector('input[name="material-option"]:checked');
            if (selectedMaterial) {
                state.filters.materials = selectedMaterial.value;
            } else {
                state.filters.materials = '';
            }
            
            // Get price range
            state.filters.minPrice = priceMinInput.value || '';
            state.filters.maxPrice = priceMaxInput.value || '';
            
            // Get dimension values
            state.filters.minHeight = heightMinInput.value || '';
            state.filters.maxHeight = heightMaxInput.value || '';
            state.filters.minWidth = widthMinInput.value || '';
            state.filters.maxWidth = widthMaxInput.value || '';
            state.filters.minLength = lengthMinInput.value || '';
            state.filters.maxLength = lengthMaxInput.value || '';
            
            // Reset to first page when applying filters
            state.pagination.currentPage = 1;
            
            // Load products with updated filters
            loadProducts();
            
            // Close sidebars
            allSidebarFilterOff();
        });
        
        // Filter reset button click
        document.getElementById('filter-reset').addEventListener('click', function() {
            // Reset filter values
            state.filters = {
                search: '',
                categories: '',
                color1s: '',
                materials: '',
                minPrice: '',
                maxPrice: '',
                minHeight: '',
                maxHeight: '',
                minWidth: '',
                maxWidth: '',
                minLength: '',
                maxLength: '',
                sortBy: 'id',
                sortOrder: 'desc'
            };
            
            // Reset UI
            document.querySelectorAll('input[type="radio"]').forEach(radio => {
                radio.checked = false;
            });
            
            document.querySelectorAll('input[type="number"]').forEach(input => {
                input.value = '';
            });
            
            document.querySelectorAll('input[type="range"]').forEach(range => {
                range.value = range.min;
            });
            
            // Reset to first page
            state.pagination.currentPage = 1;
            
            // Load products with reset filters
            loadProducts();
        });
        
        // Sort show button click
        document.getElementById('sort-show').addEventListener('click', function() {
            // Get selected sort option
            const sortByOption = document.querySelector('input[name="sort-option"]:checked');
            if (sortByOption) {
                const value = sortByOption.value;
                
                // Parse the sort value
                if (value === 'popular-sort') {
                    state.filters.sortBy = 'id';
                    state.filters.sortOrder = 'desc';
                } else if (value === 'price-htl') {
                    state.filters.sortBy = 'base_price';
                    state.filters.sortOrder = 'desc';
                } else if (value === 'price-lth') {
                    state.filters.sortBy = 'base_price';
                    state.filters.sortOrder = 'asc';
                } else if (value === 'name-htl') {
                    state.filters.sortBy = 'name';
                    state.filters.sortOrder = 'desc';
                } else if (value === 'name-lth') {
                    state.filters.sortBy = 'name';
                    state.filters.sortOrder = 'asc';
                }
                
                // Load products with updated sort
                loadProducts();
                
                // Close sidebars
                allSidebarFilterOff();
            }
        });
        
        // Search button click
        if (document.getElementById('search-btn')) {
            document.getElementById('search-btn').addEventListener('click', function() {
                state.filters.search = searchInput.value.trim();
                state.pagination.currentPage = 1;
                loadProducts();
                // Remove CloseSearchPopup() call to keep popup open
            });
            
            // Enter key in search input
            searchInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    state.filters.search = searchInput.value.trim();
                    state.pagination.currentPage = 1;
                    loadProducts();
                    // Remove CloseSearchPopup() call to keep popup open
                }
            });
        }
        
        // Page size selector change
        if (pageSizeSelector) {
            pageSizeSelector.addEventListener('change', function() {
                state.pagination.pageSize = parseInt(pageSizeSelector.value);
                state.pagination.currentPage = 1; // Reset to first page when changing page size
                loadProducts();
            });
        }
        
        // Jump to page button click
        if (jumpToPageBtn && jumpToPageInput) {
            jumpToPageBtn.addEventListener('click', function() {
                const pageNum = parseInt(jumpToPageInput.value);
                if (pageNum && pageNum > 0 && pageNum <= state.pagination.totalPages) {
                    state.pagination.currentPage = pageNum;
                    loadProducts();
                } else {
                    alert(`Please enter a valid page number between 1 and ${state.pagination.totalPages}`);
                }
            });
            
            // Enter key in jump to page input
            jumpToPageInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    jumpToPageBtn.click();
                }
            });
        }
        
        // Load more button click
        if (loadMoreBtn) {
            loadMoreBtn.addEventListener('click', function() {
                if (state.pagination.currentPage < state.pagination.totalPages) {
                    state.pagination.currentPage++;
                    loadProducts();
                }
            });
        }
    }
});


// sidebar filter
const allSortBtn = document.querySelector(".sort-btn");
const allFilterBtn = document.querySelector(".all-filters");
const colorFilterBtn = document.querySelector(".color-filters");
const caterogyFilterBtn = document.querySelector(".category-filters");
const materialFilterBtn = document.querySelector(".material-filters");
const colorFiler = document.querySelector(".color-filter");
const caterogyFilter = document.querySelector(".caterogy-filter");
const materialFilter = document.querySelector(".material-filter");
const priceFilter = document.querySelector(".price-filter");
const dimensionFilter = document.querySelector(".dimension-filter");

allSortBtn.addEventListener("click", () => {
    document.getElementById('sidebar-sort').classList.toggle('show');
    document.getElementById('menu-overlay-filter').classList.toggle('show');
});
allFilterBtn.addEventListener("click", () => {
    document.getElementById('sidebar-filter').classList.toggle('show');
    document.getElementById('menu-overlay-filter').classList.toggle('show');
});
colorFilterBtn.addEventListener("click", () => {
    document.getElementById('sidebar-filter').classList.toggle('show');
    document.getElementById('menu-overlay-filter').classList.toggle('show');
    document.getElementById('sidebar-color').classList.toggle('show');
});
materialFilterBtn.addEventListener("click", () => {
    document.getElementById('sidebar-filter').classList.toggle('show');
    document.getElementById('menu-overlay-filter').classList.toggle('show');
    document.getElementById('sidebar-material').classList.toggle('show');
});
caterogyFilterBtn.addEventListener("click", () => {
    document.getElementById('sidebar-filter').classList.toggle('show');
    document.getElementById('menu-overlay-filter').classList.toggle('show');
    document.getElementById('sidebar-category').classList.toggle('show');
});
colorFiler.addEventListener("click", (event) => {
    event.preventDefault();
    document.getElementById('sidebar-color').classList.toggle('show');
    document.getElementById('sidebar-category').classList.remove('show');
    document.getElementById('sidebar-material').classList.remove('show');
    document.getElementById('sidebar-price').classList.remove('show');
    document.getElementById('sidebar-dimension').classList.remove('show');
});

caterogyFilter.addEventListener("click", (event) => {
    event.preventDefault();
    document.getElementById('sidebar-category').classList.toggle('show');
    document.getElementById('sidebar-color').classList.remove('show');
    document.getElementById('sidebar-material').classList.remove('show');
    document.getElementById('sidebar-price').classList.remove('show');
    document.getElementById('sidebar-dimension').classList.remove('show');
});
materialFilter.addEventListener("click", (event) => {
    event.preventDefault();
    document.getElementById('sidebar-material').classList.toggle('show');
    document.getElementById('sidebar-category').classList.remove('show');
    document.getElementById('sidebar-color').classList.remove('show');
    document.getElementById('sidebar-price').classList.remove('show');
    document.getElementById('sidebar-dimension').classList.remove('show');
});
priceFilter.addEventListener("click", (event) => {
    event.preventDefault();
    document.getElementById('sidebar-price').classList.toggle('show');
    document.getElementById('sidebar-category').classList.remove('show');
    document.getElementById('sidebar-color').classList.remove('show');
    document.getElementById('sidebar-material').classList.remove('show');
    document.getElementById('sidebar-dimension').classList.remove('show');
});
dimensionFilter.addEventListener("click", (event) => {
    event.preventDefault();
    document.getElementById('sidebar-dimension').classList.toggle('show');
    document.getElementById('sidebar-category').classList.remove('show');
    document.getElementById('sidebar-color').classList.remove('show');
    document.getElementById('sidebar-price').classList.remove('show');
    document.getElementById('sidebar-material').classList.remove('show');
});

function allSidebarFilterOff() {
    document.getElementById('sidebar-sort').classList.remove('show');
    document.getElementById('sidebar-filter').classList.remove('show');
    document.getElementById('sidebar-color').classList.remove('show');
    document.getElementById('sidebar-category').classList.remove('show');
    document.getElementById('sidebar-price').classList.remove('show');
    document.getElementById('sidebar-dimension').classList.remove('show');
    document.getElementById('sidebar-material').classList.remove('show');
    document.getElementById('menu-overlay-filter').classList.remove('show');
}

// Synchronize sliders and inputs
function syncSliderAndInput(slider, input) {
    slider.addEventListener("input", () => input.value = slider.value);
    input.addEventListener("input", () => slider.value = input.value);
}

// Synchronize sliders and inputs with min-max logic
function syncSliderAndInputWithMinMax(minSlider, maxSlider, minInput, maxInput) {
    minSlider.addEventListener("input", () => {
        if (parseInt(minSlider.value) > parseInt(maxSlider.value)) {
            maxSlider.value = minSlider.value;
            maxInput.value = minSlider.value;
        }
        minInput.value = minSlider.value;
    });

    maxSlider.addEventListener("input", () => {
        if (parseInt(maxSlider.value) < parseInt(minSlider.value)) {
            minSlider.value = maxSlider.value;
            minInput.value = maxSlider.value;
        }
        maxInput.value = maxSlider.value;
    });

    minInput.addEventListener("input", () => {
        if (parseInt(minInput.value) > parseInt(maxInput.value)) {
            maxInput.value = minInput.value;
            maxSlider.value = minInput.value;
        }
        minSlider.value = minInput.value;
    });

    maxInput.addEventListener("input", () => {
        if (parseInt(maxInput.value) < parseInt(minInput.value)) {
            minInput.value = maxInput.value;
            minSlider.value = maxInput.value;
        }
        maxSlider.value = maxInput.value;
    });
}

// Initialize price sliders with min-max logic
const priceMinSlider = document.getElementById("price-min");
const priceMaxSlider = document.getElementById("price-max");
const priceMinInput = document.getElementById("price-min-input");
const priceMaxInput = document.getElementById("price-max-input");
syncSliderAndInputWithMinMax(priceMinSlider, priceMaxSlider, priceMinInput, priceMaxInput);

// Initialize dimension sliders with min-max logic
["height", "length", "width"].forEach(dim => {
    const minSlider = document.getElementById(`dimension-${dim}-min`);
    const maxSlider = document.getElementById(`dimension-${dim}-max`);
    const minInput = document.getElementById(`dimension-${dim}-min-input`);
    const maxInput = document.getElementById(`dimension-${dim}-max-input`);
    syncSliderAndInputWithMinMax(minSlider, maxSlider, minInput, maxInput);
});

// Reset filters
document.getElementById("filter-reset").addEventListener("click", () => {
    // Reset checkboxes
    document.querySelectorAll("#sidebar-filter input[type='checkbox'], #sidebar-color input[type='checkbox'], #sidebar-material input[type='checkbox'], #sidebar-category input[type='checkbox']").forEach(cb => {
        cb.checked = false; // Ensure all checkboxes are unchecked
    });

    // Reset price sliders and inputs
    [priceMinSlider, priceMaxSlider].forEach(slider => slider.value = slider.min);
    [priceMinInput, priceMaxInput].forEach(input => input.value = input.min);

    // Reset dimension sliders and inputs
    ["height", "length", "width"].forEach(dim => {
        document.getElementById(`dimension-${dim}-min`).value = 0;
        document.getElementById(`dimension-${dim}-min-input`).value = 0;
        document.getElementById(`dimension-${dim}-max`).value = 0;
        document.getElementById(`dimension-${dim}-max-input`).value = 0;
    });
});

// Show selected filters
document.getElementById("filter-show").addEventListener("click", () => {
    const selectedFilters = {
        colors: Array.from(document.querySelectorAll(".sidebar-color input:checked")).map(cb => cb.value),
        materials: Array.from(document.querySelectorAll(".sidebar-material input:checked")).map(cb => cb.value),
        categories: Array.from(document.querySelectorAll(".sidebar-category input:checked")).map(cb => cb.value),
        price: [priceMinInput.value, priceMaxInput.value],
        dimensions: {
            height: document.getElementById("dimension-height-input").value,
            length: document.getElementById("dimension-length-input").value,
            width: document.getElementById("dimension-width-input").value
        }
    };
    console.log("Selected Filters:", selectedFilters);
});

document.getElementById("sort-show").addEventListener("click", () => {
    const selectedSortOption = document.querySelector('input[name="sort-option"]:checked');
    if (selectedSortOption) {
        console.log("Selected Sort Option:", selectedSortOption.value);
        // Add logic to sort products based on the selected option
    } else {
        alert("Please select a sort option.");
    }
});



// Header and Navigation Menu
const header = document.querySelector(".header");
const navLinks = document.querySelectorAll(".nav-links a");
const navIcons = document.querySelectorAll(".nav-icons img");
const menuIcon = document.querySelector(".menu-icon");
const furMenu = document.querySelector(".fur-menu");
const collectionMenu = document.querySelector(".collecion-menu");
const careMenu = document.querySelector(".care-menu");
const logo = document.querySelector(".logo img");
const logoName = document.querySelector(".logo-name");

menuIcon.addEventListener("click", () => {
    document.getElementById('sidebar').classList.toggle('show');
    document.getElementById('menu-overlay').classList.toggle('show');
});

furMenu.addEventListener("click", (event) => {
    event.preventDefault();
    document.getElementById('sidebar-fur').classList.toggle('show');
    document.getElementById('sidebar-collection').classList.remove('show');
    document.getElementById('sidebar-care').classList.remove('show');
});

// collectionMenu.addEventListener("click", (event) => {
//     event.preventDefault();
//     document.getElementById('sidebar-collection').classList.toggle('show');
//     document.getElementById('sidebar-fur').classList.remove('show');
//     document.getElementById('sidebar-care').classList.remove('show');
// });
careMenu.addEventListener("click", (event) => {
    event.preventDefault();
    document.getElementById('sidebar-care').classList.toggle('show');
    document.getElementById('sidebar-collection').classList.remove('show');
    document.getElementById('sidebar-fur').classList.remove('show');
});


// User dropdown menu
const userIcon = document.querySelector(".user-icon");
const dropdownMenu = document.querySelector(".dropdown-menu");
const signinItem = document.getElementById("signin");
const signupItem = document.getElementById("signup");
const infoItem = document.getElementById("info");
const logoutItem = document.getElementById("logout");

// Update dropdown menu based on login state
function updateDropdownMenu() {
    const token = localStorage.getItem("authToken");
    if (token) {
        signinItem.style.display = "none";
        signupItem.style.display = "none";
        infoItem.style.display = "block";
        logoutItem.style.display = "block";
    } else {
        signinItem.style.display = "block";
        signupItem.style.display = "block";
        infoItem.style.display = "none";
        logoutItem.style.display = "none";
    }
}

// Update dropdown menu on page load
updateDropdownMenu();

// Logout functionality
logoutItem.addEventListener("click", async (event) => {
    event.preventDefault();
    const token = localStorage.getItem("authToken");
    if (token) {
        try {
            const response = await fetch("http://127.0.0.1:8080/api/auth/logout", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ token }),
            });
            const data = await response.json();
            if (data.code === 1000) {
                localStorage.removeItem("authToken");
                updateDropdownMenu();
                window.location.href = "../io/signin.html";
            } else {
                console.error("Failed to logout: Invalid response code");
            }
        } catch (error) {
            console.error("Failed to logout:", error);
        }
    }
});

userIcon.addEventListener("click", (event) => {
    event.stopPropagation(); // Prevent click from propagating to the document
    document.querySelector(".user-dropdown").classList.toggle("active");
});

document.addEventListener("click", () => {
    document.querySelector(".user-dropdown").classList.remove("active");
});

// Prevent closing the dropdown when clicking inside it
dropdownMenu.addEventListener("click", (event) => {
    event.stopPropagation();
});

function allSidebarOff() {
    document.getElementById('sidebar').classList.remove('show');
    document.getElementById('sidebar-fur').classList.remove('show');
    document.getElementById('sidebar-collection').classList.remove('show');
    document.getElementById('menu-overlay').classList.remove('show');
    document.getElementById('sidebar-care').classList.remove('show');
    searchPopup.classList.remove('show');
}

// serach popup
const searchIcon = document.querySelector(".nav-icons img[alt='Search']");
const searchPopup = document.getElementById("search-popup");
const searchInput = document.getElementById("search-input");
const searchResultsList = document.getElementById("search-results-list");

searchIcon.addEventListener("click", () => {
const searchPopup = document.getElementById("search-popup");
    searchPopup.classList.toggle('show');
    document.getElementById('menu-overlay').classList.toggle('show');
});

function CloseSearchPopup() {
    searchPopup.classList.remove('show');
    document.getElementById('menu-overlay').classList.remove('show');
    searchInput.value = "";
    searchResultsList.innerHTML = "";
}

async function performSearch() {
    const query = searchInput.value.trim();
    if (!query) return;

    try {
        // Show loading state
        searchResultsList.innerHTML = '<li style="text-align: center; padding: 20px;">Searching...</li>';
        
        // Call API with search parameter
        const response = await fetch(`${API_BASE_URL}/product/page/customer?page=1&size=10&search=${encodeURIComponent(query)}`);
        const data = await response.json();
        
        if (data.code === 1000 && data.result && data.result.products) {
            displaySearchResults(data.result.products);
        } else {
            searchResultsList.innerHTML = '<li style="text-align: center; padding: 20px; color: #666;">No products found</li>';
        }
    } catch (error) {
        console.error('Search error:', error);
        searchResultsList.innerHTML = '<li style="text-align: center; padding: 20px; color: #dc3545;">Error occurred while searching</li>';
    }
}

function displaySearchResults(products) {
    if (!products || products.length === 0) {
        searchResultsList.innerHTML = '<li style="text-align: center; padding: 20px; color: #666;">No products found</li>';
        return;
    }
    
    searchResultsList.innerHTML = '';
    
    products.forEach(product => {
        // Get base image URL
        let baseImageUrl = '../../images/placeholder.png';
        if (product.baseImageUrl) {
            const imageId = Object.keys(product.baseImageUrl)[0];
            baseImageUrl = product.baseImageUrl[imageId];
        }
        
        // Format price
        const formattedPrice = new Intl.NumberFormat('vi-VN').format(product.basePrice) + ' đ';
        
        // Create search result item
        const li = document.createElement('li');
        li.style.cssText = 'padding: 10px; border-bottom: 1px solid #eee; cursor: pointer; display: flex; align-items: center; gap: 15px;';
        li.innerHTML = `
            <img src="${baseImageUrl}" alt="${product.name}" 
                 style="width: 60px; height: 60px; object-fit: cover; border-radius: 5px;"
                 onerror="this.src='../../images/placeholder.png'">
            <div style="flex: 1;">
                <h4 style="margin: 0 0 5px 0; font-size: 16px; font-weight: 600;">${product.name}</h4>
                <p style="margin: 0; color: #666; font-size: 14px;">${product.category ? product.category.name : ''}</p>
                <p style="margin: 5px 0 0 0; font-weight: bold; color: #000;">${formattedPrice}</p>
            </div>
        `;
        
        // Add click handler to navigate to product page
        li.addEventListener('click', () => {
            window.location.href = `../product/product.html?id=${product.productId}`;
        });
        
        // Add hover effect
        li.addEventListener('mouseenter', () => {
            li.style.backgroundColor = '#f5f5f5';
        });
        li.addEventListener('mouseleave', () => {
            li.style.backgroundColor = 'transparent';
        });
        
        searchResultsList.appendChild(li);
    });
}

// Add Enter key support for search input in the main search functionality
searchInput.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        performSearch();
        // Don't close popup - let user see results
    }
});
