-- restaurants
INSERT INTO ewha.restaurants (name, city, manager, opening_date) VALUES
  ('Ewha Bistro', 'Seoul', 'James Kim', '2022-03-15'),
  ('Han River Grill', 'Seoul', 'Sarah Choi', '2021-07-20'),
  ('Busan Bay Kitchen', 'Busan', 'Michael Yoon', '2020-11-05'),
  ('Incheon Harbor', 'Incheon', 'Linda Park', '2023-01-10'),
  ('Daegu Garden', 'Daegu', 'Kevin Shin', '2019-06-18'),
  ('Gwangju Table', 'Gwangju', 'Amy Jung', '2022-08-22'),
  ('Suwon Castle Cafe', 'Suwon', 'Daniel Oh', '2021-02-14'),
  ('Jeju Ocean Diner', 'Jeju', 'Rachel Moon', '2023-05-30'),
  ('Ulsan Steel Grill', 'Ulsan', 'Tom Bae', '2020-09-09'),
  ('Daejeon Central', 'Daejeon', 'Jenny Lim', '2018-12-01');

-- customer
INSERT INTO ewha.customer (first_name, last_name, email, phone, city, age, gender) VALUES
  ('Emily', 'Park', 'emily.park@gmail.com', '+82 10-4582-9171', 'Seoul', 28, 'Female'),
  ('Daniel', 'Kim', 'daniel.kim@naver.com', '+82 10-4528-3149', 'Busan', 35, 'Male'),
  ('Sophia', 'Lee', 'sophia.lee@outlook.com', '+82 10-1693-9085', 'Incheon', 22, 'Female'),
  ('James', 'Choi', 'james.choi@kakao.com', '+82 10-8451-6466', 'Daegu', 42, 'Male'),
  ('Olivia', 'Jung', 'olivia.jung@gmail.com', '+82 10-3330-3227', 'Seoul', 19, 'Female'),
  ('Ethan', 'Yoon', 'ethan.yoon@naver.com', '+82 10-9777-5065', 'Gwangju', 31, 'Male'),
  ('Isabella', 'Shin', 'isabella.shin@gmail.com', '+82 10-7318-1382', 'Suwon', 27, 'Female'),
  ('Noah', 'Oh', 'noah.oh@daum.net', '+82 10-3894-3369', 'Jeju', 55, 'Male'),
  ('Mia', 'Moon', 'mia.moon@gmail.com', '+82 10-1970-1215', 'Ulsan', 46, 'Female'),
  ('Liam', 'Bae', 'liam.bae@naver.com', '+82 10-1239-8503', 'Daejeon', 33, 'Male');

-- customer_demographic_history
INSERT INTO ewha.customer_demographic_history (customer_id, city, age_range, gender, start_date, end_date) VALUES
  (1, 'Seoul', '20-29', 'Female', '2022-01-01', NULL),
  (2, 'Busan', '30-39', 'Male', '2022-01-01', NULL),
  (3, 'Incheon', '20-29', 'Female', '2022-01-01', NULL),
  (4, 'Daegu', '40-49', 'Male', '2022-01-01', NULL),
  (5, 'Seoul', '10-19', 'Female', '2022-01-01', NULL),
  (6, 'Gwangju', '30-39', 'Male', '2022-01-01', NULL),
  (7, 'Suwon', '20-29', 'Female', '2022-01-01', NULL),
  (8, 'Jeju', '50-59', 'Male', '2022-01-01', NULL),
  (9, 'Ulsan', '40-49', 'Female', '2022-01-01', NULL),
  (10, 'Daejeon', '30-39', 'Male', '2022-01-01', NULL);

-- menu_category
INSERT INTO ewha.menu_category (category_name) VALUES
  ('Korean Food'),
  ('Japanese Food'),
  ('Chinese Food'),
  ('Western Food'),
  ('Desserts'),
  ('Beverages'),
  ('Seafood'),
  ('Vegetarian'),
  ('Fast Food'),
  ('Soups & Stews');

