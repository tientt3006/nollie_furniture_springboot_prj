document.addEventListener("DOMContentLoaded", function() {
    const productList = document.getElementById("productList");
    const loadMoreBtn = document.getElementById("loadMore");
    const showingCount = document.getElementById("showingCount");
    let productsToShow = 15;
    const totalProducts = 399;

    function createProductCard(index) {
        const card = document.createElement("div");
        card.classList.add("product-card");
        card.innerHTML = `
            <div class="product-card">
                        <a href="../product/product.html" class="product-link">
                        <img src="../../images/topselling_product_1.png" alt="Sweet art chair">
                        </a>
                        <div class="product-info">
                            <h3>Sweet art chair</h3>
                            <p>Fabric</p>
                            <div class="color-list">
                                <span class="color-circle" style="background-color: #c4c4c4;"></span>
                                <span class="color-circle" style="background-color: #5a5a5a;"></span>
                                <span class="color-circle" style="background-color: #262626;"></span>
                                <span class="color-circle" style="background-color: #4d380e;"></span>
                                <span class="color-circle" style="background-color: #797102;"></span>
                                
                            </div>
                            <p class="price">27,390,000 đ</p>
                        </div>
                    </div>
        `;
        return card;
    }

    function loadProducts() {
        for (let i = 0; i < productsToShow; i++) {
            productList.appendChild(createProductCard(i + 1));
        }
    }

    loadProducts();

    loadMoreBtn.addEventListener("click", function() {
        productsToShow += 24;
        productList.innerHTML = "";
        loadProducts();
        showingCount.textContent = `Showing ${Math.min(productsToShow, totalProducts)} of ${totalProducts} products`;
        if (productsToShow >= totalProducts) {
            loadMoreBtn.style.display = "none";
        }
    });
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

// Simulate login state (replace with actual logic)
let isLoggedIn = false;

function updateDropdownMenu() {
    if (isLoggedIn) {
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

// Example: Toggle login state on logout click
logoutItem.addEventListener("click", (event) => {
    event.preventDefault();
    isLoggedIn = false;
    updateDropdownMenu();
});

// Example: Simulate login on sign-in click
signinItem.addEventListener("click", (event) => {
    event.preventDefault();
    isLoggedIn = true;
    updateDropdownMenu();
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
    // searchPopup.style.display = "none";
    document.getElementById('menu-overlay').classList.remove('show');
    searchInput.value = "";
    searchResultsList.innerHTML = "";
}

function performSearch() {
    const query = searchInput.value.trim();
    if (!query) return;

    // Simulate search results (replace with actual search logic)
    const results = Array.from({ length: 15 }, (_, i) => `Product ${i + 1}`);
    searchResultsList.innerHTML = results
        .slice(0, 10)
        .map(result => `<li>${result}</li>`)
        .join("");

    if (results.length > 10) {
        document.getElementById("show-all-results-btn").style.display = "block";
    } else {
        document.getElementById("show-all-results-btn").style.display = "none";
    }
}

function showAllResults() {
    alert("Redirecting to all results page...");
    // Implement redirection logic here
}
