SELECT Items.item_name, SUM(c_order_to_item.item_qty) AS total_quantity_ordered
FROM c_order_to_item
JOIN Items ON c_order_to_item.item_id = Items.item_id
GROUP BY Items.item_name
HAVING total_quantity_ordered > 10;
