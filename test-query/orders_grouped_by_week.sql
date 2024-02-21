SELECT DATE_PART('week', c_order_date) AS week_number, DATE_PART('year', c_order_date) AS year_number, COUNT(*) 
FROM Customer_order 
GROUP BY week_number, year_number
ORDER BY week_number;
