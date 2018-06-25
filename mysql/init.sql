CREATE DATABASE bookstoredb;
CREATE DATABASE bookstoretestdb;

CREATE USER 'bookstore' IDENTIFIED BY 'bookstore';
GRANT ALL PRIVILEGES ON bookstoredb.* TO 'bookstore';
GRANT ALL PRIVILEGES ON bookstoretestdb.* TO 'bookstore';
FLUSH PRIVILEGES;

