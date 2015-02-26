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
import annis.gui.admin.converter.CommaSeperatedStringConverter;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
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
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andreas Radsziwill <radsziwill@stud.tu-darmstadt.de>
 */
public class QueryAutomationPanel extends VerticalLayout implements TextChangeListener, QueryListView, CorpusSelectionChangeListener
{ 
  private static final String SCHEDULING_INFO = "Please enter a valid cron pattern formated like this:\n"
    + "'mm hh dd MM ww' where "
    + "'mm' denotes the minutes, "
    + "'hh' dentotes the hours, "
    + "'dd' denotes the days in the month, "
    + "'MM' denotes the months in the year, and"
    + "'ww' denotes the weekdays the query should be exectuted.";
  private final static String CRON4JURL = "<a href='http://www.sauronsoftware.it/projects/cron4j/manual.php#p02' target='_blank'> More info</a>";
  private final Logger log = LoggerFactory.getLogger(QueryAutomationPanel.class);
  
  private final List<QueryListView.Listener> listeners = new LinkedList<>();
 
  private final BeanContainer<UUID, AutomatedQuery> queriesContainer;
  
  private final IndexedContainer groupsContainer = new IndexedContainer();
  private final IndexedContainer corpusContainer = new IndexedContainer();
  
  //newQuery data
  private TextField query = new TextField();
  private final TreeSet<String> corpora = new TreeSet<>();
  private boolean isGroup = false;
  private String group;
  
  //UI Components
  private final Label lblSelectedCorpora;
  private final Label lblQuery;
  private final TextArea txtDescription;
  
  private final TextField schedulingPattern;
  private final TextArea txtNextExecution;
 
  
 // private final SchedulingPatternWidget patternBuilder;
  
 
  private final CheckBox chkIsGroup;
  private final ComboBox selGroup;
  private final CheckBox chkIsActive;
  
  
  private final Button btnSubmit;
  private final Button btnReset;  
 
  private final Label lblStatus;
  
  private final Table tblQueries = new Table(); 
  
  private final Button btnDelete;
  
