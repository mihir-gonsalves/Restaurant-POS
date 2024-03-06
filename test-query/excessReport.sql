SELECT
    i1.ingredient_name AS item1,
    COALESCE(SUM(ingredient_quantity), 0) AS curr
FROM
    m_order_to_ingredient_list  t1
JOIN
    manager_order o1 ON t1.m_order_id = o1.m_order_id
JOIN
    ingredients i1 ON t1.ingredient_id = i1.ingredient_id

WHERE
    o1.m_order_date BETWEEN '2024-3-4' AND '2024-3-5'
GROUP BY
    i1.ingredient_name,
    i1.ingredient_current_stock
HAVING
    COALESCE(SUM(ingredient_quantity), 0) < i1.ingredient_current_stock * 0.1
ORDER BY
    curr DESC;



SELECT
    i1.ingredient_name AS item1,
    COALESCE(SUM(t1.ingredient_quantity), 0) AS curr
FROM
    ingredients i1
LEFT JOIN
    m_order_to_ingredient_list t1 ON t1.ingredient_id = i1.ingredient_id
LEFT JOIN
    manager_order o1 ON t1.m_order_id = o1.m_order_id
WHERE
    o1.m_order_date BETWEEN '2024-03-05' AND '2024-03-05' OR o1.m_order_date IS NULL
GROUP BY
    i1.ingredient_name,
    i1.ingredient_current_stock
HAVING
    COALESCE(SUM(ingredient_quantity), 0) < i1.ingredient_current_stock * 0.1
ORDER BY
    curr DESC;
