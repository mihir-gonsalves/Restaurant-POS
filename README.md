# Project 2: Restaurant Database and GUI
-----

## Surface Overview
A Point-of-Sale system and inventory solution for Texas A&M University's very own Rev's American Grill restaurant!


### Group Members:
Amol Gupta, Caden Miller, Carson Adams, Jesung Ha, Kevin Joseph, and Mihir Gonsalves

<br>

### Configuration:
As to not expose our own database password, you should create a copy of the "config.properties" file called "config-local.properties" and then fill in the
fields with the proper database information. 

#### config.properties example:
- db.name=<database name> 
- db.user=<database user>  
- db.password=<database password>

<br>

### Building and Running:
This project uses [Maven](https://maven.apache.org/download.cgi) for dependency management and building. To build the 
project, navigate to the project directory in your terminal (bash) and run the following commands:

#### Commands

First clean the space that Maven will build onto so that compilation can be completed unhitched.

```bash
mvn clean package 
```

Then, in the same directory, run the application with the following command:

```bash
java -jar target/RevsGUI-0.0.1-jar-with-dependencies.jar
```

or on Windows

```bash
java -jar .\target\RevsGUI-0.0.1-jar-with-dependencies.jar
```

To recompile the project run the following command:

```bash
mvn package
```

If compilation is taking too long, you can force multiple cores to run compilation by add the **-T** flag

For example (using 4 cores):

```bash
mvn clean package -T 4
```

### Screen Visuals
#### Login Screen
![Login Screen](https://github.com/mihir-gonsalves/Restaurant-POS/blob/7740a574fbb153685d5a8a929b06738a5de90c58/readme-images/login.png)
#### Menu Screen
![Menu Screen](https://github.com/mihir-gonsalves/Restaurant-POS/blob/7740a574fbb153685d5a8a929b06738a5de90c58/readme-images/menu.png)
#### Checkout Popup
![Payment Screen](https://github.com/mihir-gonsalves/Restaurant-POS/blob/7740a574fbb153685d5a8a929b06738a5de90c58/readme-images/payment.png)
#### Manager Screen
![Manager Screen](https://github.com/mihir-gonsalves/Restaurant-POS/blob/7740a574fbb153685d5a8a929b06738a5de90c58/readme-images/manager.png)

<br>

## Underlying Details
Extra details about how the project's GUI and Database were created.


### Tech Used:
Java (JDBC and Java Swing), PostgreSQL, Maven, Python, AWS, Git/Github

- Java: the primary programming language used for development.
- Java Database Connectivity: used for db conectivity to PostgreSQL.
- Java Swing: employed for GUI development.
- PostgreSQL: the chosen relational db management system.
- Maven: used for project and dependency management during build process.
- Python: used to script SQL instructions to populate database.
- AWS: host for database.
- Git/Github: collaborative software tool used to track member development progress.

### ER Diagram:
![ER Diagram](https://github.com/csce-315-331-2024a/project-2-database-gui-905-1/blob/dev/images/ERD.png?raw=true)

### Database Schema:
![Database Schema](https://github.com/csce-315-331-2024a/project-2-database-gui-905-1/blob/dev/images/schema.png?raw=true)

### Icons Used for Cashier Screen navigation bar:
- [Appetizers](https://www.flaticon.com/free-icons/appetizer) created by Iconiic
- [Beverages](https://www.flaticon.com/free-icons/beverage) created by ultimatearm
- [Burgers](https://www.flaticon.com/free-icons/burger) created by Freepik
- [Limited Time Offers](https://www.flaticon.com/free-icons/offer) created by Freepik
- [Salads](https://www.flaticon.com/free-icons/salad) created by Freepik
- [Sandwiches](https://www.flaticon.com/free-icons/sandwich) created by Freepik
- [Shakes and More](https://www.flaticon.com/free-icons/milk-shake) created by Freepik
- [Value Meals](https://www.flaticon.com/free-icons/value) created by srip

<br>

## Individual Folders/File Descriptions


### Images:
This folder holds all images used in the GUI.

### Python-Utility:
All python files are scripts to create SQL files that will populate the database.

### SQL-Script:
Files created by python that will actually populate the database.

### Test-Query:
15 Queries that verify the functionality of the database.

### src/main/java/com/<db password>/revsGUI/
#### Database
model.java encapsulates database interaction logic and user authentication functionality for the application.

#### Screens
Each file represents an individual screen for the GUI that will flow intuitively.

#### Controller.java
Manages the overall flow and user interactions of the GUI.

### config.properties
Includes the access details for the database.

### pom.xml 
Configuration file for maven which builds and manages the Java components of the project.

<br>

## Query Testing Behaviours for Demo 1

### Required Queries:
#### 1. Count of orders grouped by week
- orders_grouped_by_week.sql
- Groups orders by week and year and displays the total orders in these time periods

#### 2. Count of orders and their total in an hour
- orders_and_their_total_in_an_hour.sql
- Retrieves the total number of orders and the total cost associated with said order placed during the 13th hour of service.

#### 3. Top 10 sums of order total grouped by day in descending order by order total
- top_ten_sums_of_day_descending_by_order_total.sql
- Retrieves the top 10 most profitable days (in terms of customer orders) and sorts the results in descending order.

#### 4. Inventory items for 20 items
- inventory_items_for_20_items.sql
- Retrieves the top 20 menu items with the highest number of ingredients from the menu.

### Extra Queries:
#### 5. List of ingredients in the burger
- ingredients_in_cheeseburger.sql
- Retrieves the names of all ingredients found in the item "Cheeseburger."

#### 6. Menu items with patties
- orders_with_patties.sql
- Retrieves all the deatils of menu items that contain the ingredient "Burger Patty."

#### 7. All menu items less than 5 dollars
- menu_items_less_than_five.sql
- Retrieves all the menu items that are priced less than $5 USD.

#### 8. All ingredients with less than 25 in stock ascending
- ingredients_with_less_than_25.sql
- Retrieves all the ingredients that are low (less than 25 units of said ingredient) in stock.

#### 9. Ingredients with most stock in descending order
- ingredients_with_most_stock.sq
- Retrieves all ingredients and sorts the results based on the current stock within inventory into descending order.

#### 10. Total count of ingredients already ordered by managers
- ingredients_ordered_by_manager.sql
- Retrieves the total quantity of ingredients ordered by the manager and sorts the results in descending order.

#### 11. Most ordered items in past month by descending order
- most_ordered_items_past_month.sql
- Retrieves most ordered items in the past month

#### 12. Most ordered ingredients by manager
- managers_most_ordered_ingredients.sql
- Retrieves total ingredient quantities by managers

#### 13. Names of all items ordered more than 10 times
- items_ordered_more_than_10x.sql
- Retrieves names of all items ordered more than 10 times

#### 14. Number of items an ingredient is used in
- items_per_ingredient.sql
- Retrieves most used ingredients across all items and returns them in descending order.

#### 15. Months with lowest revenue.
- lowest_sales_month.sql
- Retrieves the sum of order value by month and returns in ascending order, grouped further by year.
