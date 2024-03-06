SELECT 
    i.ingredient_name AS ingredient,
    COALESCE(SUM(CASE WHEN co.c_order_date BETWEEN '2023-03-31'  AND '2024-02-01' THEN oi.item_quantity * iti.ingredient_quantity ELSE 0 END), 0) AS used_ingredient_count,
    m.item_name AS item_name,
    COALESCE(SUM(CASE WHEN co.c_order_date BETWEEN '2023-03-31'  AND '2024-02-01' THEN oi.item_quantity ELSE 0 END), 0) AS item_count
FROM 
    ingredients i
LEFT JOIN 
    item_to_ingredient_list iti ON i.ingredient_id = iti.ingredient_id
LEFT JOIN 
    c_order_to_item_list oi ON iti.item_id = oi.item_id
LEFT JOIN 
    customer_order co ON oi.c_order_id = co.c_order_id
LEFT JOIN
    menu_items m ON iti.item_id = m.item_id
GROUP BY 
    i.ingredient_name,  
    i.ingredient_current_stock,
    m.item_name
ORDER BY
    used_ingredient_count DESC;
    

    

