SELECT id, Userid, total 
  FROM Cart;
SELECT id, quantity, Cartid, Productid 
  FROM CartItem;
SELECT id, CartItemid, ProductOptionValueid 
  FROM CartitemOption;
SELECT id, name, imgURL 
  FROM Category;
SELECT id, name 
  FROM [Option];
SELECT id, Optionid, value, imgURL 
  FROM OptionValue;
SELECT id, Userid, [date], total, status, fullName, address, email, phone, paymentMethod 
  FROM [Order];
SELECT id, Orderid, Productid, quantity 
  FROM OrderItem;
SELECT id, OrderItemid, ProductOptionValueid 
  FROM OrderItemOption;
SELECT id, Categoryid, name, basePrice, height, width, long, description 
  FROM Product;
SELECT id, Productid, imgURL, imgName 
  FROM ProductImg;
SELECT id, Productid, Optionid 
  FROM ProductOption;
SELECT id, ProductOptionid, OptionValueid, quantity, addPrice, imgURL 
  FROM ProductOptionValue;
SELECT id, fullName, email, phone, password, role, address, status 
  FROM [User];
