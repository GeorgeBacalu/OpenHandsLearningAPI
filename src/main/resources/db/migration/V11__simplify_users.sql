ALTER TABLE public.users DROP CONSTRAINT users_required_text;
ALTER TABLE public.users RENAME COLUMN mobile TO phone;
ALTER TABLE public.users RENAME CONSTRAINT users_mobile_not_null TO users_phone_not_null;
ALTER TABLE public.users DROP COLUMN address, DROP COLUMN birthday;
ALTER TABLE public.users ADD CONSTRAINT users_required_text
    CHECK (btrim(name) <> '' AND btrim(email) <> '' AND btrim(phone) <> '' AND btrim(password) <> '');
