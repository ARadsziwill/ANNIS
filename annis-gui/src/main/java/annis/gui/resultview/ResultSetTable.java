/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.resultview;

import annis.CommonHelper;
import annis.gui.Helper;
import annis.gui.PluginSystem;
import annis.resolver.ResolverEntry;
import annis.resolver.ResolverEntry.ElementType;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.Match;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author thomas
 */
public class ResultSetTable extends Table implements ResolverProvider
{

  private Map<HashSet<SingleResolverRequest>, List<ResolverEntry>> cacheResolver;
  public static final String FILESYSTEM_CACHE_RESULT =
    "ResultSetPanel_FILESYSTEM_CACHE_RESULT";
  
  private BeanItemContainer<Match> container;
  private List<SingleResultPanel> resultPanelList;
  private PluginSystem ps;
  private String segmentationName;
  private int start;
  private int contextLeft;
  private int contextRight;
  private ResultViewPanel parent;
  
  private Set<String> tokenAnnotationLevelSet = 
    Collections.synchronizedSet(new HashSet<String>());
  
  public ResultSetTable(List<Match> matches, int start, PluginSystem ps,
    int contextLeft, int contextRight, 
    String segmentationName,
    ResultViewPanel parent)
  {
    this.ps = ps;
    this.segmentationName = segmentationName;
    this.start = start;
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
    this.parent = parent;
    
    resultPanelList = new LinkedList<SingleResultPanel>();
    cacheResolver =
      Collections.synchronizedMap(
      new HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>>());

    setSizeFull();

    addStyleName("result-view");

    container = new BeanItemContainer<Match>(Match.class, matches);
    
    setContainerDataSource(container);
    setPageLength(3);
    
    addGeneratedColumn("kwic",  new KWICColumnGenerator(this));
    
    setVisibleColumns(new String[] {"kwic"});
    setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    setRowHeaderMode(Table.ROW_HEADER_MODE_HIDDEN);
    
  }

  @Override
  public ResolverEntry[] getResolverEntries(SDocument doc)
  {
    HashSet<ResolverEntry> visSet = new HashSet<ResolverEntry>();

    // create a request for resolver entries
    HashSet<SingleResolverRequest> resolverRequests =
      new HashSet<SingleResolverRequest>();

    Set<String> nodeLayers = new HashSet<String>();
    
    for (SNode n : doc.getSDocumentGraph().getSNodes())
    {
      for (SLayer layer : n.getSLayers())
      {
        nodeLayers.add(layer.getSName());
      }
    }

    Set<String> edgeLayers = new HashSet<String>();
    for (SRelation e : doc.getSDocumentGraph().getSRelations())
    {
      for (SLayer layer : e.getSLayers())
      {
        try
        {
          edgeLayers.add(layer.getSName());
        }
        catch (NullPointerException ex)
        {
          Logger.getLogger(ResultSetTable.class.getName()).log(Level.WARNING,
            "NullPointerException when using Salt, was trying to get layer name",
            ex);
        }
      }
    }

    for (String ns : nodeLayers)
    {
      resolverRequests.add(new SingleResolverRequest(doc.getSCorpusGraph().
        getSRootCorpus().get(0).getSName(), ns,
        ElementType.node));
    }
    for (String ns : edgeLayers)
    {
      resolverRequests.add(new SingleResolverRequest(doc.getSCorpusGraph().
        getSRootCorpus().get(0).getSName(), ns,
        ElementType.edge));
    }

    // query with this resolver request and make sure it is unique
    if (cacheResolver.containsKey(resolverRequests))
    {
      visSet.addAll(cacheResolver.get(resolverRequests));
    }
    else
    {
      List<ResolverEntry> resolverList = new LinkedList<ResolverEntry>();

      WebResource resResolver = Helper.getAnnisWebResource(getApplication()).
        path("resolver");

      for (SingleResolverRequest r : resolverRequests)
      {
        List<ResolverEntry> tmp;
        try
        {
          String corpusName = URLEncoder.encode(r.getCorpusName(), "UTF-8");
          String namespace = r.getNamespace();
          String type = r.getType() == null ? null : r.getType().toString();
          if(corpusName != null && namespace != null && type != null)
          {
            WebResource res = resResolver.path(corpusName).path(namespace).path(type);
            try
            {
              tmp = res.get(new GenericType<List<ResolverEntry>>(){});
              resolverList.addAll(tmp);
            }
            catch(Exception ex)
            {
               Logger.getLogger(ResultSetTable.class.getName())
            .log(Level.SEVERE, "could not query resolver entries: " 
                 + res.toString(), ex);
            }
          }
        }
        catch (Exception ex)
        {
          Logger.getLogger(ResultSetTable.class.getName())
            .log(Level.SEVERE, null, ex);
        }
      }
      visSet.addAll(resolverList);
      cacheResolver.put(resolverRequests, resolverList);
    }
    // sort everything
    ResolverEntry[] visArray = visSet.toArray(new ResolverEntry[0]);
    Arrays.sort(visArray, new Comparator<ResolverEntry>()
    {

      @Override
      public int compare(ResolverEntry o1, ResolverEntry o2)
      {
        if (o1.getOrder() < o2.getOrder())
        {
          return -1;
        }
        else if (o1.getOrder() > o2.getOrder())
        {
          return 1;
        }
        else
        {
          return 0;
        }
      }
    });
    return visArray;
  }
  
  public void setSegmentationLayer(String segmentationLayer)
  {
    for(SingleResultPanel p : resultPanelList)
    {
      p.setSegmentationLayer(segmentationLayer);
    }
  }

  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {
    for (SingleResultPanel p : resultPanelList)
    {
      p.setVisibleTokenAnnosVisible(annos);
    }
  }

  
  public class KWICColumnGenerator implements ColumnGenerator
  {
    private ResolverProvider rsProvider;

    public KWICColumnGenerator(ResolverProvider rsProvider)
    {
      this.rsProvider = rsProvider;
    }
    
    
    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      Match m = (Match) itemId;
      
      int resultNumber = container.indexOfId(itemId) + start;
      
      // get subgraph for match
      WebResource res;
      synchronized(getApplication())
      {
        res = Helper.getAnnisWebResource(source.getApplication());
      }
      
      if(res != null)
      {
        res = res.path("search/subgraph")
          .queryParam("q", StringUtils.join(m.getSaltIDs(), ""))
          .queryParam("left", "" + contextLeft)
          .queryParam("right","" + contextRight);
        
        if(segmentationName != null)
        {
          res = res.queryParam("seglayer", segmentationName);
        }
        
        SaltProject p = res.get(SaltProject.class);
        
        tokenAnnotationLevelSet.addAll(CommonHelper.getTokenAnnotationLevelSet(p));
        parent.updateTokenAnnos(tokenAnnotationLevelSet);

        SingleResultPanel resultPanel = new SingleResultPanel(
          p.getSCorpusGraphs().get(0).getSDocuments().get(0),
          resultNumber, rsProvider, ps, parent.getVisibleTokenAnnos(), segmentationName
        );
        
        resultPanelList.add(resultPanel);
        
        
        return resultPanel;
      }
      
      return new Label("ERROR: could not get result from web service");
    }
    
  }
}
