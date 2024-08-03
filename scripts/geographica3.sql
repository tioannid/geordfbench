--
-- PostgreSQL database dump
--

-- Dumped from database version 14.10 (Ubuntu 14.10-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 14.10 (Ubuntu 14.10-0ubuntu0.22.04.1)

-- Started on 2023-12-18 20:00:57 EET

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

DROP DATABASE IF EXISTS geographica3;
--
-- TOC entry 3362 (class 1262 OID 16385)
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
    TEMPLATE template0
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

--
-- TOC entry 3 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO postgres;

--
-- TOC entry 3363 (class 0 OID 0)
-- Dependencies: 3
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'standard public schema';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 209 (class 1259 OID 16386)
-- Name: EXPERIMENT; Type: TABLE; Schema: public; Owner: geographica3
--

CREATE TABLE public."EXPERIMENT" (
    id integer NOT NULL,
    instime timestamp(3) with time zone DEFAULT ('now'::text)::timestamp(3) with time zone,
    exectime timestamp(3) with time zone,
    description character varying(80),
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
-- TOC entry 210 (class 1259 OID 16392)
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
-- TOC entry 3364 (class 0 OID 0)
-- Dependencies: 210
-- Name: EXPERIMENT_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: geographica3
--

ALTER SEQUENCE public."EXPERIMENT_id_seq" OWNED BY public."EXPERIMENT".id;


--
-- TOC entry 211 (class 1259 OID 16393)
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
-- TOC entry 212 (class 1259 OID 16396)
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
-- TOC entry 3365 (class 0 OID 0)
-- Dependencies: 212
-- Name: QUERYEXECUTION_experiment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: geographica3
--

ALTER SEQUENCE public."QUERYEXECUTION_experiment_id_seq" OWNED BY public."QUERYEXECUTION".experiment_id;


--
-- TOC entry 213 (class 1259 OID 16397)
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
-- TOC entry 3366 (class 0 OID 0)
-- Dependencies: 213
-- Name: QUERYEXECUTION_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: geographica3
--

ALTER SEQUENCE public."QUERYEXECUTION_id_seq" OWNED BY public."QUERYEXECUTION".id;


--
-- TOC entry 214 (class 1259 OID 16398)
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

--
-- TOC entry 216 (class 1259 OID 16408)
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

--
-- TOC entry 215 (class 1259 OID 16403)
-- Name: vqueryexecution2; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vqueryexecution2 AS
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
    round(((("QUERYEXECUTION".eval_time + "QUERYEXECUTION".scan_time))::numeric / 1000000000.0), 3) AS total_time_s,
        CASE
            WHEN (("QUERYEXECUTION".res_exception)::text <> 'NONE'::text) THEN 'Failed'::text
            ELSE 'Success'::text
        END AS validflag
   FROM public."QUERYEXECUTION",
    public."EXPERIMENT"
  WHERE ("QUERYEXECUTION".experiment_id = "EXPERIMENT".id);


ALTER TABLE public.vqueryexecution2 OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16412)
-- Name: vquery_ordered_aggrs2; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vquery_ordered_aggrs2 AS
 SELECT vqueryexecution2.experiment_id,
    vqueryexecution2.query_no,
    vqueryexecution2.cache_type,
    count(vqueryexecution2.iteration) AS no_iterations,
    round(avg(vqueryexecution2.total_time_s), 3) AS mean,
    percentile_disc((0.5)::double precision) WITHIN GROUP (ORDER BY vqueryexecution2.total_time_s) AS median
   FROM public.vqueryexecution2
  GROUP BY vqueryexecution2.validflag, vqueryexecution2.experiment_id, vqueryexecution2.query_no, vqueryexecution2.cache_type
 HAVING (vqueryexecution2.validflag = 'Success'::text);


ALTER TABLE public.vquery_ordered_aggrs2 OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 26633)
-- Name: vqueryexecution3; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vqueryexecution3 AS
 SELECT q.id,
    q.experiment_id,
    e.description,
    e.host,
    e.sut,
    e.queryset,
    e.dataset,
    e.type,
    q.query_label,
    q.query_no,
    q.cache_type,
    q.iteration,
    q.eval_time,
    q.scan_time,
    q.no_results,
    q.no_scan_errors,
    q.eval_flag,
    q.res_exception,
    (q.eval_time + q.scan_time) AS total_time,
    round((((q.eval_time + q.scan_time))::numeric / 1000000000.0), 3) AS total_time_s,
        CASE
            WHEN ((q.res_exception)::text <> 'NONE'::text) THEN 'Failed'::text
            ELSE 'Success'::text
        END AS validflag
   FROM public."QUERYEXECUTION" q,
    public."EXPERIMENT" e
  WHERE (q.experiment_id = e.id);


