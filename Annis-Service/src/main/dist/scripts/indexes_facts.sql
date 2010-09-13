---------------------
-- node_annotation --
---------------------
CREATE INDEX idx__node_annotation__value_:id ON node_annotation_:id(name,value,namespace);
CREATE INDEX idx__node_annotation__namespace_:id ON node_annotation_:id(name,namespace);

-----------
--  node --
-----------
CREATE INDEX idx__node_token_index_:id ON node_:id (token_index);
CREATE INDEX idx__node_is_token_:id ON node_:id (is_token);
CREATE INDEX idx__node_span_:id ON node_:id (span,toplevel_corpus);

----------
--facts --
----------
-- Suche kombiniert mit parent
CREATE INDEX idx_c__parent__node_:id ON facts_:id (parent);
CREATE INDEX idx_c__parent__token_:id ON facts_:id (parent) WHERE token_index IS NULL;
CREATE INDEX idx_c__parent__span_:id ON facts_:id (span varchar_pattern_ops, parent);
CREATE INDEX idx_c__parent__node_anno_ex_:id ON facts_:id (node_annotation_name, parent);
CREATE INDEX idx_c__parent__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, parent);
CREATE INDEX idx_c__parent__edge_anno_ex_:id ON facts_:id (edge_annotation_name, parent);
CREATE INDEX idx_c__parent__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, parent);

-- Suche kombiniert mit pre WHERE type = d
CREATE INDEX idx_c__dom__node_:id ON facts_:id (pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__token_:id ON facts_:id (pre) WHERE token_index IS NULL AND edge_type = 'd';
CREATE INDEX idx_c__dom__span_:id ON facts_:id (span varchar_pattern_ops, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__node_anno_ex_:id ON facts_:id (node_annotation_name, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__edge_anno_ex_:id ON facts_:id (edge_annotation_name, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, pre) WHERE edge_type = 'd';

-- Suche kombiniert mit edge_name, pre WHERE type = p
CREATE INDEX idx_c__pr__node_:id ON facts_:id (edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__token_:id ON facts_:id (edge_name, pre) WHERE token_index IS NULL AND edge_type = 'p';
CREATE INDEX idx_c__pr__span_:id ON facts_:id (span varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__node_anno_ex_:id ON facts_:id (node_annotation_name, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__edge_anno_ex_:id ON facts_:id (edge_annotation_name, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';

----- Prezedenz
-- Suche kombiniert mit text_ref
CREATE INDEX idx_c__text__node_:id ON facts_:id (text_ref);
CREATE INDEX idx_c__text__token_:id ON facts_:id (text_ref) WHERE token_index IS NULL;
CREATE INDEX idx_c__text__span_:id ON facts_:id (span varchar_pattern_ops, text_ref);
CREATE INDEX idx_c__text__node_anno_ex_:id ON facts_:id (node_annotation_name, text_ref);
CREATE INDEX idx_c__text__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, text_ref);
CREATE INDEX idx_c__text__edge_anno_ex_:id ON facts_:id (edge_annotation_name, text_ref);
CREATE INDEX idx_c__text__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, text_ref);

----- _=_
CREATE INDEX idx__exact_cover_:id ON facts_:id (text_ref, "left", "right");

-- search for token
CREATE INDEX idx__token_search_:id on facts_:id (is_token, toplevel_corpus);

----- 2nd query
CREATE INDEX idx__2nd_query_:id ON facts_:id (text_ref, left_token, right_token);

-- optimize the select distinct
CREATE INDEX idx_distinct_helper_:id ON facts_:id(id, text_ref, left_token, right_token);
CREATE INDEX idx__column__id_:id on facts_:id using hash (id);
CREATE INDEX idx__column__text_ref_:id on facts_:id using hash (text_ref);
CREATE INDEX idx__column__left_token_:id on facts_:id using hash (left_token);
CREATE INDEX idx__column__right_token_:id on facts_:id using hash (right_token);