SELECT COUNT(*) AS totalOrdersInHour, SUM(c_order_total) AS totalOrderCostInAnHour 
FROM customer_order 
<<<<<<< Updated upstream
WHERE DATE_PART(‘hour’, c_order_time) = 5;
=======

WHERE DATE_PART('hour', c_order_time) = 5;
>>>>>>> Stashed changes
