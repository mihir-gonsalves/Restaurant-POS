CREATE TABLE c_orders (
    c_order_id SERIAL PRIMARY KEY,
    c_order_date DATE,
    c_order_time TIME,
    c_order_subtotal DECIMAL(10,2),
    c_order_tax DECIMAL(10,2),
    c_order_total DECIMAL(10,2),
    c_order_payment_type VARCHAR(20)
);

CREATE TABLE c_oti(
    c_order_id    INT,
    item_id  INT,
    item_quantity INT,
    PRIMARY KEY (c_order_id, item_id))
;
    