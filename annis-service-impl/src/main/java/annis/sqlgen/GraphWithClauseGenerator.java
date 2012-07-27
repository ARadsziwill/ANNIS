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

import annis.dao.AnnisDao;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * Generates a WITH clause sql statement for a list of salt ids.
 *
 * Salt ids are simple URI and are defined like this:
 *
 * <p>{@code salt:/corp1/corp2/doc1}</p>.
 *
 * The leading / of the URI is a must, // would cause an error, because
 * authorities are currently not supported.
 *
 * TODO support table access strategy
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class GraphWithClauseGenerator implements
  WithClauseSqlGenerator<QueryData>
{

  private Logger log = Logger.getLogger(GraphWithClauseGenerator.class);
  private AnnisDao annisDao;
  private static final String TABSTOP = "    ";
  private IslandsPolicy islandsPolicy;

  @Override
  public List<String> withClauses(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    List<SaltURIs> listOfSaltURIs = queryData.getExtensions(SaltURIs.class);
    // only work with the first element
    Validate.isTrue(!listOfSaltURIs.isEmpty());

    SaltURIs saltURIs = listOfSaltURIs.get(0);
    List<String> withClauseList = new ArrayList<String>();
    StringBuilder sb = new StringBuilder();
    List<Long> corpusList = getCorpusList(saltURIs);

    // island policies
    HashMap<Long, Properties> corpusProperties =
      queryData.getCorpusConfiguration();
    IslandsPolicy.IslandPolicies islandPolicy = islandsPolicy.
      getMostRestrictivePolicy(
      corpusList, corpusProperties);

    sb.append("node_ids AS (\n");
    sb.append("SELECT DISTINCT\n").append(TABSTOP);
    for (int i = 1; i <= saltURIs.size(); i++)
    {
      sb.append("facts").append(i).append(".id AS id").append(i);

      if (i < saltURIs.size())
      {
        sb.append(",\n").append(TABSTOP);
      }
    }

    sb.append("\nFROM\n").append(TABSTOP);
    for (int i = 1; i <= saltURIs.size(); i++)
    {
      sb.append("facts").append(" AS facts").append(i);
      sb.append(",\n");
      sb.append(TABSTOP);
    }

    sb.append("corpus\n");
    sb.append("\nWHERE\n").append(TABSTOP);
    for (int i = 1; i <= saltURIs.size(); i++)
    {
      URI uri = saltURIs.get(i - 1);

      sb.append("path_name = ").append(generatePathName(uri));

      sb.append("\nAND\n").append(TABSTOP);

      sb.append("facts").append(i).append(".corpus_ref");
      sb.append(" = ");
      sb.append("corpus.id");

      sb.append("\nAND\n").append(TABSTOP);

      sb.append("facts").append(i).append(".node_name");
      sb.append(" = ");
      sb.append("'").append(uri.getFragment()).append("'");

      if (i < saltURIs.size())
      {
        sb.append("\nAND\n").append(TABSTOP);
      }
    }

    sb.append("\n), ");
    sb.append("min_max AS (\n");
    sb.append("SELECT\n").append(TABSTOP);
    /**
     * If the island policy is set to context, the generated sql is bit more
     * complex. We need to generate an extra column for every match.
     */
    if (islandPolicy == IslandsPolicy.IslandPolicies.context)
    {
      for (int i = 1; i <= saltURIs.size(); i++)
      {
        sb.append("min(facts").append(i);
        sb.append(".left_token) as min").append(i);
        sb.append(",\n").append(TABSTOP);

        sb.append("max(facts").append(i);
        sb.append(".right_token) as max").append(i);
        sb.append(",\n").append(TABSTOP);

        sb.append("corpus").append(i).append(".id AS id").append(i);

        if (i < saltURIs.size())
        {
          sb.append(",\n").append(TABSTOP);
        }
      }


      sb.append("\nFROM\n").append(TABSTOP);
      for (int i = 1; i <= saltURIs.size(); i++)
      {
        sb.append("corpus AS corpus").append(i).append(", ");

        sb.append("facts AS facts").append(i);
        if (i < saltURIs.size())
        {
          sb.append(", ");
        }
      }
      sb.append("\n");
      sb.append("WHERE \n").append(TABSTOP);
      for (int i = 0; i < saltURIs.size(); i++)
      {
        URI uri = saltURIs.get(i);

        // the path is reversed in relAnnis saved
        sb.append("corpus").append(i + 1);
        sb.append(".path_name = ").append(generatePathName(uri));

        sb.append("\nAND\n").append(TABSTOP);

        sb.append("facts").append(i + 1).append(".corpus_ref");
        sb.append(" = ");
        sb.append("corpus").append(i + 1).append(".id");

        sb.append("\nAND\n").append(TABSTOP);

        sb.append("facts").append(i + 1).append(".node_name");
        sb.append(" = ");
        sb.append("'").append(uri.getFragment()).append("'");

        // concate conditions
        if (i < saltURIs.size() - 1)
        {
          sb.append("\nAND\n").append(TABSTOP);
        }
      }

      sb.append("\nGROUP BY\n").append(TABSTOP);
      for (int i = 1; i <= saltURIs.size(); i++)
      {
        sb.append("corpus").append(i).append(".id");

        if (i < saltURIs.size())
        {
          sb.append(", ");
        }
      }
      sb.append("\n), ");

      sb.append("matching_nodes AS (\n");
      sb.append("SELECT DISTINCT\n").append(TABSTOP);

      appendSelectedColumns(sb);

      sb.append("\nFROM min_max, facts\n");

      sb.append("WHERE\n").append(TABSTOP);
      for (int i = 1; i <= saltURIs.size(); i++)
      {
        sb.append("min_max.min").append(i).append(" - 5 <= facts.left_token  ");
        sb.append("\nAND\n").append(TABSTOP);
        sb.append("facts.right_token <= min_max.max").append(i).append(" + 5");
        sb.append("\nAND\n").append(TABSTOP);
        sb.append("corpus_ref = min_max.id").append(i);

        if (i < saltURIs.size())
        {
          sb.append("\nAND\n").append(TABSTOP);
        }
      }

      sb.append("\nORDER BY facts.token_index");
      sb.append("\n)"); //

    } // end of island policy == context
    else
    {

      sb.append("min(facts.left_token) as min,\n").append(TABSTOP);
      sb.append("max(facts.right_token) as max,\n").append(TABSTOP);

      for (int i = 1; i <= saltURIs.size(); i++)
      {
        sb.append("min(facts.id) AS min_id").append(i).append(",\n");
        sb.append(TABSTOP);
        sb.append("max(facts.id) AS max_id").append(i).append(",\n");
        sb.append(TABSTOP);
      }
      sb.append("corpus.id as id\n");

      sb.append("FROM\n").append(TABSTOP);
      sb.append("corpus, facts\n");


      sb.append("WHERE \n").append(TABSTOP);
      for (int i = 0; i < saltURIs.size(); i++)
      {
        URI uri = saltURIs.get(i);

        // the path is reversed in relAnnis saved
        sb.append("path_name = ").append(generatePathName(uri));

        sb.append("\nAND\n").append(TABSTOP);

        sb.append("facts.corpus_ref");
        sb.append(" = ");
        sb.append("corpus.id");

        sb.append("\nAND\n").append(TABSTOP);

        sb.append("facts.node_name");
        sb.append(" = ");
        sb.append("'").append(uri.getFragment()).append("'");

        // concate conditions
        if (i < saltURIs.size() - 1)
        {
          sb.append("\nOR\n").append(TABSTOP);
        }
      }

      sb.append("\nGROUP BY corpus.id");
      sb.append("\n), ");

      sb.append("matching_nodes AS (\n");
      sb.append("SELECT DISTINCT\n").append(TABSTOP);

      appendSelectedColumns(sb);

      sb.append("\nFROM min_max, facts\n");

      sb.append("WHERE\n").append(TABSTOP);

      sb.append("min_max.min - 5 <= facts.left_token  ");
      sb.append("\nAND\n").append(TABSTOP);
      sb.append("facts.right_token <= min_max.max + 5");
      sb.append("\nAND\n").append(TABSTOP);
      sb.append("corpus_ref = min_max.id");

      sb.append("\nORDER BY facts.token_index");
      sb.append("\n)"); //
    }

    withClauseList.add(sb.toString());
    return withClauseList;
  }

  private String generatePathName(URI uri)
  {
    StringBuilder sb = new StringBuilder();
    String rawPath = StringUtils.strip(uri.getPath(), "/ \t");
    String[] path = rawPath.split("/");

    sb.append("'{");
    for (int j = path.length - 1; j >= 0; j--)
    {
      sb.append(path[j]);

      if (j > 0)
      {
        sb.append(", ");
      }
    }

    sb.append("}'");
    return sb.toString();
  }

  private void appendField(StringBuilder sb, ArrayList<String> fields)
  {
    sb.append(StringUtils.join(fields, ",\n" + TABSTOP));
  }

  /**
   * @return the islandsPolicy
   */
  public IslandsPolicy getIslandsPolicy()
  {
    return islandsPolicy;
  }

  /**
   * @param islandsPolicy the islandsPolicy to set
   */
  public void setIslandsPolicy(IslandsPolicy islandsPolicy)
  {
    this.islandsPolicy = islandsPolicy;
  }

  private List<Long> getCorpusList(SaltURIs saltURIs)
  {
    List<String> corpusIds = new ArrayList<String>();

    for (URI uri : saltURIs)
    {
      corpusIds.add(uri.getPath().split("/")[0]);

    }
    return getAnnisDao().listCorpusByName(corpusIds);
  }

  /**
   * @return the annisDao
   */
  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  /**
   * @param annisDao the annisDao to set
   */
  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }

  private void appendSelectedColumns(StringBuilder sb)
  {
    ArrayList<String> fields = new ArrayList<String>();

    fields.add("facts.id");
    fields.add("facts.node_anno_ref");
    fields.add("facts.edge_anno_ref");
    fields.add("facts.text_ref");
    fields.add("facts.corpus_ref");
    fields.add("facts.toplevel_corpus");
    fields.add("facts.node_namespace");
    fields.add("facts.node_name");
    fields.add("facts.left");
    fields.add("facts.right");
    fields.add("facts.token_index");
    fields.add("facts.is_token");
    fields.add("facts.continuous");
    fields.add("facts.span");
    fields.add("facts.left_token");
    fields.add("facts.right_token");
    fields.add("facts.seg_name");
    fields.add("facts.seg_left");
    fields.add("facts.seg_right");
    fields.add("facts.pre");
    fields.add("facts.post");
    fields.add("facts.parent");
    fields.add("facts.root");
    fields.add("facts.level");
    fields.add("facts.component_id");
    fields.add("facts.edge_type");
    fields.add("facts.edge_name");
    fields.add("facts.edge_namespace");


    appendField(sb, fields);
  }
}
