CREATE TABLE books (
  id            VARCHAR(36) NOT NULL,
  title         VARCHAR(255) NOT NULL,
  release_date  DATE NOT NULL,
  category_id   VARCHAR(36),
  quantity      INTEGER,
  author        VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (category_id) REFERENCES categories(id)
);
ALTER TABLE books ADD CONSTRAINT books_unique_title UNIQUE (title);