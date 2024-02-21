SELECT c_order_to_item_list.item_id as item_id, COUNT(*) as itemCount 
FROM customer_order JOIN c_order_to_item_list ON customer_order.c_order_id = c_order_to_item_list.c_order_id 
WHERE DATE_PART('month', c_order_date) = DATE_PART('month', CURRENT_DATE - INTERVAL '1 month')
GROUP BY item_id, DATE_PART('month', c_order_date)
ORDER BY itemCount DESC;
