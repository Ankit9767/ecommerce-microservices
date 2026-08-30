CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS cart_db;
CREATE DATABASE IF NOT EXISTS inventory_db;
CREATE DATABASE IF NOT EXISTS notification_db;
CREATE DATABASE IF NOT EXISTS shipping_db;

CREATE USER IF NOT EXISTS 'product_user'@'%' IDENTIFIED BY 'product123';
CREATE USER IF NOT EXISTS 'order_user'@'%' IDENTIFIED BY 'order123';
CREATE USER IF NOT EXISTS 'payment_user'@'%' IDENTIFIED BY 'payment123';
CREATE USER IF NOT EXISTS 'auth_user'@'%' IDENTIFIED BY 'auth123';
CREATE USER IF NOT EXISTS 'cart_user'@'%' IDENTIFIED BY 'cart123';
CREATE USER IF NOT EXISTS 'inventory_user'@'%' IDENTIFIED BY 'inventory123';
CREATE USER IF NOT EXISTS 'notification_user'@'%' IDENTIFIED BY 'notification123';
CREATE USER IF NOT EXISTS 'shipping_user'@'%' IDENTIFIED BY 'shipping123';

GRANT ALL PRIVILEGES ON product_db.* TO 'product_user'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'order_user'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'payment_user'@'%';
GRANT ALL PRIVILEGES ON auth_db.* TO 'auth_user'@'%';
GRANT ALL PRIVILEGES ON cart_db.* TO 'cart_user'@'%';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'inventory_user'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'notification_user'@'%';
GRANT ALL PRIVILEGES ON shipping_db.* TO 'shipping_user'@'%';

FLUSH PRIVILEGES;