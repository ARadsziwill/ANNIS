package annis.dao;

import annis.executors.DefaultQueryExecutor;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import annis.executors.QueryExecutor;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.sqlgen.ListCorpusAnnotationsSqlHelper;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListNodeAnnotationsSqlHelper;
import annis.sqlgen.SqlGenerator;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;
import java.util.LinkedList;

// FIXME: test and refactor timeout and transaction management
public class SpringAnnisDao extends SimpleJdbcDaoSupport implements AnnisDao
{

  private static Logger log = Logger.getLogger(SpringAnnisDao.class);
  private int timeout;
  /// old
  private SqlGenerator sqlGenerator;
  private AnnotationGraphDaoHelper annotationGraphDaoHelper;
  private ListCorpusSqlHelper listCorpusSqlHelper;
  private ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
  private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsSqlHelper;
  /// new
  private List<SqlSessionModifier> sqlSessionModifiers;
  private SqlGenerator findSqlGenerator;
  private CountExtractor countExtractor;
  private MatrixExtractor matrixExtractor;
  private QueryAnalysis queryAnalysis;
  private DddQueryParser dddQueryParser;
  private ParameterizedSingleColumnRowMapper<String> planRowMapper;
  private ListCorpusByNameDaoHelper listCorpusByNameDaoHelper;
  private DefaultQueryExecutor defaultQueryExecutor;
  private GraphExtractor graphExtractor;
  private List<QueryExecutor> executorList;
  private MetaDataFilter metaDataFilter;

  public SpringAnnisDao()
  {
    planRowMapper = new ParameterizedSingleColumnRowMapper<String>();
    sqlSessionModifiers = new ArrayList<SqlSessionModifier>();
  }

  @Override
  public int countMatches(final List<Long> corpusList, final String dddQuery)
  {
    QueryData queryData = createDynamicMatchView(corpusList, dddQuery);

    return countExtractor.queryCount(getJdbcTemplate());
  }

  @Override
  public List<AnnotatedMatch> matrix(final List<Long> corpusList, final String dddquery)
  {
    QueryData queryData = createDynamicMatchView(corpusList, dddquery);

    int nodeCount = queryData.getMaxWidth();

    return matrixExtractor.queryMatrix(getJdbcTemplate(), corpusList, nodeCount);
  }

  @Override
  public String planCount(String dddQuery, List<Long> corpusList, boolean analyze)
  {
    Validate.notNull(corpusList, "corpusList=null passed as argument");

    createDynamicMatchView(corpusList, dddQuery);
    return countExtractor.explain(getJdbcTemplate(), analyze);
  }

