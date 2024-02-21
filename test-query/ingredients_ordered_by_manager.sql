SELECT ingredient_id, SUM(ingredient_quantity) AS total_quantity_ordered
FROM M_order_to_ingredient_list
GROUP BY ingredient_id
ORDER BY total_quantity_ordered DESC;
