## Provide mapping from item to ingredients

import psycopg2
import random

# id_ingredient : name_ingredient
id_ingredients = {
    1 : "Chicken Tender",
    2 : "Corn Dog",
    3 : "Hot Dog",
    4 : "Grilled Chicken",
    5 : "Burger Patty",
    6 : "Black Bean Patty",
    7 : "Hot Dog Bun",
    8 : "Caramelized Onion",
    9 : "Red Onion",
    10 : "Bacon Slice",
    11 : "Ranch Dressing",
    12 : "Lettuce",
    13 : "Tomato Slice",
    14 : "American Cheese Slice",
    15 : "Cheddar Cheese Slice",
    16 : "Crouton",
    17 : "Caesar Dressing",
    18 : "BBQ Sauce",
    19 : "Ribs",
    20 : "Aquafina Water 16 OZ",
    21 : "Aquafina Water 20 OZ",
    22 : "Chocolate Ice Cream Scoop",
    23 : "Vanilla Ice Cream Scoop",
    24 : "Strawberry Ice Cream Scoop",
    25 : "Oreo Cookie",
    26 : "Chocolate Chip Cookie",
    27 : "Chicken Sauce",
    28 : "Ketchup",
    29 : "Mustard",
    30 : "Mayonnaise",
    31 : "Sandwich Bread",
    32 : "Hamburger Bun",
    33 : "Tortilla Wrap",
    34 : "Milk",
    35 : "Frying Oil",
    36 : "French Fries",
    37 : "Pepsi",
    38 : "Root Beer",
    39 : "Dr. Pepper",
    40 : "Sprite",
    41 : "Pickle Chip",
    42 : "Gig 'Em Sauce",
    43 : "Swiss Cheese Slice",
    44 : "Chicken Patty",
    45 : "Spicy Chicken Patty",
    46 : "Avocado",
    47 : "Buffalo Sauce",
    48 : "20 OZ Cup"
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

    my_script = '''CREATE TABLE m_order_to_ingredient_list(
                      m_order_id    INT,
                      ingredient_id  INT,
                      ingredient_quantity INT,
                      PRIMARY KEY (m_order_id, ingredient_id))
     '''
    cur.execute(my_script)

    my_script = '''CREATE TABLE manager_order(
                        m_order_id    INT PRIMARY KEY,
                        m_order_date DATE,
                        m_order_time TIME,
                        m_order_total DECIMAL(10,2),
                        manager_name VARCHAR(20))
    '''
    cur.execute(my_script)
    
    insert_script = 'INSERT INTO m_order_to_ingredient_list (m_order_id, ingredient_id, ingredient_quantity) VALUES (%s, %s, %s)'
    insert_script2 = 'INSERT INTO manager_order(m_order_id, m_order_date, m_order_time, m_order_total, manager_name) VALUES (%s, %s, %s, %s, %s)'
    for i in range(1, 70):
        m_order_id = i
        manager_name_num = random.randrange(1, 6)
        if (manager_name_num == 1):
            manager_name = "John"
        elif (manager_name_num == 2):
            manager_name = "Jane"
        elif (manager_name_num == 3):
            manager_name = "Joe"
        elif (manager_name_num == 4):
            manager_name = "Jack"
        elif (manager_name_num == 5):
            manager_name = "Jill"
        
        ingredient_quantity = random.randrange(1, 40)
        ingredient_id = random.randrange(1, 49)
        m_order_date = f'%s-%s-%s' % (random.randrange(2022, 2024), random.randrange(1, 13), random.randrange(1, 29))
        m_order_time = f'%s:%s:%s' % (random.randrange(0, 24), random.randrange(0, 60), random.randrange(0, 60))
        m_order_total = random.randrange(100, 1000)
        insert_value = (m_order_id, ingredient_id, ingredient_quantity)
        insert_value2 = (m_order_id, m_order_date, m_order_time, m_order_total, manager_name)
        cur.execute(insert_script, insert_value)
        cur.execute(insert_script2, insert_value2)
        print(f"at #{i}")
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
