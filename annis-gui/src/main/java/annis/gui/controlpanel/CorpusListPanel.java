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
import annis.security.AnnisUser;
import annis.service.AnnisService;
import annis.service.ifaces.AnnisCorpus;
import annis.service.ifaces.AnnisCorpusSet;
import com.vaadin.Application.UserChangeEvent;
import com.vaadin.Application.UserChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class CorpusListPanel extends Panel implements UserChangeListener
{
  
  private static final ThemeResource INFO_ICON = new ThemeResource("info.gif");
  BeanContainer<Long, AnnisCorpus> corpusContainer;
  private Table tblCorpora;
  private ControlPanel controlPanel;
  
  public CorpusListPanel(ControlPanel controlPanel)
  {
    this.controlPanel = controlPanel;
    
    setSizeFull();
    
    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setSizeFull();
    
    tblCorpora = new Table();
    addComponent(tblCorpora);
    
    corpusContainer = new BeanContainer<Long, AnnisCorpus>(AnnisCorpus.class);
    corpusContainer.setBeanIdProperty("id");
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
    tblCorpora.setColumnExpandRatio("name", 0.7f);
    tblCorpora.setColumnExpandRatio("textCount", 0.15f);
    tblCorpora.setColumnExpandRatio("tokenCount", 0.15f);
    tblCorpora.setColumnWidth("info", 18);
    
  }
  
  @Override
  public void attach()
  { 
    super.attach();
    
    getApplication().addListener((UserChangeListener) this);
    
    tblCorpora.setSortContainerPropertyId("name");
    updateCorpusList();
  }
  
  public void updateCorpusList()
  {
    corpusContainer.removeAllItems();
    corpusContainer.addAll(getCorpusList((AnnisUser) getApplication().getUser()));
    
    tblCorpora.sort();
  }
  
  private Set<AnnisCorpus> getCorpusList(AnnisUser user)
  {
    Set<AnnisCorpus> result = new TreeSet<AnnisCorpus>();
    try
    {
      AnnisService service = Helper.getService(getApplication(), getWindow());
      if(service != null)
      {
        AnnisCorpusSet corpora = service.getCorpusSet();
        for(AnnisCorpus c : corpora)
        {
          if(user == null || user.getCorpusIdList().contains(c.getId()))
          {
            result.add(c);
          }
        }
      }
    }
    catch(RemoteException ex)
    {
      Logger.getLogger(CorpusListPanel.class.getName()).log(Level.SEVERE,
        null, ex);
      getWindow().showNotification("Remote exception: " + ex.getLocalizedMessage(),
        Notification.TYPE_WARNING_MESSAGE);
    }
    return result;
  }

  @Override
  public void applicationUserChanged(UserChangeEvent event)
  {
    updateCorpusList();
  }
  
  public class CorpusSorter extends DefaultItemSorter
  {
    
    @Override
    protected int compareProperty(Object propertyId, boolean sortDirection, Item item1, Item item2)
    {
      if("name".equals(propertyId))
      {
        String val1 = (String) item1.getItemProperty(propertyId).getValue();
        String val2 = (String) item2.getItemProperty(propertyId).getValue();
        
        if(sortDirection)
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
          MetaDataPanel meta = new MetaDataPanel(c.getId());
          if(controlPanel != null)
          {
            CorpusBrowserPanel browse = new CorpusBrowserPanel(c.getId(), controlPanel);
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
            window.setModal(true);
            
            getWindow().addWindow(window);
            window.center();
            
          }
        }
      });
      
      return l;
    }
  }
  
  protected void selectCorpora(Set<Long> corpora)
  {
    if(tblCorpora != null)
    {
      tblCorpora.setValue(null);
      for(Long l : corpora)
      {
        tblCorpora.select(l);
      }
    }
  }
  
  protected Set<Long> getSelectedCorpora()
  {
    HashSet<Long> result = new HashSet<Long>();
    
    for(Long id : corpusContainer.getItemIds())
    {
      if(tblCorpora.isSelected(id))
      {
        result.add(id);
      }
    }
      
    return result;
  }
}
