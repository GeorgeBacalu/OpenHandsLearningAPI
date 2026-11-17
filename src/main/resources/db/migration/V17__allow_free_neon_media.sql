-- Keep existing S3 catalogs valid while allowing this app's approved free bucket.
ALTER TABLE public.categories DROP CONSTRAINT categories_image_url_valid;
ALTER TABLE public.categories ADD CONSTRAINT categories_image_url_valid CHECK (
    image_url ~ '^https://[a-z0-9][a-z0-9.-]+[.]s3[.][a-z0-9-]+[.]amazonaws[.]com/(images/[a-z0-9_]+|(dev|prod)/categories/([a-z0-9_]+/)+[a-z0-9_]+[.]webp)$'
    OR image_url ~ '^https://br-red-base-b9fi1yrl[.]storage[.]c-7[.]eu-central-1[.]aws[.]neon[.]tech/openhandslearning-media/(dev|prod)/categories/([a-z0-9_]+/)+[a-z0-9_]+[.]webp$');

ALTER TABLE public.categories DROP CONSTRAINT categories_video_url_valid;
ALTER TABLE public.categories ADD CONSTRAINT categories_video_url_valid CHECK (
    video_url IS NULL
    OR video_url ~ '^https://[a-z0-9][a-z0-9.-]+[.]s3[.][a-z0-9-]+[.]amazonaws[.]com/(dev|prod)/categories/([a-z0-9_]+/)+[a-z0-9_]+[.]mp4$'
    OR video_url ~ '^https://br-red-base-b9fi1yrl[.]storage[.]c-7[.]eu-central-1[.]aws[.]neon[.]tech/openhandslearning-media/(dev|prod)/categories/([a-z0-9_]+/)+[a-z0-9_]+[.]mp4$');
