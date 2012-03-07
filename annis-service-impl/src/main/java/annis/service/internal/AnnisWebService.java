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
package annis.service.internal;

import static java.util.Arrays.asList;
import annis.WekaHelper;
import annis.dao.AnnisDao;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.AnnisCorpus;
import annis.sqlgen.AnnotateSqlGenerator.AnnotateQueryData;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 *
 * @author thomas
 */
@Component
@Path("/annis")
public class AnnisWebService
{

  private final static Logger log = Logger.getLogger(AnnisWebService.class);
  private static Logger queryLog = Logger.getLogger("QueryLog");
  private AnnisDao annisDao;
  private WekaHelper wekaHelper;
  private int maxContext = 10;
  private int port = 5711;

  /**
   * Log the successful initialization of this bean.
   *
   * <p>
   * XXX: This should be a private method annotated with <tt>@PostConstruct</tt>, but
   * that doesn't seem to work.  As a work-around, the method is called
   * by Spring as an init-method.
   */
  public void sayHello()
  {
    // log a message after successful startup
    log.info("AnnisWebService loaded.");
  }

  @GET
  @Path("search/count")
  @Produces("plain/text")
  public Response count(@QueryParam("q") String query,
    @QueryParam("corpora") String rawCorpusNames)
  {

    if (query == null)
    {
      return Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required parameter 'q'").build();
    }
    if (rawCorpusNames == null)
    {
      return Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required parameter 'corpora'").build();
    }

    List<String> corpusNames = Arrays.asList(rawCorpusNames.split(","));
    List<Long> corpusIDs = annisDao.listCorpusByName(
      corpusNames);
    if (corpusIDs.size() != corpusNames.size())
    {
      return Response.status(Response.Status.NOT_FOUND).type(
        "text/plain").entity("one ore more corpora are unknown to the system").
        build();
    }
    QueryData data = annisDao.parseAQL(query, corpusIDs);
    long start = new Date().getTime();
    int count = annisDao.count(data);
    long end = new Date().getTime();
    logQuery("COUNT", query, corpusNames, end - start);
    return Response.ok("" + count).type(MediaType.TEXT_PLAIN).build();

  }

  @GET
  @Path("search/annotate")
  @Produces("application/xml")
  public SaltProject annotate(@QueryParam("q") String query,
    @QueryParam("corpora") String rawCorpusNames,
    @DefaultValue("0") @QueryParam("offset") String offsetRaw,
    @DefaultValue("10") @QueryParam("limit") String limitRaw,
    @DefaultValue("5") @QueryParam("left") String leftRaw,
    @DefaultValue("5") @QueryParam("right") String rightRaw) throws IOException
  {
    if (query == null)
    {
      throw new WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required parameter 'q'").build());
    }
    if (rawCorpusNames == null)
    {
      throw new WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required parameter 'corpora'").build());
    }

    int offset = Integer.parseInt(offsetRaw);
    int limit = Integer.parseInt(limitRaw);
    int left = Math.min(maxContext, Integer.parseInt(leftRaw));
    int right = Math.min(maxContext, Integer.parseInt(rightRaw));

    List<String> corpusNames = Arrays.asList(rawCorpusNames.split(","));
    List<Long> corpusIDs = annisDao.listCorpusByName(
      corpusNames);
    if (corpusIDs.size() != corpusNames.size())
    {
      throw new WebApplicationException(
        Response.status(Response.Status.NOT_FOUND).type(
        "text/plain").entity("one ore more corpora are unknown to the system").
        build());
    }
    
    String logParameters = createAnnotateLogParameters(left, right, offset,
        limit);
    
    QueryData data = annisDao.parseAQL(query, corpusIDs);
    data.addExtension(new AnnotateQueryData(offset, limit, left,
      right));
    long start = new Date().getTime();
    SaltProject p = annisDao.annotate(data);
    long end = new Date().getTime();
    logQuery("ANNOTATE", query, corpusNames, end - start, logParameters);
    return p;

  }

  @GET
  @Path("graphs/{top}/{doc}")
  @Produces("application/xml")
  public SaltProject graph(@PathParam("top") String toplevelCorpusName,
    @PathParam("doc") String documentName)
  {
    try
    {
      long start = new Date().getTime();
      SaltProject p = annisDao.retrieveAnnotationGraph(toplevelCorpusName,
        documentName);
      long end = new Date().getTime();
      logQuery("GRAPH", toplevelCorpusName, documentName, end - start);
      return p;
    }
    catch (Exception ex)
    {
      log.error("error when accessing graph " + toplevelCorpusName + "/"
          + documentName, ex);
      throw new WebApplicationException(ex);
    }
  }
  
  @GET
  @Path("resolverentry")
  public List<ResolverEntry> resolverEntry(@QueryParam("request") List<SingleResolverRequest> request)
  {
    return annisDao.getResolverEntries(request.toArray(new SingleResolverRequest[0]));
  }
  
  @GET
  @Path("corpora")
  public List<AnnisCorpus> corpora()
  {
    return annisDao.listCorpora();
  }

  private String createAnnotateLogParameters(int left, int right, int offset,
      int limit)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("left: ");
    sb.append(left);
    sb.append(", ");
    sb.append("right: ");
    sb.append(right);
    sb.append(", ");
    sb.append("offset: ");
    sb.append(offset);
    sb.append(", ");
    sb.append("limit: ");
    sb.append(limit);
    String logParameters = sb.toString();
    return logParameters;
  }

  private void logQuery(String queryFunction, String toplevelCorpus,
      String documentName, long runtime)
  {
    logQuery(queryFunction, null, asList(toplevelCorpus), runtime, "document: "
        + documentName);
  }

  private void logQuery(String queryFunction, String annisQuery,
      List<String> corpusNames, long runtime)
  {
    logQuery(queryFunction, annisQuery, corpusNames, runtime, null);
  }

  private void logQuery(String queryFunction, String annisQuery,
      List<String> corpusNames, long runtime, String options)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("function: ");
    sb.append(queryFunction);
    sb.append(", ");
    if (annisQuery != null && ! annisQuery.isEmpty())
    {
      sb.append("query: ");
      sb.append(annisQuery);
      sb.append(", ");
    }
    sb.append("corpus: ");
    sb.append(corpusNames);
    sb.append(", ");
    sb.append("runtime: ");
    sb.append(runtime);
    sb.append(" ms");
    if (options != null && !options.isEmpty())
    {
      sb.append(", ");
      sb.append(options);
    }
    String message = sb.toString();
    queryLog.info(message);
  }

  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }

  public int getMaxContext()
  {
    return maxContext;
  }

  public void setMaxContext(int maxContext)
  {
    this.maxContext = maxContext;
  }

  public WekaHelper getWekaHelper()
  {
    return wekaHelper;
  }

  public void setWekaHelper(WekaHelper wekaHelper)
  {
    this.wekaHelper = wekaHelper;
  }

  public int getPort()
  {
    return port;
  }

  public void setPort(int port)
  {
    this.port = port;
  }
}
