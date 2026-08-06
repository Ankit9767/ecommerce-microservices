CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS auth_db;

CREATE USER IF NOT EXISTS 'product_user'@'%' IDENTIFIED BY 'product123';
CREATE USER IF NOT EXISTS 'order_user'@'%' IDENTIFIED BY 'order123';
CREATE USER IF NOT EXISTS 'payment_user'@'%' IDENTIFIED BY 'payment123';
CREATE USER IF NOT EXISTS 'auth_user'@'%' IDENTIFIED BY 'auth123';

GRANT ALL PRIVILEGES ON product_db.* TO 'product_user'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'order_user'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'payment_user'@'%';
GRANT ALL PRIVILEGES ON auth_db.* TO 'auth_user'@'%';

FLUSH PRIVILEGES;