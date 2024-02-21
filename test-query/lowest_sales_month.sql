SELECT DATE_PART('month', c_order_date) as month_number, DATE_PART('year', c_order_date) as year_number, SUM(c_order_total) AS totalOrderValue 
FROM customer_order 
GROUP BY month_number, year_number
ORDER BY totalOrderValue ASC 
limit 10;
