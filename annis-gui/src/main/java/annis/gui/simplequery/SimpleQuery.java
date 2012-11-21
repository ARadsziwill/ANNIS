/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.simplequery;

import annis.gui.Helper;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.HorizontalLayout;
import annis.gui.controlpanel.ControlPanel;
import annis.gui.simplequery.VerticalNode;
import annis.gui.simplequery.AddMenu;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisCorpus;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import org.slf4j.LoggerFactory;
//the following added by Martin:
import com.vaadin.ui.Component;
import com.vaadin.ui.ComboBox;
import java.util.Iterator;
import java.util.ArrayList;


/**
 *
 * @author tom
 */
public class SimpleQuery extends Panel implements Button.ClickListener
{
  private Button btInitLanguage;
  private int id = 0;
  private Button btInitMeta;
  private ControlPanel cp;
  private HorizontalLayout language;
  private HorizontalLayout meta;
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SimpleQuery.class);
  
  public SimpleQuery(ControlPanel cp)
  {
    this.cp = cp;
    
    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    btInitLanguage = new Button("Start with linguistic search", (Button.ClickListener) this);
    btInitLanguage.setStyleName(ChameleonTheme.BUTTON_SMALL);
    toolbar.addComponent(btInitLanguage);

    btInitMeta = new Button("Start with meta search", (Button.ClickListener) this);
    btInitMeta.setStyleName(ChameleonTheme.BUTTON_SMALL);
    toolbar.addComponent(btInitMeta);
    
    language = new HorizontalLayout();
    meta = new HorizontalLayout();
    addComponent(toolbar);
    addComponent(language);
    addComponent(meta);

  }
  
  
  private List<SearchBox> getSearchBoxes(VerticalNode vn)
    //added by Martin, it finally gets ALL SEARCHBOXES
  {
    //I first have to get the vertical layouts of vn
    Iterator<Component> itVn = vn.getComponentIterator();
    List<VerticalLayout> vlList = new ArrayList();
    List<SearchBox> sbList = new ArrayList();
        
    
    //get vertical layouts and SearchBoxes placed directly on VerticalNodes
    while(itVn.hasNext())//create List of vertical layouts
    {
      Component itElem = itVn.next();
      if(itElem instanceof VerticalLayout)
      {       
       vlList.add((VerticalLayout)itElem);        
      }      
      if(itElem instanceof SearchBox)
      {
        sbList.add((SearchBox)itElem);
      }
    }    
    
    //now get Searchboxes from vertical layouts
    Iterator<VerticalLayout> itVl = vlList.iterator();
    
    while(itVl.hasNext())//for each vertical layout in VerticalNode vn
      // an iteration is started to get the SearchBoxes
    {
      Iterator<Component> itSb = itVl.next().getComponentIterator();
      while(itSb.hasNext())
      {
        Component itElem = itSb.next();
        if(itElem instanceof SearchBox)
        {
          sbList.add((SearchBox)itElem);
        }
      }
    }    
    
    return sbList;
  }
  
  private String getAQLFragment(SearchBox sb)
  {
    Iterator<Component> itSbParts = sb.getComponentIterator();    
    String frag = "";
    while(itSbParts.hasNext())
    {
      Component itElem = itSbParts.next();
      if(itElem instanceof ComboBox)
      {
        ComboBox cb = (ComboBox)itElem;
        frag = cb.getCaption() +"=\""+cb.getValue()+"\"";
      }
    }
    return frag;
  }
  
  private String getAQLQuery()//by Martin    
  {
    int count = 1;
    
    //get all instances of type VerticalNode, Searchbox, Edgebox    
    Iterator<Component> itcmp = language.getComponentIterator();    
    String query = "", edgeQuery = "";
    while(itcmp.hasNext())
    {
      Component itElem = itcmp.next();
      if(itElem instanceof VerticalNode)
      {        
        List<SearchBox> sbList = getSearchBoxes((VerticalNode)itElem);
        for(SearchBox sb : sbList)
        {
          query += " & " + getAQLFragment(sb);
          String addQuery = (count > 1) ? " & #" + (count-1) +" = "+ "#" + count : "";
          edgeQuery += addQuery;
          count++;
        }        
      }      
      //after a VerticalNode there is always an... EDGEBOX!
      //so the Query will be build in the right order I guess
      if(itElem instanceof EdgeBox)      
      {        
        EdgeBox eb = (EdgeBox)itElem;
        edgeQuery += " & #" + (count-1) +" "+ eb.getValue() +" "+ "#" + count;
      }
    }
    
    return query+edgeQuery;//delete leading " & " LATER
  }
  
  public void updateQuery()//by Martin
  {    
    cp.setQuery(getAQLQuery(), null);    
  }
  
  @Override
  public void buttonClick(Button.ClickEvent event)
  {

    final SimpleQuery sq = this;    
    if(event.getButton() == btInitLanguage)
    {

      MenuBar addMenu = new MenuBar();
      Collection<String> annonames = getAvailableAnnotationNames();
      final MenuBar.MenuItem add = addMenu.addItem("Add position", null);
      for (final String annoname : annonames)
      {
        add.addItem(killNamespace(annoname), new Command() {
          @Override
          public void menuSelected(MenuBar.MenuItem selectedItem) {
            id = id + 1;
            if (id > 1)
            {
              EdgeBox eb = new EdgeBox(id, sq);
              language.addComponent(eb);
            }
            VerticalNode vn = new VerticalNode(id, killNamespace(annoname), sq);
            language.addComponent(vn);            
          }
        });
      }
      language.addComponent(addMenu);
    }    
    if(event.getButton() == btInitMeta)
    {
      
      TextField tf = new TextField("meta");
      updateQuery();//by Martin
      meta.addComponent(tf);
    }    
  }
  
