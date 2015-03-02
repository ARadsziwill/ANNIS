/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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

package annis.gui.automation.controller;

import annis.gui.automation.ResultsListView;
import annis.gui.automation.model.AutomatedQueryResultsManagement;
import com.vaadin.ui.TabSheet;
import java.util.Collection;
import java.util.UUID;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andreas
 */
public class AutomatedQueryResultsController implements ResultsListView.Listener, TabSheet.SelectedTabChangeListener 
{

    private final Logger log = LoggerFactory.getLogger(AutomatedQueryResultsController.class);
    
    private final AutomatedQueryResultsManagement model;
    private final ResultsListView view;

  public AutomatedQueryResultsController(ResultsListView view, AutomatedQueryResultsManagement model)
  {
    this.model = model;
    this.view = view;
    fetchFromService();
  }
        
  @Override
  public void deleteResults(Collection<UUID> ids, DateTime date)
  {
    log.info("deleting: " + ids + " older than " + date);
    model.deleteResults(ids, date);
    fetchFromService();
  }
    
  public void fetchFromService()
  {
    model.fetchFromService();
    view.setResults(model.getResults());
  }

  @Override
  public void selectedTabChange(TabSheet.SelectedTabChangeEvent event)
  {
    if (event.getTabSheet().getSelectedTab().equals(view))
    {
      fetchFromService();
    }
  }
    
}
