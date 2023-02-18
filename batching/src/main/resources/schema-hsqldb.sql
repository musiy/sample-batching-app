-- actual fields and their types/sizes could be discussed
CREATE TABLE USERS
(
    id  BIGINT IDENTITY NOT NULL PRIMARY KEY,
    name      VARCHAR(50),
    username  VARCHAR(50),
    email     VARCHAR(50),
    phone     VARCHAR(50),
    address   VARCHAR(2000)
);
