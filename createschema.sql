CREATE DATABASE IF NOT EXISTS ewha;

CREATE TABLE ewha.restaurants (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) UNIQUE NOT NULL,
    city VARCHAR(64) NOT NULL,
    manager VARCHAR(32) NOT NULL,
    opening_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    CHECK (CHAR_LENGTH(name) >= 2),
    CHECK (CHAR_LENGTH(city) >= 2)
);

CREATE TABLE ewha.customer (
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

CREATE TABLE ewha.customer_demographic_history (
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

CREATE TABLE ewha.menu_category (
    id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(32) UNIQUE NOT NULL,
    CHECK (CHAR_LENGTH(category_name) >= 2)
);

CREATE TABLE ewha.menu_item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL,
    item_name VARCHAR(32) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    calories INT NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    CHECK (price > 0),
    CHECK (calories >= 0),
    CONSTRAINT fk_menu_category FOREIGN KEY (category_id) REFERENCES menu_category(id) ON DELETE CASCADE
);

CREATE TABLE ewha.menu_price_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    menu_item_id INT NOT NULL,
    old_price DECIMAL(10,2) NOT NULL,
    new_price DECIMAL(10,2) NOT NULL,
    change_date DATE NOT NULL,
    CONSTRAINT fk_price_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_item(id) ON DELETE CASCADE,
    CHECK (old_price > 0),
    CHECK (new_price > 0),
    CHECK (old_price != new_price)
);

CREATE TABLE ewha.orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    restaurant_id INT NOT NULL,
    order_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_order_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

CREATE TABLE ewha.order_item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    item_price_at_order DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_orderitem_menuitem FOREIGN KEY (menu_item_id) REFERENCES menu_item(id),
    CHECK (quantity > 0),
    CHECK (item_price_at_order > 0)
);

CREATE TABLE ewha.bill (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL,
    final_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_bill_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CHECK (subtotal >= 0),
    CHECK (tax_amount >= 0),
    CHECK (final_total >= subtotal)
);

CREATE TABLE ewha.delivery_drivers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(32) NOT NULL,
    last_name VARCHAR(32) NOT NULL,
    phone VARCHAR(16) UNIQUE NOT NULL,
    city VARCHAR(64),
    employment_date DATE NOT NULL,
    CHECK (phone REGEXP '^\\+82 10-[0-9]{4}-[0-9]{4}$')
);

-- CREATE TABLE ewha.promotion (
--     id INT PRIMARY KEY AUTO_INCREMENT,
--     menu_item_id INT NOT NULL,
--     discount DECIMAL(5,2) NOT NULL,
--     start_date DATE NOT NULL,
--     end_date DATE NOT NULL,
--     CONSTRAINT fk_promotion_menuitem FOREIGN KEY (menu_item_id) REFERENCES menu_item(id) ON DELETE CASCADE,
--     CHECK (discount >= 0 AND discount <= 100),
--     CHECK (end_date >= start_date)
-- );

CREATE INDEX idx_menu_item_name
ON ewha.menu_item(item_name);

CREATE INDEX idx_order_timestamp
ON ewha.orders(order_timestamp);

CREATE INDEX idx_customer_city
ON ewha.customer(city);

CREATE INDEX idx_restaurant_city
ON ewha.restaurants(city);

CREATE VIEW ewha.order_details_view AS
SELECT
    o.id                                 AS order_id,
    o.order_timestamp,
    c.id                                 AS customer_id,
    c.first_name,
    c.last_name,
    c.email,
    r.id                                 AS restaurant_id,
    r.name                               AS restaurant_name,
    r.city                               AS restaurant_city,
    b.subtotal,
    b.tax_amount,
    b.final_total,
    oi.id                                AS order_item_id,
    oi.menu_item_id,
    mi.item_name,
    oi.quantity,
    oi.item_price_at_order,
    oi.quantity * oi.item_price_at_order AS line_subtotal
FROM ewha.orders o
JOIN ewha.customer c    ON o.customer_id = c.id
JOIN ewha.restaurants r ON o.restaurant_id = r.id
JOIN ewha.bill b        ON b.order_id = o.id
JOIN ewha.order_item oi ON oi.order_id = o.id
JOIN ewha.menu_item mi  ON oi.menu_item_id = mi.id;

CREATE VIEW ewha.customer_spending_view AS
SELECT
    c.id                             AS customer_id,
    c.first_name,
    c.last_name,
    c.email,
    c.city,
    COUNT(DISTINCT o.id)             AS total_orders,
    COALESCE(SUM(b.final_total), 0)  AS total_spent
FROM ewha.customer c
LEFT JOIN ewha.orders o ON o.customer_id = c.id
LEFT JOIN ewha.bill b   ON b.order_id = o.id
GROUP BY c.id, c.first_name, c.last_name, c.email, c.city;

CREATE VIEW ewha.v_item_sales_by_price_era AS
SELECT
    oi.id                                AS order_item_id,
    mi.id                                AS menu_item_id,
    mi.item_name,
    o.order_timestamp,
    oi.quantity,
    oi.item_price_at_order,
    oi.quantity * oi.item_price_at_order AS line_revenue,
    mph.old_price                        AS era_old_price,
    mph.new_price                        AS era_new_price,
    mph.change_date                      AS price_changed_on,
    CASE
        WHEN DATE(o.order_timestamp) < mph.change_date THEN 'before'
        ELSE 'after'
    END                                  AS price_era
FROM ewha.order_item oi
JOIN ewha.orders o ON oi.order_id = o.id
JOIN ewha.menu_item mi ON oi.menu_item_id = mi.id
JOIN ewha.menu_price_history mph ON mph.menu_item_id = mi.id;

CREATE VIEW ewha.v_customer_sales_by_demo AS
SELECT
    o.id           AS order_id,
    o.order_timestamp,
    c.id           AS customer_id,
    c.first_name,
    c.last_name,
    c.email,
    b.final_total,
    cdh.city       AS demo_city,
    cdh.age_range,
    cdh.gender,
    cdh.start_date AS demo_start,
    cdh.end_date   AS demo_end,
    CASE
        WHEN cdh.end_date IS NULL THEN 'current'
        ELSE 'historical'
    END            AS demo_status
FROM ewha.orders o
JOIN ewha.customer c ON o.customer_id = c.id
JOIN ewha.bill b     ON o.id = b.order_id
LEFT JOIN ewha.customer_demographic_history cdh
    ON cdh.customer_id = c.id
    AND DATE(o.order_timestamp) >= cdh.start_date
    AND (cdh.end_date IS NULL OR DATE(o.order_timestamp) <= cdh.end_date);