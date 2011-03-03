CREATE TEMPORARY TABLE _max (
    corpus_id numeric(38) NULL,
    corpus_post numeric(38) NULL,
    rank_post numeric(38) NULL,
    component_id numeric(38) NULL,
    node_id numeric(38) NULL,
    text_id numeric(38) NULL
);

INSERT INTO _max VALUES (
    (SELECT max(id) + 1 FROM corpus),
    (SELECT max(post) + 1 FROM corpus),
    (SELECT max(post) + 1 FROM facts),
    (SELECT max(component_id) + 1 FROM facts),
--    (SELECT max(id) + 1 FROM node),
    (SELECT max(id) + 1 FROM facts),
    (SELECT max(id) + 1 FROM text)
);

UPDATE _max
SET
corpus_id = (CASE WHEN corpus_id IS NULL THEN 0 ELSE corpus_id END),
corpus_post = (CASE WHEN corpus_post IS NULL THEN 0 ELSE corpus_post END),
rank_post = (CASE WHEN rank_post IS NULL THEN 0 ELSE rank_post END),
component_id = (CASE WHEN component_id IS NULL THEN 0 ELSE component_id END),
node_id = (CASE WHEN node_id IS NULL THEN 0 ELSE node_id END),
text_id = (CASE WHEN text_id IS NULL THEN 0 ELSE text_id END)
;

UPDATE _node_annotation SET node_ref = node_ref + (SELECT node_id FROM _max);
    
UPDATE _rank
SET 
 pre = pre + (SELECT rank_post FROM _max),
 post = post + (SELECT rank_post FROM _max),
 node_ref = node_ref + (SELECT node_id FROM _max),
 parent = parent + (SELECT rank_post FROM _max),
 component_ref = component_ref + (SELECT component_id FROM _max);

UPDATE _component SET id = id + (SELECT component_id FROM _max);

UPDATE _edge_annotation SET rank_ref = rank_ref + (SELECT rank_post FROM _max);
    
UPDATE _node
SET 
 id = id + (SELECT node_id FROM _max),
 text_ref = text_ref + (SELECT text_id FROM _max),
 corpus_ref = corpus_ref + (SELECT corpus_id FROM _max),
 toplevel_corpus = toplevel_corpus + (SELECT corpus_id FROM _max);
    
UPDATE _text SET id = id + (SELECT text_id FROM _max);
    
UPDATE _corpus
SET 
 id = id + (SELECT corpus_id FROM _max),
 pre = pre + (SELECT corpus_post FROM _max),
 post = post + (SELECT corpus_post FROM _max);

UPDATE _corpus_annotation SET corpus_ref = corpus_ref + (SELECT corpus_id FROM _max);

DROP TABLE _max;
