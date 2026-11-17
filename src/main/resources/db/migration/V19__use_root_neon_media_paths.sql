-- Neon objects must be verified at the bucket root before deploying this migration.
ALTER TABLE public.categories DROP CONSTRAINT categories_image_url_valid;
ALTER TABLE public.categories ADD CONSTRAINT categories_image_url_valid CHECK (
    image_url ~ '^https://[a-z0-9][a-z0-9.-]+[.]s3[.][a-z0-9-]+[.]amazonaws[.]com/(images/[a-z0-9_]+|((dev|prod)/)?categories/([a-z0-9_]+/)+[a-z0-9_]+[.]webp)$'
    OR image_url ~ '^https://br-red-base-b9fi1yrl[.]storage[.]c-7[.]eu-central-1[.]aws[.]neon[.]tech/openhandslearning-media/((dev|prod)/)?categories/([a-z0-9_]+/)+[a-z0-9_]+[.]webp$');

ALTER TABLE public.categories DROP CONSTRAINT categories_video_url_valid;
ALTER TABLE public.categories ADD CONSTRAINT categories_video_url_valid CHECK (
    video_url IS NULL
    OR video_url ~ '^https://[a-z0-9][a-z0-9.-]+[.]s3[.][a-z0-9-]+[.]amazonaws[.]com/((dev|prod)/)?categories/([a-z0-9_]+/)+[a-z0-9_]+[.]mp4$'
    OR video_url ~ '^https://br-red-base-b9fi1yrl[.]storage[.]c-7[.]eu-central-1[.]aws[.]neon[.]tech/openhandslearning-media/((dev|prod)/)?categories/([a-z0-9_]+/)+[a-z0-9_]+[.]mp4$');

UPDATE public.categories
SET image_url = regexp_replace(image_url,
        '^https://br-red-base-b9fi1yrl[.]storage[.]c-7[.]eu-central-1[.]aws[.]neon[.]tech/openhandslearning-media/prod/categories/',
        'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/categories/'),
    video_url = regexp_replace(video_url,
        '^https://br-red-base-b9fi1yrl[.]storage[.]c-7[.]eu-central-1[.]aws[.]neon[.]tech/openhandslearning-media/prod/categories/',
        'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/categories/')
WHERE image_url LIKE 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/prod/categories/%'
   OR video_url LIKE 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/prod/categories/%';
