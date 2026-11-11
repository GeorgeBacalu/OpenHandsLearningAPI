BEGIN;

SET LOCAL lock_timeout = '10s';

LOCK TABLE categories, feedbacks, institutions, letter_sign_pairs,
    notifications, roles, sections, users IN ACCESS EXCLUSIVE MODE;

CREATE FUNCTION pg_temp.migrated_uuid(table_name text, old_id bigint)
RETURNS uuid LANGUAGE plpgsql IMMUTABLE STRICT AS $$
DECLARE
    digest text := md5('OpenHandsLearning:' || table_name || ':' || old_id);
BEGIN
    digest := overlay(digest placing '3' from 13 for 1);
    digest := overlay(digest placing substr('89ab',
        (get_byte(decode(substr(digest, 17, 2), 'hex'), 0) >> 4) % 4 + 1, 1) from 17 for 1);
    RETURN digest::uuid;
END;
$$;

CREATE TEMP TABLE uuid_expected (table_name text NOT NULL, record jsonb NOT NULL) ON COMMIT DROP;
CREATE TEMP TABLE uuid_foreign_keys ON COMMIT DROP AS
SELECT conrelid::regclass AS table_name, conname, pg_get_constraintdef(oid) AS definition
FROM pg_constraint
WHERE contype = 'f' AND conrelid IN (
    'categories'::regclass, 'feedbacks'::regclass, 'institutions'::regclass,
    'letter_sign_pairs'::regclass, 'notifications'::regclass, 'roles'::regclass,
    'sections'::regclass, 'users'::regclass);

DO $$
DECLARE
    target text;
    reference record;
    patch text;
    expected bigint;
    actual bigint;
BEGIN
    FOREACH target IN ARRAY ARRAY['categories', 'feedbacks', 'institutions', 'letter_sign_pairs',
                                  'notifications', 'roles', 'sections', 'users'] LOOP
        IF (SELECT data_type FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = target AND column_name = 'id') <> 'bigint' THEN
            RAISE EXCEPTION 'Expected a bigint ID in %, migration has not been applied', target;
        END IF;
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS version bigint NOT NULL DEFAULT 0', target);
        IF target IN ('categories', 'sections', 'letter_sign_pairs') THEN
            EXECUTE format('ALTER TABLE %I ADD COLUMN display_order integer NOT NULL DEFAULT 2147483647', target);
            EXECUTE format('WITH ordered AS (SELECT id, row_number() OVER (ORDER BY id)::integer AS position FROM %I)
                UPDATE %I t SET display_order = ordered.position FROM ordered WHERE t.id = ordered.id', target, target);
        END IF;
        patch := format('jsonb_build_object(''id'', pg_temp.migrated_uuid(%L, t.id))', target);
        FOR reference IN
            SELECT a.attname AS column_name, c.confrelid::regclass::text AS referenced_table
            FROM pg_constraint c
            JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = c.conkey[1]
            WHERE c.contype = 'f' AND c.conrelid = target::regclass
        LOOP
            patch := patch || format(' || jsonb_build_object(%L, pg_temp.migrated_uuid(%L, t.%I))',
                reference.column_name, reference.referenced_table, reference.column_name);
        END LOOP;
        EXECUTE format('INSERT INTO uuid_expected SELECT %L, to_jsonb(t) || %s FROM %I t', target, patch, target);
    END LOOP;

    FOR reference IN SELECT * FROM uuid_foreign_keys LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', reference.table_name, reference.conname);
    END LOOP;

    FOREACH target IN ARRAY ARRAY['categories', 'feedbacks', 'institutions', 'letter_sign_pairs',
                                  'notifications', 'roles', 'sections', 'users'] LOOP
        EXECUTE format('ALTER TABLE %I ALTER COLUMN id DROP IDENTITY IF EXISTS', target);
        EXECUTE format('ALTER TABLE %I ALTER COLUMN id DROP DEFAULT', target);
        EXECUTE format('ALTER TABLE %I ALTER COLUMN id TYPE uuid USING pg_temp.migrated_uuid(%L, id)', target, target);
        EXECUTE format('ALTER TABLE %I ALTER COLUMN id SET DEFAULT gen_random_uuid()', target);
    END LOOP;

    ALTER TABLE categories ALTER COLUMN parent_id TYPE uuid USING pg_temp.migrated_uuid('categories', parent_id);
    ALTER TABLE categories ALTER COLUMN section_id TYPE uuid USING pg_temp.migrated_uuid('sections', section_id);
    ALTER TABLE users ALTER COLUMN institution_id TYPE uuid USING pg_temp.migrated_uuid('institutions', institution_id);
    ALTER TABLE users ALTER COLUMN role_id TYPE uuid USING pg_temp.migrated_uuid('roles', role_id);
    ALTER TABLE feedbacks ALTER COLUMN user_id TYPE uuid USING pg_temp.migrated_uuid('users', user_id);
    ALTER TABLE notifications ALTER COLUMN sender_id TYPE uuid USING pg_temp.migrated_uuid('users', sender_id);
    ALTER TABLE notifications ALTER COLUMN receiver_id TYPE uuid USING pg_temp.migrated_uuid('users', receiver_id);

    FOR reference IN SELECT * FROM uuid_foreign_keys LOOP
        EXECUTE format('ALTER TABLE %s ADD CONSTRAINT %I %s',
            reference.table_name, reference.conname, reference.definition);
    END LOOP;

    FOREACH target IN ARRAY ARRAY['categories', 'feedbacks', 'institutions', 'letter_sign_pairs',
                                  'notifications', 'roles', 'sections', 'users'] LOOP
        SELECT count(*) INTO expected FROM uuid_expected WHERE table_name = target;
        EXECUTE format('SELECT count(*) FROM %I', target) INTO actual;
        IF actual <> expected THEN
            RAISE EXCEPTION 'Row count mismatch in %: % expected, % actual', target, expected, actual;
        END IF;
        EXECUTE format('SELECT count(*) FROM (
            (SELECT record FROM uuid_expected WHERE table_name = %L EXCEPT ALL SELECT to_jsonb(t) FROM %I t)
            UNION ALL
            (SELECT to_jsonb(t) FROM %I t EXCEPT ALL SELECT record FROM uuid_expected WHERE table_name = %L)
        ) differences', target, target, target, target) INTO actual;
        IF actual <> 0 THEN
            RAISE EXCEPTION 'Record or relationship mismatch in %', target;
        END IF;
        RAISE NOTICE '%: % records preserved', target, expected;
    END LOOP;
END;
$$;

COMMIT;
