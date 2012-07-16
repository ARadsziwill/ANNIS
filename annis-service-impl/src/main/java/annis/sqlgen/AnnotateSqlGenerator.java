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

import static annis.sqlgen.TableAccessStrategy.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @param <T> Type into which the JDBC result set is transformed
 *
 * @author thomas
 */
public abstract class AnnotateSqlGenerator<T>
  extends AbstractSqlGenerator<T>
  implements SelectClauseSqlGenerator<QueryData>,
  FromClauseSqlGenerator<QueryData>,
  WhereClauseSqlGenerator<QueryData>, OrderByClauseSqlGenerator<QueryData>,
  WithClauseSqlGenerator<QueryData>,
  AnnotateExtractor<T>
{

  // include document name in SELECT clause
  private boolean includeDocumentNameInAnnotateQuery;
  // include is_token column in SELECT clause
  private boolean includeIsTokenColumn;
  private AnnotateInnerQuerySqlGenerator innerQuerySqlGenerator;
  private TableJoinsInFromClauseSqlGenerator tableJoinsInFromClauseSqlGenerator;
  private TableAccessStrategy outerQueryTableAccessStrategy;
  private ResultSetExtractor<T> resultExtractor;
  // helper to extract the corpus path from a JDBC result set
  private CorpusPathExtractor corpusPathExtractor;


  // old
  public enum IslandPolicies
  {

    context, none
  }
  private String matchedNodesViewName;
  private String defaultIslandsPolicy;

  public AnnotateSqlGenerator()
  {
  }

  /**
   * Create a solution key to be used inside a single call to
   * {@code extractData}.
   *
   * This method must be overridden in child classes or by Spring.
   */
  protected SolutionKey<?> createSolutionKey()
  {
    throw new NotImplementedException(
      "BUG: This method needs to be overwritten by ancestors or through Spring");
  }

  /**
   *
   * @param jdbcTemplate
   * @param textID
   * @deprecated
   */
  @Deprecated
  public T queryAnnotationGraph(
    JdbcTemplate jdbcTemplate, long textID)
  {
    return (T) jdbcTemplate.query(getTextQuery(textID), this);
  }

  public T queryAnnotationGraph(
    JdbcTemplate jdbcTemplate, String toplevelCorpusName, String documentName)
  {
    return (T) jdbcTemplate.query(getDocumentQuery(toplevelCorpusName,
      documentName), this);
  }

  private IslandPolicies getMostRestrictivePolicy(
    List<Long> corpora, Map<Long, Properties> props)
  {
    if(corpora.isEmpty())
    {
      return IslandPolicies.valueOf(defaultIslandsPolicy);
    }
    
    IslandPolicies[] all = IslandPolicies.values();
    IslandPolicies result = all[all.length - 1];
    
    for (Long l : corpora)
    {
      IslandPolicies newPolicy = IslandPolicies.valueOf(defaultIslandsPolicy);
      
      if (props.get(l) != null)
      {
        newPolicy =
          IslandPolicies.valueOf(props.get(l).getProperty("islands-policy",
          defaultIslandsPolicy));
      }
      
      if (newPolicy.ordinal() < result.ordinal())
      {
        result = newPolicy;
      }
    }
    return result;
  }

  @Deprecated
  public abstract String getTextQuery(long textID);

  public abstract String getDocumentQuery(String toplevelCorpusName,
    String documentName);

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }

  public String getDefaultIslandsPolicy()
  {
    return defaultIslandsPolicy;
  }

  public void setDefaultIslandsPolicy(String defaultIslandsPolicy)
  {
    this.defaultIslandsPolicy = defaultIslandsPolicy;
  }

  @Override
  public abstract String selectClause(QueryData queryData,
    List<QueryNode> alternative, String indent);

  protected void addSelectClauseAttribute(List<String> fields,
    String table, String column)
  {
    TableAccessStrategy tas = tables(null);
    fields.add(tas.aliasedColumn(table, column) + " AS \""
      + outerQueryTableAccessStrategy.columnName(table, column)
      + "\"");
  }

  @Override
  public List<String> withClauses(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    List<Long> corpusList = queryData.getCorpusList();
    HashMap<Long, Properties> corpusProperties =
      queryData.getCorpusConfiguration();
    IslandPolicies islandsPolicy =
      getMostRestrictivePolicy(corpusList, corpusProperties);
    
    List<String> result = new LinkedList<String>();
    
    List<AnnotateQueryData> ext = queryData.getExtensions(AnnotateQueryData.class);
    if(!ext.isEmpty())
    {
      AnnotateQueryData annoQueryData = ext.get(0);
      
      if(annoQueryData.getSegmentationLayer() == null)
      {
        // token index based method 
        
        // first get the raw matches
        result.add(getMatchesWithClause(queryData, indent));
        
        // break the columns down in a way that every matched node has it's own
        // row
        result.add(getSolutionFromMatchesWithClause(queryData, islandsPolicy, 
          alternative, "matches", indent + TABSTOP));

      }
      else
      {
        // segmentation layer based method
        
        result.add(getMatchesWithClause(queryData, indent));
        result.add(getCoveredSeqWithClause(queryData, annoQueryData, alternative,
          "matches", indent));
        result.add(getSolutionFromCoveredSegWithClause(queryData, annoQueryData,
          alternative, islandsPolicy, "coveredseg", indent));
                
      }
    }
    
    return result;
  }

  /** 
   * Uses the inner SQL generator and provides an ordered and limited view on 
   * the matches with a match number. 
   */
  protected String getMatchesWithClause(QueryData queryData, String indent)
  {
    StringBuilder sb = new StringBuilder();        
    sb.append(indent).append("matches AS\n");
    sb.append(indent).append("(\n");
    sb.append(indent).append(getInnerQuerySqlGenerator().toSql(queryData, indent + TABSTOP));
    sb.append("\n").append(indent).append(")");
    
    return sb.toString();
  }
  
  /**
   * Breaks down the matches table, so that each node of each match has
   * it's own row.
   */
  protected String getSolutionFromMatchesWithClause(QueryData queryData, 
    IslandPolicies islandPolicies,
    List<QueryNode> alternative, String matchesName, 
    String indent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(indent).append("solutions AS\n");
    sb.append(indent).append("(\n");
    
    String innerIndent = indent + TABSTOP;
    
    if(islandPolicies == IslandPolicies.none)
    {
      innerIndent = indent + TABSTOP + TABSTOP;
      sb.append(indent).append("SELECT min(\"key\") AS key, n, text, "
        + "min(\"min\") AS \"min\", "
        + "max(\"max\") AS \"max\", min(corpus) AS corpus FROM (\n");
    }

    SolutionKey<?> key = createSolutionKey();
    TableAccessStrategy tas = createTableAccessStrategy();
    tas.getTableAliases().put("solutions", matchesName);
    List<String> keyColumns =
      key.generateOuterQueryColumns(tas, alternative.size());
    
   
    for(int i=1; i <= alternative.size(); i++)
    {
      if(i >= 2)
      {
        sb.append(innerIndent)
          .append("UNION ALL\n");
      }
      sb.append(innerIndent)
      .append("SELECT ")
      .append(StringUtils.join(keyColumns, ", "))
      .append(", n,  text")
      .append(i).append(" AS text, min")
      .append(i).append(" AS \"min\", max")
      .append(i).append(" AS \"max\", corpus")
      .append(i).append(" AS corpus ")
      .append("FROM ")
      .append(matchesName)
      .append("\n");

    } // end for all nodes in query
    
    if(islandPolicies == IslandPolicies.none)
    {
      sb.append(indent).append(") AS innersolution\n");
    }
        
    if(islandPolicies == IslandPolicies.none)
    {
      sb.append(indent).append("GROUP BY text, n\n");
    }
    
    sb.append(indent).append(")");

    return sb.toString();
  }
  
  /**
   * Get with clause for all covered spans of the segmentation layer.
   */
  protected String getCoveredSeqWithClause(
    QueryData queryData, AnnotateQueryData annoQueryData, 
    List<QueryNode> alternative, String matchesName, String indent)
  {
    String indent2 = indent + TABSTOP;
    String indent3 = indent2 + TABSTOP;
    String indent4 = indent3 + TABSTOP;
    
    SolutionKey<?> key = createSolutionKey();
    TableAccessStrategy tas = createTableAccessStrategy();
    tas.getTableAliases().put("solutions", matchesName);
    List<String> keyColumns =
      key.generateOuterQueryColumns(tas, alternative.size());
    
    
    TableAccessStrategy tables = tables(null);
    
    StringBuilder sb = new StringBuilder();
        sb.append(indent).append("coveredseg AS\n");
    sb.append(indent).append("(\n");

    sb.append(indent2).append("SELECT ");
    for(String k : keyColumns)
    {
      sb.append(k);
    }
    sb.append(", matches.n, ").
      append(tas.aliasedColumn(NODE_TABLE, "seg_left")).append(" - ")
      .append(annoQueryData.getLeft()).append(" AS \"min\", ")
      .append(tas.aliasedColumn(NODE_TABLE, "seg_right")).append(" + ")
      .append(annoQueryData.getRight()).append(" AS \"max\", ")
      .append(tas.aliasedColumn(NODE_TABLE, "text_ref"))
      .append(" AS \"text\"\n");

    
    sb.append(indent2).append("FROM ").append(tas.tableName(NODE_TABLE))
      .append(", matches\n");
    sb.append(indent2).append("WHERE\n");
    
    sb.append(indent3).append(tas.aliasedColumn(NODE_TABLE, "toplevel_corpus"))
      .append(" IN (")
      .append(StringUtils.join(queryData.getCorpusList(), ","))
      .append(") AND\n");
    
   sb.append(indent3)
    .append(tas.aliasedColumn(NODE_TABLE, "n_sample"))
    .append(" IS TRUE AND\n");
    
    sb.append(indent3).append("seg_name = '")
      .append(annoQueryData.getSegmentationLayer())
      .append("' AND\n");
    
    sb.append(indent3).append("(\n");
    
    for(int i=1; i <= alternative.size(); i++)
    {
      if(i >= 2)
      {
        sb.append(indent4).append("OR\n");
      }
      
      sb.append(overlapForOneRange(indent4, 
        "matches.min" + i, "matches.max"+i, 
        "matches.text" + i, tables));
      sb.append("\n");
    }
    sb.append(indent3).append(")\n"); // end of or


    sb.append(indent).append(")\n");

    return sb.toString();
  }
  
  /**
   * Get solution from a covered segmentation using a WITH clause.
   * @param queryData
   * @param annoQueryData
   * @param alternative
   * @param islandsPolicy
   * @param coveredName
   * @param indent
   * @return 
   */
  protected String getSolutionFromCoveredSegWithClause(
    QueryData queryData, AnnotateQueryData annoQueryData, 
    List<QueryNode> alternative, IslandPolicies islandsPolicy, String coveredName, String indent)
  {
    
    TableAccessStrategy tas = createTableAccessStrategy();
    
    String indent2 = indent + TABSTOP;
    String indent3 = indent2 + TABSTOP;
    
    List<Long> corpusList = queryData.getCorpusList();
    
    StringBuilder sb = new StringBuilder();
    
    sb.append(indent).append("solutions AS\n");
    sb.append(indent).append("(\n");
    
    sb.append(indent2)
      .append("SELECT DISTINCT ");
    
    if(islandsPolicy == IslandPolicies.none)
    {
      sb.append("min(")
        .append(coveredName)
        .append(".key) AS key, ")
        .append(coveredName)
        .append(".n AS n, ")
        .append("min(").append(tas.aliasedColumn(NODE_TABLE, "left_token")).append(") AS \"min\", ")
        .append("max(").append(tas.aliasedColumn(NODE_TABLE, "right_token")).append(") AS \"max\", ")
        .append("").append(tas.aliasedColumn(NODE_TABLE, "text_ref")).append(" AS \"text\"\n");
    }
    else if(islandsPolicy == IslandPolicies.context)
    {
      sb.append(coveredName).append(".key AS key, ")
        .append(coveredName).append(".n AS n, ")
        .append(tas.aliasedColumn(NODE_TABLE, "left_token")).append(" AS \"min\", ")
        .append(tas.aliasedColumn(NODE_TABLE, "right_token")).append(" AS \"max\", ")
        .append(tas.aliasedColumn(NODE_TABLE, "text_ref")).append(" AS \"text\"\n");
    }
    else
    {
      throw new NotImplementedException("No implementation for island policy " 
        + islandsPolicy.toString());
    }
    
    sb.append(indent2)
      .append("FROM ").append(coveredName).append(", ").append(tas.tableName(NODE_TABLE)).append("\n");
    
    sb.append(indent2)
      .append("WHERE\n");
    
    sb.append(indent3)
      .append(tas.aliasedColumn(NODE_TABLE, "toplevel_corpus")).append(" IN (")
      .append(StringUtils.join(corpusList, ","))
      .append(") AND\n");
    
    sb.append(indent3)
      .append(tas.aliasedColumn(NODE_TABLE, "n_sample")).append(" IS TRUE AND\n");
    
    sb.append(indent3)
      .append(tas.aliasedColumn(NODE_TABLE, "seg_name")).append(" = '")
      .append(annoQueryData.getSegmentationLayer())
      .append("' AND\n");
    
    sb.append(indent3)
      .append(tas.aliasedColumn(NODE_TABLE, "text_ref")).append(" = ")
      .append(coveredName)
      .append(".\"text\" AND\n");
    
    sb.append(indent3)
      .append(tas.aliasedColumn(NODE_TABLE, "seg_left")).append(" <= ")
      .append(coveredName)
      .append(".\"max\" AND\n");
    sb.append(indent3)
      .append(tas.aliasedColumn(NODE_TABLE, "seg_right")).append(" >= ")
      .append(coveredName)
      .append(".\"min\"\n");
    
    if(islandsPolicy == IslandPolicies.none)
    {
      sb.append(indent2)
        .append("GROUP BY ").append(tas.aliasedColumn(NODE_TABLE, "text_ref")).append(", n\n");
    }
    
    sb.append(indent).append(")\n");
    
    return sb.toString();
  }
  

  @Override
  public Set<String> whereConditions(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tables = tables(null);

    StringBuilder sb = new StringBuilder();

    // restrict node table to corpus list
    List<Long> corpusList = queryData.getCorpusList();
    if (corpusList != null && !corpusList.isEmpty())
    {
      sb.append(indent);
      sb.append(tables.aliasedColumn(NODE_TABLE, "toplevel_corpus"));
      sb.append(" IN (");
      sb.append(StringUtils.join(corpusList, ", "));
      sb.append(") AND\n");

      if (!tables.isMaterialized(RANK_TABLE, NODE_TABLE))
      {
        sb.append(indent);
        sb.append(tables.aliasedColumn(RANK_TABLE, "toplevel_corpus"));
        sb.append(" IN (");
        sb.append(StringUtils.join(corpusList, ", "));
        sb.append(") AND\n");
      }

    }


    String overlap = overlapForOneRange(indent + TABSTOP,
      "solutions.\"min\"", "solutions.\"max\"", "solutions.text", 
      tables);
    sb.append(overlap);
    sb.append("\n");
    

    // corpus constriction
    sb.append(" AND\n");
    sb.append(indent).append(TABSTOP);
    sb.append(tables.aliasedColumn(CORPUS_TABLE, "id"));
    sb.append(" = ");
    sb.append(tables.aliasedColumn(NODE_TABLE, "corpus_ref"));

    HashSet<String> result = new HashSet<String>();

    result.add(sb.toString());

    return result;
  }

  private String overlapForOneRange(String indent,
    String rangeMin, String rangeMax, String textRef, TableAccessStrategy tables)
  {
    StringBuilder sb = new StringBuilder();

    sb.append(indent).append("(");

    sb.append(tables.aliasedColumn(NODE_TABLE, "left_token"))
      .append(" <= ").append(rangeMax).append(" AND ")
      .append(tables.aliasedColumn(NODE_TABLE, "right_token"))
      .append(" >= ").append(rangeMin)
      .append(" AND ")
      .append(tables.aliasedColumn(NODE_TABLE, "text_ref"))
      .append(" = ").append(textRef)
     ;

    sb.append(")");

    return sb.toString();
  }

  @Override
  public String orderByClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("solutions.n, ");
    sb.append(tables(null).aliasedColumn(COMPONENT_TABLE, "id")).append(", ");
    String preColumn = tables(null).aliasedColumn(RANK_TABLE, "pre");
    sb.append(preColumn);
    String orderByClause = sb.toString();
    return orderByClause;
  }

  @Override
  public abstract String fromClause(QueryData queryData,
    List<QueryNode> alternative, String indent);

  @Override
  public T extractData(ResultSet resultSet)
    throws SQLException, DataAccessException
  {
    return resultExtractor.extractData(resultSet);
  }

  public AnnotateInnerQuerySqlGenerator getInnerQuerySqlGenerator()
  {
    return innerQuerySqlGenerator;
  }

  public void setInnerQuerySqlGenerator(
    AnnotateInnerQuerySqlGenerator innerQuerySqlGenerator)
  {
    this.innerQuerySqlGenerator = innerQuerySqlGenerator;
  }

  public TableJoinsInFromClauseSqlGenerator getTableJoinsInFromClauseSqlGenerator()
  {
    return tableJoinsInFromClauseSqlGenerator;
  }

  public void setTableJoinsInFromClauseSqlGenerator(
    TableJoinsInFromClauseSqlGenerator tableJoinsInFromClauseSqlGenerator)
  {
    this.tableJoinsInFromClauseSqlGenerator = tableJoinsInFromClauseSqlGenerator;
  }

  public TableAccessStrategy getOuterQueryTableAccessStrategy()
  {
    return outerQueryTableAccessStrategy;
  }

  public boolean isIncludeDocumentNameInAnnotateQuery()
  {
    return includeDocumentNameInAnnotateQuery;
  }

  public void setIncludeDocumentNameInAnnotateQuery(
    boolean includeDocumentNameInAnnotateQuery)
  {
    this.includeDocumentNameInAnnotateQuery = includeDocumentNameInAnnotateQuery;
  }

  public boolean isIncludeIsTokenColumn()
  {
    return includeIsTokenColumn;
  }

  public void setIncludeIsTokenColumn(boolean includeIsTokenColumn)
  {
    this.includeIsTokenColumn = includeIsTokenColumn;
  }

  public CorpusPathExtractor getCorpusPathExtractor()
  {
    return corpusPathExtractor;
  }

  public void setCorpusPathExtractor(CorpusPathExtractor corpusPathExtractor)
  {
    this.corpusPathExtractor = corpusPathExtractor;
  }

  @Override
  public void setOuterQueryTableAccessStrategy(
    TableAccessStrategy outerQueryTableAccessStrategy)
  {
    this.outerQueryTableAccessStrategy = outerQueryTableAccessStrategy;
  }

  public ResultSetExtractor<T> getResultExtractor()
  {
    return resultExtractor;
  }

  public void setResultExtractor(ResultSetExtractor<T> resultExtractor)
  {
    this.resultExtractor = resultExtractor;
  }
}
