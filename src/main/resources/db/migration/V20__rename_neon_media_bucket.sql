-- Copy and verify the media in open-hands-learning before deploying this migration.
ALTER TABLE public.categories DROP CONSTRAINT categories_image_url_valid;
ALTER TABLE public.categories ADD CONSTRAINT categories_image_url_valid CHECK (
    image_url ~ '^https://[a-z0-9][a-z0-9.-]+[.]s3[.][a-z0-9-]+[.]amazonaws[.]com/(images/[a-z0-9_]+|((dev|prod)/)?categories/([a-z0-9_]+/)+[a-z0-9_]+[.]webp)$'
    OR image_url ~ '^https://br-red-base-b9fi1yrl[.]storage[.]c-7[.]eu-central-1[.]aws[.]neon[.]tech/(openhandslearning-media|open-hands-learning)/((dev|prod)/)?categories/([a-z0-9_]+/)+[a-z0-9_]+[.]webp$');

ALTER TABLE public.categories DROP CONSTRAINT categories_video_url_valid;
ALTER TABLE public.categories ADD CONSTRAINT categories_video_url_valid CHECK (
    video_url IS NULL
    OR video_url ~ '^https://[a-z0-9][a-z0-9.-]+[.]s3[.][a-z0-9-]+[.]amazonaws[.]com/((dev|prod)/)?categories/([a-z0-9_]+/)+[a-z0-9_]+[.]mp4$'
    OR video_url ~ '^https://br-red-base-b9fi1yrl[.]storage[.]c-7[.]eu-central-1[.]aws[.]neon[.]tech/(openhandslearning-media|open-hands-learning)/((dev|prod)/)?categories/([a-z0-9_]+/)+[a-z0-9_]+[.]mp4$');

UPDATE public.categories
SET image_url = replace(image_url, 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/', 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/open-hands-learning/'),
    video_url = replace(video_url, 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/', 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/open-hands-learning/')
WHERE image_url LIKE 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/%'
   OR video_url LIKE 'https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/%';
