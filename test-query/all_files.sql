SELECT ingredients.ingredient_name

FROM menu_items

JOIN item_to_ingredient_list ON menu_items.item_id = item_to_ingredient_list.item_id

JOIN ingredients ON item_to_ingredient_list.ingredient_id = ingredients.ingredient_id

WHERE menu_items.item_name = 'Cheeseburger';

SELECT ingredient_id, SUM(ingredient_quantity) AS total_quantity_ordered
FROM M_order_to_ingredient_list
GROUP BY ingredient_id
ORDER BY total_quantity_ordered DESC;

SELECT *

FROM ingredients

WHERE ingredient_current_stock < 25

ORDER BY ingredient_current_stock ASC;

SELECT *

FROM ingredients

ORDER BY ingredient_current_stock DESC;

SELECT menu_items.item_name, COUNT(item_to_ingredient_list.ingredient_id) AS ingredient_count

FROM menu_items

JOIN item_to_ingredient_list ON menu_items.item_id = item_to_ingredient_list.item_id

GROUP BY menu_items.item_name

ORDER BY ingredient_count DESC

LIMIT 20;

SELECT menu_Items.item_name, SUM(c_order_to_item_list.item_quantity) AS total_quantity_ordered
FROM c_order_to_item_list
JOIN menu_Items ON c_order_to_item_list.item_id = menu_Items.item_id
GROUP BY menu_Items.item_name
HAVING SUM(c_order_to_item_list.item_quantity) > 10;

SELECT Ingredients.ingredient_name, COUNT(DISTINCT Item_to_ingredient_list.item_id) AS number_of_items
FROM Item_to_ingredient_list
JOIN Ingredients ON Item_to_ingredient_list.ingredient_id = Ingredients.ingredient_id
GROUP BY Ingredients.ingredient_name
HAVING COUNT(DISTINCT Item_to_ingredient_list.item_id) > 1
ORDER BY number_of_items DESC;

SELECT DATE_PART('month', c_order_date) as month_number, DATE_PART('year', c_order_date) as year_number, SUM(c_order_total) AS totalOrderValue 
FROM customer_order 
GROUP BY month_number, year_number
ORDER BY totalOrderValue ASC 
limit 10;


SELECT manager_order.m_order_id AS manager_id, ingredient_id, SUM(ingredient_quantity) AS total_quantity_ordered
FROM manager_order 
JOIN m_order_to_ingredient_list ON manager_order.m_order_id = m_order_to_ingredient_list.m_order_id
GROUP BY manager_id, ingredient_id
ORDER BY total_quantity_ordered DESC;

SELECT * FROM menu_items

WHERE item_price < 5
ORDER BY item_price DESC;

SELECT c_order_to_item_list.item_id as item_id, COUNT(*) as itemCount 
FROM customer_order JOIN c_order_to_item_list ON customer_order.c_order_id = c_order_to_item_list.c_order_id 
WHERE DATE_PART('month', c_order_date) = DATE_PART('month', CURRENT_DATE - INTERVAL '1 month')
GROUP BY item_id, DATE_PART('month', c_order_date)
ORDER BY itemCount DESC;

SELECT COUNT(*) AS totalOrdersInHour, SUM(c_order_total) AS totalOrderCostInAnHour 

FROM customer_order 

WHERE DATE_PART('hour', c_order_time) = 13;

SELECT DATE_PART('week', c_order_date) AS week_number, DATE_PART('year', c_order_date) AS year_number, COUNT(*) 
FROM Customer_order 
GROUP BY week_number, year_number
ORDER BY week_number;

SELECT menu_items.*

FROM menu_items

JOIN item_to_ingredient_list ON menu_items.item_id = item_to_ingredient_list.item_id

JOIN ingredients ON item_to_ingredient_list.ingredient_id = ingredients.ingredient_id

WHERE ingredients.ingredient_name = 'Burger Patty';

SELECT DATE_PART('month', c_order_date) as month_number, DATE_PART('day', c_order_date) as day_number, DATE_PART('year', c_order_date) AS year_number, SUM(c_order_total) AS totalOrderValueInADay 

FROM customer_order 

GROUP BY month_number, day_number, year_number ORDER BY totalOrderValueInADay DESC 

limit 10;








