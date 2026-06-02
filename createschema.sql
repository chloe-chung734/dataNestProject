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

CREATE INDEX idx_customer_demo_history_date
    ON customer_demographic_history(customer_id, start_date);

CREATE INDEX idx_menu_price_history_date
    ON menu_price_history(menu_item_id, change_date);
    
-- CREATE VIEW customer_order_summary_view AS
-- SELECT
--     c.id AS customer_id,
--     c.first_name,
--     c.last_name,
--     co.id AS order_id,
--     co.order_timestamp,
--     b.final_total
-- FROM ewha.customer c
-- JOIN ewha.customer_order co ON c.id = co.customer_id
-- JOIN ewha.bill b ON co.id = b.order_id;

-- CREATE VIEW restaurant_sales_view AS
-- SELECT r.name AS restaurant_name, DATE(co.order_timestamp) AS sales_date, SUM(b.final_total) AS daily_sales FROM ewha.restaurants r
-- JOIN ewha.customer_order co ON r.id = co.restaurant_id
-- JOIN ewha.bill b ON co.id = b.order_id
-- GROUP BY r.name, DATE(co.order_timestamp);

----REQ13
CREATE VIEW v_item_sales_by_price_era AS
SELECT
    oi.id                               AS order_item_id,
    mi.id                               AS menu_item_id,
    mi.item_name,
    o.order_timestamp,
    oi.quantity,
    oi.item_price_at_order,             -- the price actually charged (snapshot)
    oi.quantity * oi.item_price_at_order AS line_revenue,

    -- price era: which historical window does this order fall in?
    mph.old_price                       AS era_old_price,
    mph.new_price                       AS era_new_price,
    mph.change_date                     AS price_changed_on,

    -- label: was this order before or after the price change?
    CASE
        WHEN DATE(o.order_timestamp) < mph.change_date THEN 'before'
        WHEN DATE(o.order_timestamp) >= mph.change_date THEN 'after'
    END                                 AS price_era

FROM order_item oi
JOIN orders      o   ON oi.order_id   = o.id
JOIN menu_item   mi  ON oi.menu_item_id = mi.id

-- each item joins to most recent price before change
-- left join to maintain any orders before a price change (handle NULL in menu query)
LEFT JOIN menu_price_history mph
    ON mph.menu_item_id = mi.id
    AND mph.change_date = (
        SELECT MAX(change_date)
        FROM menu_price_history
        WHERE menu_item_id = mi.id
        AND change_date <= DATE (o.order_timestamp)
    );


--REQ 14
CREATE VIEW v_customer_sales_by_demo AS
SELECT
    o.id                                AS order_id,
    o.order_timestamp,
    c.id                                AS customer_id,
    c.first_name,
    c.last_name,
    b.final_total,

    -- demographic snapshot active at order time
    cdh.city                            AS demo_city,
    cdh.age_range,
    cdh.gender,
    cdh.start_date                      AS demo_start,
    cdh.end_date                        AS demo_end,

    -- was this order placed under the current or an old demographic?
    CASE
        WHEN cdh.end_date IS NULL THEN 'current'
        ELSE 'historical'
    END                                 AS demo_status

FROM orders o
JOIN customer  c   ON o.customer_id = c.id
JOIN bill      b   ON o.id          = b.order_id

-- Match demographic window to order timestamp
LEFT JOIN customer_demographic_history cdh
    ON cdh.customer_id = c.id
    AND DATE(o.order_timestamp) >= cdh.start_date
    AND (cdh.end_date IS NULL OR DATE(o.order_timestamp) <= cdh.end_date);

