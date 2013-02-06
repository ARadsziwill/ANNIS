
/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.precedencequerybuilder;

import annis.gui.Helper;
import annis.gui.controlpanel.ControlPanel;
import annis.model.Annotation;
import annis.service.objects.AnnisAttribute;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author tom
 */
public class PrecedenceQueryBuilder extends Panel implements Button.ClickListener
  {
  private Button btInitLanguage;
  private Button btInitMeta;
  private Button btGo;
  private Button btClear;
  private ControlPanel cp;
  private HorizontalLayout language;
  private HorizontalLayout meta;
  private HorizontalLayout option;
  private HorizontalLayout toolbar;
  private VerticalLayout mainLayout;
  private SpanBox spb;
  private Collection<VerticalNode> vnodes;
  private Collection<EdgeBox> eboxes;
  private Collection<MetaBox> mboxes;

  public PrecedenceQueryBuilder(ControlPanel cp)
  {
    launch(cp);
  }

  private void launch(ControlPanel cp)
  {
    this.cp = cp;
    mainLayout = new VerticalLayout();
    
    vnodes = new ArrayList<VerticalNode>();
    eboxes = new ArrayList<EdgeBox>();
    mboxes = new ArrayList<MetaBox>();

    btInitLanguage = new Button("Start with linguistic search", (Button.ClickListener) this);
    btInitLanguage.setStyleName(ChameleonTheme.BUTTON_SMALL);

    btInitMeta = new Button("Start with meta search", (Button.ClickListener) this);
    btInitMeta.setStyleName(ChameleonTheme.BUTTON_SMALL);

    btGo = new Button("Create AQL Query", (Button.ClickListener) this);
    btGo.setStyleName(ChameleonTheme.BUTTON_SMALL);

    btClear = new Button("Clear the Query Builder", (Button.ClickListener) this);
    btClear.setStyleName(ChameleonTheme.BUTTON_SMALL);

    spb = new SpanBox(this);
    
    language = new HorizontalLayout();
    language.addComponent(btInitLanguage);
    meta = new HorizontalLayout();
    meta.addComponent(btInitMeta);
    option = new HorizontalLayout();
    option.addComponent(spb);
    toolbar = new HorizontalLayout();
    toolbar.addComponent(btGo);
    toolbar.addComponent(btClear);
    
    mainLayout.addComponent(language);
    mainLayout.addComponent(meta);
    mainLayout.addComponent(option);
    mainLayout.addComponent(toolbar);
    
    setContent(mainLayout);
    setScrollable(true);
    getContent().setSizeUndefined();
    setHeight("100%");
    
  }
  
  private String getAQLFragment(SearchBox sb)
    //by Martin
  {
    String result, value=sb.getValue(), level=sb.getAttribute();
    if (sb.isRegEx())
    {
      result = (value==null) ? level+"=/.*/" : level+"=/"+value+"/";
      return result;
    }
    else
    {
      result = (value==null) ? level+"=/.*/" : level+"=\""+value+"\"";
      return result;
    }
  }

  private String getAQLQuery()//by Martin
  {
    int count = 1;

    //get all instances of type VerticalNode, Searchbox, Edgebox
    Iterator<Component> itcmp = language.getComponentIterator();
    Collection<Integer> sentenceVars = new ArrayList<Integer>();
    String query = "", edgeQuery = "", sentenceQuery = "";
    while(itcmp.hasNext())
    {
      Component itElem = itcmp.next();

      if(itElem instanceof VerticalNode)
      {
        Collection<SearchBox> sbList = ((VerticalNode)itElem).getSearchBoxes();

        for (SearchBox sb : sbList)
        {
          query += " & " + getAQLFragment(sb);
        }



        sentenceVars.add(new Integer(count));

        for(int i=1; i < sbList.size(); i++)
        {
          String addQuery = "\n& #" + count +"_=_"+ "#" + ++count;
          edgeQuery += addQuery;
        }
        //if a VerticalNode contains no condition/SearchBox, a placeholder
        //is inserted to describe the gap as "anything"
        if (sbList.isEmpty()) {query += "\n& /.*/";}
      }

      //after a VerticalNode there is always an... EDGEBOX!
      //so the Query will be build in the right order
      if(itElem instanceof EdgeBox)
      {
        count++;
        EdgeBox eb = (EdgeBox)itElem;
        edgeQuery += "\n& #" + (count-1) +" "+ eb.getValue() +" "+ "#" + count;
      }
    }
    //search within span?
    if(spb.searchWithinSpan())
    {
      String addQuery;
      if (spb.isRegEx())
      {
        addQuery = "\n& "+ spb.getSpanName() + " = /" + spb.getSpanValue() + "/";
      }
      else
      {
        addQuery = "\n& "+ spb.getSpanName() + " = \"" + spb.getSpanValue() + "\"";
      }
      query += addQuery;    
      count++;
      for(Integer i : sentenceVars)
      {
        sentenceQuery += "\n& #" + count + "_i_#"+i.toString();
      }
    }

    String fullQuery = (query+edgeQuery+sentenceQuery);
    if (fullQuery.length() < 3) {return "";}
    fullQuery = fullQuery.substring(3);//deletes leading " & "    
    return fullQuery;
  }

  public void updateQuery()
  {
    cp.setQuery(getAQLQuery(), null);
  }

  @Override
  public void buttonClick(Button.ClickEvent event)
  {

    final PrecedenceQueryBuilder sq = this;
    
    if (cp.getSelectedCorpora().isEmpty()){
      getWindow().showNotification("No corpora selected");
    }
    
    else
    {
      if(event.getButton() == btInitLanguage)
      {
        language.removeComponent(btInitLanguage);
        MenuBar addMenu = new MenuBar();
        Collection<String> annonames = getAvailableAnnotationNames();
        final MenuBar.MenuItem add = addMenu.addItem("Add position", null);
        for (final String annoname : annonames)
        {
          add.addItem(killNamespace(annoname), new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
              if (!vnodes.isEmpty())
              {
                EdgeBox eb = new EdgeBox(sq);
                language.addComponent(eb);
                eboxes.add(eb);
              }

              VerticalNode vn = new VerticalNode(killNamespace(annoname), sq);
              language.addComponent(vn);
              vnodes.add(vn);

            }
          });
        }
        language.addComponent(addMenu);
      }
      if(event.getButton() == btInitMeta)
      {
        meta.removeComponent(btInitMeta);
        MenuBar addMenu = new MenuBar();
        Collection<String> annonames = getAvailableMetaNames();
        final MenuBar.MenuItem add = addMenu.addItem("Add position", null);
        for (final String annoname : annonames)
        {
          add.addItem(killNamespace(annoname), new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
              MetaBox mb = new MetaBox(killNamespace(annoname), sq);
              meta.addComponent(mb);
              mboxes.add(mb);
            }
          });
        }
        meta.addComponent(addMenu);
      }
      if (event.getButton() == btGo)
      {
        updateQuery();
      }

      if (event.getButton() == btClear)
      {
        option.removeAllComponents();
        language.removeAllComponents();
        meta.removeAllComponents();
        toolbar.removeAllComponents();
        removeComponent(option);
        removeComponent(language);
        removeComponent(meta);
        removeComponent(toolbar);
        vnodes.clear();
        eboxes.clear();
        mboxes.clear();
        updateQuery();
        launch(cp);
      }
    }
  }

