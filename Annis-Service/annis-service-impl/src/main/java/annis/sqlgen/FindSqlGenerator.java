/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;

import annis.dao.Match;
import annis.dao.Span;
import annis.model.AnnisNode;
import annis.ql.parser.QueryData;


public class FindSqlGenerator extends UnionBaseSqlGenerator<List<Match>> implements SelectClauseSqlGenerator {

	@Override
	public String selectClause(QueryData queryData, List<AnnisNode> alternative, String indent) {
		List<String> ids = new ArrayList<String>();
		int i = 0;
		for (AnnisNode node : alternative) {
			++i;
			ids.add(tables(node).aliasedColumn(NODE_TABLE, "id") + 
					" AS id" + i);
		}
		return "DISTINCT\n" + indent + TABSTOP + StringUtils.join(ids, ", ");
	}

	@Override
	public List<Match> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<Match> matches = new ArrayList<Match>();
		int rowNum = 0;
		while (rs.next())
			matches.add(mapRow(rs, ++rowNum));
		return matches;			
	}
	
	public Match mapRow(ResultSet rs, int rowNum) throws SQLException {
		Match match = new Match();

		// get size of solution
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		
		// one match per column
		for (int column = 1; column <= columnCount; ++column) {
			int id = rs.getInt((column));

			// no more matches in this row if an id was NULL
			if (rs.wasNull())
				break;

			match.add(new Span(id));
		}
		
		return match;
	}

}
