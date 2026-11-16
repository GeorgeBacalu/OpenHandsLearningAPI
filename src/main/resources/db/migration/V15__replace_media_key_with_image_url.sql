ALTER TABLE public.categories DROP CONSTRAINT categories_media_key_valid;
ALTER TABLE public.categories RENAME COLUMN media_key TO image_url;
ALTER TABLE public.categories ALTER COLUMN image_url TYPE varchar(2048);

UPDATE public.categories
SET image_url = 'https://open-hands-learning.s3.us-east-1.amazonaws.com/images/' || image_url;

ALTER TABLE public.categories ADD CONSTRAINT categories_image_url_valid
    CHECK (image_url ~ '^https://[a-z0-9][a-z0-9.-]+[.]s3[.][a-z0-9-]+[.]amazonaws[.]com/images/[a-z0-9_]+$');
