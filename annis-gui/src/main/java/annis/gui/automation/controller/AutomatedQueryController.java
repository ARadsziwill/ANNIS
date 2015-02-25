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
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andreas Radsziwill <radsziwill@stud.tu-darmstadt.de>
 */
public class AutomatedQueryController implements QueryListView.Listener
{

    private final Logger log = LoggerFactory.getLogger(AutomatedQueryController.class);
    
    private final AutomatedQueryManagement model;
    private final GroupManagement groupModel;
    private final CorpusManagement corpusModel;
    private final QueryListView view;
    private final SearchUI ui;
    
    
    
  public AutomatedQueryController(AutomatedQueryManagement model, GroupManagement groupModel, CorpusManagement corpusModel, QueryListView view, SearchUI ui)
  {
    this.model = model;
    this.view = view;
    this.ui = ui;
    this.groupModel = groupModel;
    this.corpusModel = corpusModel;
    view.setAvailableGroups(groupModel.getGroupNames());
    model.fetchFromService();
    view.setQueryList(model.getQueries());
    view.setAvailableCorpusNames(corpusModel.getCorpusNames());
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
  }

  @Override
  public void deleteQueries(Set<UUID> queryIds)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
