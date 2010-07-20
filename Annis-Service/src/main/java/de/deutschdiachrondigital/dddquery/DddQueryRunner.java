package de.deutschdiachrondigital.dddquery;


import java.util.ArrayList;
import java.util.List;


import annis.AnnisBaseRunner;
import annis.AnnotationGraphDotExporter;
import annis.TableFormatter;
import annis.WekaDaoHelper;
import annis.dao.AnnisDao;
import annis.dao.AnnotationGraphDaoHelper;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListNodeAnnotationsSqlHelper;
import annis.sqlgen.SqlGenerator;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

public class DddQueryRunner extends AnnisBaseRunner {

//	private static Logger log = Logger.getLogger(DddQueryRunner.class);
	
	// dependencies
	private DddQueryParser dddQueryParser;
	private SqlGenerator findSqlGenerator;
	private AnnisDao annisDao;
	
	private AnnotationGraphDaoHelper annotationGraphDaoHelper;
	private WekaDaoHelper wekaDaoHelper;
	private ListCorpusSqlHelper listCorpusHelper;
	private ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper;

	private AnnotationGraphDotExporter annotationGraphDotExporter;
	private TableFormatter tableFormatter;
	
	// settings
	private int matchLimit;
	private int context;
	private List<Long> corpusList;
	
	public DddQueryRunner() {
		corpusList = new ArrayList<Long>();
	}
	
	public static void main(String[] args) {
		// get runner from Spring
		AnnisBaseRunner.getInstance("dddQueryRunner", "de/deutschdiachrondigital/dddquery/DddQueryRunner-context.xml").run(args);
	}
	
	///// CLI methods
	
	public void doHelp(String dddquery) {
		out.println("not implemented");
	}

	public void doParse(String dddQuery) {
		out.println(DddQueryParser.dumpTree(dddQueryParser.parse(dddQuery)));
	}
	
	// FIXME: missing tests
	public void doSql(String dddQuery) {
		// sql query
		Start statement = dddQueryParser.parse(dddQuery);
		String sql = findSqlGenerator.toSql(statement, corpusList);

		out.println(sql);
	}
	
	public void doCount(String dddQuery) {
		out.println(annisDao.countMatches(getCorpusList(), dddQuery));
	}
	
	public void doPlan(String dddQuery) {
		out.println(annisDao.plan(dddQuery, getCorpusList(), false));
	}
	
	public void doAnalyze(String dddQuery) {
		out.println(annisDao.plan(dddQuery, getCorpusList(), true));
	}
	
	public void doAnnotate2(String dddQuery) {
		List<AnnotationGraph> graphs = annisDao.retrieveAnnotationGraph(getCorpusList(), dddQuery, 0, matchLimit, context, context);
		printAsTable(graphs, "nodes", "edges");
	}
	
	public void doCorpus(List<Long> corpora) {
		setCorpusList(corpora);
	}

	public void doWait(String seconds) {
		try {
			out.println(annisDao.doWait(Integer.parseInt(seconds)));
		} catch (Exception e) {
			//
		}
	}
	
	public void doList(String unused) {
		List<AnnisCorpus> corpora = annisDao.listCorpora();
		printAsTable(corpora, "id", "name", "textCount", "tokenCount");
	}
	
	public void doNodeAnnotations(String doListValues) {
		boolean listValues = "values".equals(doListValues);
		List<AnnisAttribute> nodeAnnotations = annisDao.listNodeAnnotations(corpusList, listValues);
		printAsTable(nodeAnnotations, "name", "distinctValues");
	}
	
	public void doMeta(String corpusId) {
		List<Annotation> corpusAnnotations = annisDao.listCorpusAnnotations(Long.parseLong(corpusId));
		printAsTable(corpusAnnotations, "namespace", "name", "value");
	}
	///// Helper
	
	private void printAsTable(List<? extends Object> list, String... fields) {
		out.println(tableFormatter.formatAsTable(list, fields));
	}
	
	///// Getter / Setter
	
	public DddQueryParser getDddQueryParser() {
		return dddQueryParser;
	}
	
	public void setDddQueryParser(DddQueryParser dddQueryParser) {
		this.dddQueryParser = dddQueryParser;
	}
	
	public SqlGenerator getFindSqlGenerator() {
		return findSqlGenerator;
	}
	
	public void setFindSqlGenerator(SqlGenerator sqlGenerator) {
		this.findSqlGenerator = sqlGenerator;
	}
	
	public AnnisDao getAnnisDao() {
		return annisDao;
	}
	
	public void setAnnisDao(AnnisDao annisDao) {
		this.annisDao = annisDao;
	}
	
	public AnnotationGraphDaoHelper getAnnotationGraphDaoHelper() {
		return annotationGraphDaoHelper;
	}
	
	public void setAnnotationGraphDaoHelper(
			AnnotationGraphDaoHelper annotationGraphDaoHelper) {
		this.annotationGraphDaoHelper = annotationGraphDaoHelper;
	}
	
	public int getMatchLimit() {
		return matchLimit;
	}
	
	public void setMatchLimit(int matchLimit) {
		this.matchLimit = matchLimit;
	}
	
	public List<Long> getCorpusList() {
		return corpusList;
	}
	
	public void setCorpusList(List<Long> corpusList) {
		this.corpusList = corpusList;
	}

	public TableFormatter getTableFormatter() {
		return tableFormatter;
	}

	public void setTableFormatter(TableFormatter tableFormatter) {
		this.tableFormatter = tableFormatter;
	}

	public WekaDaoHelper getWekaDaoHelper() {
		return wekaDaoHelper;
	}

	public void setWekaDaoHelper(WekaDaoHelper wekaDaoHelper) {
		this.wekaDaoHelper = wekaDaoHelper;
	}

	public ListCorpusSqlHelper getListCorpusHelper() {
		return listCorpusHelper;
	}

	public void setListCorpusHelper(ListCorpusSqlHelper listCorpusHelper) {
		this.listCorpusHelper = listCorpusHelper;
	}

	public ListNodeAnnotationsSqlHelper getListNodeAnnotationsSqlHelper() {
		return listNodeAnnotationsSqlHelper;
	}

	public void setListNodeAnnotationsSqlHelper(
			ListNodeAnnotationsSqlHelper listNodeAnnotationsSqlHelper) {
		this.listNodeAnnotationsSqlHelper = listNodeAnnotationsSqlHelper;
	}

	public AnnotationGraphDotExporter getAnnotationGraphDotExporter() {
		return annotationGraphDotExporter;
	}

	public void setAnnotationGraphDotExporter(
			AnnotationGraphDotExporter annotationGraphDotExporter) {
		this.annotationGraphDotExporter = annotationGraphDotExporter;
	}

	public int getContext() {
		return context;
	}

	public void setContext(int context) {
		this.context = context;
	}

}
