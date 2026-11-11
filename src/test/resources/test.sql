INSERT INTO institutions (id, version, school, classroom) VALUES
    ('00000000-0000-4000-8000-000000000001', 0, 'School A', 'A'),
    ('00000000-0000-4000-8000-000000000002', 0, 'School B', 'B');

INSERT INTO roles (id, version, authority) VALUES
    ('00000000-0000-4000-8000-000000000001', 0, 'USER'),
    ('00000000-0000-4000-8000-000000000002', 0, 'ADMIN');

INSERT INTO sections (id, version, name, icon_id, image_id, display_order) VALUES
    ('00000000-0000-4000-8000-000000000001', 0, 'Video', 1, 1, 1),
    ('00000000-0000-4000-8000-000000000002', 0, 'Expressions', 2, 2, 2);

INSERT INTO categories (id, version, name, image_id, is_expanded, is_favorite, parent_id, section_id, display_order) VALUES
    ('00000000-0000-4000-8000-000000000001', 0, 'Alphabet', 1, FALSE, FALSE, NULL, '00000000-0000-4000-8000-000000000001', 1),
    ('00000000-0000-4000-8000-000000000002', 0, 'Letter A', 2, FALSE, TRUE, '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000001', 2);

INSERT INTO letter_sign_pairs (id, version, letter, image_id, display_order) VALUES
    ('00000000-0000-4000-8000-000000000001', 0, 'A', 1, 1),
    ('00000000-0000-4000-8000-000000000002', 0, 'B', 2, 2);

INSERT INTO users (id, version, name, email, password, mobile, address, birthday, institution_id, role_id) VALUES
    ('00000000-0000-4000-8000-000000000001', 0, 'Ana', 'ana@example.test', '$2b$12$Ha1CrwAOpG1UtXg6qdsCrOdDxxK8.gonT5w45mG1NBNqgudz9FYUy', '0712345678', 'Bucharest', '2000-01-01', '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000001'),
    ('00000000-0000-4000-8000-000000000002', 0, 'Dan', 'dan@example.test', '$2b$12$Ha1CrwAOpG1UtXg6qdsCrOdDxxK8.gonT5w45mG1NBNqgudz9FYUy', '0798765432', 'Cluj', '2001-02-02', '00000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000002');

INSERT INTO feedbacks (id, version, type, description, sent_at, user_id) VALUES
    ('00000000-0000-4000-8000-000000000001', 0, 'ISSUE', 'Test issue', '2026-01-01 10:00:00', '00000000-0000-4000-8000-000000000001'),
    ('00000000-0000-4000-8000-000000000002', 0, 'IMPROVEMENT', 'Test suggestion', '2026-01-02 10:00:00', '00000000-0000-4000-8000-000000000002');

INSERT INTO notifications (id, version, message, sent_at, is_read, sender_id, receiver_id) VALUES
    ('00000000-0000-4000-8000-000000000001', 0, 'Welcome Ana', '2026-01-01 10:00:00', FALSE, '00000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000001'),
    ('00000000-0000-4000-8000-000000000002', 0, 'Welcome Dan', '2026-01-02 10:00:00', TRUE, '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002');
