SELECT COUNT(*) AS totalOrdersInHour, SUM(c_order_total) AS totalOrderCostInAnHour 

FROM customer_order 

<<<<<<< HEAD
WHERE DATE_PART('hour', c_order_time) = 5;
=======
WHERE DATE_PART('hour', c_order_time) = 5;
>>>>>>> 1ac5440107bff2b57f62460f281b3282c56318db
