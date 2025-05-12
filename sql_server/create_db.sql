--CREATE DATABASE NollieFurnitureStoreDB;
USE NollieFurnitureStoreDB;
CREATE TABLE invalidated_token (
    id NVARCHAR(500) PRIMARY KEY,
    expiry_time DATETIME
);

-- 1. Bảng độc lập
CREATE TABLE categories (
  id       INT IDENTITY NOT NULL, 
  name     NVARCHAR(255) NOT NULL UNIQUE, 
  img_url  NVARCHAR(500), 
  PRIMARY KEY (id)
);

CREATE TABLE options (
  id   INT IDENTITY NOT NULL, 
  name NVARCHAR(255) NOT NULL, 
  PRIMARY KEY (id)
);
ALTER TABLE options
ADD CONSTRAINT UQ_options_name UNIQUE (name);


CREATE TABLE users (
  id       INT IDENTITY NOT NULL, 
  full_name NVARCHAR(255) NOT NULL, 
  email    NVARCHAR(255) UNIQUE NOT NULL, 
  phone    NVARCHAR(20), 
  password NVARCHAR(255) NOT NULL, 
  role     NVARCHAR(20) NOT NULL, 
  address  NVARCHAR(255), 
  active   BIT NOT NULL, 
  PRIMARY KEY (id),
  CONSTRAINT chk_role CHECK (role IN ('ADMIN', 'STAFF', 'CUSTOMER'))
);


-- 2. Bảng có khóa ngoại tham chiếu các bảng đã có
CREATE TABLE verification_code (
	id INT IDENTITY NOT NULL,
	users_id INT NOT NULL,
	code VARCHAR(255),
	created_at DATETIME,
	expires_at DATETIME,
	PRIMARY KEY (id),
	FOREIGN KEY (users_id) REFERENCES users(id)
);
CREATE TABLE products (
  id          INT IDENTITY NOT NULL, 
  category_id INT, 
  name        NVARCHAR(255) NOT NULL, 
  base_price  DECIMAL(18, 2) NOT NULL, 
  height      DECIMAL(18, 2), 
  width       DECIMAL(18, 2), 
  length      DECIMAL(18, 2), 
  description NVARCHAR(500), 
  base_product_quantity INT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE options_value (
  id       INT IDENTITY NOT NULL, 
  option_id INT NOT NULL, 
  value    NVARCHAR(255) NOT NULL, 
  img_url  NVARCHAR(500), 
  PRIMARY KEY (id),
  FOREIGN KEY (option_id) REFERENCES options(id)
);

CREATE TABLE products_options (
  id        INT IDENTITY NOT NULL, 
  product_id INT NOT NULL, 
  option_id  INT NOT NULL, 
  PRIMARY KEY (id),
  FOREIGN KEY (product_id) REFERENCES products(id),
  FOREIGN KEY (option_id) REFERENCES options(id)
);

-- 3. Bảng liên kết giữa các bảng
CREATE TABLE products_img (
  id        INT IDENTITY NOT NULL, 
  product_id INT NOT NULL, 
  img_url   NVARCHAR(500), 
  img_name  NVARCHAR(255), 
  PRIMARY KEY (id),
  FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE products_options_value (
  id              INT IDENTITY NOT NULL, 
  product_option_id INT NOT NULL, 
  option_value_id   INT NOT NULL, 
  quantity        INT NOT NULL, 
  add_price       DECIMAL(18, 2) NOT NULL, 
  img_url         NVARCHAR(500), 
  PRIMARY KEY (id),
  FOREIGN KEY (product_option_id) REFERENCES products_options(id),
  FOREIGN KEY (option_value_id) REFERENCES options_value(id)
);

-- 4. Bảng giỏ hàng và các bảng liên quan
CREATE TABLE carts (
  id      INT IDENTITY NOT NULL, 
  user_id INT NOT NULL, 
  total   DECIMAL(18, 2) NOT NULL, 
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE carts_item (
  id         INT IDENTITY NOT NULL, 
  quantity   INT NOT NULL, 
  cart_id    INT NOT NULL, 
  product_id INT, 
  product_option_value_id INT,
  PRIMARY KEY (id),
  FOREIGN KEY (cart_id) REFERENCES carts(id),
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
  FOREIGN KEY (product_option_value_id) REFERENCES products_options_value(id) ON DELETE SET NULL
);
ALTER TABLE carts_item
ADD item_price DECIMAL(18, 2) NOT NULL DEFAULT 0;


-- 5. Bảng đơn hàng và các bảng liên quan
CREATE TABLE orders (
  id             INT IDENTITY NOT NULL, 
  user_id        INT NOT NULL, 
  order_date     DATETIME NOT NULL, 
  cancel_date    DATETIME,
  start_delivery_date DATETIME,
  receive_date   DATETIME,
  total          DECIMAL(18, 2) NOT NULL, 
  status         NVARCHAR(255) NOT NULL, 
  status_detail  NVARCHAR(500),
  refund         DECIMAL(2, 2),
  full_name      NVARCHAR(255) NOT NULL, 
  address        NVARCHAR(255) NOT NULL, 
  email          NVARCHAR(255) NOT NULL, 
  phone          NVARCHAR(20) NOT NULL, 
  payment_method NVARCHAR(255), 
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
ALTER TABLE orders
ADD CONSTRAINT chk_status CHECK (status IN ('ORDER_SUCCESSFUL', 'ON_DELIVERY', 'RECEIVED', 'CANCELED'));

ALTER TABLE orders
ADD CONSTRAINT chk_refund CHECK (refund >= 0 AND refund <= 1);

CREATE TABLE orders_item (
  id         INT IDENTITY NOT NULL, 
  order_id   INT NOT NULL, 
  product_id INT, 
  quantity   INT NOT NULL, 
  product_option_value_id INT, 
  PRIMARY KEY (id),
  FOREIGN KEY (order_id) REFERENCES orders(id),
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
  FOREIGN KEY (product_option_value_id) REFERENCES products_options_value(id) ON DELETE SET NULL
);
ALTER TABLE orders_item
ADD item_price DECIMAL(18, 2) NOT NULL DEFAULT 0;