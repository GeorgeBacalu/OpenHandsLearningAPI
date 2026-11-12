INSERT INTO counties (id, name) VALUES
    ('00000000-0000-4000-8000-000000000102', 'Bucuresti'),
    ('00000000-0000-4000-8000-000000000103', 'Cluj');
INSERT INTO cities (id, name, county_id) VALUES
    ('00000000-0000-4000-8000-000000000104', 'Bucuresti', '00000000-0000-4000-8000-000000000102'),
    ('00000000-0000-4000-8000-000000000105', 'Cluj-Napoca', '00000000-0000-4000-8000-000000000103');
INSERT INTO schools (id, name, city_id) VALUES
    ('e882d373-7f2f-44b2-8c87-530cd04964d4', 'School A', '00000000-0000-4000-8000-000000000104'),
    ('5d958f7d-3407-49ff-b7c1-91098cc5e9e0', 'School B', '00000000-0000-4000-8000-000000000105');

INSERT INTO roles (id, authority) VALUES
    ('99d0dd27-ea57-42b3-8fd8-64e3973a05d3', 'USER'),
    ('4c517662-ab55-47ea-b758-84c1af759525', 'ADMIN');

INSERT INTO sections (id, name, media_key, icon_key) VALUES
    ('7bfc50e0-5106-495b-b08a-5c04b8f5c6a2', 'Video', 'video_section', 'ic_video_section'),
    ('d65b741b-1b28-451f-9faa-51763b236348', 'Voice to Sign', 'voice_to_sign_section', 'ic_voice_to_sign_section');

INSERT INTO categories (id, name, parent_id, media_key) VALUES
    ('176ccfb5-fe9b-484c-a799-310eb72a02a5', 'Alphabet', NULL, 'alfabet'),
    ('f7198e8f-3cd4-4fe5-bc7e-8f0a39fcf185', 'Letter A', '176ccfb5-fe9b-484c-a799-310eb72a02a5', 'dactilema_a');

INSERT INTO users (id, name, email, password, phone, school_id, role_id) VALUES
    ('4b16f89f-f513-4723-9d19-5750cf7199d6', 'Ana', 'ana@example.test', '$2b$12$Ha1CrwAOpG1UtXg6qdsCrOdDxxK8.gonT5w45mG1NBNqgudz9FYUy', '0712345678', 'e882d373-7f2f-44b2-8c87-530cd04964d4', '99d0dd27-ea57-42b3-8fd8-64e3973a05d3'),
    ('0f7cc0da-f19f-48b7-ae3b-5139cdfbd690', 'Dan', 'dan@example.test', '$2b$12$Ha1CrwAOpG1UtXg6qdsCrOdDxxK8.gonT5w45mG1NBNqgudz9FYUy', '0798765432', '5d958f7d-3407-49ff-b7c1-91098cc5e9e0', '4c517662-ab55-47ea-b758-84c1af759525');

INSERT INTO favorites (user_id, category_id) VALUES
    ('4b16f89f-f513-4723-9d19-5750cf7199d6', 'f7198e8f-3cd4-4fe5-bc7e-8f0a39fcf185'),
    ('0f7cc0da-f19f-48b7-ae3b-5139cdfbd690', '176ccfb5-fe9b-484c-a799-310eb72a02a5');
