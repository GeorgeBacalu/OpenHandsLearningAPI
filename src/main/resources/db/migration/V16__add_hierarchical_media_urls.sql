-- Object upload and catalog URL replacement are explicit maintenance operations.
-- Keep V15 immutable: it has already been applied to development.
ALTER TABLE public.categories DROP CONSTRAINT categories_image_url_valid;
ALTER TABLE public.categories ADD COLUMN video_url varchar(2048);
-- Legacy URLs remain valid during the upload/rewrite transition.
ALTER TABLE public.categories ADD CONSTRAINT categories_image_url_valid CHECK (
    image_url ~ '^https://[a-z0-9][a-z0-9.-]+[.]s3[.][a-z0-9-]+[.]amazonaws[.]com/(images/[a-z0-9_]+|(dev|prod)/categories/([a-z0-9_]+/)+[a-z0-9_]+[.]webp)$');
ALTER TABLE public.categories ADD CONSTRAINT categories_video_url_valid CHECK (
    video_url IS NULL OR video_url ~ '^https://[a-z0-9][a-z0-9.-]+[.]s3[.][a-z0-9-]+[.]amazonaws[.]com/(dev|prod)/categories/([a-z0-9_]+/)+[a-z0-9_]+[.]mp4$');
