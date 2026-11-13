ALTER TABLE public.users DROP CONSTRAINT users_classroom_school_fkey;
ALTER TABLE public.users DROP COLUMN classroom_id;
DROP TABLE public.classrooms;
