SELECT menu_Items.item_name, SUM(c_order_to_item_list.item_quantity) AS total_quantity_ordered
FROM c_order_to_item_list
JOIN menu_Items ON c_order_to_item_list.item_id = menu_Items.item_id
GROUP BY menu_Items.item_name
HAVING SUM(c_order_to_item_list.item_quantity) > 10;
