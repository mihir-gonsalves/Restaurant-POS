CREATE OR REPLACE FUNCTION update_order_totals()
RETURNS TRIGGER AS $$
BEGIN
    -- Update subtotal
    UPDATE customer_order
    SET c_order_subtotal = (
        SELECT SUM(i.item_price * oti.item_quantity)  
        FROM c_order_to_item_list oti
        JOIN menu_items i ON oti.item_id = i.item_id
        WHERE oti.c_order_id = NEW.c_order_id
        GROUP BY oti.c_order_id
    );
    
    -- Update tax
    UPDATE customer_order
    SET c_order_tax = c_order_subtotal * 0.0825
    WHERE c_order_id = NEW.c_order_id;

    -- Update total
    UPDATE customer_order
    SET c_order_total = c_order_subtotal + c_order_tax
    WHERE c_order_id = NEW.c_order_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to execute the function after insert on order_to_item table
CREATE TRIGGER after_insert_order_to_item
AFTER INSERT ON c_order_to_item_list
FOR EACH ROW
EXECUTE FUNCTION update_order_totals();