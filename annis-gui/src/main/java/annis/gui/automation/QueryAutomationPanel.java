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
import annis.gui.automation.controller.AutomatedQueryResultsController;
import annis.gui.components.HelpButton;
import annis.gui.objects.Query;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
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
public class QueryAutomationPanel extends VerticalLayout implements TextChangeListener, AutomatedQueryListView, CorpusSelectionChangeListener
{ 
  private static final String SCHEDULING_INFO = "Please enter a valid scheduling pattern expression formatted like this:<br />"
    + "'mm hh dd MM ww' where<br />"
    + "'mm' denotes the minutes,<br />"
    + "'hh' dentotes the hours,<br />"
    + "'dd' denotes the days in the month,<br />"
    + "'MM' denotes the months in the year, and <br />"
    + "'ww' denotes the weekdays at which the query should be exectuted.<br /><br />"
    + "Special characters:<br />"
    + "Use ',' to denote alternatives inside a slot.<br />"
    + "Use '-' to denote ranges inside a slot.<br />"
    + "A '*' schedules the execution at 'every' of the slot it is used in.<br />"
    + "A '|' (pipe) followed by another expression can be used for an alternative. "
    + "Execution happens when any of the alternatives matches the current time. <br />"
    + "For more information and examples visit: <a href='http://www.sauronsoftware.it/projects/cron4j/manual.php#p02' target='_blank'> cron4j documentation</a>";

  private final Logger log = LoggerFactory.getLogger(QueryAutomationPanel.class);
  
  private final List<AutomatedQueryListView.Listener> listeners = new LinkedList<>();
 
  private final BeanContainer<UUID, AutomatedQuery> queriesContainer;
  
  private final IndexedContainer groupsContainer = new IndexedContainer();
  private final IndexedContainer corpusContainer = new IndexedContainer();
  
  //newQuery data
  private AutomatedQuery editingQuery;
  private boolean editing; 
  
  private TextField fieldQuery = new TextField();
  private TreeSet<String> corpora = new TreeSet<>();
  
  //UI Components
  private final Label lblSelectedCorpora;
  private final Label lblQuery;
  private final TextArea txtDescription;
  
  private final TextField schedulingPattern;
  private final TextArea txtNextExecution;
 
  private final CheckBox chkIsGroup;
  private final ComboBox selGroup;
  private final CheckBox chkIsActive;
  
  private final Button btnSubmit;
  private final Button btnReset;  
 
  private final Label lblStatus;
  
  private final Table tblQueries = new Table(); 
  
  private final Button btnDelete;
  
  //Other components
  private ResultsViewPanel resultsView;
  private final QueryController queryController;
  private final SearchUI searchUI;
  
