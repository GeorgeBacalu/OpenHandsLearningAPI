ALTER TABLE public.users ADD COLUMN role varchar(5);

UPDATE public.users AS u
SET role = r.authority
FROM public.roles AS r
WHERE u.role_id = r.id;

ALTER TABLE public.users
    ALTER COLUMN role SET NOT NULL,
    ADD CONSTRAINT users_role_valid CHECK (role IN ('USER', 'ADMIN'));

ALTER TABLE public.users DROP COLUMN role_id;
DROP TABLE public.roles;