-- menu_item
INSERT INTO ewha.menu_item (category_id, item_name, price, calories, is_available) VALUES
  (1, 'Bibimbap', 9000.0, 650, TRUE),
  (2, 'Sushi Set', 12000.0, 520, TRUE),
  (3, 'Kung Pao Chicken', 8500.0, 700, TRUE),
  (4, 'Beef Steak', 22000.0, 850, TRUE),
  (5, 'Tiramisu', 6000.0, 380, TRUE),
  (6, 'Americano', 4000.0, 10, TRUE),
  (7, 'Grilled Salmon', 18000.0, 480, TRUE),
  (8, 'Veggie Wrap', 7500.0, 420, TRUE),
  (9, 'Cheese Burger', 10000.0, 750, TRUE),
  (10, 'Kimchi Jjigae', 8000.0, 580, TRUE);

-- menu_price_history
INSERT INTO ewha.menu_price_history (menu_item_id, old_price, new_price, change_date) VALUES
  (1, 8100.0, 9000.0, '2024-01-01'),
  (2, 10800.0, 12000.0, '2024-01-01'),
  (3, 7650.0, 8500.0, '2024-01-01'),
  (4, 19800.0, 22000.0, '2024-01-01'),
  (5, 5400.0, 6000.0, '2024-01-01'),
  (6, 3600.0, 4000.0, '2024-01-01'),
  (7, 16200.0, 18000.0, '2024-01-01'),
  (8, 6750.0, 7500.0, '2024-01-01'),
  (9, 9000.0, 10000.0, '2024-01-01'),
  (10, 7200.0, 8000.0, '2024-01-01');

-- orders
INSERT INTO ewha.orders (customer_id, restaurant_id, order_timestamp) VALUES
  (1, 2, '2024-01-13 20:17:00'),
  (2, 4, '2024-04-24 11:47:00'),
  (3, 2, '2024-12-12 20:57:00'),
  (4, 9, '2024-02-14 18:27:00'),
  (5, 1, '2024-01-16 10:13:00'),
  (6, 4, '2024-09-15 18:01:00'),
  (7, 9, '2024-04-11 20:41:00'),
  (8, 9, '2024-08-02 12:28:00'),
  (9, 10, '2024-05-22 21:55:00'),
  (10, 1, '2024-03-22 20:27:00');

-- order_item
INSERT INTO ewha.order_item (order_id, menu_item_id, quantity, item_price_at_order) VALUES
  (1, 6, 2, 4000.0),
  (2, 3, 1, 8500.0),
  (3, 6, 1, 4000.0),
  (4, 2, 2, 12000.0),
  (5, 2, 2, 12000.0),
  (6, 6, 3, 4000.0),
  (7, 5, 1, 6000.0),
  (8, 8, 3, 7500.0),
  (9, 2, 2, 12000.0),
  (10, 2, 3, 12000.0);

-- bill
INSERT INTO ewha.bill (order_id, subtotal, tax_amount, final_total) VALUES
  (1, 8000.0, 800.0, 8800.0),
  (2, 8500.0, 850.0, 9350.0),
  (3, 4000.0, 400.0, 4400.0),
  (4, 24000.0, 2400.0, 26400.0),
  (5, 24000.0, 2400.0, 26400.0),
  (6, 12000.0, 1200.0, 13200.0),
  (7, 6000.0, 600.0, 6600.0),
  (8, 22500.0, 2250.0, 24750.0),
  (9, 24000.0, 2400.0, 26400.0),
  (10, 36000.0, 3600.0, 39600.0);

-- delivery_drivers
INSERT INTO ewha.delivery_drivers (first_name, last_name, phone, city, employment_date) VALUES
  ('Tom', 'Lee', '+82 10-6925-4150', 'Seoul', '2021-08-23'),
  ('Anna', 'Choi', '+82 10-2139-1750', 'Busan', '2023-12-13'),
  ('Brian', 'Park', '+82 10-4733-5741', 'Incheon', '2023-09-16'),
  ('Clara', 'Kim', '+82 10-4814-2654', 'Daegu', '2020-06-12'),
  ('David', 'Jung', '+82 10-5554-8428', 'Seoul', '2022-02-17'),
  ('Eva', 'Yoon', '+82 10-6977-3664', 'Gwangju', '2023-07-25'),
  ('Frank', 'Shin', '+82 10-6820-4432', 'Suwon', '2022-01-28'),
  ('Grace', 'Oh', '+82 10-5374-2169', 'Jeju', '2023-10-04'),
  ('Henry', 'Moon', '+82 10-3803-9751', 'Ulsan', '2023-06-01'),
  ('Iris', 'Bae', '+82 10-3677-8573', 'Daejeon', '2021-05-16');
