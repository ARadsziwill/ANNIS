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


import java.util.UUID;
import annis.automation.AutomatedQuery;
import annis.gui.CorpusSelectionChangeListener;
import annis.gui.QueryController;
import annis.gui.SearchUI;
import annis.gui.admin.PopupTwinColumnSelect;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import it.sauronsoftware.cron4j.Predictor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import it.sauronsoftware.cron4j.SchedulingPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andreas Radsziwill <radsziwill@stud.tu-darmstadt.de>
 */
public class QueryAutomationPanel extends VerticalLayout implements TextChangeListener, QueryListView, CorpusSelectionChangeListener
{  
  private final Logger log = LoggerFactory.getLogger(QueryAutomationPanel.class);
  
  private final List<QueryListView.Listener> listeners = new LinkedList<>();
 
  private final BeanContainer<UUID, AutomatedQuery> queriesContainer;
  
  private final IndexedContainer groupsContainer = new IndexedContainer();
 
  
  //newQuery data
  private TextField query = new TextField();
  private final Set<String> corpora;
  private boolean isGroup = false;
  private String group;
  
  //UI Components
  private final Label lblSelectedCorpora;
  private final Label lblQuery;
  private final TextArea txtDescription;
  
  private final TextField schedulingPattern;
  private final Label lblNextExecution;
 
  
 // private final SchedulingPatternWidget patternBuilder;
  
 
  private final CheckBox chkIsGroup;
  private final ComboBox selGroup;
  private final CheckBox chkIsActive;
  
  
  private final Button btnSubmit;
  private final Button btnReset;  
 
  private final Label lblStatus;
  /*
  private final Table tblQueries = new Table(); 
  */
  
