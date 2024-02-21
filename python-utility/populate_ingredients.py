import psycopg2
import random



id_ingredients = {
    1 : "Chicken Tender",
    2 : "Corn Dog",
    3 : "Sausage",
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

    my_script = '''CREATE TABLE ingredients(
                     ingredient_id    INT,
                     ingredient_name  VARCHAR(50),
                     ingredient_current_stock INT,
                     ingredient_unit_price DECIMAL(15,2))
    '''
    cur.execute(my_script)

    insert_script = 'INSERT INTO ingredients (ingredient_id, ingredient_name, ingredient_current_stock, ingredient_unit_price) VALUES (%s, %s, %s, %s)'

    for i in range(1, 49):
        random_int = random.randint(8, 92)
        random_decimal = random.uniform(1.86, 8.99)
        insert_value = (i, id_ingredients[i], random_int, random_decimal)
        cur.execute(insert_script, insert_value)

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
