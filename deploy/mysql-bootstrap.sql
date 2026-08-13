-- Run ONCE on the server's native MySQL (127.0.0.1:3306), not the dockerized
-- easycom-mysql on 3307:
--   mysql -u root -p < deploy/mysql-bootstrap.sql
-- Edit the password below before running - do not commit a real one here.

CREATE DATABASE IF NOT EXISTS construction_material_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- '%' (not '127.0.0.1' or 'localhost') because the connection arrives from
-- the Docker bridge network via host.docker.internal, not from the host itself.
CREATE USER IF NOT EXISTS 'construction_btp'@'%' IDENTIFIED BY 'CHANGE_ME';

GRANT ALL PRIVILEGES ON construction_material_db.* TO 'construction_btp'@'%';
FLUSH PRIVILEGES;
