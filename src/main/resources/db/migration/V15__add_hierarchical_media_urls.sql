-- Object upload and catalog URL replacement are explicit maintenance operations.
-- Keep V14 immutable: it has already been applied to development.
ALTER TABLE public.categories DROP CONSTRAINT categories_image_url_valid;
ALTER TABLE public.categories ADD COLUMN video_url varchar(2048);
-- Legacy URLs remain valid during the upload/rewrite transition.
ALTER TABLE public.categories ADD CONSTRAINT categories_image_url_valid CHECK (
    image_url ~ '^https://[a-z0-9][a-z0-9.-]+[.]s3[.][a-z0-9-]+[.]amazonaws[.]com/(images/[a-z0-9_]+|(dev|prod)/categories/([a-z0-9_]+/)+[a-z0-9_]+[.]webp)$');
ALTER TABLE public.categories ADD CONSTRAINT categories_video_url_valid CHECK (
    video_url IS NULL OR video_url ~ '^https://[a-z0-9][a-z0-9.-]+[.]s3[.][a-z0-9-]+[.]amazonaws[.]com/(dev|prod)/categories/([a-z0-9_]+/)+[a-z0-9_]+[.]mp4$');

-- An auditable inventory includes voice images and section artwork, which have no category row.
CREATE TABLE public.media_assets (
    object_key varchar(1024) PRIMARY KEY,
    public_url varchar(2048) NOT NULL UNIQUE,
    content_type varchar(32) NOT NULL CHECK (content_type IN ('image/webp', 'video/mp4')),
    byte_size bigint NOT NULL CHECK (byte_size > 0),
    sha256 varchar(64) NOT NULL CHECK (sha256 ~ '^[a-f0-9]{64}$'),
    CHECK (object_key ~ '^(dev|prod)/(voice/[a-z0-9_]+[.]webp|categories/([a-z0-9_]+/)+[a-z0-9_]+[.](webp|mp4))$'),
    CHECK (public_url = 'https://open-hands-learning.s3.us-east-1.amazonaws.com/' || object_key)
);
