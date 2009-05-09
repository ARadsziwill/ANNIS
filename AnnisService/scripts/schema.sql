-- (modified) source tables
CREATE TABLE corpus
(
	id			numeric(38) PRIMARY KEY,
	name		varchar(100) NOT NULL, -- UNIQUE,
	type		varchar(100) NOT NULL,
	version 	varchar(100),
	pre			numeric(38) NOT NULL UNIQUE,
	post		numeric(38) NOT NULL UNIQUE,
	top_level	boolean NOT NULL	-- true for roots of the corpus forest
);

CREATE TABLE corpus_annotation
(
	corpus_ref	numeric(38) NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
	namespace	varchar(100),
	name		varchar(1000) NOT NULL,
	value		varchar(2000),
	UNIQUE (corpus_ref, namespace, name)
);

CREATE TABLE text
(
	id		numeric(38) PRIMARY KEY,
	name	varchar(1000),
	text	text
);

CREATE TABLE node
(
	id			numeric(38)	PRIMARY KEY,
	text_ref	numeric(38) NOT NULL REFERENCES text (id) ON DELETE CASCADE,
	corpus_ref	numeric(38) NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
	namespace	varchar(100),
	name		varchar(100) NOT NULL,
	"left"		integer NOT NULL,
	"right"		integer NOT NULL,
	token_index	integer,
	cont		boolean,
	span		varchar(2000),
	left_token	integer NULL,	-- token_index of left-most token in tree under this node
	right_token	integer	NULL	-- token_index of right-most token in tree under this node
);

CREATE TABLE component
(
	id			numeric(38) PRIMARY KEY,
	type		char(1),
	namespace	varchar(255),
	name		varchar(255)
);

CREATE TABLE rank
(
	pre				numeric(38)	PRIMARY KEY,
	post			numeric(38)	NOT NULL UNIQUE,
	node_ref		numeric(38)	NOT NULL REFERENCES node (id) ON DELETE CASCADE,
	component_ref	numeric(38) NOT NULL REFERENCES component (id) ON DELETE CASCADE,
	parent			numeric(38) NULL REFERENCES rank (pre) ON DELETE CASCADE,
	root			boolean,
	level			numeric(38) NOT NULL	-- depth of the node in the annotation graph
);

CREATE TABLE node_annotation
(
	node_ref	numeric(38) REFERENCES node (id) ON DELETE CASCADE,
	namespace	varchar(150),
	name		varchar(150) NOT NULL,
	value		varchar(1500),
	UNIQUE (node_ref, namespace, name)
);

CREATE TABLE edge_annotation
(
	rank_ref	numeric(38)	REFERENCES rank (pre) ON DELETE CASCADE,
	namespace	varchar(150),
	name		varchar(150) NOT NULL,
	value		varchar(1500),
	UNIQUE (rank_ref, namespace, name)
);


-- external data
CREATE TABLE extData
(
	id			serial PRIMARY KEY,
	filename	varchar(500) NOT NULL,
	orig_name	varchar(100) NOT NULL,
	branch		varchar(100) NOT NULL,
	mime		varchar(100) NOT NULL,
	comment		varchar(1500) NOT NULL,
	UNIQUE (filename, branch) 
);


-- stats
CREATE TABLE corpus_stats
(
	name				varchar,
	id					numeric NOT NULL REFERENCES corpus ON DELETE CASCADE,
	corpus				numeric,
	text				numeric,
	node				numeric,
	rank				numeric,
	component			numeric,
	corpus_annotation	numeric,
	node_annotation		numeric,
	edge_annotation		numeric,
	tokens				numeric,
	roots				numeric,
	depth				numeric,
	c					numeric,
	d					numeric,
	p					numeric,
	u					numeric,
	avg_level			real,
	avg_children		real,
	avg_duplicates		real
);

CREATE VIEW corpus_info AS SELECT 
	name,
	id, 
	tokens,
	roots,
	depth,
	to_char(avg_level, '990.99') as avg_level,
	to_char(avg_children, '990.99') as avg_children,
	to_char(avg_duplicates, '990.99') as avg_duplicates
FROM 
	corpus_stats;
	
CREATE VIEW table_stats AS select
	(select count(*) from corpus ) as corpus,	
	(select count(*) from corpus_annotation) as corpus_annotation,
	(select count(*) from text ) as text,
	(select count(*) from node ) as node,
	(select count(*) from node_annotation ) as node_annotation,
	(select count(*) from rank ) as rank,	
	(select count(*) from component) as component,
	(select count(*) from edge_annotation ) as edge_annotation,	
--	(select count(*) from corp_2_viz) as corp_2_viz,
--	(select count(*) from xcorp_2_viz) as xcorp_2_viz,
	(select count(*) from extdata) as extdata
;

--- Resolver
CREATE TABLE corp_2_viz
(
	"corpus_name"	character varying(100) NOT NULL,
	"level"			character varying(100) NOT NULL,
	"type_ref"		character varying(100) NOT NULL,
	"corpus_ref"	numeric(38) NOT NULL
);
ALTER TABLE corp_2_viz ADD CONSTRAINT "FK_corp_2_viz_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);

CREATE TABLE xcorp_2_viz
(
	"corpus_name"	character varying(100) NOT NULL,
	"level"			character varying(100) NOT NULL,
	"annotation"	character varying(100) NOT NULL,
	"type_ref"		character varying(100) NOT NULL,
	"corpus_ref"	numeric(38) NOT NULL
);
ALTER TABLE xcorp_2_viz ADD CONSTRAINT "FK_xcorp_2_viz_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);

CREATE TABLE viz_type
(
	id		numeric(38) NOT NULL,
	"type"	character varying(100) NOT NULL
);
ALTER TABLE viz_type ADD CONSTRAINT "PK_viz_type" PRIMARY KEY (id);
ALTER TABLE viz_type ADD  CONSTRAINT "UNIQUE_type" UNIQUE("type");

CREATE TABLE viz_errors
(
	"corpus_id"		character varying(100) NOT NULL,
	"corpus_name"	character varying(100) NOT NULL,
	"anno_level"	character varying(100) NOT NULL
);
COMMENT ON TABLE viz_errors IS 'Relation viz_errors contains errors of visualization computing';
