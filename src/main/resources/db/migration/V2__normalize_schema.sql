CREATE TABLE public.classrooms (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    institution_id uuid NOT NULL REFERENCES public.institutions(id),
    name varchar(255) NOT NULL CHECK (btrim(name) <> ''),
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT classrooms_institution_key UNIQUE (id, institution_id)
);

ALTER TABLE public.users ADD COLUMN classroom_id uuid;

WITH schools AS (
    SELECT id, first_value(id) OVER (PARTITION BY lower(btrim(school)) ORDER BY id) AS institution_id,
           classroom AS name FROM public.institutions
)
INSERT INTO public.classrooms (id, institution_id, name)
SELECT id, institution_id, name FROM schools;

UPDATE public.users u SET classroom_id = u.institution_id, institution_id = c.institution_id
FROM public.classrooms c WHERE c.id = u.institution_id;

DELETE FROM public.institutions i WHERE EXISTS (
    SELECT 1 FROM public.classrooms c WHERE c.id = i.id AND c.institution_id <> i.id
);

ALTER TABLE public.institutions DROP COLUMN classroom;
ALTER TABLE public.users ADD CONSTRAINT users_classroom_institution_fkey
    FOREIGN KEY (classroom_id, institution_id) REFERENCES public.classrooms(id, institution_id);

CREATE UNIQUE INDEX institutions_school_unique ON public.institutions (lower(btrim(school)));
CREATE UNIQUE INDEX classrooms_name_unique ON public.classrooms (institution_id, lower(btrim(name)));
CREATE UNIQUE INDEX users_email_unique ON public.users (lower(btrim(email)));
CREATE UNIQUE INDEX roles_authority_unique ON public.roles (authority);

CREATE TABLE public.user_favorites (
    user_id uuid NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    category_id uuid NOT NULL REFERENCES public.categories(id) ON DELETE CASCADE,
    created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, category_id)
);

INSERT INTO public.user_favorites (user_id, category_id)
SELECT u.id, c.id FROM public.users u CROSS JOIN public.categories c WHERE c.is_favorite;

ALTER TABLE public.categories DROP COLUMN is_favorite, DROP COLUMN is_expanded;
ALTER TABLE public.categories ADD COLUMN code varchar(120), ADD COLUMN media_key varchar(255);
ALTER TABLE public.sections ADD COLUMN code varchar(120), ADD COLUMN media_key varchar(255), ADD COLUMN icon_key varchar(255);

UPDATE public.sections SET code = CASE lower(btrim(name))
    WHEN 'video section' THEN 'VIDEO'
    WHEN 'text to sign section' THEN 'TEXT_TO_SIGN'
    WHEN 'voice to sign section' THEN 'VOICE_TO_SIGN'
    WHEN 'expressions section' THEN 'EXPRESSIONS'
    ELSE 'SECTION_' || upper(replace(id::text, '-', '')) END,
    media_key = lower(regexp_replace(btrim(name), '[^a-zA-Z0-9_]+', '_', 'g')),
    icon_key = 'ic_' || lower(regexp_replace(btrim(name), '[^a-zA-Z0-9_]+', '_', 'g'));

UPDATE public.categories SET code = CASE btrim(name)
    WHEN 'ALFABET' THEN 'ALPHABET'
    WHEN 'NUMERE' THEN 'NUMBERS'
    WHEN 'CULORI' THEN 'COLORS'
    WHEN 'ANIMALE' THEN 'ANIMALS'
    WHEN 'OBIECTE' THEN 'OBJECTS'
    WHEN 'PERSOANE' THEN 'PEOPLE'
    WHEN 'EMOTII' THEN 'EMOTIONS'
    WHEN 'VERBE' THEN 'VERBS'
    WHEN 'FORMULE DE ADRESARE' THEN 'FORMS_OF_ADDRESS'
    WHEN 'ANIMALE DOMESTICE' THEN 'HOUSEHOLD_ANIMALS'
    WHEN 'ANIMALE SALBATICE' THEN 'WILD_ANIMALS'
    WHEN 'ACASA' THEN 'HOME'
    WHEN 'AFARA' THEN 'OUTSIDE'
    WHEN 'CLASA' THEN 'CLASSROOM'
    WHEN 'MAGAZIN' THEN 'SHOP'
    WHEN 'ORAS' THEN 'CITY'
    WHEN 'BANI' THEN 'MONEY'
    WHEN 'PRODUSE' THEN 'PRODUCTS'
    WHEN 'MEMBRII FAMILIEI' THEN 'FAMILY_MEMBERS'
    WHEN 'PRONUME' THEN 'PRONOUNS'
    ELSE 'CATEGORY_' || upper(replace(id::text, '-', '')) END,
    media_key = lower(regexp_replace(translate(btrim(name), 'Ã„Æ’ÃƒÂ¢ÃƒÂ®Ãˆâ„¢Ã…Å¸Ãˆâ€ºÃ…Â£Ã„â€šÃƒâ€šÃƒÅ½ÃˆËœÃ…Å¾ÃˆÅ¡Ã…Â¢', 'aaissttAAISSTT'), '[^a-zA-Z0-9_]+', '_', 'g'));

