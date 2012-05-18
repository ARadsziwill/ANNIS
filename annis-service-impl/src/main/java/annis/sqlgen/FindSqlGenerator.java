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
import static annis.sqlgen.TableAccessStrategy.CORPUS_TABLE;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.dao.DataAccessException;

import annis.dao.Match;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;

public class FindSqlGenerator extends AbstractUnionSqlGenerator<List<Match>>
  implements SelectClauseSqlGenerator<QueryData>
{

  // optimize DISTINCT operation in SELECT clause
  private boolean optimizeDistinct;

  @Override
  public String selectClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    int maxWidth = queryData.getMaxWidth();
    Validate.isTrue(alternative.size() <= maxWidth,
      "BUG: nodes.size() > maxWidth");

    boolean isDistinct = false || !optimizeDistinct;
    List<String> ids = new ArrayList<String>();
    int i = 0;

    for (QueryNode node : alternative)
    {
      ++i;

      TableAccessStrategy tblAccessStr = tables(node);
      ids.add(tblAccessStr.aliasedColumn(NODE_TABLE, "id") + " AS id" + i);
      ids.add(tblAccessStr.aliasedColumn(NODE_TABLE, "node_name")
        + " AS node_name" + i);
      ids.add(tblAccessStr.aliasedColumn(CORPUS_TABLE, "path_name") + " AS path_name" + i);

      if (tblAccessStr.usesRankTable())
      {
        isDistinct = true;
      }
    }

    for (i = alternative.size(); i < maxWidth; ++i)
    {
      ids.add("NULL");
    }

    ids.add(tables(alternative.get(0)).aliasedColumn(NODE_TABLE,
      "toplevel_corpus"));

    return (isDistinct ? "DISTINCT" : "") + "\n" + indent + TABSTOP
      + StringUtils.join(ids, ", ");
  }

  @Override
  public List<Match> extractData(ResultSet rs) throws SQLException,
    DataAccessException
  {
    List<Match> matches = new ArrayList<Match>();
    int rowNum = 0;
    while (rs.next())
    {
      matches.add(mapRow(rs, ++rowNum));
    }
    return matches;
  }

  public Match mapRow(ResultSet rs, int rowNum) throws SQLException
  {
    Match match = new Match();

    // get size of solution
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();

    // one match per column
    for (int column = 1; column <= columnCount; ++column)
    {

      if (metaData.getColumnName(column).startsWith("id"))
      {
        match.add(rs.getLong((column)));
      }
      else if (metaData.getColumnName(column).startsWith("toplevel_corpus"))
      {
        match.setToplevelCorpusId(rs.getLong((column)));
      }
      else if (metaData.getColumnName(column).startsWith("node_name"))
      {
        match.setSaltId(rs.getString(column));
      }
      else if (metaData.getColumnName(column).startsWith("path_name"))
      {
        match.setSaltId(rs.getString(column));
      }

      // no more matches in this row if an id was NULL
      if (rs.wasNull())
      {
        break;
      }

    }

    return match;
  }

  public boolean isOptimizeDistinct()
  {
    return optimizeDistinct;
  }

  public void setOptimizeDistinct(boolean optimizeDistinct)
  {
    this.optimizeDistinct = optimizeDistinct;
  }
}
