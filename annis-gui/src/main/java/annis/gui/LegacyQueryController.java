/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui;

import annis.gui.resultfetch.SingleResultFetchJob;
import annis.gui.beans.HistoryEntry;
import annis.gui.objects.PagedResultQuery;
import annis.gui.paging.PagingCallback;
import annis.gui.resultview.ResultViewPanel;
import annis.gui.resultview.VisualizerContextChanger;
import annis.libgui.PollControl;
import annis.gui.resultfetch.ResultFetchJob;
import annis.service.objects.Match;
import annis.service.objects.MatchAndDocumentCount;
import annis.service.objects.MatchGroup;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.TabSheet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all the query related actions.
 *
 * <strong>This class is not reentrant.</strong>
 * It is expected that you call the functions from the Vaadin session lock
 * context, either implicitly (e.g. from a component constructor or a handler
 * callback) or explicitly with {@link VaadinSession#lock() }.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Deprecated
public class LegacyQueryController implements TabSheet.SelectedTabChangeListener,
  Serializable
{

  private static final Logger log = LoggerFactory.getLogger(
    ResultViewPanel.class);

  private final SearchUI ui;

  private transient Future<?> lastMatchFuture;

  private final ListOrderedSet<HistoryEntry> history;

  private transient Future<MatchAndDocumentCount> futureCount;

  private UUID lastQueryUUID;

  private PagedResultQuery preparedQuery;

  private Map<UUID, PagedResultQuery> queries;

  private BiMap<UUID, ResultViewPanel> resultPanels;

  private Map<UUID, MatchAndDocumentCount> counts;

  /**
   * Stores updated queries. They are created when single results are queried
   * again with a different context.
   */
  private Map<UUID, Map<Integer, PagedResultQuery>> updatedQueries;

  /**
   * Holds the matches from the last query. Useful for repeated queries in order
   * to change the context.
   */
  private MatchGroup matches;

  private int maxShortID;

  private List<CorpusSelectionChangeListener> corpusSelChangeListeners
    = new LinkedList<>();
  
  private final QueryController parent;

  public LegacyQueryController(SearchUI ui)
  {
    this.ui = ui;
    this.parent = ui.getQueryController();
    this.history = new ListOrderedSet<>();
  }
  

  public void updateCorpusSetList()
  {
    ui.getControlPanel().getCorpusList().updateCorpusSetList();
  }


  public void addCorpusSelectionChangeListener(
    CorpusSelectionChangeListener listener)
  {
    if (corpusSelChangeListeners == null)
    {
      corpusSelChangeListeners = new LinkedList<>();
    }
    corpusSelChangeListeners.add(listener);
  }

  public void removeCorpusSelectionChangeListener(
    CorpusSelectionChangeListener listener)
  {
    if (corpusSelChangeListeners == null)
    {
      corpusSelChangeListeners = new LinkedList<>();
    }
    else
    {
      corpusSelChangeListeners.remove(listener);
    }
  }

  private void updateMatches(UUID uuid, PagedResultQuery newQuery)
  {
    ResultViewPanel panel = getResultPanels().get(uuid);
    if (panel != null && preparedQuery != null)
    {
//      prepareExecuteQuery();
      
      getQueries().put(uuid, newQuery);
      
      ui.getControlPanel().getQueryPanel().getPiCount().setVisible(true);
      ui.getControlPanel().getQueryPanel().getPiCount().setEnabled(true);
      
      lastMatchFuture = PollControl.runInBackground(500, ui,
        new ResultFetchJob(newQuery, panel, ui));
    }
  }

  public void corpusSelectionChangedInBackground()
  {
    ui.getControlPanel().getSearchOptions()
      .updateSearchPanelConfigurationInBackground(ui.getControlPanel().
        getCorpusList().
        getSelectedCorpora(), ui);

    // Since there is a serious lag when selecting the corpus don't update
    // the corpus fragment any longer.
    // The user can manually get the corpus link with the corpus explorer.
    //ui.updateFragementWithSelectedCorpus(getSelectedCorpora());

    if (corpusSelChangeListeners != null)
    {

      Set<String> selected = getSelectedCorpora();
      for (CorpusSelectionChangeListener listener : corpusSelChangeListeners)
      {
        listener.onCorpusSelectionChanged(selected);
      }
    }
  }

  @Override
  public void selectedTabChange(TabSheet.SelectedTabChangeEvent event)
  {
    if (event.getTabSheet().getSelectedTab() instanceof ResultViewPanel)
    {
      ResultViewPanel panel = (ResultViewPanel) event.getTabSheet().
        getSelectedTab();
      UUID uuid = getResultPanels().inverse().get(panel);
      if (uuid != null)
      {
        lastQueryUUID = uuid;
        PagedResultQuery query = getQueries().get(uuid);
        if (query != null)
        {
          ui.updateFragment(query);
        }
      }
    }
  }

  public Set<String> getSelectedCorpora()
  {
    return ui.getControlPanel().getCorpusList().getSelectedCorpora();
  }

  /**
   * Clear the collected informations about a certain query. Also remove any
   * attached {@link ResultViewPanel} for that query.
   *
   * @param uuid The UUID of the query to remove.
   */
  private void removeQuery(UUID uuid)
  {
    if (uuid != null)
    {
      getQueries().remove(uuid);
      getResultPanels().remove(uuid);
      getCounts().remove(uuid);
    }
  }

  public void notifyTabClose(ResultViewPanel panel)
  {
    if (panel != null)
    {
      removeQuery(getResultPanels().inverse().get(panel));
    }
  }

  public String getQueryDraft()
  {
    return ui.getControlPanel().getQueryPanel().getQuery();
  }

  private Map<UUID, PagedResultQuery> getQueries()
  {
    if (queries == null)
    {
      queries = new HashMap<>();
    }
    return queries;
  }

  private BiMap<UUID, ResultViewPanel> getResultPanels()
  {
    if (resultPanels == null)
    {
      resultPanels = HashBiMap.create();
    }
    return resultPanels;
  }

  private Map<UUID, MatchAndDocumentCount> getCounts()
  {
    if (counts == null)
    {
      counts = new HashMap<>();
    }
    return counts;
  }

  private class SpecificPagingCallback implements PagingCallback
  {

    private final UUID uuid;

    public SpecificPagingCallback(UUID uuid)
    {
      this.uuid = uuid;
    }

    @Override
    public void switchPage(int offset, int limit)
    {
      PagedResultQuery query = getQueries().get(uuid);
      if (query != null)
      {
        query.setOffset(offset);
        query.setLimit(limit);

        // execute the result query again
        updateMatches(uuid, query);
      }
    }
  }

  

  public void changeCtx(UUID queryID, int offset, int context,
    VisualizerContextChanger visCtxChange, boolean left)
  {

    PagedResultQuery query;

    if (queries.containsKey(queryID) && resultPanels.containsKey(queryID))
    {
      if (updatedQueries == null)
      {
        updatedQueries = new HashMap<>();
      }

      if (!updatedQueries.containsKey(queryID))
      {
        updatedQueries.put(queryID, new HashMap<Integer, PagedResultQuery>());
      }

      if (!updatedQueries.get(queryID).containsKey(offset))
      {
        query = (PagedResultQuery) queries.get(queryID).clone();
        updatedQueries.get(queryID).put(offset, query);
      }
      else
      {
        query = updatedQueries.get(queryID).get(offset);
      }

      if (left)
      {
        query.setContextLeft(context);
      }
      else
      {
        query.setContextRight(context);
      }

      query.setOffset(offset);
      query.setLimit(1);

      if (matches != null && matches.getMatches() != null
        && !matches.getMatches().isEmpty())
      {
        // The size is the match list corresponds to the page size of the 
        // result view, thus we can make an index shift to the right position of 
        // match in the match list via modulo of size of the match list.
        List<Match> extractMatches = matches.getMatches();
        Match m = extractMatches.get(offset % extractMatches.size());

        PollControl.runInBackground(500, ui,
          new SingleResultFetchJob(m, query,
            visCtxChange));
      }
    }
    else
    {
      log.warn("no query with {} found");
      return;
    }
  }

  public void setMatches(MatchGroup matches)
  {
    this.matches = matches;
  }
}
