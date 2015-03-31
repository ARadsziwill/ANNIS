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
package annis.gui.automation;

import annis.automation.AutomatedQueryResult;
import annis.gui.admin.OptionalDateTimeField;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andreas Radsziwill <radsziwill@stud.tu-darmstadt.de>
 */
public class ResultsViewPanel extends VerticalLayout implements ResultsListView
{
  Logger log = LoggerFactory.getLogger(ResultsViewPanel.class);
  
  private final List<ResultsListView.Listener> listeners = new LinkedList<>();
  
  private final BeanContainer<DateTime, AutomatedQueryResult> resultsContainer; 
  
  private final Table tblResults = new Table();
  
  private final OptionalDateTimeField dateDeleteOlder;
  private final Button btnDelete;
  
  public ResultsViewPanel()
  {  
    resultsContainer = new BeanContainer<>(AutomatedQueryResult.class);
    resultsContainer.setBeanIdProperty("executed");
    resultsContainer.addNestedContainerProperty("query.query");
    resultsContainer.addNestedContainerProperty("query.corpora");
    resultsContainer.addNestedContainerProperty("query.description");
        
    tblResults.setEditable(false);
    tblResults.setSelectable(true);
    tblResults.setMultiSelect(true);
    tblResults.setSizeFull();
    tblResults.addStyleName(ChameleonTheme.TABLE_STRIPED);
    tblResults.setContainerDataSource(resultsContainer);
    tblResults.addStyleName("grey-selection");
    
    tblResults.setVisibleColumns("query.query", "query.corpora", "result", "executed", "query.description");
    tblResults.setColumnHeaders("Query", "Corpora", "Result", "executed", "Description");
    
    addComponent(tblResults);
    
    dateDeleteOlder = new OptionalDateTimeField("only delete results older than");
    btnDelete = new Button("Delete selected");
    btnDelete.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        Set<DateTime> exeDates = (Set<DateTime>) tblResults.getValue();
        Set<UUID> ids = new HashSet<>();
                
        if (exeDates == null || exeDates.isEmpty())
        {
          Notification not = new Notification("Nothing selected", "You have to select one of the results that should be deleted",
            Notification.Type.ERROR_MESSAGE, true);
            not.show(getUI().getPage());
            return;
        }
        if (dateDeleteOlder.getValue() == null)
        {
          for (ResultsListView.Listener l : listeners)
          {
            l.deleteResults(exeDates);
          }
        }
        else{
          for (DateTime date : exeDates)
          {
            ids.add(((BeanItem<AutomatedQueryResult>) tblResults.getItem(date)).getBean().getQuery().getId());
          }
          for (ResultsListView.Listener l : listeners)
          {
            l.deleteResults(ids, dateDeleteOlder.getValue());
          }
        }
        for (DateTime date : exeDates)
        {
          tblResults.unselect(date);
        }
      }
    });
    
    addComponent(dateDeleteOlder);
    addComponent(btnDelete);
  }
  
  public void setActiveQuery(UUID id)
  {
      
    Iterator<DateTime> it = resultsContainer.getItemIds().iterator();
    while (it.hasNext())
    {
      DateTime date = it.next();
      AutomatedQueryResult res = ((BeanItem<AutomatedQueryResult>) tblResults.getItem(date)).getBean();
      
      if (res != null && res.getQuery()
        .getId().equals(id))
      {
        tblResults.select(date);
      }
      else
      {
        tblResults.unselect(date);
      }
    }
  }
  
  public void setResults(Collection<AutomatedQueryResult> results)
  {
    this.resultsContainer.removeAllItems();
    resultsContainer.addAll(results);
  }
  
  public void addListener(ResultsListView.Listener listener)
  {
    listeners.add(listener);
  }
}
