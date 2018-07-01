CREATE TABLE categories (
  id      VARCHAR(36) NOT NULL,
  title   VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);
ALTER TABLE categories ADD CONSTRAINT categories_unique_title UNIQUE (title);