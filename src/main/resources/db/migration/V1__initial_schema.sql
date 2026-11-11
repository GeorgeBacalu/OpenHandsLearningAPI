CREATE TABLE public.categories (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    image_id integer,
    is_expanded boolean DEFAULT false NOT NULL,
    is_favorite boolean DEFAULT false NOT NULL,
    name character varying(255),
    parent_id uuid,
    section_id uuid,
    version bigint DEFAULT 0 NOT NULL,
    display_order integer DEFAULT 2147483647 NOT NULL
);

CREATE TABLE public.feedbacks (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    description character varying(255),
    sent_at timestamp(6) without time zone,
    type character varying(255),
    user_id uuid,
    version bigint DEFAULT 0 NOT NULL
);

CREATE TABLE public.institutions (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    classroom character varying(255),
    school character varying(255),
    version bigint DEFAULT 0 NOT NULL
);

CREATE TABLE public.letter_sign_pairs (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    image_id integer,
    letter character varying(255),
    version bigint DEFAULT 0 NOT NULL,
    display_order integer DEFAULT 2147483647 NOT NULL
);

CREATE TABLE public.notifications (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    is_read boolean DEFAULT false NOT NULL,
    message character varying(255),
    sent_at timestamp(6) without time zone,
    receiver_id uuid,
    sender_id uuid,
    version bigint DEFAULT 0 NOT NULL
);

CREATE TABLE public.roles (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    authority character varying(255),
    version bigint DEFAULT 0 NOT NULL
);

CREATE TABLE public.sections (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    icon_id integer,
    image_id integer,
    name character varying(255),
    version bigint DEFAULT 0 NOT NULL,
    display_order integer DEFAULT 2147483647 NOT NULL
);

CREATE TABLE public.users (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    address character varying(255),
    birthday date,
    email character varying(255),
    mobile character varying(255),
    name character varying(255),
    password character varying(255),
    institution_id uuid,
    role_id uuid,
    version bigint DEFAULT 0 NOT NULL
);

ALTER TABLE public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);

ALTER TABLE public.feedbacks
    ADD CONSTRAINT feedbacks_pkey PRIMARY KEY (id);

ALTER TABLE public.institutions
    ADD CONSTRAINT institutions_pkey PRIMARY KEY (id);

ALTER TABLE public.letter_sign_pairs
    ADD CONSTRAINT letter_sign_pairs_pkey PRIMARY KEY (id);

ALTER TABLE public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);

ALTER TABLE public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);

ALTER TABLE public.sections
    ADD CONSTRAINT sections_pkey PRIMARY KEY (id);

ALTER TABLE public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE public.categories
    ADD CONSTRAINT categories_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.categories(id);

ALTER TABLE public.categories
    ADD CONSTRAINT categories_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id);

ALTER TABLE public.feedbacks
    ADD CONSTRAINT feedbacks_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE public.notifications
    ADD CONSTRAINT notifications_receiver_id_fkey FOREIGN KEY (receiver_id) REFERENCES public.users(id);

ALTER TABLE public.notifications
    ADD CONSTRAINT notifications_sender_id_fkey FOREIGN KEY (sender_id) REFERENCES public.users(id);

ALTER TABLE public.users
    ADD CONSTRAINT users_institution_id_fkey FOREIGN KEY (institution_id) REFERENCES public.institutions(id);

ALTER TABLE public.users
    ADD CONSTRAINT users_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id);