 public QueryAutomationPanel(final QueryController queryController, final SearchUI ui) 
 {
   this.queryController = queryController;
   this.searchUI = ui;
     
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
   lblQuery.setPropertyDataSource(fieldQuery);
   lblQuery.setEnabled(false);
   Label lblQueryCaption = new Label("Query to analyze");
   editLayout.addComponent(lblQueryCaption, 0, 2);
   editLayout.addComponent(lblQuery, 1, 2);
 
   fieldQuery.addValueChangeListener(lblQuery);
  
   txtDescription = new TextArea();
   txtDescription.setRows(5);
   txtDescription.setColumns(20);
   Label lblDescription = new Label("Description");
   editLayout.addComponent(lblDescription, 0, 3);
   editLayout.addComponent(txtDescription, 1, 3);
   
   txtDescription.addValueChangeListener(new Property.ValueChangeListener()
   {

     @Override
     public void valueChange(Property.ValueChangeEvent event)
     {
       editingQuery.setDescription(txtDescription.getValue());
       validateInput();
     }
   });
   //Scheduling pattern setting
   
   schedulingPattern = new TextField();
   schedulingPattern.setValue("");
   schedulingPattern.addTextChangeListener(new TextChangeListener()
   {

     @Override
     public void textChange(TextChangeEvent event)
     {
       editingQuery.setSchedulingPattern(event.getText());
       validateInput();
     }
   });
   
   
   schedulingPattern.setDescription(SCHEDULING_INFO);
   HelpButton patternHelp = new HelpButton<>(schedulingPattern);
   
   HorizontalLayout scheduleLayout = new HorizontalLayout(schedulingPattern, patternHelp);
   txtNextExecution = new TextArea();   
   txtNextExecution.setValue("Scheduling pattern empty.");
   txtNextExecution.setRows(5);
   txtNextExecution.setColumns(20);
   txtNextExecution.setReadOnly(true);
     
   Label lblNextCaption = new Label("Next 5 executions:");
   
   Label lblScheduling = new Label("Scheduling Pattern");
   editLayout.addComponent(lblScheduling, 2, 0);
   editLayout.addComponent(scheduleLayout, 2, 1);
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
       boolean isGroup = (Boolean) event.getProperty().getValue(); 
       
       selGroup.setValue(null);       
       selGroup.setEnabled(isGroup);
       
       editingQuery.setOwner(null);
       editingQuery.setIsOwnerGroup(isGroup);
       validateInput();
     }
   });
   
   selGroup = new ComboBox();
   selGroup.setContainerDataSource(groupsContainer);
   selGroup.setEnabled(false);
   selGroup.addValueChangeListener(new Property.ValueChangeListener()
   {

     @Override
     public void valueChange(Property.ValueChangeEvent event)
     {
       editingQuery.setOwner((String) selGroup.getValue());
       validateInput();
     }
   });
   
   chkIsActive = new CheckBox("is active?");
   chkIsActive.addValueChangeListener(new CheckBox.ValueChangeListener()
   {

     @Override
     public void valueChange(Property.ValueChangeEvent event)
     {
       editingQuery.setIsActive(chkIsActive.getValue());
       validateInput();
     }
   }
);
   
   editLayout.addComponent(new Label("Owner/ Status"), 3, 0, 4, 0);
   editLayout.addComponent(chkIsGroup, 3, 1);
   editLayout.addComponent(selGroup, 4, 1);
   editLayout.addComponent(chkIsActive, 3, 2);
   
   //Reset Button
   btnReset = new Button();
   btnReset.setCaption("Abort");
   btnReset.addClickListener(new Button.ClickListener()
   {

     @Override
     public void buttonClick(Button.ClickEvent event)
     {
       setEditingQuery(null);
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
       for (AutomatedQueryListView.Listener l : listeners)
     {
       if (editing)
          {
            l.queryUpdated(editingQuery);
          }
       else
         {
           l.addNewQuery(editingQuery);
         }
     }
     }
   });
   
   editLayout.addComponent(btnSubmit, 3, 3);
   editLayout.addComponent(btnReset, 4, 3);
   
   addComponent(editPanel);
   
   lblStatus = new Label();
   lblStatus.setContentMode(ContentMode.PREFORMATTED);
   lblStatus.setValue("Enter your query and choose corpora from the list on the left. Enter a valid cron scheduling pattern and choose the owner for your query above.");
   
   Panel statusPanel = new Panel(lblStatus);
   statusPanel.setHeightUndefined();
   
   addComponent(statusPanel);
   
    
   queriesContainer = new BeanContainer<>(AutomatedQuery.class);
   queriesContainer.setBeanIdProperty("id");
   
   tblQueries.setEditable(false);
   tblQueries.setSelectable(true);
   tblQueries.setMultiSelect(true);
   tblQueries.setSizeFull();
   tblQueries.addStyleName(ChameleonTheme.TABLE_STRIPED);
   tblQueries.setContainerDataSource(queriesContainer);
   tblQueries.addStyleName("grey-selection");
   
   //tblQueries.setTableFieldFactory(new FieldFactory());
   
   tblQueries.setVisibleColumns("corpora", "query", "description", "schedulingPattern", "isOwnerGroup", "owner", "isActive");
   tblQueries.setColumnHeaders("Corpora", "Query", "Description", "Scheduling Pattern", "Assigned To Group?", "Owner", "Is Active?");
   
   tblQueries.addGeneratedColumn("Action", new Table.ColumnGenerator()
   {

     @Override
     public Object generateCell(final Table source, final Object itemId, Object columnId)
     {
       HorizontalLayout layout = new HorizontalLayout();
       Button btnEdit = new Button("Edit");
       btnEdit.addClickListener(new Button.ClickListener()
       {

         @Override
         public void buttonClick(Button.ClickEvent event)
         {
           BeanItem<AutomatedQuery> bean = (BeanItem<AutomatedQuery>) source.getItem(itemId);
           
           AutomatedQuery copyForEditing = new AutomatedQuery(bean.getBean());
           setEditingQuery(copyForEditing);
         }
       });
       
       Button btnShowResults = new Button("Results");
       btnShowResults.addClickListener(new Button.ClickListener()
       {

         @Override
         public void buttonClick(Button.ClickEvent event)
         {
           if (resultsView == null)
           {
             resultsView = new ResultsViewPanel();
             AutomatedQueryResultsController aqrc = new AutomatedQueryResultsController(resultsView, ui.getAutomatedQueryResultsManagement());
             resultsView.addListener(aqrc);
           }
           BeanItem<AutomatedQuery> beanItem = (BeanItem<AutomatedQuery>) source.getItem(itemId);
          resultsView.setActiveQuery(beanItem.getBean().getId());
          final TabSheet tabSheet = searchUI.getMainTab();
          Tab tab = tabSheet.getTab(resultsView);
          
          if (tab == null)
          {
            tab = tabSheet.addTab(resultsView, "Automated Query Results");
            tab.setIcon(FontAwesome.TABLE);
          }
          tab.setClosable(true);
          tabSheet.setSelectedTab(resultsView);
         }
       });
              
       Button btnExecute = new Button("Execute now");
       btnExecute.addClickListener(new Button.ClickListener()
       {

         @Override
         public void buttonClick(Button.ClickEvent event)
         {
           AutomatedQuery query = ((BeanItem<AutomatedQuery>) source.getItem(itemId)).getBean();
           queryController.setQuery(new Query(query.getQuery(), query.getCorpora()));
           queryController.executeQuery();
         }
       });
       
       layout.addComponents(btnEdit, btnShowResults, btnExecute);
       return layout;
     }
   });
  
   addComponent(tblQueries);
   setExpandRatio(tblQueries, 1.0f);
   
   btnDelete = new Button("Delete Queries");
   btnDelete.addClickListener(new Button.ClickListener()
   {

     @Override
     public void buttonClick(Button.ClickEvent event)
     {
       Set<UUID> queryIds = (Set<UUID>) tblQueries.getValue();
      for (AutomatedQueryListView.Listener l : listeners)
      {
        l.deleteQueries(queryIds);
      }
      for (UUID id : queryIds)
      {
        tblQueries.unselect(id);
      }
     }
   });
   
   addComponent(btnDelete);
   setQueryAndCorpusData(queryController);
   setEditingQuery(null);
 }
 
 public void setEditingQuery(AutomatedQuery query)
 {
   if (query != null)
   {
     editing = true;
     btnSubmit.setCaption("Save changes");
     editingQuery = query;
     queryController.setQuery(new Query(query.getQuery(), query.getCorpora()));
     //synchronize data
     fieldQuery.setValue(query.getQuery());
     corpora = query.getCorpora();
     lblSelectedCorpora.setValue(StringUtils.join(corpora, ", "));
     txtDescription.setValue(query.getDescription());
     schedulingPattern.setValue(query.getSchedulingPattern());
     chkIsActive.setValue(query.getIsActive());
     chkIsGroup.setValue(query.getIsOwnerGroup());
     selGroup.setValue(query.getOwner());
     validateInput();
   }
   else
   {
     editing = false;
     btnSubmit.setCaption("Save query");
     editingQuery = new AutomatedQuery(fieldQuery.getValue(), corpora);
   }
 }
   
 private void validateInput()
 {
   boolean valid = true;
   StringBuilder sb = new StringBuilder("Input invalid: ");
   //check all the things on editingQuery

   if (editingQuery.getQuery() == null || editingQuery.getQuery().isEmpty())
   {
     sb.append("Query is empty. ");
     valid = false;
   }
   
   if (editingQuery.getCorpora() == null || editingQuery.getCorpora().isEmpty())
   {
     sb.append("No corpus selected. ");
     valid = false;      
   }
   
   if (editingQuery.getIsOwnerGroup() && (editingQuery.getOwner() == null || editingQuery.getOwner().isEmpty()))
   {
     sb.append("Owner is group, but no group selected. ");
     valid = false;
   }
   
   if (editingQuery.getSchedulingPattern() == null || editingQuery.getSchedulingPattern().isEmpty())
   {
     sb.append("Scheduling pattern empty. ");
     txtNextExecution.setReadOnly(false);
     txtNextExecution.setValue("Scheduling pattern empty.");
     txtNextExecution.setReadOnly(true);
     valid = false;
   }
   else if (SchedulingPattern.validate(editingQuery.getSchedulingPattern()))
   {
      Predictor p = new Predictor(editingQuery.getSchedulingPattern());
          StringBuilder executions = new StringBuilder();
          
             for(int i = 0; i < 4; i++)
             {
               executions.append(p.nextMatchingDate());
               executions.append("\n");
             }
             executions.append(p.nextMatchingDate());
          txtNextExecution.setReadOnly(false);
          txtNextExecution.setValue(executions.toString());      
          txtNextExecution.setReadOnly(true);
   }
   else
   {
     sb.append("Scheduling Pattern invalid. ");
     txtNextExecution.setReadOnly(false);
     txtNextExecution.setValue("Scheduling pattern invalid.");
     txtNextExecution.setReadOnly(true);
     valid = false;
   }
   
   btnSubmit.setEnabled(valid); 
   String msg = valid? "Input is valid." : sb.toString();
   lblStatus.setValue(msg);
 }

  public void setQueryAndCorpusData(final QueryController queryController) throws Property.ReadOnlyException
  {
      corpora.clear();
      corpora.addAll(queryController.getSelectedCorpora());
      lblSelectedCorpora.setValue(StringUtils.join((corpora), ", "));
      fieldQuery.setValue(queryController.getQueryDraft());
      
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
    fieldQuery.setValue(event.getText());
    editingQuery.setQuery(event.getText());
    validateInput();
  }

  @Override
  public void addListener(AutomatedQueryListView.Listener listener)
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
    txtNextExecution.setValue("Scheduling pattern empty.");
    txtNextExecution.setReadOnly(true);
    btnSubmit.setEnabled(false);
  }


  @Override
  public void onCorpusSelectionChanged(Set<String> selectedCorpora)
  {
    corpora.clear();
    corpora.addAll(selectedCorpora);
    lblSelectedCorpora.setValue(StringUtils.join((corpora), ", "));
    editingQuery.setCorpora(corpora);
    validateInput();
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
  /*
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
          TreeSet<String> corpora = (TreeSet<String>) container.getContainerProperty(itemId, propertyId);
          TextField txt = new TextField("",StringUtils.join(corpora, ", "));
          txt.setEnabled(false);
          result = txt;
          break;
        case "query":
          result = null;
          break;
        case "description":
          TextArea descArea = new TextArea();
          descArea.setPropertyDataSource(container.getItem(itemId).getItemProperty(propertyId));
          descArea.setRows(2);
          descArea.setColumns(15);
          descArea.setEnabled(false);
          result = descArea;
          break;
        case "schedulingPatern":
          result = null;
          break;
        default:
          result = super.createField(container, itemId, propertyId, uiContext);
          break;
      }
      
      return result;
    }
  }
  */
}