ALTER TABLE public.categories
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN section_id SET NOT NULL,
    ALTER COLUMN code SET NOT NULL,
    ADD CONSTRAINT categories_code_unique UNIQUE (code),
    ADD CONSTRAINT categories_code_format CHECK (code ~ '^[A-Z][A-Z0-9_]*$'),
    ADD CONSTRAINT categories_name_not_blank CHECK (btrim(name) <> ''),
    ADD CONSTRAINT categories_not_self_parent CHECK (parent_id IS NULL OR parent_id <> id),
    ADD CONSTRAINT categories_display_order_valid CHECK (display_order >= 0),
    ADD CONSTRAINT categories_section_key UNIQUE (id, section_id),
    DROP CONSTRAINT categories_parent_id_fkey,
    ADD CONSTRAINT categories_parent_section_fkey FOREIGN KEY (parent_id, section_id) REFERENCES public.categories(id, section_id);

ALTER TABLE public.sections
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN code SET NOT NULL,
    ADD CONSTRAINT sections_code_unique UNIQUE (code),
    ADD CONSTRAINT sections_code_format CHECK (code ~ '^[A-Z][A-Z0-9_]*$'),
    ADD CONSTRAINT sections_name_not_blank CHECK (btrim(name) <> ''),
    ADD CONSTRAINT sections_display_order_valid CHECK (display_order >= 0);

ALTER TABLE public.roles
    ALTER COLUMN authority SET NOT NULL,
    ADD CONSTRAINT roles_authority_valid CHECK (authority IN ('USER', 'ADMIN'));

ALTER TABLE public.institutions
    ALTER COLUMN school SET NOT NULL,
    ADD CONSTRAINT institutions_school_not_blank CHECK (btrim(school) <> '');

ALTER TABLE public.letter_sign_pairs
    ALTER COLUMN letter SET NOT NULL,
    ADD CONSTRAINT letters_not_empty CHECK (char_length(letter) > 0),
    ADD CONSTRAINT letters_display_order_valid CHECK (display_order >= 0);

ALTER TABLE public.users
    ALTER COLUMN name SET NOT NULL, ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN password SET NOT NULL, ALTER COLUMN mobile SET NOT NULL,
    ALTER COLUMN address SET NOT NULL, ALTER COLUMN institution_id SET NOT NULL,
    ALTER COLUMN role_id SET NOT NULL,
    ADD CONSTRAINT users_required_text CHECK (btrim(name) <> '' AND btrim(email) <> ''
        AND btrim(password) <> '' AND btrim(mobile) <> '' AND btrim(address) <> '');

ALTER TABLE public.feedbacks
    ALTER COLUMN description SET NOT NULL, ALTER COLUMN type SET NOT NULL,
    ALTER COLUMN user_id SET NOT NULL, ALTER COLUMN sent_at SET NOT NULL,
    ADD CONSTRAINT feedback_description_not_blank CHECK (btrim(description) <> ''),
    ADD CONSTRAINT feedback_type_valid CHECK (type IN ('ISSUE', 'OPTIMIZATION', 'IMPROVEMENT'));

ALTER TABLE public.notifications
    ALTER COLUMN message SET NOT NULL, ALTER COLUMN sender_id SET NOT NULL,
    ALTER COLUMN receiver_id SET NOT NULL, ALTER COLUMN sent_at SET NOT NULL,
    ADD CONSTRAINT notifications_message_not_blank CHECK (btrim(message) <> '');

CREATE INDEX idx_categories_section_parent_order ON public.categories (section_id, parent_id, display_order, id);
CREATE INDEX idx_categories_parent ON public.categories (parent_id);
CREATE INDEX idx_user_favorites_category ON public.user_favorites (category_id);
CREATE INDEX idx_users_institution ON public.users (institution_id);
CREATE INDEX idx_users_role ON public.users (role_id);
CREATE INDEX idx_users_classroom ON public.users (classroom_id);
CREATE INDEX idx_notifications_receiver ON public.notifications (receiver_id, sent_at DESC, id);
CREATE INDEX idx_notifications_sender ON public.notifications (sender_id);
CREATE INDEX idx_feedbacks_user ON public.feedbacks (user_id, sent_at DESC, id);

CREATE FUNCTION public.check_category_hierarchy() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND NEW.parent_id IS NOT DISTINCT FROM OLD.parent_id
        AND NEW.section_id IS NOT DISTINCT FROM OLD.section_id THEN RETURN NEW; END IF;
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

CREATE TRIGGER categories_hierarchy_guard BEFORE INSERT OR UPDATE OF parent_id, section_id
ON public.categories FOR EACH ROW EXECUTE FUNCTION public.check_category_hierarchy();

DO $$
BEGIN
    IF EXISTS (
        WITH RECURSIVE paths AS (
            SELECT id, parent_id, ARRAY[id] AS visited, false AS cycle FROM public.categories
            UNION ALL
            SELECT p.id, c.parent_id, p.visited || c.id, c.id = ANY(p.visited)
            FROM paths p JOIN public.categories c ON c.id = p.parent_id WHERE NOT p.cycle
        ) SELECT 1 FROM paths WHERE cycle
    ) THEN RAISE EXCEPTION 'Existing category hierarchy contains cycles'; END IF;
END;
$$;
