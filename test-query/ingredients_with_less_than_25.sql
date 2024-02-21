SELECT *
FROM ingredients
WHERE ingredient_current_stock < 25
ORDER BY ingredient_current_stock ASC;
