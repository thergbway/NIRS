DROP TABLE IF EXISTS tokens;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  username   TEXT PRIMARY KEY,
  password   TEXT NOT NULL,
  first_name TEXT NOT NULL,
  last_name  TEXT NOT NULL,
  email      TEXT NOT NULL UNIQUE
);


CREATE TABLE tokens (
  value    TEXT PRIMARY KEY,
  username TEXT REFERENCES users(username)
);

INSERT INTO users(username, password, first_name, last_name, email) VALUES
  ('thergbway', '12345', 'Andrey', 'Selivanov', 'thergbworld@gmail.com'),
  ('zetro', '12345', 'Dmitry', 'Korobov', 'zps@gmail.com');

INSERT INTO tokens (value, username) VALUES
  ('abc', 'thergbway'),
  ('123', 'zetro');
