DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM public.categories
        WHERE section_id <> '20a6880c-2e37-32e3-8e45-07a17e99a000'
    ) THEN
        RAISE EXCEPTION 'Move all categories into the Video catalog before removing section_id';
    END IF;
END;
$$;

DROP TRIGGER categories_hierarchy_guard ON public.categories;

ALTER TABLE public.categories
    DROP CONSTRAINT categories_parent_section_fkey,
    DROP CONSTRAINT categories_section_key,
    DROP COLUMN section_id,
    ADD CONSTRAINT categories_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.categories(id);

CREATE UNIQUE INDEX categories_sibling_name_unique
    ON public.categories (parent_id, lower(btrim(name))) NULLS NOT DISTINCT;

CREATE OR REPLACE FUNCTION public.check_category_hierarchy() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND NEW.parent_id IS NOT DISTINCT FROM OLD.parent_id THEN RETURN NEW; END IF;
    PERFORM pg_advisory_xact_lock(1869114476);
    IF NEW.parent_id IS NOT NULL AND EXISTS (
        WITH RECURSIVE ancestors AS (
            SELECT id, parent_id FROM public.categories WHERE id = NEW.parent_id
            UNION
            SELECT c.id, c.parent_id FROM public.categories c JOIN ancestors a ON c.id = a.parent_id
        ) SELECT 1 FROM ancestors WHERE id = NEW.id
    ) THEN
        RAISE EXCEPTION 'Category hierarchy must not contain cycles' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER categories_hierarchy_guard BEFORE INSERT OR UPDATE OF parent_id
ON public.categories FOR EACH ROW EXECUTE FUNCTION public.check_category_hierarchy();
