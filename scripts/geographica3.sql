--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.25
-- Dumped by pg_dump version 14.2 (Ubuntu 14.2-1.pgdg18.04+1)

-- Started on 2022-05-10 13:12:24 EEST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2165 (class 1262 OID 449069)
-- Name: geographica3; Type: DATABASE; Schema: -; Owner: geographica3
--

-- Create geographica3 login
--
CREATE ROLE geographica3 LOGIN
  ENCRYPTED PASSWORD 'md5026b364a2c8491da2cca1b5772723d43'
  NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;

ALTER USER geographica3 WITH PASSWORD 'geographica3';

CREATE DATABASE geographica3
    WITH 
    OWNER = geographica3
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;


ALTER DATABASE geographica3 OWNER TO geographica3;

\connect geographica3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

--
-- TOC entry 181 (class 1259 OID 449070)
-- Name: EXPERIMENT; Type: TABLE; Schema: public; Owner: geographica3
--

CREATE TABLE public."EXPERIMENT" (
    id integer NOT NULL,
    instime timestamp(3) with time zone DEFAULT ('now'::text)::timestamp(3) with time zone,
    exectime timestamp(3) with time zone,
    description character varying(50),
    host character varying(200),
    os character varying(100),
    sut character varying(200),
    queryset character varying(200),
    dataset character varying(150),
    executionspec character varying(200),
    reportspec character varying(150),
    type character varying(50)
);


ALTER TABLE public."EXPERIMENT" OWNER TO geographica3;

--
-- TOC entry 182 (class 1259 OID 449077)
-- Name: EXPERIMENT_id_seq; Type: SEQUENCE; Schema: public; Owner: geographica3
--

CREATE SEQUENCE public."EXPERIMENT_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."EXPERIMENT_id_seq" OWNER TO geographica3;

--
-- TOC entry 2167 (class 0 OID 0)
-- Dependencies: 182
-- Name: EXPERIMENT_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: geographica3
--

ALTER SEQUENCE public."EXPERIMENT_id_seq" OWNED BY public."EXPERIMENT".id;


--
-- TOC entry 183 (class 1259 OID 449079)
-- Name: QUERYEXECUTION; Type: TABLE; Schema: public; Owner: geographica3
--

CREATE TABLE public."QUERYEXECUTION" (
    id integer NOT NULL,
    experiment_id integer NOT NULL,
    query_no integer NOT NULL,
    query_label character varying(50),
    cache_type character varying(15),
    iteration smallint,
    eval_time bigint,
    scan_time bigint,
    no_results bigint,
    no_scan_errors bigint,
    eval_flag character varying(35),
    res_exception character varying(35)
);


ALTER TABLE public."QUERYEXECUTION" OWNER TO geographica3;

--
-- TOC entry 184 (class 1259 OID 449082)
-- Name: QUERYEXECUTION_experiment_id_seq; Type: SEQUENCE; Schema: public; Owner: geographica3
--

CREATE SEQUENCE public."QUERYEXECUTION_experiment_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."QUERYEXECUTION_experiment_id_seq" OWNER TO geographica3;

--
-- TOC entry 2168 (class 0 OID 0)
-- Dependencies: 184
-- Name: QUERYEXECUTION_experiment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: geographica3
--

ALTER SEQUENCE public."QUERYEXECUTION_experiment_id_seq" OWNED BY public."QUERYEXECUTION".experiment_id;


--
-- TOC entry 185 (class 1259 OID 449084)
-- Name: QUERYEXECUTION_id_seq; Type: SEQUENCE; Schema: public; Owner: geographica3
--

CREATE SEQUENCE public."QUERYEXECUTION_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."QUERYEXECUTION_id_seq" OWNER TO geographica3;

--
-- TOC entry 2169 (class 0 OID 0)
-- Dependencies: 185
-- Name: QUERYEXECUTION_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: geographica3
--

ALTER SEQUENCE public."QUERYEXECUTION_id_seq" OWNED BY public."QUERYEXECUTION".id;


--
-- TOC entry 186 (class 1259 OID 449141)
-- Name: vqueryexecution; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vqueryexecution AS
 SELECT "QUERYEXECUTION".id,
    "QUERYEXECUTION".experiment_id,
    "EXPERIMENT".type,
    "QUERYEXECUTION".query_label,
    "QUERYEXECUTION".query_no,
    "QUERYEXECUTION".cache_type,
    "QUERYEXECUTION".iteration,
    "QUERYEXECUTION".eval_time,
    "QUERYEXECUTION".scan_time,
    "QUERYEXECUTION".no_results,
    "QUERYEXECUTION".no_scan_errors,
    "QUERYEXECUTION".eval_flag,
    "QUERYEXECUTION".res_exception,
    ("QUERYEXECUTION".eval_time + "QUERYEXECUTION".scan_time) AS total_time,
    round(((("QUERYEXECUTION".eval_time + "QUERYEXECUTION".scan_time))::numeric / 1000000000.0), 3) AS total_time_s
   FROM public."QUERYEXECUTION",
    public."EXPERIMENT"
  WHERE ("QUERYEXECUTION".experiment_id = "EXPERIMENT".id);


ALTER TABLE public.vqueryexecution OWNER TO postgres;

-- View: public.vqueryexecution2

-- DROP VIEW public.vqueryexecution2;

