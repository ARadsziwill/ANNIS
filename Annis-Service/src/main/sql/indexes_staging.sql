BEGIN; -- transaction

CREATE INDEX tmpidx_pk_corpus ON _corpus USING HASH  (id);
CREATE INDEX tmpidx_pk_node ON _node USING HASH (id);
CREATE INDEX tmpidx_pk_node_token ON _node(id) WHERE token_index is not null;
CREATE INDEX tmpidx_fk_node_annotation ON _node_annotation USING HASH (node_ref);
CREATE INDEX tmpidx_pk_component ON _component USING HASH (id);
CREATE INDEX tmpidx_fk1_rank ON _rank USING HASH (node_ref);
CREATE INDEX tmpidx_fk2_rank ON _rank USING HASH (component_ref);
CREATE INDEX tmpidx_fk2_rank_pre ON _rank (pre);
CREATE INDEX tmpidx_fk2_component_type ON _component ("type");
CREATE INDEX tmpidx_fk_edge_annotation ON _edge_annotation USING HASH (rank_ref);

END; -- transaction