ALTER TABLE public.user_favorites RENAME TO favorites;
ALTER TABLE public.favorites DROP COLUMN created_at;
ALTER TABLE public.favorites RENAME CONSTRAINT user_favorites_pkey TO favorites_pkey;
ALTER TABLE public.favorites RENAME CONSTRAINT user_favorites_user_id_fkey TO favorites_user_id_fkey;
ALTER TABLE public.favorites RENAME CONSTRAINT user_favorites_category_id_fkey TO favorites_category_id_fkey;
ALTER TABLE public.favorites RENAME CONSTRAINT user_favorites_user_id_not_null TO favorites_user_id_not_null;
ALTER TABLE public.favorites RENAME CONSTRAINT user_favorites_category_id_not_null TO favorites_category_id_not_null;
ALTER INDEX public.idx_user_favorites_category RENAME TO idx_favorites_category;
