/*
 * Copyright 2012 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class GraphSqlGenerator<T> extends AbstractSqlGenerator<T>
  implements FromClauseSqlGenerator<QueryData>,
  SelectClauseSqlGenerator<QueryData>, OrderByClauseSqlGenerator<QueryData>
{

  @Override
  public String toSql(QueryData queryData, String indent)
  {
    StringBuffer sb = new StringBuffer();
    sb.append(indent);
    sb.append(createSqlForAlternative(queryData, null, indent));
    appendOrderByClause(sb, queryData, null, indent);
    appendLimitOffsetClause(sb, queryData, null, indent);
    return sb.toString();
  }

  @Override
  public String fromClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("\n").append(TABSTOP);
    sb.append("node_ids, matching_nodes \n");
    sb.append(
      "LEFT OUTER JOIN annotation_pool as anno_node ON(matching_nodes.node_anno_ref = anno_node.id)\n");
    sb.append(
      "LEFT OUTER JOIN annotation_pool as anno_edge ON(matching_nodes.edge_anno_ref = anno_edge.id)\n");

    return sb.toString();
  }

  @Override
  public String selectClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuilder sb = new StringBuilder();
    List<SaltURIs> listOfSaltURIs = queryData.getExtensions(SaltURIs.class);
    // only work with the first element
    Validate.isTrue(!listOfSaltURIs.isEmpty());
    SaltURIs saltURIs = listOfSaltURIs.get(0);

    sb.append("ARRAY[");
    for (int i = 1; i <= saltURIs.size(); i++)
    {
      sb.append("node_ids.id").append(i);

      if (i < saltURIs.size())
      {
        sb.append(", ");
      }
    }

    sb.append("] AS key,\n").append(TABSTOP);
    sb.append("0 AS matchstart,\n").append(TABSTOP);
    sb.append("1 AS n,\n").append(TABSTOP);

    sb.append("matching_nodes.id, ");
    sb.append("matching_nodes.text_ref, ");
    sb.append("matching_nodes.corpus_ref, ");
    sb.append("matching_nodes.toplevel_corpus, ");
    sb.append("matching_nodes.namespace, ");
    sb.append("matching_nodes.name, ");
    sb.append("matching_nodes.left, ");
    sb.append("matching_nodes.right, ");
    sb.append("matching_nodes.token_index");

    return sb.toString();
  }

  @Override
  public String orderByClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    return "ORDER BY token_index, matching_nodes.pre\n";
  }

  @Override
  public T extractData(ResultSet rs) throws SQLException, DataAccessException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  private String generateSolutionKey(QueryData queryData)
  {
    List<SaltURIs> listOfSaltURIs = queryData.getExtensions(SaltURIs.class);
    StringBuilder sb = new StringBuilder();

    // only work with the first element
    Validate.isTrue(!listOfSaltURIs.isEmpty());
    SaltURIs saltURIs = listOfSaltURIs.get(0);

    sb.append(" ARRAY[");
    for (int i = 0; i < saltURIs.size(); i++)
    {
      sb.append("matching_nodes.id").append(i + 1);
      if (i < saltURIs.size() - 1)
      {
        sb.append(", ");
      }
    }

    sb.append("] AS key,\n");

    return sb.toString();
  }
}