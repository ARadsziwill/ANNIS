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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


import org.springframework.dao.DataAccessException;

import annis.model.AnnisNode;
import annis.ql.parser.QueryData;


public class CountSqlGenerator extends BaseSqlGenerator<Integer>
	implements SelectClauseSqlGenerator<QueryData>, FromClauseSqlGenerator<QueryData> {

	@SuppressWarnings("rawtypes")
	private SqlGenerator findSqlGenerator;
	
	@Override
	public String selectClause(QueryData queryData, List<AnnisNode> alternative, String indent) {
		return "\n" + indent + TABSTOP + "count(*)";
	}

	@Override
	public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
		int sum = 0;
		while (rs.next())
			sum += rs.getInt(1);
		return sum;	
	}

	@Override
	public String fromClause(QueryData queryData, List<AnnisNode> alternative, String indent) {
		StringBuffer sb = new StringBuffer();
		
		indent(sb, indent);
		sb.append("(\n");
		indent(sb, indent);
		int indentBy = indent.length() / 2 + 2;
		sb.append(findSqlGenerator.toSql(queryData, indentBy));
		indent(sb, indent + TABSTOP);
		sb.append(") AS solutions");
		
		return sb.toString();
	}

	public SqlGenerator getFindSqlGenerator() {
		return findSqlGenerator;
	}

	public void setFindSqlGenerator(@SuppressWarnings("rawtypes") SqlGenerator findSqlGenerator) {
		this.findSqlGenerator = findSqlGenerator;
	}

}
