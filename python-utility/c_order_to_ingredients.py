## Provide mapping from item to ingredients

import psycopg2
import random

MAX_CUSTOMER_ID = 140872

# id_item : name_item
id_items = {
    1 : "3 Tender Entree",
    2 : "2 Corn Dog Value Meal",
    3 : "2 Hot Dog Value Meal",
    4 : "Chicken Caesar Salad", # doesn't appear in mobile app
    5 : "Yell BBQ Rib Sandwich", # doesn't appear in mobile app
    6 : "2 Chicken Bacon Ranch Wraps",
    7 : "Single Chicken Bacon Ranch Wrap",
    8 : "2 Classic Chicken Wraps",
    9 : "Single Classic Chicken Wrap",
    10 : "Double Scoop Ice Cream",
    11 : "Cookie Ice Cream Sundae",
    12 : "Chocolate Aggie Shake",
    13 : "Vanilla Aggie Shake",
    14 : "Strawberry Aggie Shake",
    15 : "Oreo Cookie Aggie Shake",
    16 : "Root Beer Float",
    17 : "Bacon Cheeseburger",
    18 : "Black Bean Burger",
    19 : "Cheeseburger",
    20 : "Gig 'Em Patty Melt",
    21 : "Classic Hamburger",
    22 : "French Fries",
    23 : "Aggie Chicken Club",
    24 : "Rev's Grilled Chicken Sandwich",
    25 : "Spicy Chicken Sandwich",
    26 : "Aquafina Water 16 OZ",
    27 : "Aquafina Water 20 OZ",
    28 : "Pepsi Fountain 20 OZ"
    # add tuna melt and fish sandwich
}


hostname = 'csce-315-db.engr.tamu.edu'
database = 'csce331_905_01_db'
username = 'csce331_905_01_user'
pwd = 'webdevwizards'
port_id = 5432
conn = None
cur = None

try:
    conn = psycopg2.connect(
                host = hostname,
                dbname = database,
                user = username,
                password = pwd,
                port = port_id
    )
    cur = conn.cursor()

    my_script = '''CREATE TABLE c_order_to_ingredient_list(
                      c_order_id    INT,
                      item_id  INT,
                      item_quantity INT,
                      PRIMARY KEY (c_order_id, item_id))
     '''
    cur.execute(my_script)
    
    insert_script = 'INSERT INTO c_order_to_ingredient_list (c_order_id, item_id, item_quantity) VALUES (%s, %s, %s)'

    c_order_id = 1
    for c_order_id in range(1, MAX_CUSTOMER_ID + 1):
        item_quantity = 1
        item_id = random.randrange(1, 29)
        # print (f"Customer Item ID: {c_order_id}, Item ID: {item_id}, Item Quantity: {item_quantity}")
        insert_value = (c_order_id, item_id, item_quantity)
        cur.execute(insert_script, insert_value)
        if (item_id >= 26 and item_id <= 28):
            second_item_id = random.randrange(1, 25)
            insert_value = (c_order_id, second_item_id, item_quantity)
            cur.execute(insert_script, insert_value)
        elif (item_id <= 9):
            second_item_id = 22
            insert_value = (c_order_id, second_item_id, item_quantity)
            cur.execute(insert_script, insert_value)
        # print("\n")

    conn.commit()
    cur.close()
    conn.close()

except Exception as error:
    print(error)

finally:
    if cur is not None:
        cur.close()
    if conn is not None:
        conn.close()
