SELECT menu_items.*

FROM menu_items

JOIN item_to_ingredient_list ON menu_items.item_id = item_to_ingredient_list.item_id

JOIN ingredients ON item_to_ingredient_list.ingredient_id = ingredients.ingredient_id

WHERE ingredients.ingredient_name = 'Burger Patty';