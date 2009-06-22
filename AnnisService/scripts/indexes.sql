-- Ursprungstabellen: Suche
CREATE INDEX idx_corpus__pre_post ON corpus USING btree (pre, post);
CREATE INDEX idx_s__corpus_ref__istoken ON node USING btree (corpus_ref) WHERE (token_index IS NOT NULL);
CREATE INDEX idx_struct__token_index ON node USING btree (token_index);
CREATE INDEX idx_rank_anno__value ON edge_annotation USING btree (value);
CREATE INDEX idx_annotation__ns_attribute_not_null ON node_annotation USING btree (namespace, name) WHERE (name IS NOT NULL);

-- Ursprungstabellen: FKs
CREATE INDEX fk_struct_2_text ON node USING btree (text_ref);
CREATE INDEX fk_rank_2_rank ON rank USING btree (parent);
CREATE INDEX fk_rank_2_struct ON rank USING btree (node_ref);
CREATE INDEX idx_rank__pre ON rank USING btree (pre);
CREATE INDEX fk_rank_anno_2_rank ON edge_annotation USING btree (rank_ref);
CREATE INDEX fk_annotation_2_struct ON node_annotation USING btree (node_ref);

-- Materialisierungen (Alt)
-- CREATE INDEX idx_s_a__corpus_ref_ns_attribute_not_null_value ON struct_annotation USING btree (corpus_ref, anno_namespace, anno_name, anno_value) WHERE (anno_name IS NOT NULL);
-- CREATE INDEX idx_s_a__text_ref_left_token_minus_1_right_token_value ON struct_annotation USING btree (text_ref, ((left_token - 1)), right_token, anno_value);
-- CREATE INDEX idx_s_a__id_text_ref_left ON struct_annotation USING btree (id, text_ref, "left");
-- CREATE INDEX idx_s_a__id_text_ref_left_token_right_token ON struct_annotation USING btree (id, text_ref, left_token, right_token);
-- CREATE INDEX idx_s_a__left_text_ref ON struct_annotation USING btree ("left", text_ref);
-- CREATE INDEX idx_s_a__text_ref_left_token_minus_1_right_token ON struct_annotation USING btree (text_ref, ((left_token - 1)), right_token);
-- CREATE INDEX idx_s_a__id_value_attribute ON struct_annotation USING btree (id, anno_value, anno_name);
-- CREATE INDEX idx_s_a__attribute_value_id ON struct_annotation USING btree (anno_name, anno_value, id);
-- CREATE INDEX idx_s_a__value_attribute ON struct_annotation USING btree (anno_value, anno_name);
-- CREATE INDEX idx_s_a__attribute ON struct_annotation USING btree (anno_name);
-- CREATE INDEX idx_s_a__span ON struct_annotation USING btree (span);
-- CREATE INDEX fk_struct_annotation_2_text ON struct_annotation USING btree (text_ref);
-- CREATE INDEX idx_s_a__id ON struct_annotation USING btree (id);
-- CREATE INDEX idx_r_a__pre_post ON rank_annotations USING btree (pre, post);
-- CREATE INDEX idx_r_t_r__level_text_ref_pre_post ON rank_text_ref USING btree (level, text_ref, pre, post);
-- CREATE INDEX idx_rank__parent_zshg ON edges (parent, zshg);
-- CREATE INDEX idx_rank__edge_type ON edges (edge_type);