CREATE TABLE menu_items (
    item_id SERIAL PRIMARY KEY,
    item_name VARCHAR(50),
    item_price DECIMAL(15,2),
    category VARCHAR(50),
    date_range DATERANGE
);

INSERT INTO  menu_items( item_name,  item_price, category) VALUES ('3 Tender Entree', 4.99, 'Value Meals');
INSERT INTO  menu_items( item_name,  item_price, category) VALUES ('2 Corn Dog Value Meal', 4.99, 'Value Meals');
INSERT INTO  menu_items( item_name,  item_price, category) VALUES ('2 Hot Dog Value Meal', 4.99, 'Value Meals');
INSERT INTO  menu_items( item_name,  item_price, category) VALUES ('Chicken Caesar Salad', 8.29, 'Salads');
INSERT INTO  menu_items( item_name,  item_price, category) VALUES ('Yell BBQ Rib Sandwich', 7.99, 'Limited Time Offer');
INSERT INTO  menu_items( item_name,  item_price, category) VALUES ( '2 Chicken Bacon Ranch Wrap', 6.00, 'Limited Time Offer');
INSERT INTO  menu_items( item_name,  item_price, category) VALUES ( 'Single Chicken Bacon Ranch Wrap', 3.49, 'Limited Time Offer');
INSERT INTO  menu_items( item_name,  item_price, category) VALUES ( '2 Classic Chicken Wraps', 5.00, 'Limited Time Offer');
INSERT INTO  menu_items( item_name,  item_price, category) VALUES ( 'Single Classic Chicken Wrap', 2.99, 'Limited Time Offer');
INSERT INTO  menu_items( item_name,  item_price, category) VALUES ( 'Double Scoop Ice Cream', 3.29, 'Shakes & More');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Cookie Ice Cream Sundae', 4.69, 'Shakes & More');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Chocolate Aggie Shake', 4.49, 'Shakes & More');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Vanilla Aggie Shake', 4.49, 'Shakes & More');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Strawberry Aggie Shake', 4.49, 'Shakes & More');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Oreo Cookie Aggie Shake', 4.49, 'Shakes & More');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Root Beer Float', 5.49, 'Shakes & More');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Bacon Cheeseburger', 8.29, 'Burgers');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Black Bean Burger', 7.59, 'Burgers');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Cheeseburger', 6.89, 'Burgers');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Gig Em Patty Melt', 7.59, 'Burgers');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Classic Hamburger', 6.89, 'Burgers');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'French Fries', 1.99, 'Appetizers');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Aggie Chicken Club', 8.39, 'Sandwiches');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Revs Grilled Chicken Sandwich', 8.39, 'Sandwiches');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Spicy Chicken Sandwich', 8.39, 'Sandwiches');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Aquafina Water 16 OZ', 1.79, 'Beverages');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Aquafina Water 20 OZ', 2.19, 'Beverages');
INSERT INTO  menu_items(  item_name,  item_price, category) VALUES ( 'Pepsi Fountain 20 OZ', 1.99, 'Beverages');
