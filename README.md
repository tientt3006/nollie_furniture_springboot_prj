(English version below)
# 🏠 NollieConcept - Website Bán hàng trực tuyến (đồ nội thất)

**NollieConcept** là một hệ thống thương mại điện tử chuyên cung cấp các sản phẩm nội thất, được xây dựng dựa trên kiến trúc **Client-Server**. Dự án tập trung vào việc tối ưu hóa trải nghiệm người dùng trong việc tìm kiếm, lựa chọn và mua sắm nội thất trực tuyến, đồng thời cung cấp công cụ quản trị mạnh mẽ cho người quản lý,,.

## 🛠 Công Nghệ Sử Dụng

Dự án sử dụng các công nghệ hiện đại để đảm bảo hiệu năng, tính bảo mật và khả năng mở rộng:

### Backend
*   **Java & Spring Boot:** Framework chính giúp phát triển ứng dụng nhanh chóng, cấu hình tự động và hoạt động độc lập (Standalone),.
*   **Spring Security:** Quản lý xác thực (Authentication) và phân quyền (Authorization), tích hợp mã hóa mật khẩu **BCrypt** để bảo mật thông tin người dùng.
*   **JWT (JSON Web Token):** Sử dụng token để xác thực bảo mật trong truyền thông giữa Client và Server.
*   **JPA/Hibernate:** Tương tác với cơ sở dữ liệu.
*   **AWS S3 Bucket:** Dịch vụ lưu trữ đám mây dùng để lưu trữ và truy xuất hình ảnh sản phẩm, đảm bảo tốc độ và khả năng mở rộng.

### Frontend
*   **HTML5, CSS3, JavaScript:** Xây dựng cấu trúc và giao diện người dùng,.
*   **Fetch API:** Thực hiện các yêu cầu HTTP bất đồng bộ (AJAX) để tương tác với Backend mà không cần tải lại trang.

### Cơ sở dữ liệu
*   **MySQL:** Hệ quản trị cơ sở dữ liệu quan hệ lưu trữ thông tin người dùng, sản phẩm, đơn hàng.

### Công cụ phát triển
*   **IDE:** IntelliJ IDEA, Visual Studio Code.
*   **Thiết kế:** ERD Diagram, Data Flow Diagram, Sequence Diagram.

## 🚀 Chức Năng Chính

Hệ thống phân chia quyền hạn rõ ràng giữa hai tác nhân: **Khách hàng (Customer)** và **Quản trị viên (Admin)**.

### 👤 Khách Hàng (Customer)
*   **Authentication:** Đăng ký tài khoản (xác thực qua email), Đăng nhập, Quên mật khẩu,.
*   **Quản lý thông tin cá nhân:** Cập nhật hồ sơ, thay đổi mật khẩu.
*   **Mua sắm:**
    *   Xem danh sách và chi tiết sản phẩm (kích thước, màu sắc, tùy chọn).
    *   Tìm kiếm và lọc sản phẩm nâng cao.
    *   Thêm sản phẩm vào giỏ hàng, cập nhật số lượng hoặc xóa sản phẩm.
*   **Đặt hàng & Thanh toán:**
    *   Thực hiện quy trình Checkout.
    *   Hỗ trợ phương thức thanh toán COD (Cash On Delivery).
*   **Lịch sử đơn hàng:** Theo dõi trạng thái đơn hàng (Đang giao, Đã nhận, Đã hủy).

### 🛡️ Quản Trị Viên (Admin)
*   **Quản lý người dùng:** Xem danh sách, khóa/mở khóa tài khoản người dùng,.
*   **Quản lý danh mục (Categories):** Thêm, sửa, xóa các loại sản phẩm.
*   **Quản lý sản phẩm (Products):**
    *   CRUD (Thêm, Xem, Sửa, Xóa) sản phẩm.
    *   Tích hợp upload ảnh trực tiếp lên **AWS S3**,.
    *   Quản lý các tùy chọn (Options) của sản phẩm (màu sắc, vật liệu...).
*   **Quản lý đơn hàng:** Xem danh sách đơn hàng, cập nhật trạng thái đơn hàng (Xác nhận, Đang giao, Hoàn thành, Hủy),.
*   **Thống kê:** (Tính năng đang phát triển) Thống kê doanh số và hoạt động.

## 💾 Thiết Kế Cơ Sở Dữ Liệu

Cơ sở dữ liệu bao gồm các bảng chính quan trọng sau-:
*   `users`: Lưu thông tin tài khoản, vai trò (Role: ADMIN, CUSTOMER).
*   `products`, `categories`: Quản lý thông tin hàng hóa.
*   `product_options`, `option_values`: Lưu trữ các biến thể sản phẩm (size, màu).
*   `orders`, `order_items`: Lưu trữ thông tin giao dịch.
*   `carts`, `cart_items`: Quản lý giỏ hàng tạm thời.

