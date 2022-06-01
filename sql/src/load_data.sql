COPY MENU
FROM '/extra/skang121/project1/data/menu.csv'
WITH DELIMITER ';';

COPY USERS
FROM '/extra/skang121/project1/data/users.csv'
WITH DELIMITER ';';

COPY ORDERS
FROM '/extra/skang121/project1/data/orders.csv'
WITH DELIMITER ';';
ALTER SEQUENCE orders_orderid_seq RESTART 87257;

COPY ITEMSTATUS
FROM '/extra/skang121/project1/data/itemStatus.csv'
WITH DELIMITER ';';

