INSERT INTO institutions (id, school) VALUES
    ('e882d373-7f2f-44b2-8c87-530cd04964d4', 'School A'),
    ('5d958f7d-3407-49ff-b7c1-91098cc5e9e0', 'School B');

INSERT INTO classrooms (id, name, institution_id) VALUES
    ('00000000-0000-4000-8000-00000000000b', 'A', 'e882d373-7f2f-44b2-8c87-530cd04964d4'),
    ('00000000-0000-4000-8000-00000000000c', 'B', '5d958f7d-3407-49ff-b7c1-91098cc5e9e0');

INSERT INTO roles (id, authority) VALUES
    ('99d0dd27-ea57-42b3-8fd8-64e3973a05d3', 'USER'),
    ('4c517662-ab55-47ea-b758-84c1af759525', 'ADMIN');

INSERT INTO sections (id, name, display_order, code, media_key, icon_key) VALUES
    ('7bfc50e0-5106-495b-b08a-5c04b8f5c6a2', 'Video', 1, 'VIDEO', 'video_section', 'ic_video_section'),
    ('d65b741b-1b28-451f-9faa-51763b236348', 'Expressions', 2, 'EXPRESSIONS', 'expressions_section', 'ic_expressions_section');

INSERT INTO categories (id, name, parent_id, section_id, media_key) VALUES
    ('176ccfb5-fe9b-484c-a799-310eb72a02a5', 'Alphabet', NULL, '7bfc50e0-5106-495b-b08a-5c04b8f5c6a2', 'alfabet'),
    ('f7198e8f-3cd4-4fe5-bc7e-8f0a39fcf185', 'Letter A', '176ccfb5-fe9b-484c-a799-310eb72a02a5', '7bfc50e0-5106-495b-b08a-5c04b8f5c6a2', 'dactilema_a');



INSERT INTO users (id, name, email, password, mobile, address, birthday, institution_id, role_id, classroom_id) VALUES
    ('4b16f89f-f513-4723-9d19-5750cf7199d6', 'Ana', 'ana@example.test', '$2b$12$Ha1CrwAOpG1UtXg6qdsCrOdDxxK8.gonT5w45mG1NBNqgudz9FYUy', '0712345678', 'Bucharest', '2000-01-01', 'e882d373-7f2f-44b2-8c87-530cd04964d4', '99d0dd27-ea57-42b3-8fd8-64e3973a05d3', '00000000-0000-4000-8000-00000000000b'),
    ('0f7cc0da-f19f-48b7-ae3b-5139cdfbd690', 'Dan', 'dan@example.test', '$2b$12$Ha1CrwAOpG1UtXg6qdsCrOdDxxK8.gonT5w45mG1NBNqgudz9FYUy', '0798765432', 'Cluj', '2001-02-02', '5d958f7d-3407-49ff-b7c1-91098cc5e9e0', '4c517662-ab55-47ea-b758-84c1af759525', '00000000-0000-4000-8000-00000000000c');

INSERT INTO feedbacks (id, type, description, sent_at, user_id) VALUES
    ('bc1ff3a7-cbd1-4972-a335-1ba02e3c4bea', 'ISSUE', 'Test issue', '2026-01-01 08:00:00+00', '4b16f89f-f513-4723-9d19-5750cf7199d6'),
    ('a01325a0-2987-4e48-8020-301cde9971d3', 'IMPROVEMENT', 'Test suggestion', '2026-01-02 08:00:00+00', '0f7cc0da-f19f-48b7-ae3b-5139cdfbd690');

INSERT INTO notifications (id, message, sent_at, is_read, sender_id, receiver_id) VALUES
    ('03f3a65b-bb5c-4920-b7df-8eb43d116e38', 'Welcome Ana', '2026-01-01 08:00:00+00', FALSE, '0f7cc0da-f19f-48b7-ae3b-5139cdfbd690', '4b16f89f-f513-4723-9d19-5750cf7199d6'),
    ('4863fd89-f275-4079-bebd-4456592c2aa1', 'Welcome Dan', '2026-01-02 08:00:00+00', TRUE, '4b16f89f-f513-4723-9d19-5750cf7199d6', '0f7cc0da-f19f-48b7-ae3b-5139cdfbd690');


INSERT INTO user_favorites (user_id, category_id, created_at) VALUES
    ('4b16f89f-f513-4723-9d19-5750cf7199d6', 'f7198e8f-3cd4-4fe5-bc7e-8f0a39fcf185', CURRENT_TIMESTAMP),
    ('0f7cc0da-f19f-48b7-ae3b-5139cdfbd690', '176ccfb5-fe9b-484c-a799-310eb72a02a5', CURRENT_TIMESTAMP);
