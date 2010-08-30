package annis;

import annis.ql.parser.AnnisParser;
import de.deutschdiachrondigital.dddquery.DddQueryMapper;
import de.deutschdiachrondigital.dddquery.DddQueryRunner;

// TODO: test AnnisRunner
public class AnnisRunner extends AnnisBaseRunner
{

//	private static Logger log = Logger.getLogger(AnnisRunner.class);
  // delegate most commands to DddQueryRunner
  private DddQueryRunner dddQueryRunner;
  // parser for Annis queries
  private AnnisParser annisParser;
  // map Annis queries to DDDquery
  private DddQueryMapper dddQueryMapper;

  public static void main(String[] args)
  {
    // get runner from Spring
    AnnisBaseRunner.getInstance("annisRunner", "annis/AnnisRunner-context.xml").run(args);
  }

  ///// Commands
  public void doDddquery(String annisQuery)
  {
    out.println(translate(annisQuery));
  }

  public void doParseInternal(String annisQuery)
  {
    dddQueryRunner.doParse(translate(annisQuery));
  }

  public void doParse(String annisQuery)
  {
    out.println(annisParser.dumpTree(annisQuery));
  }

  public void doSql(String annisQuery)
  {
    dddQueryRunner.doSql(translate(annisQuery));
  }

  public void doCount(String annisQuery)
  {
    dddQueryRunner.doCount(translate(annisQuery));
  }

  public void doPlanCount(String annisQuery)
  {
    dddQueryRunner.doPlanCount(translate(annisQuery));
  }

  public void doAnalyzeCount(String annisQuery)
  {
    dddQueryRunner.doAnalyzeCount(translate(annisQuery));
  }

  public void doPlanGraph(String annisQuery)
  {
    dddQueryRunner.doPlanGraph(translate(annisQuery));
  }

  public void doAnalyzeGraph(String annisQuery)
  {
    dddQueryRunner.doAnalyzeGraph(translate(annisQuery));
  }

  public void doAnnotate(String annisQuery)
  {
    dddQueryRunner.doAnnotate(translate(annisQuery));
  }

  public void doCorpus(String corpusList)
  {
    dddQueryRunner.doCorpus(dddQueryMapper.translateCorpusList(corpusList));
  }

  public void doWait(String seconds)
  {
    dddQueryRunner.doWait(seconds);
  }

  public void doList(String unused)
  {
    dddQueryRunner.doList(unused);
  }

  public void doNodeAnnotations(String doListValues)
  {
    dddQueryRunner.doNodeAnnotations(doListValues);
  }

  public void doMeta(String corpusId)
  {
    dddQueryRunner.doMeta(corpusId);
  }

  ///// Delegates for convenience
  private String translate(String annisQuery)
  {
    return dddQueryMapper.translate(annisQuery);
  }

  ///// Getter / Setter
  public DddQueryMapper getDddQueryMapper()
  {
    return dddQueryMapper;
  }

  public void setDddQueryMapper(DddQueryMapper dddQueryMapper)
  {
    this.dddQueryMapper = dddQueryMapper;
  }

  public AnnisParser getAnnisParser()
  {
    return annisParser;
  }

  public void setAnnisParser(AnnisParser annisParser)
  {
    this.annisParser = annisParser;
  }

  public DddQueryRunner getDddQueryRunner()
  {
    return dddQueryRunner;
  }

  public void setDddQueryRunner(DddQueryRunner dddQueryRunner)
  {
    this.dddQueryRunner = dddQueryRunner;
  }
}
