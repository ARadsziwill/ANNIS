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

import annis.model.AnnisNode;
import annis.model.AnnisNode.TextMatching;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;

public class BaseNodeSqlGenerator
{

  // pluggable access to tables representing the node
  private TableAccessStrategyFactory tableAccessStrategyFactory;

  ///// Helpers
  protected TableAccessStrategy tables(AnnisNode node)
  {
    return tableAccessStrategyFactory.tables(node);
  }

  protected String tableAliasDefinition(AnnisNode node, String table, int count)
  {
    StringBuffer sb = new StringBuffer();

    sb.append(tables(node).tableName(table));
    sb.append(" AS ");
    sb.append(tables(node).aliasedTable(table, count));

    return sb.toString();
  }

  protected String join(String op, String lhs, String rhs)
  {
    return lhs + " " + op + " " + rhs;
  }

  protected String numberJoin(String op, String lhs, String rhs,
    int offset)
  {
    String plus = offset >= 0 ? " + " : " - ";
    return join(op, lhs, rhs) + plus + String.valueOf(Math.abs(offset));
  }

  protected String bitSelect(String column, boolean[] bits)
  {
    StringBuilder sbBits = new StringBuilder();
    for(int i=0; i < bits.length; i++)
    {
      sbBits.append(bits[i] ? "1" : "0");
    }
    return "(" + column + " & B'" + sbBits.toString() + "') "
      + "= B'" + sbBits.toString() + "'";
  }

  protected String between(String lhs, String rhs, int min, int max)
  {
    String minPlus = min >= 0 ? " + " : " - ";
    String maxPlus = max >= 0 ? " + " : " - ";
    return lhs + " BETWEEN SYMMETRIC " + rhs + minPlus + String.valueOf(Math.abs(min)) + " AND " + rhs + maxPlus + String.valueOf(Math.abs(max));
  }

  protected String between(String lhs, int min, int max)
  {
    return lhs + " BETWEEN SYMMETRIC " + min + " AND " + max;
  }

  protected String in(String lhs, String rhs)
  {
    return lhs + " IN (" + rhs + ")";
  }

  protected String in(String lhs, Collection values)
  {
    if (values.isEmpty())
    {
      return in(lhs, "NULL");
    }
    else
    {
      return in(lhs, StringUtils.join(values, ","));
    }
  }

  protected String sqlString(String string)
  {
    return SQLHelper.sqlString(string);
  }

  protected String sqlString(String string, TextMatching textMatching)
  {
    return SQLHelper.sqlString(string, textMatching);
  }

  ///// Getter / Setter
  public TableAccessStrategyFactory getTableAccessStrategyFactory()
  {
    return tableAccessStrategyFactory;
  }

  public void setTableAccessStrategyFactory(TableAccessStrategyFactory tableAccessStrategyFactory)
  {
    this.tableAccessStrategyFactory = tableAccessStrategyFactory;
  }
}