public Set<String> getAvailableAnnotationNames()
  {
    Set<String> result = new TreeSet<String>();

    WebResource service = Helper.getAnnisWebResource(getApplication());

    // get current corpus selection
    Set<String> corpusSelection = cp.getSelectedCorpora();

    if (service != null)
    {
      try
      {
        List<AnnisAttribute> atts = new LinkedList<AnnisAttribute>();
        
        for(String corpus : corpusSelection)
        {
          atts.addAll(
            service.path("query").path("corpora").path(corpus).path("annotations")
              .queryParam("fetchvalues", "false")
              .queryParam("onlymostfrequentvalues", "true")
              .get(new GenericType<List<AnnisAttribute>>() {})
            );
        }

        for (AnnisAttribute a : atts)
        {
          if (a.getType() == AnnisAttribute.Type.node)
          {
            result.add(a.getName());
          }
        }

      }
      catch (Exception ex)
      {
        log.error(null, ex);
      }
    }
    return result;
  }
  
  public Collection<String> getAvailableAnnotationLevels(String meta)
  {
    Collection<String> result = new TreeSet<String>();

    WebResource service = Helper.getAnnisWebResource(getApplication());

    // get current corpus selection
    Set<String> corpusSelection = cp.getSelectedCorpora();

    if (service != null)
    {
      try
      {
        List<AnnisAttribute> atts = new LinkedList<AnnisAttribute>();
        
        for(String corpus : corpusSelection)
        {
          atts.addAll(
            service.path("query").path("corpora").path(corpus).path("annotations")
              .queryParam("fetchvalues", "true")
              .queryParam("onlymostfrequentvalues", "false")
              .get(new GenericType<List<AnnisAttribute>>() {})
            );
        }
        
        for (AnnisAttribute a : atts)
        {
          if (a.getType() == AnnisAttribute.Type.node)
          {
            String aa = killNamespace(a.getName());
            if (aa.equals(meta))
            {
              result = a.getValueSet();
              break;
            }
          }
        }

      }
      catch (Exception ex)
      {
        log.error(null, ex);
      }
    }
    return result;
  }
    
  public String killNamespace(String qName)
  {
    String[] splitted = qName.split(":");
    return splitted[splitted.length - 1];
  }
}