ALTER TABLE public.vqueryexecution3 OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 26639)
-- Name: vquery_ordered_aggrs_3; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vquery_ordered_aggrs_3 AS
 SELECT v.experiment_id,
    v.sut,
    v.queryset,
    v.dataset,
    v.query_label,
    v.query_no,
    v.validflag,
    v.cache_type,
    count(v.iteration) AS no_iterations,
    round(avg(v.total_time_s), 3) AS mean,
    percentile_disc((0.5)::double precision) WITHIN GROUP (ORDER BY v.total_time_s) AS median
   FROM public.vqueryexecution3 v
  GROUP BY v.experiment_id, v.sut, v.queryset, v.dataset, v.query_label, v.query_no, v.validflag, v.cache_type;


ALTER TABLE public.vquery_ordered_aggrs_3 OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 26648)
-- Name: vreport; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vreport AS
 SELECT v.cache_type,
    v.query_no,
    v.query_label,
    v.validflag,
    v.sut,
    v.mean,
    v.median
   FROM public.vquery_ordered_aggrs_3 v;


ALTER TABLE public.vreport OWNER TO postgres;

--
-- TOC entry 3204 (class 2604 OID 16431)
-- Name: EXPERIMENT id; Type: DEFAULT; Schema: public; Owner: geographica3
--

ALTER TABLE ONLY public."EXPERIMENT" ALTER COLUMN id SET DEFAULT nextval('public."EXPERIMENT_id_seq"'::regclass);


--
-- TOC entry 3206 (class 2604 OID 16433)
-- Name: QUERYEXECUTION id; Type: DEFAULT; Schema: public; Owner: geographica3
--

ALTER TABLE ONLY public."QUERYEXECUTION" ALTER COLUMN id SET DEFAULT nextval('public."QUERYEXECUTION_id_seq"'::regclass);


--
-- TOC entry 3205 (class 2604 OID 16432)
-- Name: QUERYEXECUTION experiment_id; Type: DEFAULT; Schema: public; Owner: geographica3
--

ALTER TABLE ONLY public."QUERYEXECUTION" ALTER COLUMN experiment_id SET DEFAULT nextval('public."QUERYEXECUTION_experiment_id_seq"'::regclass);


--
-- TOC entry 3208 (class 2606 OID 16435)
-- Name: EXPERIMENT EXPERIMENT_pkey; Type: CONSTRAINT; Schema: public; Owner: geographica3
--

ALTER TABLE ONLY public."EXPERIMENT"
    ADD CONSTRAINT "EXPERIMENT_pkey" PRIMARY KEY (id);


--
-- TOC entry 3209 (class 1259 OID 16436)
-- Name: FKI_EXPERIMENT_ID; Type: INDEX; Schema: public; Owner: geographica3
--

CREATE INDEX "FKI_EXPERIMENT_ID" ON public."QUERYEXECUTION" USING btree (experiment_id);


--
-- TOC entry 3210 (class 2606 OID 16437)
-- Name: QUERYEXECUTION FK_EXPERIMENT_ID; Type: FK CONSTRAINT; Schema: public; Owner: geographica3
--

ALTER TABLE ONLY public."QUERYEXECUTION"
    ADD CONSTRAINT "FK_EXPERIMENT_ID" FOREIGN KEY (experiment_id) REFERENCES public."EXPERIMENT"(id) ON DELETE CASCADE;


-- Completed on 2023-12-18 20:00:57 EET

--
-- PostgreSQL database dump complete
--

