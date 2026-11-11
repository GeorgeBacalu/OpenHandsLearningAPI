CREATE TABLE public.counties (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE CHECK (btrim(name) <> '')
);
CREATE TABLE public.cities (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    name varchar(255) NOT NULL CHECK (btrim(name) <> ''),
    county_id uuid NOT NULL REFERENCES public.counties(id)
);
CREATE INDEX idx_cities_county_name ON public.cities (county_id, name, id);
ALTER TABLE public.institutions RENAME TO schools;
ALTER TABLE public.schools RENAME COLUMN school TO name;
DROP INDEX public.institutions_school_unique;
ALTER TABLE public.schools ADD COLUMN city_id uuid REFERENCES public.cities(id);
ALTER TABLE public.schools ADD CONSTRAINT schools_city_required CHECK (city_id IS NOT NULL) NOT VALID;
CREATE INDEX idx_schools_city_name ON public.schools (city_id, name, id);
ALTER TABLE public.classrooms RENAME COLUMN institution_id TO school_id;
ALTER TABLE public.users RENAME COLUMN institution_id TO school_id;
ALTER TABLE public.users RENAME CONSTRAINT users_classroom_institution_fkey TO users_classroom_school_fkey;
ALTER TABLE public.users ALTER CONSTRAINT users_classroom_school_fkey DEFERRABLE INITIALLY IMMEDIATE;
ALTER TABLE public.classrooms RENAME CONSTRAINT classrooms_institution_key TO classrooms_school_key;
ALTER INDEX public.idx_users_institution RENAME TO idx_users_school;
ALTER TABLE public.schools RENAME CONSTRAINT institutions_pkey TO schools_pkey;
ALTER TABLE public.schools RENAME CONSTRAINT institutions_school_not_blank TO schools_name_not_blank;
ALTER TABLE public.users RENAME CONSTRAINT users_institution_id_fkey TO users_school_id_fkey;
ALTER TABLE public.classrooms RENAME CONSTRAINT classrooms_institution_id_fkey TO classrooms_school_id_fkey;
