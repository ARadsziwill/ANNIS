package de.deutschdiachrondigital.dddquery.sql.old2;

import java.util.List;

import annis.dao.Match;
import annis.dao.Span;
import de.deutschdiachrondigital.dddquery.sql.old2.AnnotationRetriever.SqlGenerator;

public class AnnotationRetrieverSqlGenerator7 implements SqlGenerator {

	public String generateSql(List<Match> matches, int left, int right) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("SET enable_mergejoin TO off;\n");
		sb.append("SELECT DISTINCT\n"); 
		sb.append("	tokens.key, everything.pre, everything.post, everything.struct, everything.name, everything.text_ref, everything.\"left\", everything.\"right\", everything.token_left, everything.token_right, everything.span, everything.ns, everything.attribute, everything.value\n");
		sb.append("FROM\n");            
		
		sb.append(tokenRelation(matches, left, right));
		sb.append("\n");
		
		sb.append("	JOIN rank_struct ON (tokens.text_ref = rank_struct.text_ref AND tokens.min <= rank_struct.token_count AND tokens.max >= rank_struct.token_count)\n");
		sb.append("	JOIN rank ON (rank.parent IS NULL AND rank.pre <= rank_struct.pre AND rank.post >= rank_struct.post)\n");
		sb.append("	JOIN everything ON (everything.pre >= rank.pre AND everything.pre <= rank_struct.pre AND everything.post <= rank.post AND everything.post >= rank_struct.post)\n");
		sb.append("ORDER BY tokens.key, everything.pre");
		
		return sb.toString();
	}

	String tokenRelation(List<Match> matches, int left, int right) {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("\t(\n");
		
		for (List<Span> match : matches) {

			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			int textRef = match.get(0).getTextRef();
			
			for (Span node : match) {
				min = Math.min(min, node.getTokenLeft());
				max = Math.max(max, node.getTokenRight());
			}
			
			sb.append("\t\tSELECT '{");
	
			for (Span node : match) {
				sb.append(node.getStructId());
				sb.append(", ");
			}
			sb.setLength(sb.length() - ", ".length());
			sb.append("}'::numeric[] AS key, ");
			
			sb.append(textRef);
			sb.append(" AS text_ref, ");
			
			sb.append(min - left);
			sb.append(" AS min, ");
			
			sb.append(max + right);
			sb.append(" AS max UNION\n");
		}
		sb.setLength(sb.length() - " UNION\n".length());
		sb.append("\n");

		sb.append("\t) AS tokens");

		return sb.toString();
	}

}
