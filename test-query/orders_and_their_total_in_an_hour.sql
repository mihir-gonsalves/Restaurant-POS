SELECT COUNT(*) AS totalOrdersInHour, SUM(c_order_total) AS totalOrderCostInAnHour 
FROM customer_order 

WHERE DATE_PART('hour', c_order_time) = 5;
