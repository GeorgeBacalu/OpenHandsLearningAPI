ALTER TABLE public.users DROP CONSTRAINT users_required_text;
ALTER TABLE public.users RENAME COLUMN mobile TO phone;
ALTER TABLE public.users RENAME COLUMN password TO password_hash;
ALTER TABLE public.users RENAME CONSTRAINT users_mobile_not_null TO users_phone_not_null;
ALTER TABLE public.users DROP COLUMN address, DROP COLUMN birthday;
ALTER TABLE public.users ADD CONSTRAINT users_required_text
    CHECK (btrim(name) <> '' AND btrim(email) <> '' AND btrim(phone) <> '' AND btrim(password_hash) <> '');

ALTER TABLE public.user_favorites RENAME TO favorites;
ALTER TABLE public.favorites DROP COLUMN created_at;
ALTER TABLE public.favorites RENAME CONSTRAINT user_favorites_pkey TO favorites_pkey;
ALTER TABLE public.favorites RENAME CONSTRAINT user_favorites_user_id_fkey TO favorites_user_id_fkey;
ALTER TABLE public.favorites RENAME CONSTRAINT user_favorites_category_id_fkey TO favorites_category_id_fkey;
ALTER TABLE public.favorites RENAME CONSTRAINT user_favorites_user_id_not_null TO favorites_user_id_not_null;
ALTER TABLE public.favorites RENAME CONSTRAINT user_favorites_category_id_not_null TO favorites_category_id_not_null;
ALTER INDEX public.idx_user_favorites_category RENAME TO idx_favorites_category;

ALTER TABLE public.sections
    DROP COLUMN display_order,
    DROP COLUMN code;