 public QueryAutomationPanel(final QueryController queryController) 
 {
   setWidth("99%");
   setHeight("99%");
   setMargin(true);
   setSpacing(true);
   
   //Setup GUI Components
   Panel editPanel = new Panel();
   
   GridLayout editLayout = new GridLayout(5, 4);
   editLayout.setSpacing(true);
   editLayout.setMargin(true);
   editLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
   
   editPanel.setContent(editLayout);
   //Query Info
   editLayout.addComponent(new Label("Query Information"), 0, 0, 1, 0);
   
   lblSelectedCorpora = new Label();
   Label lblSelectedCaption = new Label("Selected corpora");
   lblSelectedCorpora.setValue(StringUtils.join(corpora, ", "));
   editLayout.addComponent(lblSelectedCaption, 0, 1);
   editLayout.addComponent(lblSelectedCorpora, 1, 1);
   
   lblQuery = new Label();
   lblQuery.setStyleName("corpus-font-force");
   lblQuery.setPropertyDataSource(query);
   lblQuery.setEnabled(false);
   Label lblQueryCaption = new Label("Query to analyze");
   editLayout.addComponent(lblQueryCaption, 0, 2);
   editLayout.addComponent(lblQuery, 1, 2);
 
  
   txtDescription = new TextArea();
   txtDescription.setRows(5);
   txtDescription.setColumns(20);
   Label lblDescription = new Label("Description");
   editLayout.addComponent(lblDescription, 0, 3);
   editLayout.addComponent(txtDescription, 1, 3);
   
   //Scheduling pattern setting
   
   schedulingPattern = new TextField();
   schedulingPattern.setValue("");
   schedulingPattern.addTextChangeListener(new TextChangeListener()
   {

     @Override
     public void textChange(TextChangeEvent event)
     {
       txtNextExecution.setReadOnly(false);
       if (SchedulingPattern.validate(event.getText()))
        {
          Predictor p = new Predictor(event.getText());
          StringBuilder executions = new StringBuilder();

             for(int i = 0; i < 5; i++)
             {
               executions.append(p.nextMatchingDate());
               executions.append("\n");
             }
             executions.reverse().deleteCharAt(0).reverse();

          txtNextExecution.setValue(executions.toString());
          btnSubmit.setEnabled(true);
        }
          else
        { 
          txtNextExecution.setValue(SCHEDULING_INFO);
          btnSubmit.setEnabled(false);
        }
       txtNextExecution.setReadOnly(true);
     }
   });
   
   txtNextExecution = new TextArea();   
   txtNextExecution.setValue(SCHEDULING_INFO);
   txtNextExecution.setRows(5);
   txtNextExecution.setColumns(20);
   txtNextExecution.setReadOnly(true);
     
   Label lblNextCaption = new Label("Next 5 executions:");
   
   Label lblScheduling = new Label("Scheduling Pattern");
   lblScheduling.setDescription(CRON4JURL);
   editLayout.addComponent(lblScheduling, 2, 0);
   editLayout.addComponent(schedulingPattern, 2, 1);
   editLayout.addComponent(lblNextCaption, 2, 2);
   editLayout.addComponent(txtNextExecution, 2, 3);
   
   //Owner and Active setting

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
   
   selGroup = new ComboBox();
   selGroup.setContainerDataSource(groupsContainer);
   selGroup.setEnabled(false);
   selGroup.addBlurListener(new FieldEvents.BlurListener()
   {

     @Override
     public void blur(FieldEvents.BlurEvent event)
     {
       group = (String) selGroup.getValue();
     }
   });
   
   chkIsActive = new CheckBox("is active?");
   
   editLayout.addComponent(new Label("Owner/ Status"), 3, 0, 4, 0);
   editLayout.addComponent(chkIsGroup, 3, 1);
   editLayout.addComponent(selGroup, 4, 1);
   editLayout.addComponent(chkIsActive, 3, 2);
   
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
             
       l.addNewQuery(new AutomatedQuery(query.getValue(), corpora, schedulingPattern.getValue(), txtDescription.getValue(),
        group, isGroup, chkIsActive.getValue()));
     }
     }
   });
   
   editLayout.addComponent(btnSubmit, 3, 3);
   editLayout.addComponent(btnReset, 4, 3);
   
   addComponent(editPanel);
   
   lblStatus = new Label();
   lblStatus.setContentMode(ContentMode.PREFORMATTED);
   lblStatus.setValue("Enter your query on the left, choose corpora from the list. Enter a valid cron scheduling pattern and choose the owner for your query above.");
   
   Panel statusPanel = new Panel(lblStatus);
   statusPanel.setHeightUndefined();
   
   addComponent(statusPanel);
   
    
   queriesContainer = new BeanContainer<>(AutomatedQuery.class);
   queriesContainer.setBeanIdProperty("id");
   
   tblQueries.setEditable(true);
   tblQueries.setSelectable(true);
   tblQueries.setMultiSelect(true);
   tblQueries.setSizeFull();
   tblQueries.addStyleName(ChameleonTheme.TABLE_STRIPED);
   tblQueries.setContainerDataSource(queriesContainer);
   tblQueries.addStyleName("grey-selection");
   
   tblQueries.setTableFieldFactory(new FieldFactory());
   
   tblQueries.setVisibleColumns("corpora", "query", "description", "schedulingPattern", "isOwnerGroup", "owner", "isActive");
   tblQueries.setColumnHeaders("Corpora", "Query", "Description", "Scheduling Pattern", "Assigned To Group?", "Owner", "Is Active?");
   
  
   addComponent(tblQueries);
   setExpandRatio(tblQueries, 1.0f);
   
   btnDelete = new Button("Delete Query");
   btnDelete.addClickListener(new Button.ClickListener()
   {

     @Override
     public void buttonClick(Button.ClickEvent event)
     {
       Set<UUID> queryIds = (Set<UUID>) tblQueries.getValue();
      for (QueryListView.Listener l : listeners)
      {
        l.deleteQueries(queryIds);
      }
     }
   });
   
   addComponent(btnDelete);
   setQueryAndCorpora(queryController);
 }

  public void setQueryAndCorpora(final QueryController queryController) throws Property.ReadOnlyException
  {
      corpora.clear();
      corpora.addAll(queryController.getSelectedCorpora());
      lblSelectedCorpora.setValue(StringUtils.join((corpora), ", "));
      query.setValue(queryController.getQueryDraft());
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
    txtDescription.setValue("");
    txtNextExecution.setReadOnly(false);
    txtNextExecution.setValue(SCHEDULING_INFO);
    txtNextExecution.setReadOnly(true);
    btnSubmit.setEnabled(false);
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
  
  @Override
  public void setAvailableCorpusNames(Collection<String> corpusNames)
  {
    corpusContainer.removeAllItems();
    for (String c : corpusNames)
    {
      corpusContainer.addItem(c);
    }
  }
  
  public class FieldFactory extends DefaultFieldFactory
  {
    @Override
    public Field<?> createField(Container container, final Object itemId,
      Object propertyId, Component uiContext)
    {
      
      Field<?> result = null;
      
      switch ((String) propertyId)
      {
        case "corpora":
          PopupTwinColumnSelect selector = new PopupTwinColumnSelect(corpusContainer);
          selector.setWidth("100%");
          result = selector;
          break;
        case "query":
          result = null;
          break;
        case "description":
          TextArea descArea = new TextArea();
          descArea.setPropertyDataSource(container.getItem(itemId).getItemProperty(propertyId));
          descArea.setRows(2);
          descArea.setColumns(15);
          result = descArea;
          break;
       default:
          result = super.createField(container, itemId, propertyId, uiContext);
          break;
      
      }
      
      
      return result;
    }
  }
}
