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

import annis.automation.AutomatedQuery;
import annis.gui.SearchUI;
import annis.gui.admin.model.CorpusManagement;
import annis.gui.admin.model.GroupManagement;
import annis.gui.admin.view.GroupListView;
import annis.gui.admin.view.UIView;
import annis.gui.admin.view.UserListView;
import annis.gui.automation.QueryListView;
import annis.gui.automation.model.AutomatedQueryManagement;
import annis.security.Group;
import annis.security.User;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.TabSheet;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andreas Radsziwill <radsziwill@stud.tu-darmstadt.de>
 */
public class AutomatedQueryController implements QueryListView.Listener, TabSheet.SelectedTabChangeListener
{

    private final Logger log = LoggerFactory.getLogger(AutomatedQueryController.class);
    
    private final AutomatedQueryManagement model;
    private final GroupManagement groupModel;
    private final QueryListView view;
    private final SearchUI ui;
    
    
    
  public AutomatedQueryController(AutomatedQueryManagement model, GroupManagement groupModel, QueryListView view, SearchUI ui)
  {
    this.model = model;
    this.view = view;
    this.ui = ui;
    this.groupModel = groupModel;
    updateView();
  }
  
  private void clearModel()
  {
    model.clear();
    view.setQueryList(model.getQueries());
  }

  
  @Override
  public void queryUpdated(AutomatedQuery query)
  {
    if(model.updateAutomatedQuery(query))
    {
      view.setStatus("Query saved");
    }
    else
    {
      view.setStatus("Query not saved");
    }
    updateView();
  }

  @Override
  public void addNewQuery(AutomatedQuery query)
  {
    if(model.createAutomatedQuery(query))
    {
      view.setStatus("Query created."); 
      view.emptyNewQueryInputFields();
    }
    else 
    {
      view.setStatus("Query not created");
    }
    updateView();
  }

  @Override
  public void deleteQueries(Set<UUID> queryIds)
  {
    StringBuilder status = new StringBuilder("Deleting Queries: ");
    StringBuilder deleted = new StringBuilder("Deleted: ");
    StringBuilder notDeleted = new StringBuilder("Not deleted: ");
    for (UUID id : queryIds)
    {
      status.append(id + "\t");
      if (model.deleteQuery(id))
      {
        deleted.append(id + "\t");
      }
      else
      {
        notDeleted.append(id + "\t");
      }
    }
    status.append("\n");
    status.append(deleted);
    status.append("\n");
    status.append(notDeleted);
    view.setStatus(status.toString());
    updateView();    
  }
  
  private void updateView()
  {
    model.fetchFromService();
    view.setQueryList(model.getQueries());
    view.setAvailableCorpusNames(ui.getControlPanel().getCorpusList().getVisibleCorpora());
    view.setAvailableGroups(groupModel.getGroupNames());
    view.setQueryAndCorpora(ui.getQueryController());
  }

  @Override
  public void selectedTabChange(TabSheet.SelectedTabChangeEvent event)
  {
    if (event.getTabSheet().getSelectedTab().equals(view))
    {
      updateView();
    }
  }
}