CREATE OR REPLACE VIEW public.vqueryexecution2 AS 
 SELECT "QUERYEXECUTION".id,
    "QUERYEXECUTION".experiment_id,
    "EXPERIMENT".type,
    "QUERYEXECUTION".query_label,
    "QUERYEXECUTION".query_no,
    "QUERYEXECUTION".cache_type,
    "QUERYEXECUTION".iteration,
    "QUERYEXECUTION".eval_time,
    "QUERYEXECUTION".scan_time,
    "QUERYEXECUTION".no_results,
    "QUERYEXECUTION".no_scan_errors,
    "QUERYEXECUTION".eval_flag,
    "QUERYEXECUTION".res_exception,
    "QUERYEXECUTION".eval_time + "QUERYEXECUTION".scan_time AS total_time,
    round(("QUERYEXECUTION".eval_time + "QUERYEXECUTION".scan_time)::numeric / 1000000000.0, 3) AS total_time_s,
        CASE
            WHEN "QUERYEXECUTION".res_exception <> 'NONE'::text THEN 'Failed'::text
            ELSE 'Success'::text
        END AS validflag
   FROM public."QUERYEXECUTION",
    public."EXPERIMENT"
  WHERE "QUERYEXECUTION".experiment_id = "EXPERIMENT".id;

ALTER TABLE public.vqueryexecution2
  OWNER TO postgres;

--
-- TOC entry 187 (class 1259 OID 449154)
-- Name: vquery_ordered_aggrs; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vquery_ordered_aggrs AS
 SELECT vqueryexecution.experiment_id,
    vqueryexecution.query_no,
    vqueryexecution.cache_type,
    count(vqueryexecution.iteration) AS no_iterations,
    round(avg(vqueryexecution.total_time_s), 3) AS mean,
    percentile_disc((0.5)::double precision) WITHIN GROUP (ORDER BY vqueryexecution.total_time_s) AS median
   FROM public.vqueryexecution
  GROUP BY vqueryexecution.experiment_id, vqueryexecution.query_no, vqueryexecution.cache_type;


ALTER TABLE public.vquery_ordered_aggrs OWNER TO postgres;

-- View: public.vquery_ordered_aggrs2

-- DROP VIEW public.vquery_ordered_aggrs2;

CREATE OR REPLACE VIEW public.vquery_ordered_aggrs2 AS 
 SELECT vqueryexecution2.experiment_id,
    vqueryexecution2.query_no,
    vqueryexecution2.cache_type,
    count(vqueryexecution2.iteration) AS no_iterations,
    round(avg(vqueryexecution2.total_time_s), 3) AS mean,
    percentile_disc(0.5::double precision) WITHIN GROUP (ORDER BY vqueryexecution2.total_time_s) AS median
   FROM public.vqueryexecution2
  GROUP BY vqueryexecution2.validflag, vqueryexecution2.experiment_id, vqueryexecution2.query_no, vqueryexecution2.cache_type
 HAVING vqueryexecution2.validflag = 'Success'::text;

ALTER TABLE public.vquery_ordered_aggrs2
  OWNER TO postgres;

--
-- TOC entry 2037 (class 2604 OID 449094)
-- Name: EXPERIMENT id; Type: DEFAULT; Schema: public; Owner: geographica3
--

ALTER TABLE ONLY public."EXPERIMENT" ALTER COLUMN id SET DEFAULT nextval('public."EXPERIMENT_id_seq"'::regclass);


--
-- TOC entry 2038 (class 2604 OID 449095)
-- Name: QUERYEXECUTION experiment_id; Type: DEFAULT; Schema: public; Owner: geographica3
--

ALTER TABLE ONLY public."QUERYEXECUTION" ALTER COLUMN experiment_id SET DEFAULT nextval('public."QUERYEXECUTION_experiment_id_seq"'::regclass);


--
-- TOC entry 2039 (class 2604 OID 449096)
-- Name: QUERYEXECUTION id; Type: DEFAULT; Schema: public; Owner: geographica3
--

ALTER TABLE ONLY public."QUERYEXECUTION" ALTER COLUMN id SET DEFAULT nextval('public."QUERYEXECUTION_id_seq"'::regclass);


--
-- TOC entry 2041 (class 2606 OID 449098)
-- Name: EXPERIMENT EXPERIMENT_pkey; Type: CONSTRAINT; Schema: public; Owner: geographica3
--

ALTER TABLE ONLY public."EXPERIMENT"
    ADD CONSTRAINT "EXPERIMENT_pkey" PRIMARY KEY (id);


--
-- TOC entry 2042 (class 1259 OID 449099)
-- Name: FKI_EXPERIMENT_ID; Type: INDEX; Schema: public; Owner: geographica3
--

CREATE INDEX "FKI_EXPERIMENT_ID" ON public."QUERYEXECUTION" USING btree (experiment_id);


--
-- TOC entry 2043 (class 2606 OID 449100)
-- Name: QUERYEXECUTION FK_EXPERIMENT_ID; Type: FK CONSTRAINT; Schema: public; Owner: geographica3
--

ALTER TABLE ONLY public."QUERYEXECUTION"
    ADD CONSTRAINT "FK_EXPERIMENT_ID" FOREIGN KEY (experiment_id) REFERENCES public."EXPERIMENT"(id) ON DELETE CASCADE;


--
-- TOC entry 2166 (class 0 OID 0)
-- Dependencies: 7
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2022-05-10 13:12:24 EEST

--
-- PostgreSQL database dump complete
--
