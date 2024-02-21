SELECT DATE_PART('month', c_order_date) as month_number, DATE_PART('day', c_order_date) as day_number, DATE_PART('year', c_order_date) AS year_number, SUM(c_order_total) AS totalOrderValueInADay 

FROM customer_order 

GROUP BY month_number, day_number, year_number ORDER BY totalOrderValueInADay DESC 

limit 10;