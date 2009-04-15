-- **************************************************************************************************************
-- *****					SQL create script zur Erzeugung des relANNIS Schemas					*****
-- **************************************************************************************************************
-- *****	author:		Florian Zipser													*****
-- *****	version:		2.2															*****
-- *****	Datum:		08.04.2008													*****
-- **************************************************************************************************************

-- Dieses Script erzeugt alle f�r relANNIS n�tigen Relationen mit den dazugeh�rigen Indizes, Achtung, es werden alle Relationen vorher
-- gel�scht
-- Es ist notwendig f�r den PAULAImporter
-- �nderungen zu http://www2.informatik.hu-berlin.de/mac/macwiki/wiki/DddSqlSchema:
-- Die Relationen Span und Element sind zu Element zusammengefasst
-- Auf das Attribut Element.parent_id wird verzichtet
-- Die Relation bekommt einen zus�tzlichen Identifier id, damit ist pre nicht primary key, sonder id

-- Datenbank anlegen:
-- ==================
-- 1)	Einen neuen Benutzer mit Super-User Rechten anlegen.
--		Name:		relANNIS_user 
-- 		Passwort:		relANNIS
--
-- 2) 	Die Datenbank relANNIS anlegen, daf�r gibt es das Create-Script relANNIS.dat.
-- 	Die Datenbank kann in einer Shell mit POSTGRES_HOME\psql -U relANNIS_user -d relANNIS -f datei angelegt werden.
-- 	!!! ACHTUNG: Der Postgres-Server verlangt, dass die Datei, bzw. das Verzeichnis in dem diese liegt freigegeben wird
-- 	Unter Windows muss man sie im ganzen Netzwerk freigeben, damit der DB-Server darauf zugreifen kann

--
-- Name: relANNIS; Type: DATABASE; Schema: -; Owner: relANNIS_user
--
DROP DATABASE "relANNIS";

--CREATE USER "relANNIS_user" WITH PASSWORD 'relANNIS';
CREATE DATABASE "relANNIS" ENCODING = 'UTF8';
ALTER DATABASE "relANNIS" OWNER TO "relANNIS_user";
\connect "relANNIS"

-- ************************************ l�schen der Relationen ************************************
DROP TABLE meta_attribute;
DROP TABLE anno_attribute;
DROP TABLE anno;
DROP TABLE "rank_anno";
DROP TABLE "rank";
DROP TABLE doc_2_korp;
DROP TABLE struct;
DROP TABLE text;
DROP TABLE korpus;
DROP TABLE col_rank;
DROP TABLE collection;
-- ************************************ Ende l�schen der Relationen ************************************

-- ************************************ Erzeugen der Relationen ************************************
-- ===================================== Relation: Collection =====================================
CREATE TABLE collection
(
	id		numeric(38) NOT NULL,
	"type"	character varying(100) NOT NULL,
	name	character varying(100) NOT NULL,
	--pre		numeric(38)	NOT NULL,
	--post	numeric(38)	NOT NULL,
	CONSTRAINT "PK_collection" PRIMARY KEY (id)
);
COMMENT ON TABLE collection IS 'Relation collection is a collection of tuple from other relations like struct, annotation, text. With this relation you can realize a union of other tuples for example from wich files do they come. One collection tuple can contain other collection tuples, so we have sub- ans supercollections (see korpus).';

-- ===================================== Relation: Col_rank =====================================
CREATE TABLE col_rank
(
	col_ref	numeric(38) NOT NULL,
	pre		numeric(38)	NOT NULL,
	post	numeric(38)	NOT NULL,
	CONSTRAINT "FK_col_rank_2_collection" FOREIGN KEY (col_ref) REFERENCES collection(id),
	CONSTRAINT "KEY_col_rank_pre" UNIQUE(pre),
	CONSTRAINT "KEY_col_rank_post" UNIQUE(post)
);
COMMENT ON TABLE col_rank IS 'Relation col_rank contains pre- and post-values for tuples in relation collection. It builds up a DAG-structure over collection-tuples.';

-- ===================================== Relation: Korpus =====================================
CREATE TABLE korpus
(
	id 			numeric(38) NOT NULL,
	name		character varying(100) NOT NULL,
	pre			numeric(38)	NOT NULL,			-- Idee: pre k�nnte sein: id + neuer preWert, das f�hrt dazu, dass kein artifizielles Wurzelelement eingef�hrt werden muss
	post		numeric(38)	NOT NULL,
	CONSTRAINT "PK_korpus" PRIMARY KEY (id),
	CONSTRAINT "KEY_korpus_pre" UNIQUE(pre),
	CONSTRAINT "KEY_korpus_post" UNIQUE(post)
);
COMMENT ON TABLE korpus IS 'Relation korpus is for the metastrucure and to organize korpus and subkorpus..';

-- ===================================== Relation: document =====================================
 CREATE TABLE doc_2_korp
(
	doc_id				numeric(38)	NOT NULL,
	korpus_ref			numeric(38)	NOT NULL,
	CONSTRAINT "PK_doc" PRIMARY KEY (doc_id, korpus_ref),
	CONSTRAINT "FK_doc_2_korpus" FOREIGN KEY (korpus_ref) REFERENCES korpus(id)
) ;
COMMENT ON TABLE doc_2_korp IS 'The Relation doc_2_korp relates the root-tuples of struct and korpus with each other';