 public QueryAutomationPanel(final QueryController queryController) 
 {
   setWidth("99%");
   setHeight("99%");
   setMargin(true);
   
   //Setup Data
   corpora = new TreeSet<>();
   corpora.addAll(queryController.getSelectedCorpora());
   
   queriesContainer = new BeanContainer<>(AutomatedQuery.class);
   queriesContainer.setBeanIdProperty("id");
   
   query.setValue(queryController.getQueryDraft());
   
   //Setup GUI Components
   
   HorizontalLayout editLayout = new HorizontalLayout();
   editLayout.setSpacing(true);
   editLayout.setWidth("100%");
   editLayout.setHeight("-1px");
   
   //Query Info
   VerticalLayout firstColumn = new VerticalLayout();
   
   lblSelectedCorpora = new Label();
   lblSelectedCorpora.setCaption("Selected corpora");
   lblSelectedCorpora.setValue(StringUtils.join(corpora, ", "));
   firstColumn.addComponent(lblSelectedCorpora);
   
   lblQuery = new Label();
   lblQuery.setCaption("Query to analyze");
   lblQuery.setStyleName("corpus-font-force");
   lblQuery.setPropertyDataSource(query);
   firstColumn.addComponent(lblQuery);
   
   txtDescription = new TextArea();
   txtDescription.setCaption("Description");
   
   firstColumn.addComponent(txtDescription);
   
   //Scheduling pattern setting
   VerticalLayout secondColumn = new VerticalLayout();
   
   schedulingPattern = new TextField();
   schedulingPattern.setCaption("Scheduling Pattern:");
   schedulingPattern.setValue("");
   schedulingPattern.addTextChangeListener(new TextChangeListener()
   {

     @Override
     public void textChange(TextChangeEvent event)
     {
       if (SchedulingPattern.validate(event.getText()))
        {
          Predictor p = new Predictor(event.getText());
          StringBuilder executions = new StringBuilder();

             for(int i = 0; i < 5; i++)
             {
               executions.append(p.nextMatchingDate());
               executions.append("\n");
             }

          lblNextExecution.setValue(executions.toString());
          btnSubmit.setEnabled(true);
        }
          else
        { 
          lblNextExecution.setValue("Scheduling Pattern invalid.");
          btnSubmit.setEnabled(false);
        }
     }
   });
   
   lblNextExecution = new Label();
   lblNextExecution.setCaption("Next 5 executions: ");
   lblNextExecution.setContentMode(ContentMode.PREFORMATTED);
   lblNextExecution.setValue("");
   
   secondColumn.addComponent(schedulingPattern);
   secondColumn.addComponent(lblNextExecution);
   
   //Owner and Active setting
   VerticalLayout thirdColumn = new VerticalLayout();

   chkIsGroup = new CheckBox();
   chkIsGroup.setCaption("assign to group");
   chkIsGroup.addValueChangeListener(new Property.ValueChangeListener()
   {

     @Override
     public void valueChange(Property.ValueChangeEvent event)
     {
       if(!(Boolean) event.getProperty().getValue())
       {
         selGroup.setValue(null); 
         isGroup = false;
       }
       else 
       {
         isGroup = true;
       }
         selGroup.setEnabled(isGroup);
     }
   });
   
   selGroup = new ComboBox("Group", groupsContainer);
   selGroup.setEnabled(isGroup);
   selGroup.addBlurListener(new FieldEvents.BlurListener()
   {

     @Override
     public void blur(FieldEvents.BlurEvent event)
     {
       group = (String) selGroup.getValue();
     }
   });
   
   chkIsActive = new CheckBox("is active?");
   
   thirdColumn.addComponent(chkIsGroup);
   thirdColumn.addComponent(selGroup);
   thirdColumn.addComponent(chkIsActive);
   
   VerticalLayout fourthColumn = new VerticalLayout();
      

//Reset Button
   btnReset = new Button();
   btnReset.setCaption("Reset data");
   btnReset.addClickListener(new Button.ClickListener()
   {

     @Override
     public void buttonClick(Button.ClickEvent event)
     {
       emptyNewQueryInputFields();
     }
   });
      //Submit Button
   btnSubmit = new Button();
   btnSubmit.setCaption("Save query");
   btnSubmit.setEnabled(false);
   btnSubmit.addClickListener(new Button.ClickListener()
   {

     @Override
     public void buttonClick(Button.ClickEvent event)
     {
       for (QueryListView.Listener l : listeners)
     {
       //TODO group name
       
       l.addNewQuery(new AutomatedQuery(query.getValue(), new LinkedList<>(corpora), schedulingPattern.getValue(), txtDescription.getValue(),
        group, isGroup, chkIsActive.getValue()));
     }
     }
   });
   fourthColumn.addComponent(btnReset);
   fourthColumn.addComponent(btnSubmit);
   
   editLayout.addComponent(firstColumn);
   editLayout.addComponent(secondColumn);
   editLayout.addComponent(thirdColumn);
   editLayout.addComponent(fourthColumn);
   
   addComponent(editLayout);
   
   lblStatus = new Label();
   lblStatus.setContentMode(ContentMode.PREFORMATTED);
   lblStatus.setValue("Enter your query.");
   
   addComponent(lblStatus);
   /*
   tblQueries.setContainerDataSource(queriesContainer);
   tblQueries.setEditable(true);
   tblQueries.setSelectable(true);
   tblQueries.setMultiSelect(true);
   tblQueries.setSizeFull();
   tblQueries.addStyleName(ChameleonTheme.TABLE_STRIPED);
   tblQueries.addStyleName("grey-selection");
   */
  
 }
 
  @Override
  public void setStatus(String message)
  {
    lblStatus.setValue(message);
  }
 
  @Override
  public void textChange(TextChangeEvent event)
  {
    //query Text in Panel changed
    query.setValue(event.getText());    
  }

  @Override
  public void addListener(QueryListView.Listener listener)
  {
      listeners.add(listener);
  }

  @Override
  public void setQueryList(Collection<AutomatedQuery> queries)
  {
    queriesContainer.removeAllItems();
    queriesContainer.addAll(queries);
  }

  @Override
  public void emptyNewQueryInputFields()
  {
    schedulingPattern.setValue("");
    chkIsGroup.setValue(false);
    chkIsActive.setValue(false);
  }


  @Override
  public void onCorpusSelectionChanged(Set<String> selectedCorpora)
  {
    corpora.clear();
    corpora.addAll(selectedCorpora);
    lblSelectedCorpora.setValue(StringUtils.join((corpora), ", "));
  }

  @Override
  public void setAvailableGroups(Collection<String> groups)
  {
    groupsContainer.removeAllItems();
    for (String group : groups)
    {
      groupsContainer.addItem(group);
    }    
  }
}
