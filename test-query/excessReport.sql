SELECT 
    i.ingredient_name AS ingredient,
    COALESCE(SUM(CASE WHEN co.c_order_date BETWEEN date(?) AND date(?) THEN oi.item_quantity * iti.ingredient_quantity ELSE 0 END), 0) AS consumed_quantity,
    (COALESCE(SUM(CASE WHEN co.c_order_date BETWEEN date(?) AND date(?) THEN oi.item_quantity * iti.ingredient_quantity ELSE 0 END), 0) + i.ingredient_current_stock) * 0.1 AS threshold_quantity
FROM 
    ingredients i
LEFT JOIN 
    item_to_ingredient_list iti ON i.ingredient_id = iti.ingredient_id
LEFT JOIN 
    c_order_to_item_list oi ON iti.item_id = oi.item_id
LEFT JOIN 
    customer_order co ON oi.c_order_id = co.c_order_id
GROUP BY
    i.ingredient_name,
    i.ingredient_current_stock
HAVING
    COALESCE(SUM(CASE WHEN co.c_order_date BETWEEN date(?) AND date(?) THEN oi.item_quantity * iti.ingredient_quantity ELSE 0 END), 0) < (COALESCE(SUM(CASE WHEN co.c_order_date BETWEEN date(?)  AND date(?) THEN oi.item_quantity * iti.ingredient_quantity ELSE 0 END), 0) + i.ingredient_current_stock) * 0.1;