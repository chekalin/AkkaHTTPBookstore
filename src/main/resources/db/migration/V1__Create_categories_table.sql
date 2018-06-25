CREATE TABLE categories (
  id      INTEGER NOT NULL AUTO_INCREMENT,
  title   VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);
ALTER TABLE categories ADD CONSTRAINT categories_unique_title UNIQUE (title);