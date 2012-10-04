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
package annis.gui.controlpanel;

import annis.gui.CorpusBrowserPanel;
import annis.gui.MetaDataPanel;
import annis.gui.Helper;
import annis.gui.MainApp;
import annis.security.AnnisSecurityManager;
import annis.security.AnnisUser;
import annis.service.objects.AnnisCorpus;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.Application;
import com.vaadin.Application.UserChangeEvent;
import com.vaadin.Application.UserChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.event.Action;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.naming.AuthenticationException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class CorpusListPanel extends Panel implements UserChangeListener,
  AbstractSelect.NewItemHandler, ValueChangeListener, Action.Handler
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CorpusListPanel.class);
  
  private static final ThemeResource INFO_ICON = new ThemeResource("info.gif");
  public static final String ALL_CORPORA = "All";
  public static final String CORPUSSET_PREFIX = "corpusset_";

  public enum ActionType
  {

    Add, Remove
  };
  BeanContainer<String, AnnisCorpus> corpusContainer;
  private Table tblCorpora;
  private ControlPanel controlPanel;
  private ComboBox cbSelection;
  private Map<String, Map<String, AnnisCorpus>> corpusSets =
    new TreeMap<String, Map<String, AnnisCorpus>>();

  public CorpusListPanel(ControlPanel controlPanel)
  {
    this.controlPanel = controlPanel;
    final CorpusListPanel finalThis = this;
    
    setSizeFull();

    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setSizeFull();

    HorizontalLayout selectionLayout = new HorizontalLayout();
    selectionLayout.setWidth("100%");
    selectionLayout.setHeight("-1px");

    Label lblVisible = new Label("Visible: ");
    lblVisible.setSizeUndefined();
    selectionLayout.addComponent(lblVisible);

    cbSelection = new ComboBox();
    cbSelection.setDescription("Choose corpus selection set");
    cbSelection.setWidth("100%");
    cbSelection.setHeight("-1px");
    cbSelection.setInputPrompt("Add new corpus selection set");
    cbSelection.setNullSelectionAllowed(false);
    cbSelection.setNewItemsAllowed(true);
    cbSelection.setNewItemHandler((AbstractSelect.NewItemHandler) this);
    cbSelection.setImmediate(true);
    cbSelection.addListener((ValueChangeListener) this);

    selectionLayout.addComponent(cbSelection);
    selectionLayout.setExpandRatio(cbSelection, 1.0f);
    selectionLayout.setSpacing(true);
    selectionLayout.setComponentAlignment(cbSelection, Alignment.MIDDLE_RIGHT);
    selectionLayout.setComponentAlignment(lblVisible, Alignment.MIDDLE_LEFT);

    layout.addComponent(selectionLayout);

    tblCorpora = new Table();
    addComponent(tblCorpora);

    corpusContainer = new BeanContainer<String, AnnisCorpus>(AnnisCorpus.class);
    corpusContainer.setBeanIdProperty("name");
    corpusContainer.setItemSorter(new CorpusSorter());


    tblCorpora.setContainerDataSource(corpusContainer);

    tblCorpora.addGeneratedColumn("info", new InfoGenerator());

    tblCorpora.setVisibleColumns(new String[]
      {
        "name", "textCount", "tokenCount", "info"
      });
    tblCorpora.setColumnHeaders(new String[]
      {
        "Name", "Texts", "Tokens", ""
      });
    tblCorpora.setHeight(100f, UNITS_PERCENTAGE);
    tblCorpora.setWidth(100f, UNITS_PERCENTAGE);
    tblCorpora.setSelectable(true);
    tblCorpora.setMultiSelect(true);
    tblCorpora.setNullSelectionAllowed(false);
    tblCorpora.setColumnExpandRatio("name", 0.6f);
    tblCorpora.setColumnExpandRatio("textCount", 0.15f);
    tblCorpora.setColumnExpandRatio("tokenCount", 0.25f);
    tblCorpora.addActionHandler((Action.Handler) this);
    tblCorpora.setImmediate(true);
    tblCorpora.addListener(new ValueChangeListener() 
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      { 
        finalThis.controlPanel.corpusSelectionChanged();
      }
    });
    
    layout.setExpandRatio(tblCorpora, 1.0f);

    Button btReload = new Button();
    btReload.addListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        MainApp app = (MainApp) getApplication();
        try
        {
          app.getWindowSearch().getSecurityManager().updateUserCorpusList(app.getUser(), true);
        }
        catch (AuthenticationException ex)
        {
          log.error(null, ex);
        }
        updateCorpusSetList(false);
        getWindow().showNotification("Reloaded corpus list", 
          Notification.TYPE_HUMANIZED_MESSAGE);
      }
    });
    btReload.setIcon(new ThemeResource("../runo/icons/16/reload.png"));
    btReload.setDescription("Reload corpus list");
    btReload.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
    
    selectionLayout.addComponent(btReload);
    selectionLayout.setComponentAlignment(btReload, Alignment.MIDDLE_RIGHT);
    
  }

  @Override
  public void attach()
  {
    super.attach();

    getApplication().addListener((UserChangeListener) this);

    tblCorpora.setSortContainerPropertyId("name");
    updateCorpusSetList();

  }
  
  private void updateCorpusSetList()
  {
    updateCorpusSetList(true);
  }

  private void updateCorpusSetList(boolean showLoginMessage)
  {
    corpusSets.clear();


    AnnisUser user = (AnnisUser) getApplication().getUser();
    Map<String, AnnisCorpus> allCorpora = getCorpusList(user);
    corpusSets.put(ALL_CORPORA, allCorpora);

    if (user != null)
    {
      if (user.getUserName().equals(AnnisSecurityManager.FALLBACK_USER))
      {
        if (corpusSets.get(ALL_CORPORA).isEmpty())
        {
          getWindow().showNotification("No corpora found. Please login "
            + "(use button at upper right corner) to see more corpora.",
            Notification.TYPE_HUMANIZED_MESSAGE);
        }
        else if(showLoginMessage)
        {
          getWindow().showNotification(
            "You can login (use button at upper right corner) to see more corpora",
            Notification.TYPE_TRAY_NOTIFICATION);
        }
      }

      for (String p : user.stringPropertyNames())
      {
        if (p.startsWith(CORPUSSET_PREFIX))
        {
          String setName = p.substring(CORPUSSET_PREFIX.length());
          Map<String, AnnisCorpus> corpora = new TreeMap<String, AnnisCorpus>();

          String corpusString = user.getProperty(p);
          if (!ALL_CORPORA.equals(setName) && corpusString != null)
          {
            String[] splitted = corpusString.split(",");
            for (String s : splitted)
            {
              if (!"".equals(s))
              {
                try
                {
                  AnnisCorpus c = allCorpora.get(s);
                  if (c != null)
                  {
                    corpora.put(c.getName(), c);
                  }
                }
                catch (NumberFormatException ex)
                {
                  log.warn("invalid number in corpus set " + setName, ex);
                }
              }
            }
            corpusSets.put(setName, corpora);
          }
        }
      }
    } // end if user not null

    Object oldSelection = cbSelection.getValue();
    cbSelection.removeAllItems();
    for (String n : corpusSets.keySet())
    {
      cbSelection.addItem(n);
    }
    if (oldSelection != null && cbSelection.containsId(oldSelection))
    {
      cbSelection.select(oldSelection);
    }
    else
    {
      cbSelection.select(ALL_CORPORA);
    }

    updateCorpusList();

  }

  private void updateCorpusList()
  {
    corpusContainer.removeAllItems();
    String selectedCorpusSet = (String) cbSelection.getValue();
    if (selectedCorpusSet == null)
    {
      selectedCorpusSet = ALL_CORPORA;
    }
    if (corpusSets.containsKey(selectedCorpusSet))
    {
      corpusContainer.addAll(corpusSets.get(selectedCorpusSet).values());
    }
    tblCorpora.sort();
  }

  private Map<String, AnnisCorpus> getCorpusList(AnnisUser user)
  {
    Map<String, AnnisCorpus> result = new TreeMap<String, AnnisCorpus>();
    try
    {
      WebResource res = Helper.getAnnisWebResource(getApplication());

      List<AnnisCorpus> corpora = res.path("corpora").get(new GenericType<List<AnnisCorpus>>()
      {
      });
      for (AnnisCorpus c : corpora)
      {
        if (user == null || user.getCorpusNameList().contains(c.getName()))
        {
          result.put(c.getName(), c);
        }
      }
    }
    catch (ClientHandlerException ex)
    {
      log.error(
        null, ex);
      getWindow().showNotification("Service not available: "
        + ex.getLocalizedMessage(),
        Notification.TYPE_TRAY_NOTIFICATION);
    }
    catch (UniformInterfaceException ex)
    {
      log.error(
        null, ex);
      getWindow().showNotification("Remote exception: "
        + ex.getLocalizedMessage(),
        Notification.TYPE_TRAY_NOTIFICATION);
    }
    catch (Exception ex)
    {
      log.error(
        null, ex);
      getWindow().showNotification("Exception: "
        + ex.getLocalizedMessage(),
        Notification.TYPE_TRAY_NOTIFICATION);
    }
    return result;
  }

  @Override
  public void applicationUserChanged(UserChangeEvent event)
  {
    updateCorpusSetList();
  }

  @Override
  public void addNewItem(String newItemCaption)
  {
    if (!cbSelection.containsId(newItemCaption))
    {
      cbSelection.addItem(newItemCaption);
      cbSelection.setValue(newItemCaption);

      corpusSets.put(newItemCaption, new TreeMap<String, AnnisCorpus>());
      updateCorpusList();

      // add the new item to the user configuration
      Application app = getApplication();
      if (app instanceof MainApp)
      {
        AnnisSecurityManager sm = ((MainApp) app).getSecurityManager();
        AnnisUser user = (AnnisUser) app.getUser();
        if (sm != null
          && !AnnisSecurityManager.FALLBACK_USER.equals(user.getUserName()))
        {
          user.put(CORPUSSET_PREFIX + newItemCaption, "");
          try
          {
            sm.storeUserProperties(user);
          }
          catch (Exception ex)
          {
            log.error(
              "could not store new corpus set", ex);
            getWindow().showNotification("Could not store new corpus set: "
              + ex.getLocalizedMessage(),
              Notification.TYPE_ERROR_MESSAGE);
          }

        }
      }
    }
  }

  @Override
  public void valueChange(ValueChangeEvent event)
  {
    updateCorpusList();
  }

  @Override
  public Action[] getActions(Object target, Object sender)
  {
    String corpusName = (String) target;
    LinkedList<Action> result = new LinkedList<Action>();

    AnnisUser user = (AnnisUser) getApplication().getUser();
    if (user == null || AnnisSecurityManager.FALLBACK_USER.equals(user.
      getUserName()))
    {
      return new Action[0];
    }

    for (Map.Entry<String, Map<String, AnnisCorpus>> entry :
      corpusSets.entrySet())
    {
      if (entry.getValue() != null && !ALL_CORPORA.equals(entry.getKey())
        && corpusName != null)
      {
        if (entry.getValue().containsKey(corpusName))
        {
          // add possibility to remove
          result.add(new AddRemoveAction(ActionType.Remove, entry.getKey(),
            corpusName,
            "Remove from " + entry.getKey()));
        }
        else
        {
          // add possibility to add
          result.add(new AddRemoveAction(ActionType.Add, entry.getKey(),
            corpusName,
            "Add to " + entry.getKey()));
        }
      }
    }

    return result.toArray(new Action[0]);
  }

  @Override
  public void handleAction(Action action, Object sender, Object target)
  {
    if(action instanceof AddRemoveAction)
    {
      AddRemoveAction a = (AddRemoveAction) action;

      Map<String, AnnisCorpus> set = corpusSets.get(a.getCorpusSet());
      Map<String, AnnisCorpus> allCorpora = corpusSets.get(ALL_CORPORA);

      if (a.type == ActionType.Remove)
      {
        set.remove(a.getCorpusId());
        if (set.isEmpty())
        {
          // remove the set itself when it gets empty
          corpusSets.remove(a.getCorpusSet());
          cbSelection.removeItem(a.getCorpusSet());
          cbSelection.select(ALL_CORPORA);
        }
      }
      else if (a.type == ActionType.Add)
      {
        set.put(a.getCorpusId(), allCorpora.get(a.getCorpusId()));
      }

      // save to file
      Application app = getApplication();
      if (app instanceof MainApp)
      {
        AnnisSecurityManager sm = ((MainApp) app).getSecurityManager();
        AnnisUser user = (AnnisUser) app.getUser();

        LinkedList<String> keys = new LinkedList<String>(
          user.stringPropertyNames());

        for (String key : keys)
        {
          if (key.startsWith(CORPUSSET_PREFIX))
          {
            user.remove(key);
          }
        }

        for (Map.Entry<String, Map<String, AnnisCorpus>> entry : corpusSets.
          entrySet())
        {
          if (!ALL_CORPORA.equals(entry.getKey()))
          {
            String key = CORPUSSET_PREFIX + entry.getKey();
            String value = StringUtils.join(entry.getValue().keySet(), ",");

            user.setProperty(key, value);
          }
        }

        try
        {
          sm.storeUserProperties(user);
        }
        catch (Exception ex)
        {
          log.error(null,
            ex);
        }
      }

      // update view
      updateCorpusList();
    }
  }

  public static class CorpusSorter extends DefaultItemSorter
  {

    @Override
    protected int compareProperty(Object propertyId, boolean sortDirection,
      Item item1, Item item2)
    {
      if ("name".equals(propertyId))
      {
        String val1 = (String) item1.getItemProperty(propertyId).getValue();
        String val2 = (String) item2.getItemProperty(propertyId).getValue();

        if (sortDirection)
        {
          return val1.compareToIgnoreCase(val2);
        }
        else
        {
          return val2.compareToIgnoreCase(val1);
        }
      }
      else
      {
        return super.compareProperty(propertyId, sortDirection, item1, item2);
      }
    }
  }

  protected void selectCorpora(Map<String, AnnisCorpus> corpora)
  {
    if (tblCorpora != null)
    {
      tblCorpora.setValue(corpora.keySet());
    }
  }

  protected Map<String, AnnisCorpus> getSelectedCorpora()
  {
    HashMap<String, AnnisCorpus> result = new HashMap<String, AnnisCorpus>();

    for (String id : corpusContainer.getItemIds())
    {
      if (tblCorpora.isSelected(id))
      {
        AnnisCorpus c = (AnnisCorpus) corpusContainer.getItem(id).getBean();
        result.put(id, c);
      }
    }

    return result;
  }

  public class InfoGenerator implements Table.ColumnGenerator
  {

    @Override
    public Component generateCell(Table source, Object itemId, Object columnId)
    {
      final AnnisCorpus c = corpusContainer.getItem(itemId).getBean();
      Button l = new Button();
      l.setStyleName(BaseTheme.BUTTON_LINK);
      l.setIcon(INFO_ICON);
      l.setDescription(c.getName());

      l.addListener(new Button.ClickListener()
      {

        @Override
        public void buttonClick(ClickEvent event)
        {
          MetaDataPanel meta = new MetaDataPanel(c.getName());
          if (controlPanel != null)
          {
            CorpusBrowserPanel browse = new CorpusBrowserPanel(c, controlPanel);
            HorizontalLayout layout = new HorizontalLayout();
            layout.addComponent(meta);
            layout.addComponent(browse);
            layout.setSizeFull();
            layout.setExpandRatio(meta, 0.5f);
            layout.setExpandRatio(browse, 0.5f);

            Window window = new Window("Corpus information for " + c.getName()
              + " (ID: " + c.getId() + ")", layout);
            window.setWidth(70, UNITS_EM);
            window.setHeight(40, UNITS_EM);
            window.setResizable(false);
            window.setModal(false);

            getWindow().addWindow(window);
            window.center();

          }
        }
      });

      return l;
    }
  }

  public static class AddRemoveAction extends Action
  {

    private ActionType type;
    private String corpusSet;
    private String corpusId;

    public AddRemoveAction(ActionType type, String corpusSet, String corpusId,
      String caption)
    {
      super(caption);
      this.type = type;
      this.corpusSet = corpusSet;
      this.corpusId = corpusId;
    }

    public ActionType getType()
    {
      return type;
    }

    public String getCorpusId()
    {
      return corpusId;
    }

    public String getCorpusSet()
    {
      return corpusSet;
    }
  }
}
