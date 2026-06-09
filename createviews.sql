USE ewha;

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

