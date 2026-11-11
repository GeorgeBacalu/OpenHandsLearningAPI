UPDATE public.categories
SET media_key = lower(regexp_replace(translate(btrim(name),
    U&'\0103\00e2\00ee\0219\015f\021b\0163\0102\00c2\00ce\0218\015e\021a\0162',
    'aaissttAAISSTT'), '[^a-zA-Z0-9_]+', '_', 'g'))
WHERE media_key = lower(regexp_replace(btrim(name), '[^a-zA-Z0-9_]+', '_', 'g'));

UPDATE public.categories SET media_key = 'dactilema_a' WHERE media_key = 'a';

DELETE FROM public.letter_sign_pairs
WHERE id = '5f3b697f-312e-391b-8680-281023bf67a1'
    AND letter = U&'\0102' AND image_id = 207;

ALTER TABLE public.letter_sign_pairs ADD COLUMN media_key varchar(255);
UPDATE public.letter_sign_pairs SET media_key = CASE WHEN letter = ' ' THEN 'space'
    ELSE 'dactilema_' || lower(translate(letter,
        U&'\0103\00e2\00ee\0219\015f\021b\0163\0102\00c2\00ce\0218\015e\021a\0162',
        'aaissttAAISSTT')) END;

ALTER TABLE public.letter_sign_pairs
    ALTER COLUMN media_key SET NOT NULL,
    ADD CONSTRAINT letters_letter_unique UNIQUE (letter),
    ADD CONSTRAINT letters_media_key_valid CHECK (media_key ~ '^[a-z0-9_]+$'),
    DROP COLUMN image_id;

ALTER TABLE public.categories
    ALTER COLUMN media_key SET NOT NULL,
    ADD CONSTRAINT categories_media_key_valid CHECK (media_key ~ '^[a-z0-9_]+$'),
    DROP COLUMN image_id;

CREATE UNIQUE INDEX categories_sibling_name_unique
    ON public.categories (section_id, parent_id, lower(btrim(name))) NULLS NOT DISTINCT;

ALTER TABLE public.sections
    ALTER COLUMN media_key SET NOT NULL,
    ALTER COLUMN icon_key SET NOT NULL,
    ADD CONSTRAINT sections_media_key_valid CHECK (media_key ~ '^[a-z0-9_]+$'),
    ADD CONSTRAINT sections_icon_key_valid CHECK (icon_key ~ '^[a-z0-9_]+$'),
    DROP COLUMN image_id, DROP COLUMN icon_id;

ALTER TABLE public.feedbacks
    ALTER COLUMN description TYPE text,
    ALTER COLUMN sent_at TYPE timestamp with time zone USING sent_at AT TIME ZONE 'Europe/Bucharest';

ALTER TABLE public.notifications
    ALTER COLUMN message TYPE text,
    ALTER COLUMN sent_at TYPE timestamp with time zone USING sent_at AT TIME ZONE 'Europe/Bucharest';
