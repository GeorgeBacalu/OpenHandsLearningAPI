DELETE FROM favorites;
UPDATE categories SET parent_id = NULL;
DELETE FROM categories;
DELETE FROM users;
DELETE FROM roles;
DELETE FROM schools;
DELETE FROM sections;
DELETE FROM cities;
DELETE FROM counties;
