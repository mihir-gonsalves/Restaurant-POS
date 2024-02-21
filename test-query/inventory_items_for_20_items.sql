SELECT menu_items.item_name, COUNT(item_to_ingredient_list.ingredient_id) AS ingredient_count

FROM menu_items

JOIN item_to_ingredient_list ON menu_items.item_id = item_to_ingredient_list.item_id

GROUP BY menu_items.item_name

ORDER BY ingredient_count DESC

LIMIT 20;