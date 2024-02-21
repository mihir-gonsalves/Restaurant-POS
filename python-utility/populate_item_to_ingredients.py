## Provide mapping from item to ingredients

import psycopg2

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

# id_ingredient : name_ingredient
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


# id_item : [(id_ingredient, quantity), ...]
id_items_ingredients = {
    1 : [(1, 3), (27, 1), (35, 1)],
    2 : [(2, 2), (28, 1)],
    3 : [(3, 2), (7, 2), (28, 1), (29, 1)],
    4 : [(4, 1), (12, 3), (13, 3), (16, 1), (17, 1)],
    5 : [(19, 1), (31, 1), (18, 1)],
    6 : [(1, 2), (10, 2), (11, 2), (13, 2), (15, 2), (33, 2), (35, 2)],
    7 : [(1, 1), (10, 1), (11, 1), (13, 1), (15, 1), (33, 1), (35, 1)],
    8 : [(1, 2), (13, 2), (15, 2), (33, 2), (35, 1)],
    9 : [(1, 1), (13, 1), (15, 1), (33, 1), (35, 1)],
    10 : [(22, 2), (23, 2), (24, 2)], # kind of a weird one seeing as the scoops are all applied initially
    11 : [(26, 2), (23, 1)],
    12 : [(22, 2), (34, 1)],
    13 : [(23, 2), (34, 1)],
    14 : [(24, 2), (34, 1)],
    15 : [(23, 2), (25, 2), (34, 1)],
    16 : [(23, 1), (38, 1)],
    17 : [(5, 1), (10, 3), (14, 1), (32, 1)],
    18 : [(6, 1), (12, 1), (13, 2), (32, 1), (41, 2)],
    19 : [(5, 1), (9, 1), (14, 1), (32, 1), (41, 2), (42, 1)],
    20 : [(5, 1), (8, 1), (43, 1), (31, 1), (42, 1)],
    21 : [(5, 1), (9, 1), (12, 1), (13, 2), (32, 1), (41, 2)],
    22 : [(36, 1), (35, 1)],
    23 : [(44, 1), (46, 1), (43, 1), (10, 2), (32, 1)],
    24 : [(4, 1), (12, 1), (9, 1), (32, 1)],
    25 : [(45, 1), (12, 1), (11, 1), (31, 1), (47, 1)],
    26 : [(20, 1)],
    27 : [(21, 1)],
    28 : [(48, 1)]
}

# printing the items and their ingredients
for item_id, ingredients in id_items_ingredients.items():
    item_name = id_items[item_id]
    print(f"{item_id}. Item: {item_name}")
    for ingredient_id, quantity in ingredients:
        ingredient_name = id_ingredients[ingredient_id]
        print(f"Ingredient: {ingredient_name}, Quantity: {quantity}")
    print("\n")  # newline for whitespace between items


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

    my_script = '''CREATE TABLE item_to_ingredient_list(
                      item_id    INT,
                      ingredient_id  INT,
                      ingredient_quantity INT,
                      PRIMARY KEY (item_id, ingredient_id))
     '''
    cur.execute(my_script)
    
    insert_script = 'INSERT INTO item_to_ingredient_list (item_id, ingredient_id, ingredient_quantity) VALUES (%s, %s, %s)'

    for item_id, ingredients in id_items_ingredients.items():
        for ingredient_id, quantity in ingredients:
            # print (f"Item ID: {item_id}, Ingredient ID: {ingredient_id}, Quantity: {quantity}")
            insert_value = (item_id, ingredient_id, quantity)
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
