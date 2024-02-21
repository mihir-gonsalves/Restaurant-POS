SELECT Ingredients.ingredient_name, COUNT(DISTINCT Item_to_ingredient_list.item_id) AS number_of_items
FROM Item_to_ingredient_list
JOIN Ingredients ON Item_to_ingredient_list.ingredient_id = Ingredients.ingredient_id
GROUP BY Ingredients.ingredient_name
HAVING COUNT(DISTINCT Item_to_ingredient_list.item_id) > 1
ORDER BY number_of_items DESC;
