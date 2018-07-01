CREATE TABLE users (
  id        VARCHAR(36) NOT NULL,
  name      VARCHAR(255) NOT NULL,
  email     VARCHAR(255) NOT NULL UNIQUE ,
  password  VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);
