-- Run once only on a database that completed the original V11-V16 sequence.
-- This changes migration metadata only. Take a full database backup first.
BEGIN;
LOCK TABLE public.flyway_schema_history IN EXCLUSIVE MODE;
DO $$
BEGIN
    IF (SELECT count(*) FROM public.flyway_schema_history
        WHERE success AND (version, script) IN (
            ('11', 'V11__simplify_users.sql'),
            ('12', 'V12__simplify_favorites.sql'),
            ('13', 'V13__simplify_sections.sql'),
            ('14', 'V14__unify_category_catalog.sql'),
            ('15', 'V15__remove_sections_table.sql'),
            ('16', 'db.migration.V16__rename_password_to_password_hash')
        )) <> 6 THEN
        RAISE EXCEPTION 'Expected the original successful V11-V16 history. Do not reconcile a partial or already-renumbered database.';
    END IF;
    IF EXISTS (SELECT 1 FROM public.flyway_schema_history WHERE NOT success)
        OR EXISTS (SELECT 1 FROM public.flyway_schema_history
                   WHERE success AND version::numeric > 16) THEN
        RAISE EXCEPTION 'Unexpected failed or newer migrations; review history manually.';
    END IF;
    IF to_regclass('public.sections') IS NOT NULL
        OR to_regclass('public.favorites') IS NULL
        OR NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'password_hash')
        OR NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema = 'public' AND table_name = 'categories' AND column_name = 'media_key')
        OR EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public' AND table_name = 'favorites' AND column_name = 'created_at') THEN
        RAISE EXCEPTION 'The schema does not match the completed original V16 installation.';
    END IF;
END $$;
CREATE TABLE public.flyway_schema_history_before_s3 AS TABLE public.flyway_schema_history;
DELETE FROM public.flyway_schema_history WHERE success AND version IN ('12', '13', '16');
UPDATE public.flyway_schema_history
SET description = 'simplify users favorites and sections',
    script = 'V11__simplify_users_favorites_and_sections.sql',
    checksum = 1814184016
WHERE success AND version = '11';
UPDATE public.flyway_schema_history
SET version = CASE version WHEN '14' THEN '12' WHEN '15' THEN '13' END,
    script = CASE version
        WHEN '14' THEN 'V12__unify_category_catalog.sql'
        WHEN '15' THEN 'V13__remove_sections_table.sql'
    END
WHERE success AND version IN ('14', '15');
COMMIT;
