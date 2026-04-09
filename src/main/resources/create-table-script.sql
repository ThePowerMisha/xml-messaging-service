CREATE TABLE public.messages
(
    id bigserial NOT NULL,
    "time" timestamp without time zone NOT NULL,
    username text NOT NULL,
    text text NOT NULL,
    result integer NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.messages
    OWNER to postgres;