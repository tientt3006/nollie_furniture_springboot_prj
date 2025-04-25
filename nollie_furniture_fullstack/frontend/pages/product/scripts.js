// Thay đổi hình ảnh chính khi click vào thumbnail
document.querySelectorAll('.thumbnail').forEach(img => {
    img.addEventListener('click', function() {
        document.getElementById('main-image').src = this.src;
    });
});

// Hiển thị thêm ảnh khi click vào nút "Hiển thị thêm ảnh"
document.getElementById('show-more').addEventListener('click', function() {
    const thumbnailContainer = document.querySelector('.thumbnail-container');
    
    // Thêm các ảnh mới vào container
    const newThumbnails = [
        '../../images/prod1_5.png',
        '../../images/prod1_6.png',
        '../../images/prod1_7.png'
    ];

    newThumbnails.forEach(src => {
        const img = document.createElement('img');
        img.src = src;
        img.alt = 'New Thumbnail';
        img.classList.add('thumbnail');
        img.style.cursor = 'pointer';

        // Thêm sự kiện click cho ảnh mới
        img.addEventListener('click', function() {
            document.getElementById('main-image').src = this.src;
        });

        thumbnailContainer.appendChild(img);
    });

    // Ẩn nút "Hiển thị thêm ảnh" sau khi thêm ảnh
    this.style.display = 'none';
});

// Khi nhấn nút thay đổi thông tin sản phẩm -> mở menu dọc ở bên phải
document.querySelectorAll('.option-btn').forEach(btn => {
    btn.addEventListener('click', function() {
      const option = this.dataset.option;
      showOptionMenu(option, this);
    });
  });
  
  function showOptionMenu(option, button) {
    // Kiểm tra xem menu đã tồn tại chưa
    let sidebar = document.getElementById('option-menu');
    if (!sidebar) {
      sidebar = document.createElement('div');
      sidebar.id = 'option-menu';
      // Thiết lập style menu dọc
      Object.assign(sidebar.style, {
        position: 'fixed',
        top: '0',
        right: '0',
        width: '250px',
        height: '100%',
        backgroundColor: '#fff',
        color: '#000',
        boxShadow: '-2px 0 5px rgba(0,0,0,0.1)',
        zIndex: '9999',
        padding: '20px',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between'
      });
      document.body.appendChild(sidebar);
    }
  
    // Xóa nội dung cũ, thêm nội dung mới
    sidebar.innerHTML = '';
    const title = document.createElement('h2');
    title.textContent = `Chọn ${option}`;
    sidebar.appendChild(title);
  
    // Ví dụ các lựa chọn tùy theo option
    const content = document.createElement('div');
    content.style.flexGrow = '1';
    if (option === 'color') {
      content.innerHTML = `
        <p>Ví dụ các màu:</p>
        <label><input type="radio" name="color" value="Black" checked>Black</label><br>
        <label><input type="radio" name="color" value="White">White</label><br>
        <label><input type="radio" name="color" value="Grey">Grey</label>
      `;
    } else if (option === 'material') {
      content.innerHTML = `
        <p>Ví dụ các chất liệu:</p>
        <label><input type="radio" name="material" value="Fabric" checked>Fabric</label><br>
        <label><input type="radio" name="material" value="Leather">Leather</label><br>
        <label><input type="radio" name="material" value="Metal">Metal</label>
      `;
    } else {
      content.innerHTML = `
        <p>Ví dụ các kích thước:</p>
        <label><input type="radio" name="dimension" value="60×60×100" checked>60×60×100</label><br>
        <label><input type="radio" name="dimension" value="50×50×90">50×50×90</label><br>
        <label><input type="radio" name="dimension" value="70×70×110">70×70×110</label>
      `;
    }
    sidebar.appendChild(content);
  
    // Nút Lưu
    const saveBtn = document.createElement('button');
    saveBtn.textContent = 'Save';
    Object.assign(saveBtn.style, {
      background: '#000',
      color: '#fff',
      border: 'none',
      borderRadius: '4px',
      padding: '12px',
      cursor: 'pointer',
      fontSize: '16px'
    });
    saveBtn.addEventListener('click', function() {
      // Lấy giá trị được chọn
      const checkedInput = sidebar.querySelector('input[type="radio"]:checked');
      if (checkedInput) {
        const newValue = checkedInput.value;
        button.textContent = `${option.charAt(0).toUpperCase() + option.slice(1)}: ${newValue} `;
        const span = document.createElement('span');
        span.textContent = 'Change >';
        button.appendChild(span);
      }
  
      // Đóng menu
      sidebar.remove();
    });
    sidebar.appendChild(saveBtn);
  }



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

collectionMenu.addEventListener("click", (event) => {
    event.preventDefault();
    document.getElementById('sidebar-collection').classList.toggle('show');
    document.getElementById('sidebar-fur').classList.remove('show');
    document.getElementById('sidebar-care').classList.remove('show');
});
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

// Add to cart functionality
document.querySelector('.add-to-cart').addEventListener('click', function () {
    const productName = document.querySelector('.product-info h1').textContent;
    const productPrice = document.querySelector('.product-info .price').textContent;

    // Simulate adding to cart (replace with actual logic)
    alert(`Added "${productName}" to cart for ${productPrice}.`);

    // Optionally, update cart UI or redirect to cart page
});
