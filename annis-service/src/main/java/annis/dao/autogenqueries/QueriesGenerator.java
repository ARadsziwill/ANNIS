/*
 * Copyright 2013 SFB 632.
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
package annis.dao.autogenqueries;

import annis.dao.AnnisDao;
import annis.examplequeries.ExampleQuery;
import annis.ql.parser.QueryData;
import annis.sqlgen.AnnotateQueryData;
import annis.sqlgen.LimitOffsetQueryData;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class QueriesGenerator
{

  private final Logger log = LoggerFactory.getLogger(QueriesGenerator.class);

  // for executing AQL queries
  private AnnisDao annisDao;

  // only contains an element: the top level corpus id of the imported corpus
  private List<Long> corpusIds;

  // the name of the imported top level corpus
  private String corpusName;

  // defines which cols of the tmp table are selected
  private Map<String, String> tableInsertSelect;

  private Set<QueryBuilder> queryBuilder;

  // to execute some sql commands directtly
  private JdbcTemplate jdbcTemplate;

  public void generateQueries(long corpusId)
  {
    corpusIds = new ArrayList<Long>();
    corpusIds.add(corpusId);
    List<String> corpusNames = getAnnisDao().mapCorpusIdsToNames(corpusIds);
    corpusName = corpusNames.get(0);

    if (queryBuilder != null)
    {
      for (QueryBuilder qB : queryBuilder)
      {
        generateQuery(qB);
      }
    }
  }

  public void generateQuery(QueryBuilder queryBuilder)
  {
    log.info("generate auto query for {}", corpusName);

    // retrieve the aql query for analyzing purposes
    String aql = queryBuilder.getAQL();


    // set some necessary extensions for generating complete sql
    QueryData queryData = getAnnisDao().parseAQL(aql, this.corpusIds);
    queryData.addExtension(new LimitOffsetQueryData(5, 5));
    queryData.addExtension(new AnnotateQueryData(5, 5, null));


    // retrieve the salt project to analyze
    SaltProject saltProject = getAnnisDao().annotate(queryData);
    queryBuilder.analyzingQuery(saltProject);

    // set the corpus name
    ExampleQuery exampleQuery = queryBuilder.getExampleQuery();
    exampleQuery.setCorpusName(corpusName);

    // copy the example query to the database
    if (!"".equals(exampleQuery.getExampleQuery()))
    {
      if (getTableInsertSelect().containsKey("example_queries"))
      {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO example_queries (");
        sql.append(getTableInsertSelect().get("example_queries")).append(") ");
        sql.append("VALUES (\n");
        sql.append("'").append(exampleQuery.getExampleQuery()).append("', ");
        sql.append("'").append(exampleQuery.getDescription()).append("', ");
        sql.append("'").append(exampleQuery.getType()).append("', ");
        sql.append("'").append(exampleQuery.getNodes()).append("', ");
        sql.append("'").append("{}").append("', ");
        sql.append("'").append(corpusIds.get(0)).append("'");
        sql.append("\n)");

        getJdbcTemplate().execute(sql.toString());
        log.info("generated example query: {}", exampleQuery.getExampleQuery());
      }
    }
    else
    {
      log.warn("could not generating queries");
    }
  }

  /**
   * @return the tableInsertSelect
   */
  public Map<String, String> getTableInsertSelect()
  {
    return tableInsertSelect;
  }

  /**
   * @param tableInsertSelect the tableInsertSelect to set
   */
  public void setTableInsertSelect(
    Map<String, String> tableInsertSelect)
  {
    this.tableInsertSelect = tableInsertSelect;
  }

  /**
   * @return the jdbcTemplate
   */
  public JdbcTemplate getJdbcTemplate()
  {
    return jdbcTemplate;
  }

  /**
   * @param jdbcTemplate the jdbcTemplate to set
   */
  public void setJdbcTemplate(
    JdbcTemplate jdbcTemplate)
  {
    this.jdbcTemplate = jdbcTemplate;
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

  /**
   * @return the queryBuilder
   */
  public Set<QueryBuilder> getQueryBuilder()
  {
    return queryBuilder;
  }

  /**
   * @param queryBuilder the queryBuilder to set
   */
  public void setQueryBuilder(
    Set<QueryBuilder> queryBuilder)
  {
    this.queryBuilder = queryBuilder;
  }

  public interface QueryBuilder
  {

    public String getAQL();

    public void analyzingQuery(SaltProject saltProject);

    public ExampleQuery getExampleQuery();
  }
}
