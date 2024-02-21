SELECT manager_order.m_order_id AS manager_id, ingredient_id, SUM(ingredient_quantity) AS total_quantity_ordered
FROM manager_order 
JOIN m_order_to_ingredient_list ON manager_order.m_order_id = m_order_to_ingredient_list.m_order_id
GROUP BY manager_id, ingredient_id
ORDER BY total_quantity_ordered DESC;