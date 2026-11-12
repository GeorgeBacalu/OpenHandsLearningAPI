DELETE FROM favorites;
UPDATE categories SET parent_id = NULL;
DELETE FROM categories;
DELETE FROM users;
DELETE FROM roles;
DELETE FROM schools;
DELETE FROM cities;
DELETE FROM counties;
