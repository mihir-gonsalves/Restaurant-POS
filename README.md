# Project 2 Rev's GUI
-----


## Team Members:
Amol Gupta, Caden Miller, Carson Adams, Jesung Ha, Kevin Joseph, and Mihir Gonsalves

## Config:
As to not expose our database password, you should create a copy of the config.properties file and then fill in the fields with the proper database information.

## Building and Running:
This project uses [Maven] (https://maven.apache.org/download.cgi) for dependency management and building. To build the project, navigate to the project directory in your terminal and run the following command:

```bash
mvn clean package
```
Then, in the same directory, run the application with the following command:
```bash
java -jar target/RevsGUI-0.0.1-jar-with-dependencies.jar
```



## Query Behaviors:

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
- Retrieves total ingredient quantities by manager

#### 13. Names of all items ordered more than 10 times
- items_ordered_more_than_10x.sql
- Retrieves names of all items ordered more than 10 times

#### 14. Number of items an ingredient is used in
- items_per_ingredient.sql
- Retrieves most used ingredients across all items and returns them in descending order.

#### 15. Months with lowest revenue.
- lowest_sales_month.sql
- Retrieves the sum of order value by month and returns in ascending order, grouped further by year.


## ER Diagram:
![ER Diagram](https://github.com/csce-315-331-2024a/project-2-database-gui-905-1/blob/dev/images/ERD.png?raw=true)


## Database Schema:
![Database Schema](https://github.com/csce-315-331-2024a/project-2-database-gui-905-1/blob/dev/images/schema.png?raw=true)


[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/7JP64vQQ)
[![Open in Codespaces](https://classroom.github.com/assets/launch-codespace-7f7980b617ed060a017424585567c406b6ee15c891e84e1186181d67ecf80aa0.svg)](https://classroom.github.com/open-in-codespaces?assignment_repo_id=13882821)
