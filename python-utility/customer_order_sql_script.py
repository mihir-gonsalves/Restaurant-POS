import math
import random
from decimal import Decimal, ROUND_HALF_UP



def printout():
    f = open("order_table.sql" ,"w")
    f.write("CREATE TABLE Customer_order (\n")
    f.write("    c_order_id INT PRIMARY KEY,\n")
    f.write("    c_order_date DATE,\n")
    f.write("    c_order_time TIME,\n")
    f.write("    c_order_subtotal DECIMAL(10,2),\n")
    f.write("    c_order_tax DECIMAL(10,2),\n")
    f.write("    c_order_total DECIMAL(10,2),\n")
    f.write("    c_order_payment_type VARCHAR(20)\n")
    f.write(");\n")

    total_sales = 0

    i = 1
    for month in range(2,26):
        if month > 24:
            year = "2024-"
            month -= 24
        elif month > 12:
            year = "2023-"
            month -= 12
        elif month > 24:
            year = "2024-"
            month -= 24
        else:
            year = "2022-"
        if month in [1,3,5,7,8,10,12]:
            days = 31
        elif month == 2:
            days = 28
        else:
            days = 30
        
        for day in range(1,days+1):
            day_sale = 0
            if month == 8 and day == 23:
                day_limit = 9000
            elif month == 1 and day == 17:
                day_limit = 9000
            else:
                day_limit = random.randrange(2000,4500)
            while day_sale < day_limit:
                f.write("INSERT INTO Customer_order (c_order_id, c_order_date, c_order_time, c_order_subtotal, c_order_tax, c_order_total, c_order_payment_type) VALUES (")
                f.write(str(i)+", '"+ year+ str(month)+ "-" + str(day)+ "', '")
                f.write(str(random.randrange(11,23))+ ":" + str(random.randrange(0,60))+ ":" + str(random.randrange(0,60))+ "', ") # 11am to pm
                subtotal = Decimal(random.randrange(5,30))
                tax = subtotal * Decimal('0.0825')

                total = subtotal + tax
                total_sales += subtotal
                day_sale += subtotal
                subtotal = subtotal.quantize(Decimal('0.01'), rounding=ROUND_HALF_UP)
                tax = tax.quantize(Decimal('0.01'), rounding=ROUND_HALF_UP)
                total = total.quantize(Decimal('0.01'), rounding=ROUND_HALF_UP)
                f.write(str(subtotal)+ ", ") # subtotal
                f.write(str(tax)+ ", ")# tax
                f.write(str(total)+ ", ")   # total
                payments = ["cash", "credit", "debit"]
                f.write("'"+ payments[random.randrange(0,3)]+ "');\n")
                i += 1
            print(day_sale)
    print(total_sales)

    f.close()
if __name__ == "__main__":
    printout()
