CREATE DATABASE IF NOT EXISTS authservice;
CREATE DATABASE IF NOT EXISTS userservice;
CREATE DATABASE IF NOT EXISTS productservice;
CREATE DATABASE IF NOT EXISTS notificationservice;

CREATE USER IF NOT EXISTS 'authuser'@'%' IDENTIFIED BY 'authpassword';
GRANT ALL PRIVILEGES ON authservice.* TO 'authuser'@'%';

GRANT ALL PRIVILEGES ON userservice.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON productservice.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON notificationservice.* TO 'root'@'%';

FLUSH PRIVILEGES;

USE productservice;

CREATE TABLE IF NOT EXISTS shedlock (
  name VARCHAR(64) NOT NULL,
  lock_until TIMESTAMP(3) NOT NULL,
  locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  locked_by VARCHAR(255) NOT NULL,
  PRIMARY KEY (name)
);
