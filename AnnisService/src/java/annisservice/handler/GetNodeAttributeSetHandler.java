package annisservice.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import annisservice.ifaces.AnnisAttribute;
import annisservice.ifaces.AnnisAttributeSet;
import annisservice.objects.AnnisAttributeImpl;
import annisservice.objects.AnnisAttributeSetImpl;

public class GetNodeAttributeSetHandler extends AnnisServiceHandler<AnnisAttributeSet> {

	private Logger log = Logger.getLogger(this.getClass());
	
	public GetNodeAttributeSetHandler() {
		super("GET NODE ATTRIBUTES");
	}
	
	public final String CORPUS_LIST = "corpusList";
	
	@SuppressWarnings("unchecked")
	@Override
	protected AnnisAttributeSet getResult(Map<String, Object> args) {
		List<Long> corpusList = (List<Long>) args.get(CORPUS_LIST);
		
//		// FIXME: als PreparedStatement ändern
//		StringBuffer sql2 = new StringBuffer();
//		sql2.append("SELECT DISTINCT ns, attribute ");
//		sql2.append("FROM struct_annotations ");
//		sql2.append("WHERE attribute IS NOT NULL ");
//		if ( ! corpusList.isEmpty() ) {
//			sql2.append("AND doc_ref IN ( SELECT DISTINCT doc_id FROM doc_2_corp d, corpus c1, corpus c2 WHERE d.corpus_ref = c1.id AND c1.pre >= c2.pre AND c1.post <= c2.post AND c2.id IN ( ");
//			for (long corpus : corpusList) {
//				sql2.append(corpus);
//				sql2.append(", ");
//			}
//			sql2.setLength(sql2.length() - ", ".length());
//			sql2.append(") )");
//		}
//		sql2.append("ORDER BY ns, attribute");

		ParameterizedRowMapper<AnnisAttribute> rowMapper = new ParameterizedRowMapper<AnnisAttribute>() {

			public AnnisAttribute mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				AnnisAttribute annisAttribute = new AnnisAttributeImpl();
				String ns = rs.getString("ns");
				String name = rs.getString("attribute");
				if (ns != null)
					name = ns + ":" + name;
				annisAttribute.setName(name);
				
				log.debug("found an attribute: " + name);
				
				return annisAttribute;
			}
			
		};
		
		List<AnnisAttribute> result;
		if (corpusList.isEmpty()) {
			String sql = "SELECT DISTINCT ns, attribute FROM annotation WHERE attribute IS NOT NULL ORDER BY ns, attribute";
			result = getSimpleJdbcTemplate().query(sql, rowMapper);
		} else {
			String sql = "SELECT DISTINCT ns, attribute FROM struct_annotation WHERE attribute IS NOT NULL AND doc_ref IN ( :doc_ref ) ORDER BY ns, attribute";
			SqlParameterSource sqlParameterSource = new MapSqlParameterSource().addValue("doc_ref", corpusList);
			result = getSimpleJdbcTemplate().query(sql, rowMapper, sqlParameterSource);
		}

		AnnisAttributeSet annisAttributeSet = new AnnisAttributeSetImpl();
		annisAttributeSet.addAll(result);

		log.info("Found " + annisAttributeSet.size() + " node attributes.");

		return annisAttributeSet;
	}

}
