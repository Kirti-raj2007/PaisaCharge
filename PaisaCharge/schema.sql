CREATE DATABASE IF NOT EXISTS paisacharge;
USE paisacharge;

CREATE TABLE IF NOT EXISTS subscribers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(60) NOT NULL,
  mobile CHAR(10) NOT NULL UNIQUE,
  type ENUM('Prepaid','Postpaid') NOT NULL,
  expiry DATETIME NULL
);

CREATE TABLE IF NOT EXISTS plans (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(40) NOT NULL,
  price DECIMAL(8,2) NOT NULL,
  days INT NOT NULL,
  data VARCHAR(30) NOT NULL,
  type ENUM('Prepaid','Postpaid') NOT NULL
);

CREATE TABLE IF NOT EXISTS recharges (
  id INT AUTO_INCREMENT PRIMARY KEY,
  subscriber_id INT NOT NULL,
  plan_id INT NOT NULL,
  amount DECIMAL(8,2) NOT NULL,
  base DECIMAL(8,2) NOT NULL,
  gst DECIMAL(8,2) NOT NULL,
  recharged_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  expiry DATETIME NOT NULL,
  FOREIGN KEY (subscriber_id) REFERENCES subscribers(id) ON DELETE CASCADE,
  FOREIGN KEY (plan_id) REFERENCES plans(id)
);

INSERT INTO plans (name, price, days, data, type) VALUES
 ('Basic 28',199,28,'1.5 GB/day','Prepaid'),
 ('Plus 28',299,28,'2 GB/day','Prepaid'),
 ('Quarterly',749,84,'2 GB/day','Prepaid'),
 ('Annual Saver',2399,365,'2.5 GB/day','Prepaid'),
 ('Postpaid 399',399,30,'75 GB','Postpaid'),
 ('Postpaid 599',599,30,'150 GB','Postpaid'),
 ('Family 999',999,30,'300 GB','Postpaid');

-- Called from Java through a CallableStatement
DROP PROCEDURE IF EXISTS recharge_plan;
DELIMITER //
CREATE PROCEDURE recharge_plan(
  IN p_sub INT, IN p_plan INT, IN p_amount DECIMAL(8,2),
  IN p_base DECIMAL(8,2), IN p_gst DECIMAL(8,2), OUT p_expiry DATETIME)
BEGIN
  DECLARE d INT;
  DECLARE start_at DATETIME;
  SELECT days INTO d FROM plans WHERE id = p_plan;
  SELECT GREATEST(NOW(), IFNULL(expiry, NOW())) INTO start_at FROM subscribers WHERE id = p_sub;
  SET p_expiry = DATE_ADD(start_at, INTERVAL d DAY);
  INSERT INTO recharges (subscriber_id, plan_id, amount, base, gst, expiry)
    VALUES (p_sub, p_plan, p_amount, p_base, p_gst, p_expiry);
  UPDATE subscribers SET expiry = p_expiry WHERE id = p_sub;
END //
DELIMITER ;
