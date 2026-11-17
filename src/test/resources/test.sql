INSERT INTO counties (id, name) VALUES
    ('00000000-0000-4000-8000-000000000102', 'Bucuresti'),
    ('00000000-0000-4000-8000-000000000103', 'Cluj');
INSERT INTO cities (id, name, county_id) VALUES
    ('00000000-0000-4000-8000-000000000104', 'Bucuresti', '00000000-0000-4000-8000-000000000102'),
    ('00000000-0000-4000-8000-000000000105', 'Cluj-Napoca', '00000000-0000-4000-8000-000000000103');
INSERT INTO schools (id, name, city_id) VALUES
    ('e882d373-7f2f-44b2-8c87-530cd04964d4', 'School A', '00000000-0000-4000-8000-000000000104'),
    ('5d958f7d-3407-49ff-b7c1-91098cc5e9e0', 'School B', '00000000-0000-4000-8000-000000000105');

INSERT INTO categories (id, name, parent_id, image_url) VALUES
    ('176ccfb5-fe9b-484c-a799-310eb72a02a5', 'Alphabet', NULL, 'https://open-hands-learning.s3.us-east-1.amazonaws.com/categories/alfabet/main.webp'),
    ('f7198e8f-3cd4-4fe5-bc7e-8f0a39fcf185', 'Letter A', '176ccfb5-fe9b-484c-a799-310eb72a02a5', 'https://open-hands-learning.s3.us-east-1.amazonaws.com/categories/alfabet/a/main.webp');

INSERT INTO users (id, name, email, password_hash, phone, school_id, role) VALUES
    ('4b16f89f-f513-4723-9d19-5750cf7199d6', 'Ana', 'ana@example.test', '$2b$12$Ha1CrwAOpG1UtXg6qdsCrOdDxxK8.gonT5w45mG1NBNqgudz9FYUy', '0712345678', 'e882d373-7f2f-44b2-8c87-530cd04964d4', 'USER'),
    ('0f7cc0da-f19f-48b7-ae3b-5139cdfbd690', 'Dan', 'dan@example.test', '$2b$12$Ha1CrwAOpG1UtXg6qdsCrOdDxxK8.gonT5w45mG1NBNqgudz9FYUy', '0798765432', '5d958f7d-3407-49ff-b7c1-91098cc5e9e0', 'ADMIN');

INSERT INTO favorites (user_id, category_id) VALUES
    ('4b16f89f-f513-4723-9d19-5750cf7199d6', 'f7198e8f-3cd4-4fe5-bc7e-8f0a39fcf185'),
    ('0f7cc0da-f19f-48b7-ae3b-5139cdfbd690', '176ccfb5-fe9b-484c-a799-310eb72a02a5');