  @Override
  public String planGraph(String dddQuery, List<Long> corpusList,
    long offset, long limit, int left, int right,
    boolean analyze)
  {
    Validate.notNull(corpusList, "corpusList=null passed as argument");

    QueryData queryData = createDynamicMatchView(corpusList, dddQuery);

    int nodeCount = queryData.getMaxWidth();
    return graphExtractor.explain(getJdbcTemplate(), corpusList, nodeCount,
      offset, limit, left, right, analyze);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<AnnotationGraph> retrieveAnnotationGraph(List<Long> corpusList, String dddQuery, long offset, long limit, int left, int right)
  {
    QueryData queryData = createDynamicMatchView(corpusList, dddQuery);

    int nodeCount = queryData.getMaxWidth();

    // create the Annis graphs
    return graphExtractor.queryAnnotationGraph(getJdbcTemplate(), corpusList, nodeCount, offset, limit, left, right);
    //return annotationGraphDaoHelper.queryAnnotationGraph(getJdbcTemplate(), nodeCount, corpusList, dddQuery, offset, limit, left, right);
  }

  private QueryData createDynamicMatchView(List<Long> corpusList, String dddQuery)
  {
    // parse the query
    Start statement = dddQueryParser.parse(dddQuery);

    // analyze it
    QueryData queryData = queryAnalysis.analyzeQuery(statement, corpusList);

    // execute session modifiers
    for (SqlSessionModifier sqlSessionModifier : sqlSessionModifiers)
    {
      sqlSessionModifier.modifySqlSession(getSimpleJdbcTemplate(), queryData);
    }

    List<Long> documents = metaDataFilter.getDocumentsForMetadata(queryData);

    // generate the view with the matched node IDs
    // TODO: use the constraint approach to filter the executors before we iterate over them
    for (QueryExecutor e : executorList)
    {
      if (e.checkIfApplicable(queryData))
      {
        e.createMatchView(getJdbcTemplate(), corpusList, documents, queryData);
        // leave the loop
        break;
      }
    }

    return queryData;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<AnnisCorpus> listCorpora()
  {
    return (List<AnnisCorpus>) getJdbcTemplate().query(
      listCorpusSqlHelper.createSqlQuery(), listCorpusSqlHelper);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<AnnisAttribute> listNodeAnnotations(List<Long> corpusList,
    boolean listValues)
  {
    return (List<AnnisAttribute>) getJdbcTemplate().query(
      listNodeAnnotationsSqlHelper.createSqlQuery(corpusList,
      listValues), listNodeAnnotationsSqlHelper);
  }

  @SuppressWarnings("unchecked")
  @Override
  public AnnotationGraph retrieveAnnotationGraph(long textId)
  {
    List<AnnotationGraph> graphs = (List<AnnotationGraph>) getJdbcTemplate().query(annotationGraphDaoHelper.createSqlQuery(textId),
      annotationGraphDaoHelper);
    if (graphs.isEmpty())
    {
      return null;
    }
    if (graphs.size() > 1)
    {
      throw new IllegalStateException("Expected only one annotation graph");
    }
    return graphs.get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Annotation> listCorpusAnnotations(long corpusId)
  {
    final String sql = listCorpusAnnotationsSqlHelper.createSqlQuery(corpusId);
    final List<Annotation> corpusAnnotations =
      (List<Annotation>) getJdbcTemplate().query(sql, listCorpusAnnotationsSqlHelper);
    return corpusAnnotations;
  }

  @Override
  public List<Long> listCorpusByName(List<String> corpusNames)
  {
    final String sql = listCorpusByNameDaoHelper.createSql(corpusNames);
    final List<Long> result = getSimpleJdbcTemplate().query(sql, listCorpusByNameDaoHelper);
    return result;
  }

  @Override
  public List<ResolverEntry> getResolverEntries(SingleResolverRequest[] request)
  {
    try
    {
      ResolverDaoHelper helper = new ResolverDaoHelper(request.length);
      PreparedStatement stmt = helper.createPreparedStatement(getConnection());
      helper.fillPreparedStatement(request, stmt);
      List<ResolverEntry> result = helper.extractData(stmt.executeQuery());
      return result;
    }
    catch (SQLException ex)
    {
      log.error("Could not get resolver entries from database", ex);
      return new LinkedList<ResolverEntry>();
    }

  }

  // /// Getter / Setter
  public DddQueryParser getDddQueryParser()
  {
    return dddQueryParser;
  }

  public void setDddQueryParser(DddQueryParser parser)
  {
    this.dddQueryParser = parser;
  }

  public SqlGenerator getSqlGenerator()
  {
    return sqlGenerator;
  }

  public void setSqlGenerator(SqlGenerator sqlGenerator)
  {
    this.sqlGenerator = sqlGenerator;
  }

  public ParameterizedSingleColumnRowMapper<String> getPlanRowMapper()
  {
    return planRowMapper;
  }

  public void setPlanRowMapper(
    ParameterizedSingleColumnRowMapper<String> planRowMapper)
  {
    this.planRowMapper = planRowMapper;
  }

  public AnnotationGraphDaoHelper getAnnotateMatchesQueryHelper()
  {
    return getAnnotationGraphDaoHelper();
  }

  public AnnotationGraphDaoHelper getAnnotationGraphDaoHelper()
  {
    return annotationGraphDaoHelper;
  }

  public void setAnnotateMatchesQueryHelper(
    AnnotationGraphDaoHelper annotateMatchesQueryHelper)
  {
    setAnnotationGraphDaoHelper(annotateMatchesQueryHelper);
  }

  public void setAnnotationGraphDaoHelper(
    AnnotationGraphDaoHelper annotationGraphDaoHelper)
  {
    this.annotationGraphDaoHelper = annotationGraphDaoHelper;
  }

  public int getTimeout()
  {
    return timeout;
  }

  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }

  public ListCorpusSqlHelper getListCorpusSqlHelper()
  {
    return listCorpusSqlHelper;
  }

  public void setListCorpusSqlHelper(ListCorpusSqlHelper listCorpusHelper)
  {
    this.listCorpusSqlHelper = listCorpusHelper;
  }

  public ListNodeAnnotationsSqlHelper getListNodeAnnotationsSqlHelper()
  {
    return listNodeAnnotationsSqlHelper;
  }

  public void setListNodeAnnotationsSqlHelper(
    ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper)
  {
    this.listNodeAnnotationsSqlHelper = listNodeAnnotationsSqlHelper;
  }

  public ListCorpusAnnotationsSqlHelper getListCorpusAnnotationsSqlHelper()
  {
    return listCorpusAnnotationsSqlHelper;
  }

  public void setListCorpusAnnotationsSqlHelper(
    ListCorpusAnnotationsSqlHelper listCorpusAnnotationsHelper)
  {
    this.listCorpusAnnotationsSqlHelper = listCorpusAnnotationsHelper;
  }

  public List<SqlSessionModifier> getSqlSessionModifiers()
  {
    return sqlSessionModifiers;
  }

  public void setSqlSessionModifiers(List<SqlSessionModifier> sqlSessionModifiers)
  {
    this.sqlSessionModifiers = sqlSessionModifiers;
  }

  public SqlGenerator getFindSqlGenerator()
  {
    return findSqlGenerator;
  }

  public void setFindSqlGenerator(SqlGenerator findSqlGenerator)
  {
    this.findSqlGenerator = findSqlGenerator;
  }

  public QueryAnalysis getQueryAnalysis()
  {
    return queryAnalysis;
  }

  public void setQueryAnalysis(QueryAnalysis queryAnalysis)
  {
    this.queryAnalysis = queryAnalysis;
  }

  public ListCorpusByNameDaoHelper getListCorpusByNameDaoHelper()
  {
    return listCorpusByNameDaoHelper;
  }

  public void setListCorpusByNameDaoHelper(
    ListCorpusByNameDaoHelper listCorpusByNameDaoHelper)
  {
    this.listCorpusByNameDaoHelper = listCorpusByNameDaoHelper;
  }

  public CountExtractor getCountExtractor()
  {
    return countExtractor;
  }

  public void setCountExtractor(CountExtractor countExtractor)
  {
    this.countExtractor = countExtractor;
  }

  public DefaultQueryExecutor getDefaultQueryExecutor()
  {
    return defaultQueryExecutor;
  }

  public void setDefaultQueryExecutor(DefaultQueryExecutor defaultQueryExecutor)
  {
    this.defaultQueryExecutor = defaultQueryExecutor;
  }

  public GraphExtractor getGraphExtractor()
  {
    return graphExtractor;
  }

  public void setGraphExtractor(GraphExtractor graphExtractor)
  {
    this.graphExtractor = graphExtractor;
  }

  public List<QueryExecutor> getExecutorList()
  {
    return executorList;
  }

  public void setExecutorList(List<QueryExecutor> executorList)
  {
    this.executorList = executorList;
  }

  public MetaDataFilter getMetaDataFilter()
  {
    return metaDataFilter;
  }

  public void setMetaDataFilter(MetaDataFilter metaDataFilter)
  {
    this.metaDataFilter = metaDataFilter;
  }

  public MatrixExtractor getMatrixExtractor()
  {
    return matrixExtractor;
  }

  public void setMatrixExtractor(MatrixExtractor matrixExtractor)
  {
    this.matrixExtractor = matrixExtractor;
  }
}