## 🔌 API Documentation

Update later. API document in .pdf file is not belong to this project.

## ⚙️ Cài Đặt & Triển Khai

Để chạy dự án trên máy cục bộ, vui lòng thực hiện các bước sau:

1.  **Clone repository:**
    ```bash
    git clone https://github.com/username/nollie-concept.git
    ```
2.  **Cấu hình Database:**
    *   Tạo cơ sở dữ liệu MySQL.
    *   Cập nhật thông tin kết nối (url, username, password) trong file `application.properties`.
3.  **Cấu hình AWS S3:**
    *   Cung cấp `accessKey`, `secretKey`, `region`, và `bucketName` trong file cấu hình để tính năng upload ảnh hoạt động.
4.  **Chạy Backend:**
    *   Mở dự án bằng IntelliJ IDEA hoặc Eclipse.
    *   Chạy `NollieConceptApplication.java`.
5.  **Chạy Frontend:**
    *   Mở thư mục frontend bằng VS Code.
    *   Sử dụng Live Server hoặc mở trực tiếp file `index.html` (đảm bảo cấu hình đúng đường dẫn API).

## 📸 Hình Ảnh Demo

See .pdf file.

---
---

# 🏠 NollieConcept – Online Furniture E-commerce Website

**NollieConcept** is an e-commerce system specializing in furniture products, built on a **Client–Server** architecture. The project focuses on optimizing user experience for searching, selecting, and purchasing furniture online, while also providing powerful management tools for administrators.

## 🛠 Technologies Used

The project uses modern technologies to ensure performance, security, and scalability.

### Backend

* **Java & Spring Boot:** Core framework enabling rapid application development, auto-configuration, and standalone deployment.
* **Spring Security:** Manages authentication and authorization, integrates **BCrypt** password hashing to secure user credentials.
* **JWT (JSON Web Token):** Token-based authentication for secure communication between Client and Server.
* **JPA/Hibernate:** Database interaction layer.
* **AWS S3 Bucket:** Cloud storage service for storing and retrieving product images, ensuring speed and scalability.

### Frontend

* **HTML5, CSS3, JavaScript:** Used to build the structure and user interface.
* **Fetch API:** Performs asynchronous HTTP (AJAX) requests to interact with the Backend without page reloads.

### Database

* **MySQL:** Relational database management system storing user, product, and order information.

### Development Tools

* **IDE:** IntelliJ IDEA, Visual Studio Code.
* **Design:** ERD Diagram, Data Flow Diagram, Sequence Diagram.

## 🚀 Main Features

The system clearly separates permissions between two roles: **Customer** and **Admin**.

### 👤 Customer

* **Authentication:** Account registration (email verification), login, forgot password.
* **Profile management:** Update personal information, change password.
* **Shopping:**

  * View product lists and product details (dimensions, colors, options).
  * Advanced product search and filtering.
  * Add products to cart, update quantities, or remove items.
* **Order & Payment:**

  * Perform checkout process.
  * Support COD (Cash On Delivery) payment method.
* **Order history:** Track order status (Shipping, Completed, Cancelled).

### 🛡️ Admin

* **User management:** View user list, lock/unlock user accounts.
* **Category management:** Add, edit, delete product categories.
* **Product management:**

  * CRUD (Create, Read, Update, Delete) products.
  * Integrate direct image upload to **AWS S3**.
  * Manage product options (color, material, etc.).
* **Order management:** View orders, update order status (Confirmed, Shipping, Completed, Cancelled).
* **Statistics:** (Feature under development) Sales and activity statistics.

## 💾 Database Design

The database includes the following main tables:

* `users`: Store account information and roles (Role: ADMIN, CUSTOMER).
* `products`, `categories`: Manage product information.
* `product_options`, `option_values`: Store product variations (size, color).
* `orders`, `order_items`: Store transaction information.
* `carts`, `cart_items`: Manage temporary shopping carts.

## 🔌 API Documentation

Update later. API documentation in PDF format is not part of this project.

## ⚙️ Installation & Deployment

To run the project locally, follow these steps:

1. **Clone repository:**

   ```bash
   git clone https://github.com/username/nollie-concept.git
   ```
2. **Configure Database:**

   * Create a MySQL database.
   * Update connection information (url, username, password) in `application.properties`.
3. **Configure AWS S3:**

   * Provide `accessKey`, `secretKey`, `region`, and `bucketName` in the configuration file to enable image upload.
4. **Run Backend:**

   * Open the project in IntelliJ IDEA or Eclipse.
   * Run `NollieConceptApplication.java`.
5. **Run Frontend:**

   * Open the frontend directory in VS Code.
   * Use Live Server or open `index.html` directly (ensure API endpoint paths are configured correctly).

## 📸 Demo Images

See the PDF file.
