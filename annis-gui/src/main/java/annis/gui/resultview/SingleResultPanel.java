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

import annis.gui.PluginSystem;
import annis.gui.Helper;
import annis.gui.MatchedNodeColors;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.resolver.ResolverEntry;
import annis.service.AnnisService;
import annis.service.ifaces.AnnisResult;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class SingleResultPanel extends Panel
{
  private AnnisResult result;
  private Map<AnnisNode,Long> markedAndCovered;
  private Map<String,String> markedCoveredMap;
  private Map<String,String> markedExactMap;
  
  private Set<String> tokenAnnos;
  private Set<String> nodeAnnos;
  private VerticalLayout vLayout;
  private ResolverProvider resolverProvider;
  private PluginSystem ps;
  private KWICPanel kwic;
    
  public SingleResultPanel(AnnisResult result, int resultNumber, 
    ResolverProvider resolverProvider, PluginSystem ps)
  {
    this.ps = ps;
    this.result = result;
    this.resolverProvider = resolverProvider;
    
    calculateHelperVariables();
    
    setWidth("100%");
    setHeight("-1px");
       
    setScrollable(true);
    
    HorizontalLayout hLayout = new HorizontalLayout();
    setContent(hLayout);
        
    hLayout.setWidth("100%");
    hLayout.setHeight("-1px");
    hLayout.setMargin(true);
    hLayout.setSpacing(true);
    
    Label lblNumber = new Label("" + (resultNumber+1));
    hLayout.addComponent(lblNumber);
    lblNumber.setSizeUndefined();
    
    vLayout = new VerticalLayout();
    hLayout.addComponent(vLayout);
    
    
    kwic = new KWICPanel(result, tokenAnnos, markedAndCovered);
    vLayout.addComponent(kwic);
    
    vLayout.setWidth("100%");
    vLayout.setHeight("-1px");
    vLayout.setMargin(false);
    vLayout.setSpacing(false);
    
    hLayout.setExpandRatio(vLayout, 1.0f);
  }

  @Override
  public void attach()
  {    
    AnnisService service = Helper.getService(getApplication(), getWindow());
    if(service != null && resolverProvider != null)
    {
      try
      {
        ResolverEntry[] entries = 
          resolverProvider.getResolverEntries(result, service);
        for(ResolverEntry e : entries)
        {
          vLayout.addComponent(new VisualizerPanel(e, result, ps, markedExactMap, 
            markedCoveredMap));
        }
      }
      catch(RemoteException ex)
      {
        Logger.getLogger(SingleResultPanel.class.getName()).log(Level.SEVERE, 
          "could not get resolver entries", ex);
        getWindow().showNotification("could not get resolver entries: " + 
          ex.getLocalizedMessage(), Notification.TYPE_TRAY_NOTIFICATION);
      }
    }
    
    super.attach();
  }
  
  public void setTokenAnnosVisible(String anno, boolean visible)
  {
    if(kwic != null)
    {
      kwic.setTokenAnnosVisible(anno, visible);
    }
  }
  
  private void calculateHelperVariables()
  {
    nodeAnnos = new TreeSet<String>();
    tokenAnnos = new TreeSet<String>();
    
    markedExactMap = new HashMap<String, String>();
    markedCoveredMap = new HashMap<String, String>();
    
    for(AnnisNode n : result.getGraph().getNodes())
    {
      Long match = n.getMatchedNodeInQuery();
      if(match != null)
      {     
        int color = Math.max(0, Math.min((int) match.longValue()-1, 
          MatchedNodeColors.values().length-1));
        markedExactMap.put("" + n.getId(), 
          MatchedNodeColors.values()[color].name());
      }
      // add to annotation overview      
      for(Annotation a : n.getNodeAnnotations())
      {
        nodeAnnos.add(a.getQualifiedName());
        if(n.isToken())
        {
          tokenAnnos.add(a.getQualifiedName());
        }
      }      
    }
    
    markedAndCovered = calculateMarkedAndCoveredIDs(result.getGraph());
    
    for(Entry<AnnisNode,Long> markedEntry : markedAndCovered.entrySet())
    {
      int color = Math.max(0, Math.min((int) markedEntry.getValue().longValue()-1, 
          MatchedNodeColors.values().length-1));
      markedCoveredMap.put("" + markedEntry.getKey().getId(),
        MatchedNodeColors.values()[color].name());
    }
  }
  
  private Map<AnnisNode,Long> calculateMarkedAndCoveredIDs(AnnotationGraph graph)
  {
    Set<Long> matchedNodes = graph.getMatchedNodeIds();
    Map<AnnisNode,Long> matchedAndCovered = new HashMap<AnnisNode, Long>();

    // add all covered nodes
    for (AnnisNode n : graph.getNodes())
    {
      if (matchedNodes.contains(n.getId()) && n.getMatchedNodeInQuery() != null)
      {
        Long matchPosition = n.getMatchedNodeInQuery();
        matchedAndCovered.put(n,matchPosition);

        long left = n.getLeftToken();
        long right = n.getRightToken();

        for (long i = left; i <= right; i++)
        {
          AnnisNode tok = graph.getToken(i);
          Long oldTokenPosition = matchedAndCovered.get(tok);
          if(oldTokenPosition == null 
            || (!matchedNodes.contains(tok.getId()) && matchPosition.compareTo(oldTokenPosition) >= 0) )
          {
            matchedAndCovered.put(tok, matchPosition);
          }
        }
      }
    }

    return matchedAndCovered;
  }
 
 
  
}
