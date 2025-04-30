INSERT INTO Cart
  (id, 
  Userid, 
  total) 
VALUES 
  (?, 
  ?, 
  ?);
INSERT INTO CartItem
  (id, 
  quantity, 
  Cartid, 
  Productid) 
VALUES 
  (?, 
  ?, 
  ?, 
  ?);
INSERT INTO CartitemOption
  (id, 
  CartItemid, 
  ProductOptionValueid) 
VALUES 
  (?, 
  ?, 
  ?);
INSERT INTO Category
  (id, 
  name, 
  imgURL) 
VALUES 
  (?, 
  ?, 
  ?);
INSERT INTO [Option]
  (id, 
  name) 
VALUES 
  (?, 
  ?);
INSERT INTO OptionValue
  (id, 
  Optionid, 
  value, 
  imgURL) 
VALUES 
  (?, 
  ?, 
  ?, 
  ?);
INSERT INTO [Order]
  (id, 
  Userid, 
  [date], 
  total, 
  status, 
  fullName, 
  address, 
  email, 
  phone, 
  paymentMethod) 
VALUES 
  (?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?);
INSERT INTO OrderItem
  (id, 
  Orderid, 
  Productid, 
  quantity) 
VALUES 
  (?, 
  ?, 
  ?, 
  ?);
INSERT INTO OrderItemOption
  (id, 
  OrderItemid, 
  ProductOptionValueid) 
VALUES 
  (?, 
  ?, 
  ?);
INSERT INTO Product
  (id, 
  Categoryid, 
  name, 
  basePrice, 
  height, 
  width, 
  long, 
  description) 
VALUES 
  (?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?);
INSERT INTO ProductImg
  (id, 
  Productid, 
  imgURL, 
  imgName) 
VALUES 
  (?, 
  ?, 
  ?, 
  ?);
INSERT INTO ProductOption
  (id, 
  Productid, 
  Optionid) 
VALUES 
  (?, 
  ?, 
  ?);
INSERT INTO ProductOptionValue
  (id, 
  ProductOptionid, 
  OptionValueid, 
  quantity, 
  addPrice, 
  imgURL) 
VALUES 
  (?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?);
INSERT INTO [User]
  (id, 
  fullName, 
  email, 
  phone, 
  password, 
  role, 
  address, 
  status) 
VALUES 
  (?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?, 
  ?);


UPDATE Cart SET 
  Userid = ?, 
  total = ? 
WHERE
  id = ?;
UPDATE CartItem SET 
  quantity = ?, 
  Cartid = ?, 
  Productid = ? 
WHERE
  id = ?;
UPDATE CartitemOption SET 
  CartItemid = ?, 
  ProductOptionValueid = ? 
WHERE
  id = ?;
UPDATE Category SET 
  name = ?, 
  imgURL = ? 
WHERE
  id = ?;
UPDATE [Option] SET 
  name = ? 
WHERE
  id = ?;
UPDATE OptionValue SET 
  Optionid = ?, 
  value = ?, 
  imgURL = ? 
WHERE
  id = ?;
UPDATE [Order] SET 
  Userid = ?, 
  [date] = ?, 
  total = ?, 
  status = ?, 
  fullName = ?, 
  address = ?, 
  email = ?, 
  phone = ?, 
  paymentMethod = ? 
WHERE
  id = ?;
UPDATE OrderItem SET 
  Orderid = ?, 
  Productid = ?, 
  quantity = ? 
WHERE
  id = ?;
UPDATE OrderItemOption SET 
  OrderItemid = ?, 
  ProductOptionValueid = ? 
WHERE
  id = ?;
UPDATE Product SET 
  Categoryid = ?, 
  name = ?, 
  basePrice = ?, 
  height = ?, 
  width = ?, 
  long = ?, 
  description = ? 
WHERE
  id = ?;
UPDATE ProductImg SET 
  Productid = ?, 
  imgURL = ?, 
  imgName = ? 
WHERE
  id = ?;
UPDATE ProductOption SET 
  Productid = ?, 
  Optionid = ? 
WHERE
  id = ?;
UPDATE ProductOptionValue SET 
  ProductOptionid = ?, 
  OptionValueid = ?, 
  quantity = ?, 
  addPrice = ?, 
  imgURL = ? 
WHERE
  id = ?;
UPDATE [User] SET 
  fullName = ?, 
  email = ?, 
  phone = ?, 
  password = ?, 
  role = ?, 
  address = ?, 
  status = ? 
WHERE
  id = ?;




DELETE FROM Cart 
  WHERE id = ?;
DELETE FROM CartItem 
  WHERE id = ?;
DELETE FROM CartitemOption 
  WHERE id = ?;
DELETE FROM Category 
  WHERE id = ?;
DELETE FROM [Option] 
  WHERE id = ?;
DELETE FROM OptionValue 
  WHERE id = ?;
DELETE FROM [Order] 
  WHERE id = ?;
DELETE FROM OrderItem 
  WHERE id = ?;
DELETE FROM OrderItemOption 
  WHERE id = ?;
DELETE FROM Product 
  WHERE id = ?;
DELETE FROM ProductImg 
  WHERE id = ?;
DELETE FROM ProductOption 
  WHERE id = ?;
DELETE FROM ProductOptionValue 
  WHERE id = ?;
DELETE FROM [User] 
  WHERE id = ?;
