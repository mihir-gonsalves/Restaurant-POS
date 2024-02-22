-- file containing all SQL queries --
-- 1.
-- Groups orders by week and year and displays the total orders in these time periods --
SELECT DATE_PART('week', c_order_date) AS week_number, 
DATE_PART('year', c_order_date) AS year_number, 
COUNT(*) 
FROM customer_order 
GROUP BY week_number, year_number
ORDER BY week_number;

-- 2.
-- Retrieves the total number of orders and the total cost associated with said order placed --
-- during the 13th hour of service. --
SELECT COUNT(*) AS totalOrdersInHour, 
SUM(c_order_total) AS totalOrderCostInAnHour 
FROM customer_order 
WHERE DATE_PART('hour', c_order_time) = 13;

-- 3.
-- Retrieves the top 10 most profitable days (in terms of customer orders) and sorts the --
-- results in descending order. --
SELECT DATE_PART('month', c_order_date) as month_number, 
DATE_PART('day', c_order_date) as day_number, 
DATE_PART('year', c_order_date) AS year_number, 
SUM(c_order_total) AS totalOrderValueInADay 
FROM customer_order 
GROUP BY month_number, day_number, 
year_number ORDER BY totalOrderValueInADay DESC 
LIMIT 10;

-- 4.
-- Retrieves the top 20 menu items with the highest number of ingredients from the menu. --
SELECT menu_items.item_name, 
COUNT(item_to_ingredient_list.ingredient_id) AS ingredient_count
FROM menu_items
JOIN item_to_ingredient_list ON 
menu_items.item_id = item_to_ingredient_list.item_id
GROUP BY menu_items.item_name
ORDER BY ingredient_count DESC
LIMIT 20;

-- 5.
-- Retrieves the names of all ingredients found in the item "Cheeseburger." --
SELECT ingredients.ingredient_name
FROM menu_items
JOIN item_to_ingredient_list ON 
menu_items.item_id = item_to_ingredient_list.item_id
JOIN ingredients ON 
item_to_ingredient_list.ingredient_id = ingredients.ingredient_id
WHERE menu_items.item_name = 'Cheeseburger';

-- 6.
-- Retrieves all the deatils of menu items that contain the ingredient "Burger Patty." --
SELECT menu_items.*
FROM menu_items
JOIN item_to_ingredient_list ON 
menu_items.item_id = item_to_ingredient_list.item_id
JOIN ingredients ON 
item_to_ingredient_list.ingredient_id = ingredients.ingredient_id
WHERE ingredients.ingredient_name = 'Burger Patty';

-- 7.
-- Retrieves all the menu items that are priced less than $5 USD. --
SELECT * 
FROM menu_items
WHERE item_price < 5
ORDER BY item_price DESC;

-- 8.
-- Retrieves all the ingredients that are low (less than 25 units of said ingredient) in stock. --
SELECT *
FROM ingredients
WHERE ingredient_current_stock < 25
ORDER BY ingredient_current_stock ASC;

-- 9.
-- Retrieves all ingredients and sorts the results based on the current stock within inventory --
-- into descending order. --
SELECT *
FROM ingredients
ORDER BY ingredient_current_stock DESC;

-- 10.
-- Retrieves the total quantity of ingredients ordered by the manager and sorts the results --
-- in descending order. --
SELECT ingredient_id, 
SUM(ingredient_quantity) AS total_quantity_ordered
FROM m_order_to_ingredient_list
GROUP BY ingredient_id
ORDER BY total_quantity_ordered DESC;

-- 11.
-- Retrieves most ordered items in the past month --
SELECT c_order_to_item_list.item_id as item_id, 
COUNT(*) as itemCount 
FROM customer_order JOIN c_order_to_item_list ON 
customer_order.c_order_id = c_order_to_item_list.c_order_id 
WHERE DATE_PART('month', c_order_date) = 
DATE_PART('month', CURRENT_DATE - INTERVAL '1 month')
GROUP BY item_id, DATE_PART('month', c_order_date)
ORDER BY itemCount DESC;

-- 12.
-- Retrieves total ingredient quantities by manager --
SELECT manager_order.m_order_id AS manager_id, 
ingredient_id, 
SUM(ingredient_quantity) AS total_quantity_ordered
FROM manager_order 
JOIN m_order_to_ingredient_list ON 
manager_order.m_order_id = m_order_to_ingredient_list.m_order_id
GROUP BY manager_id, ingredient_id
ORDER BY total_quantity_ordered DESC;

-- 13.
-- Retrieves names of all items ordered more than 10 times--
SELECT menu_items.item_name, 
SUM(c_order_to_item_list.item_quantity) AS total_quantity_ordered
FROM c_order_to_item_list
JOIN menu_items ON 
c_order_to_item_list.item_id = menu_items.item_id
GROUP BY menu_items.item_name
HAVING SUM(c_order_to_item_list.item_quantity) > 10;


-- 14.
-- Retrieves most used ingredients across all items and returns them in descending order. --
SELECT ingredients.ingredient_name, 
COUNT(DISTINCT item_to_ingredient_list.item_id) AS number_of_items
FROM item_to_ingredient_list
JOIN ingredients ON 
item_to_ingredient_list.ingredient_id = ingredients.ingredient_id
GROUP BY ingredients.ingredient_name
HAVING COUNT(DISTINCT Item_to_ingredient_list.item_id) > 1
ORDER BY number_of_items DESC;


-- 15.
-- Retrieves the sum of order value by month and returns in ascending order, grouped further 
-- by year. --
SELECT DATE_PART('month', c_order_date) as month_number, 
DATE_PART('year', c_order_date) as year_number, 
SUM(c_order_total) AS totalOrderValue 
FROM customer_order 
GROUP BY month_number, year_number
ORDER BY totalOrderValue ASC 
LIMIT 10;













SELECT DATE_PART('week', c_order_date) AS week_number, 
DATE_PART('year', c_order_date) AS year_number, COUNT(*) 
FROM Customer_order 
GROUP BY week_number, year_number
ORDER BY week_number;





SELECT DATE_PART('month', c_order_date) AS month_number, 
DATE_PART('day', c_order_date) AS day_number, 
DATE_PART('year', c_order_date) AS year_number, 
SUM(c_order_total) AS totalOrderValueInADay 
FROM customer_order 
GROUP BY month_number, day_number, year_number ORDER BY totalOrderValueInADay DESC 
limit 10;








