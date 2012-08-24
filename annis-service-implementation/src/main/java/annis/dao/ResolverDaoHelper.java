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
package annis.dao;

import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author thomas
 */
public class ResolverDaoHelper implements ResultSetExtractor, PreparedStatementCreator
{

  public ResolverDaoHelper()
  {
  }

  @Override
  public PreparedStatement createPreparedStatement(Connection cnctn) throws SQLException
  {
    String select = "SELECT resolver_vis_map.id, "
      + "resolver_vis_map.corpus, "
      + "resolver_vis_map.version, "
      + "resolver_vis_map.namespace, "
      + "resolver_vis_map.element, "
      + "resolver_vis_map.vis_type, "
      + "resolver_vis_map.display_name, "
      + "resolver_vis_map.order, "
      + "resolver_vis_map.visibility, "
      + "resolver_vis_map.mappings\n";

    String defaultFromWhere = 
      "FROM resolver_vis_map \nWHERE ";

    // If (corp=null && ns=null && element=null) => show this visualisation no matter what
    String firstUnion =
      select
      + defaultFromWhere
      + " resolver_vis_map.corpus is NULL AND resolver_vis_map.namespace is NULL" 
      + " AND resolver_vis_map.element is NULL\n";

    // if (not_exists(my_corp+my_ns) && exists(corp=null && ms=my_ns && element=my_element.type)) => show this visulization for this hit;
    String secondUnion =
      select
      + defaultFromWhere
      + " resolver_vis_map.corpus is NULL AND resolver_vis_map.namespace = "
      + "?" // namespace
      + " AND resolver_vis_map.element = "
      + "?" // type
      + " AND resolver_vis_map.namespace NOT IN ("
          + "SELECT resolver_vis_map.namespace FROM resolver_vis_map WHERE"
          + " resolver_vis_map.namespace = "
          +  "?" //namespace
          + " AND resolver_vis_map.corpus = "
          + "?" // corpus
      + ")\n";

    // if (corp=my_corp && ns=my_ns && element=my_element.type) => show this visulization for this hit;
    String thirdUnion =
      select
      + defaultFromWhere
      + " resolver_vis_map.namespace = "
      + "?" //namespace
      + " AND resolver_vis_map.element = "
      + "?" //type
      + " AND resolver_vis_map.corpus = "
      + "?" //corpus
      + "\n"; 

    // if (corp=my_corp && ns=null) => always show this visualization for this corpus;
    String fourthUnion =
      select
      +  defaultFromWhere
      + "  resolver_vis_map.namespace IS NULL "
      + " AND resolver_vis_map.corpus = "
      + "?" // corpus
      + "\n";


    StringBuilder result = new StringBuilder();
    
    result.append(firstUnion);
    result.append(" \nUNION \n");
    result.append(secondUnion);
    result.append(" \nUNION \n");
    result.append(thirdUnion);
    result.append(" \nUNION \n");
    result.append(fourthUnion);
    result.append(" \nORDER BY \"order\" ;");

    return cnctn.prepareStatement(result.toString());
  }

  public void fillPreparedStatement(SingleResolverRequest resolverRequest, PreparedStatement stmt) throws SQLException
  {
    stmt.setString(1, resolverRequest.getNamespace());
    stmt.setString(2, resolverRequest.getType().name());
    stmt.setString(3, resolverRequest.getNamespace());
    stmt.setString(4, resolverRequest.getCorpusName());

    stmt.setString(5, resolverRequest.getNamespace());
    stmt.setString(6, resolverRequest.getType().name());
    stmt.setString(7, resolverRequest.getCorpusName());
    stmt.setString(8, resolverRequest.getCorpusName());
  }

  @Override
  public List<ResolverEntry> extractData(ResultSet rs) throws SQLException, DataAccessException
  {
    List<ResolverEntry> result = new LinkedList<ResolverEntry>();


    while (rs.next())
    {
      Properties mappings = new Properties();

      String mappingsAsString = rs.getString("mappings");
      if(mappingsAsString != null)
      {
        // split the entrys
        String[] entries = mappingsAsString.split(";");
        for(String e : entries)
        {
          // split key-value
          String[] keyvalue= e.split(":", 2);
          if(keyvalue.length == 2)
          {
            mappings.put(keyvalue[0].trim(), keyvalue[1].trim());
          }
        }
      }

      String element = rs.getString("element");

      ResolverEntry e = new ResolverEntry(
        rs.getLong("id"),
        rs.getString("corpus"),
        rs.getString("version"),
        rs.getString("namespace"),
        element == null ? null : ResolverEntry.ElementType.valueOf(element),
        rs.getString("vis_type"),
        rs.getString("display_name"),
        rs.getString("visibility"),
        mappings,
        rs.getInt("order"));
      result.add(e);

    }

    return result;
  }
}