-- ===================================== Relation: text =====================================
CREATE TABLE text
(
	id 				numeric(38) NOT NULL,
	name			character varying(150)	NOT NULL,
	"text" 			text,
	col_ref			numeric(38),		-- reference to a collection tuple
	CONSTRAINT "PK_text" PRIMARY KEY (id),
	CONSTRAINT "FK_text_2_collection" FOREIGN KEY (col_ref) REFERENCES collection(id)
);
COMMENT ON TABLE text IS 'Relation text stores the primary text wich has to be tated.';
-- ===================================== Relation: struct =====================================
 CREATE TABLE struct
(
	id 			numeric(38)	NOT NULL,
	text_ref 	numeric(38),
	col_ref		numeric(38),		-- reference to a collection tuple
	doc_ref		numeric(38),											-- dient der Identifikation von Strukturelementen, die sich auf einen Text/ Dokument(nicht spezifiziert) beziehen
	name 		character varying(100) NOT NULL,		-- ALL, ROOT, STRUCT, STRUCTEDGE oder TOKEN(bisher bekannt)
	"type"		character varying(100) NOT NULL,		-- genauere Bezeichnung aus PAULA heraus, bspw. primmarkSeg
	"left" 		integer,
	"right" 	integer,
	"order"		numeric(38),											-- speichert die Reihenfolge von Tupeln mit name= TOKEN, sonst NULL
	cont 		boolean,
	"text"		text,
	CONSTRAINT "PK_struct" PRIMARY KEY (id),
	CONSTRAINT "FK_struct_2_text" FOREIGN KEY (text_ref) REFERENCES text(id),
	CONSTRAINT "FK_struct_2_collection" FOREIGN KEY (col_ref) REFERENCES collection(id)
	--CONSTRAINT "FK_struct_2_doc" FOREIGN KEY (doc_ref) REFERENCES doc_2_korp(doc_id) doc_id ist nicht eindeutig, daher kann es kein FK geben
) ;
COMMENT ON TABLE struct IS 'Relation struct stores a atructure wich is build over primary data. This structure is used to annotate it.';
CREATE INDEX IDX_STRUCT_ORDER on struct("order");
-- ===================================== Relation: rank =====================================
CREATE TABLE rank
(
	pre					numeric(38)	NOT NULL,
	post				numeric(38)	NOT NULL,
	struct_ref			numeric(38)	NOT NULL,
	parent				numeric(38),
	dominance			boolean		NOT NULL,
	CONSTRAINT "PK_rank" PRIMARY KEY (pre),
	CONSTRAINT "FK_rank_2_struct" FOREIGN KEY (struct_ref) REFERENCES struct(id),
	CONSTRAINT "FK_rank_2_rank" FOREIGN KEY (parent) REFERENCES rank(pre),	
	CONSTRAINT "KEY_rank_pre" UNIQUE(pre),
	CONSTRAINT "KEY_rank_post" UNIQUE(post)
);
COMMENT ON TABLE rank IS 'The Relation rank builds the structure uf struct. It relates the tuple of struct with eaxh other. The flag dominance means, this edge is an dominance-edge or a non-dominance-edge.';

-- ===================================== Relation: rank_anno =====================================
CREATE TABLE rank_anno
(
	rank_ref		numeric(38)	NOT NULL,
	"type"			character varying(100),
	"name"			character varying(100),
	"value"			character varying(100),
	CONSTRAINT "FK_rank_anno_2_rank" FOREIGN KEY (rank_ref) REFERENCES rank(pre)
);
COMMENT ON TABLE rank IS 'The Relation rank_anno is an edge annotation for tuples in relation rank.';
-- ===================================== Relation: anno =====================================
CREATE TABLE anno
(
	id 				numeric(38)	NOT NULL,
	struct_ref		numeric(38)	NOT NULL,
	col_ref			numeric(38),		-- reference to a collection tuple
	anno_level	character varying(150),		-- Typensatz (urml, exmeralda, tiger)
	CONSTRAINT "PK_anno" PRIMARY KEY (id),
	CONSTRAINT "FK_anno_2_struct" FOREIGN KEY (struct_ref) REFERENCES struct(id),
	CONSTRAINT "FK_anno_2_collection" FOREIGN KEY (col_ref) REFERENCES collection(id)
) ;
COMMENT ON TABLE anno IS 'Relation anno is a placeholder for real annotations and is build over the relation struct.';

-- ===================================== Relation: anno_attribute =====================================
 CREATE TABLE anno_attribute
(
	anno_ref	numeric(38)						NOT NULL,
	name		character varying(150)		NOT NULL,
	"value"	character varying(150),
	CONSTRAINT "PK_anno_attribute" PRIMARY KEY (anno_ref, name),
	CONSTRAINT "FK_anno_attribute_2_anno" FOREIGN KEY (anno_ref) REFERENCES anno(id)
) ;
COMMENT ON TABLE anno_attribute IS 'Relation anno_attribute stores the real annotation over the anno tuples.';

-- ===================================== Relation: anno_attribute =====================================
CREATE TABLE meta_attribute
(
	col_ref		numeric(38)					NOT NULL,
	name		character varying(150)		NOT NULL,
	"value"		character varying(150),
	--CONSTRAINT "PK_meta_attribute" PRIMARY KEY (col_ref, name),
	CONSTRAINT "FK_meta_attribute_2_collection" FOREIGN KEY (col_ref) REFERENCES collection(id)
) ;
COMMENT ON TABLE anno_attribute IS 'The relation meta_attribute stores meta attributes to objects of relation collection';

-- ************************************ Ende Erzeugen der Relationen ************************************