public void removeVerticalNode(VerticalNode v)
  {
    language.removeComponent(v);
    for (VerticalNode vnode : vnodes)
    {
      Iterator<EdgeBox> ebIterator = eboxes.iterator();
      if((ebIterator.hasNext()) && (v.equals(vnode)))
      {
        EdgeBox eb = eboxes.iterator().next();
        eboxes.remove(eb);
        language.removeComponent(eb);
        break;
      }
    }
    vnodes.remove(v);
    updateQuery();
  }

public void removeMetaBox(MetaBox v)
  {
    meta.removeComponent(v);
    mboxes.remove(v);
    updateQuery();
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
              .queryParam("onlymostfrequentvalues", "false")
              .get(new GenericType<List<AnnisAttribute>>() {})
            );
        }

        for (AnnisAttribute a : atts)
        {
          if (a.getType() == AnnisAttribute.Type.node)
          {
            result.add(killNamespace(a.getName()));
          }
        }

      }
      catch (Exception ex)
      {

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

      }
    }
    return result;
  }

  public String killNamespace(String qName)
  {
    String[] splitted = qName.split(":");
    return splitted[splitted.length - 1];
  }

  public Set<String> getAvailableMetaNames()
  {
    Set<String> result = new TreeSet<String>();

    WebResource service = Helper.getAnnisWebResource(getApplication());

    // get current corpus selection
    Set<String> corpusSelection = cp.getSelectedCorpora();

    if (service != null)
    {
      try
      {
        List<Annotation> atts = new LinkedList<Annotation>();
        for(String corpus : corpusSelection)
        {
          atts.addAll(
service.path("query").path("corpora").path(corpus).path("docmetadata")
            .get(new GenericType<List<Annotation>>() {}));
        }
        for (Annotation a : atts)
        {
          result.add(a.getName());
        }

      }
      catch (Exception ex)
      {

      }
    }
    return result;
  }

  public Set<String> getAvailableMetaLevels(String ebene)
  {
    Set<String> result = new TreeSet<String>();

    WebResource service = Helper.getAnnisWebResource(getApplication());

    // get current corpus selection
    Set<String> corpusSelection = cp.getSelectedCorpora();

    if (service != null)
    {
      try
      {
        List<Annotation> atts = new LinkedList<Annotation>();
        for(String corpus : corpusSelection)
        {
          atts.addAll(
service.path("query").path("corpora").path(corpus).path("docmetadata")
            .get(new GenericType<List<Annotation>>() {}));
        }
        for (Annotation a : atts)
        {
          if (killNamespace(a.getName()).equals(ebene))
          {
            result.add(a.getValue());
          }
        }

      }
      catch (Exception ex)
      {

      }
    }
    return result;
  }
}

