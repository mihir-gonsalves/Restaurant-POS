SELECT DATE_PART('month', c_order_date) as month_number, DATE_PART('day', c_order_date) as day_number, SUM(c_order_total) AS totalOrderValueInADay 

FROM customer_order 

GROUP BY DATE_PART('month', c_order_date), DATE_PART('day', c_order_date) ORDER BY totalOrderValueInADay DESC 

limit 10;