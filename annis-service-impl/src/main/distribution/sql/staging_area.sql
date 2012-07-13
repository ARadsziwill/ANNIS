-- cleanup in the proper order
DROP TABLE IF EXISTS _media_files;
DROP TABLE IF EXISTS _resolver_vis_map;
DROP TABLE IF EXISTS _corpus_stats;
DROP TABLE IF EXISTS _corpus_annotation;
DROP TABLE IF EXISTS _edge_annotation;
DROP TABLE IF EXISTS _rank;
DROP TABLE IF EXISTS _component;
DROP TABLE IF EXISTS _node_annotation;
DROP TABLE IF EXISTS _node;
DROP TABLE IF EXISTS _corpus;
DROP TABLE IF EXISTS _text;


-- corpora
-- each corpus has a unique name
-- corpora are ordered in a tree

CREATE :tmp TABLE _corpus
(
	id 			bigint NOT NULL,		-- primary key
	name		varchar(100) NOT NULL,		-- unique name
	type		varchar(100) NOT NULL,		-- CORPUS, DOCUMENT, SUBDOCUMENT (not used)
	version		varchar(100),				-- version number (not used)
	pre			bigint	NOT NULL,		-- pre and post order of the corpus tree
	post		bigint	NOT NULL
);

-- corpus annotations
-- unique combinantion of corpus_ref, namespace and name

CREATE :tmp TABLE _corpus_annotation
(
	corpus_ref	bigint NOT NULL,		-- foreign key to _corpus.id
	namespace	varchar(100),
	name		varchar(1000) NOT NULL,
	value		varchar(2000)
);

-- source texts
-- stores each source text in its entirety
CREATE :tmp TABLE _text
(
	id 		bigint NOT NULL,			-- primary key
	name	varchar(1000),					-- name (not used)
	text 	text							-- text contents (not used)
);

-- nodes in the annotation graph
-- nodes are named
-- are part of a corpus and reference a text
-- cover a span of the text
-- can be tokens (token_index NOT NULL, span NOT NULL)
CREATE :tmp TABLE _node
(
	id 				bigint	NOT NULL,	-- primary key
	text_ref 		bigint NOT NULL,	-- foreign key to _text.id
	corpus_ref		bigint NOT NULL,	-- foreign key to _corpus.id
	namespace		varchar(100),			-- namespace (not used)
	name 			varchar(100) NOT NULL,	-- name (not used)
	"left" 			integer NOT NULL,		-- start of covered substring in _text.text (inclusive)
	"right" 		integer NOT NULL,		-- end of covered substring in _text.text (inclusive)
	token_index		integer,				-- token number in _text.text, NULL if node is not a token
	seg_name    varchar(100),      -- segmentation name
	seg_left     integer,        -- most left segmentation index of covered token 
	seg_right    integer,        -- most right segmentation index of covered token
	continuous		boolean,				-- true if spanned text in _text.text is continuous (not used)
	span			varchar(2000)			-- for tokens: substring in _text.text (indexed for text search), else: NULL
);

-- connected components of the annotation graph
-- are of a type: Coverage, Dominance, Pointing relation or NULL for root nodes
-- have a name
CREATE :tmp TABLE _component
(
	id			bigint NOT NULL,		-- primary key
	type		char(1),					-- edge type: c, d, P, NULL
	namespace	varchar(255),				-- namespace (not used)
	name		varchar(255)
);

-- pre and post order of the annotation graph
-- root nodes: parent IS NULL
-- component and rank together model edges in the annotation graph
CREATE :tmp TABLE _rank
(
	pre				bigint	NOT NULL,	-- pre and post order of the annotation ODAG
	post			bigint	NOT NULL,
	node_ref		bigint	NOT NULL,	-- foreign key to _node.id
	component_ref	bigint NOT NULL,	-- foreign key to _component.id
	parent			bigint NULL		-- foreign key to _rank.pre, NULL for root nodes
);

-- node annotations
-- unique combinantion of node_ref, namespace and name
CREATE :tmp TABLE _node_annotation
(
	node_ref	bigint	NOT NULL,		-- foreign key to _node.id
	namespace	varchar(150),
	name		varchar(150) NOT NULL,
	value		varchar(1500)
);


-- edge annotations
-- unique combinantion of node_ref, namespace and name
CREATE :tmp TABLE _edge_annotation
(
	rank_ref		bigint	NOT NULL,	-- foreign key to _rank.pre
	namespace		varchar(1000),
	name			varchar(1000) NOT NULL,
	value			varchar(1000)
);

-- resolver visualization mappings
-- this table is just a subset of resolver_vis_map. It contains all columns needed for copying data from relANNIS format
CREATE :tmp TABLE _resolver_vis_map
(
  "corpus"   varchar(100), -- the name of the supercorpus
  "version" 	varchar(100), -- the version of the corpus
  "namespace"	varchar(100), -- the several layers of the corpus
  "element"    varchar(4), -- the type of the entry: node | edge
  "vis_type"   varchar(100) NOT NULL, -- the abstract type of visualization: tree, discourse, grid, ...
  "display_name"   varchar(100) NOT NULL, -- the name of the layer which shall be shown for display
  "order" bigint default '0', -- the order of the layers, in which they shall be shown
  "mappings" varchar(100)			    
);

CREATE :tmp TABLE _media_files
(
  file  bytea NOT NULL,
  corpus_ref  bigint NOT NULL,
  bytes bigint NOT NULL,
  mime_type character varying(40) NOT NULL,
  title character varying(40) NOT NULL  
);
