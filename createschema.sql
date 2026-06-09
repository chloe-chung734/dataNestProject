CREATE DATABASE IF NOT EXISTS ewha;

CREATE TABLE IF NOT EXISTS ewha.restaurants (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) UNIQUE NOT NULL,
    city VARCHAR(64) NOT NULL,
    manager VARCHAR(32) NOT NULL,
    opening_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    CHECK (CHAR_LENGTH(name) >= 2),
    CHECK (CHAR_LENGTH(city) >= 2)
);

CREATE TABLE IF NOT EXISTS ewha.customer (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(32) NOT NULL,
    last_name VARCHAR(32) NOT NULL,
    email VARCHAR(32) UNIQUE NOT NULL,
    phone VARCHAR(16) UNIQUE NOT NULL,
    city VARCHAR(64),
    age INT,
    gender ENUM ('Male', 'Female', 'Other'),
    CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'),
    CHECK (phone REGEXP '^\\+82 10-[0-9]{4}-[0-9]{4}$'),
    CHECK (age >= 0 AND age <= 120)
);

CREATE TABLE IF NOT EXISTS ewha.customer_demographic_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    city VARCHAR(64),
    age_range VARCHAR(8),
    gender ENUM ('Male', 'Female', 'Other'),
    start_date DATE NOT NULL,
    end_date DATE,
    CONSTRAINT fk_demo_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CHECK (age_range REGEXP '^(0|[1-9][0-9]?|1[01][0-9]|120)-(0|[1-9][0-9]?|1[01][0-9]|120)$'),
    CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE TABLE IF NOT EXISTS ewha.menu_category (
    id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(32) UNIQUE NOT NULL,
    CHECK (CHAR_LENGTH(category_name) >= 2)
);

CREATE TABLE IF NOT EXISTS ewha.menu_item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL,
    item_name VARCHAR(32) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    calories INT NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    CHECK (price > 0),
    CHECK (calories >= 0),
    CONSTRAINT fk_menu_category FOREIGN KEY (category_id) REFERENCES menu_category(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ewha.menu_price_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    menu_item_id INT NOT NULL,
    old_price DECIMAL(10, 2) NOT NULL,
    new_price DECIMAL(10, 2) NOT NULL,
    change_date DATE NOT NULL,
    CONSTRAINT fk_price_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_item(id) ON DELETE CASCADE,
    CHECK (old_price > 0),
    CHECK (new_price > 0),
    CHECK (old_price != new_price)
);

CREATE TABLE IF NOT EXISTS ewha.orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    restaurant_id INT NOT NULL,
    order_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_order_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

CREATE TABLE IF NOT EXISTS ewha.order_item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    item_price_at_order DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_orderitem_menuitem FOREIGN KEY (menu_item_id) REFERENCES menu_item(id),
    CHECK (quantity > 0),
    CHECK (item_price_at_order > 0)
);

CREATE TABLE IF NOT EXISTS ewha.bill (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL,
    final_total DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_bill_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CHECK (subtotal >= 0),
    CHECK (tax_amount >= 0),
    CHECK (final_total >= subtotal)
);

CREATE TABLE IF NOT EXISTS ewha.delivery_drivers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(32) NOT NULL,
    last_name VARCHAR(32) NOT NULL,
    phone VARCHAR(16) UNIQUE NOT NULL,
    city VARCHAR(64),
    employment_date DATE NOT NULL,
    CHECK (phone REGEXP '^\\+82 10-[0-9]{4}-[0-9]{4}$')
);

CREATE INDEX menu_item_name_index ON ewha.menu_item(item_name);
CREATE INDEX order_timestamp_index ON ewha.orders(order_timestamp);
CREATE INDEX customer_city_index ON ewha.customer(city);
CREATE INDEX restaurant_city_index ON ewha.restaurants(city);

CREATE OR REPLACE VIEW ewha.order_details_view AS
SELECT
    ewha.orders.id AS order_id,
    ewha.orders.order_timestamp,
    ewha.customer.id AS customer_id,
    ewha.customer.first_name,
    ewha.customer.last_name,
    ewha.restaurants.id AS restaurant_id,
    ewha.restaurants.name AS restaurant_name,
    ewha.bill.subtotal,
    ewha.bill.tax_amount,
    ewha.bill.final_total,
    ewha.order_item.menu_item_id,
    ewha.menu_item.item_name,
    ewha.order_item.quantity,
    ewha.order_item.item_price_at_order
FROM ewha.orders
JOIN ewha.customer ON ewha.orders.customer_id = ewha.customer.id
JOIN ewha.restaurants ON ewha.orders.restaurant_id = ewha.restaurants.id
JOIN ewha.bill ON ewha.bill.order_id = ewha.orders.id
JOIN ewha.order_item ON ewha.order_item.order_id = ewha.orders.id
JOIN ewha.menu_item ON ewha.menu_item.id = ewha.order_item.menu_item_id;

CREATE OR REPLACE VIEW ewha.customer_spending_view AS
SELECT
    ewha.customer.id AS customer_id,
    ewha.customer.first_name,
    ewha.customer.last_name,
    ewha.customer.email,
    COUNT(ewha.orders.id) AS total_orders,
    COALESCE(SUM(ewha.bill.final_total), 0) AS total_spent
FROM ewha.customer
LEFT JOIN ewha.orders ON ewha.customer.id = ewha.orders.customer_id
LEFT JOIN ewha.bill ON ewha.orders.id = ewha.bill.order_id
GROUP BY ewha.customer.id, ewha.customer.first_name, ewha.customer.last_name, ewha.customer.email;