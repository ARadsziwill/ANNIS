/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.components;

import com.google.common.base.Joiner;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embedds a single HTML page and adds navigation to it's headers (if they have
 * an id).
 *
 * This is e.g. usefull for documentation.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class NavigateableSinglePage extends VerticalLayout
{

  private final static Logger log = LoggerFactory.getLogger(
    NavigateableSinglePage.class);

  private final IFrameComponent iframe = new IFrameComponent();

  private MenuBar navigation;

  private final Map<String, MenuItem> menuItemRegistry = new HashMap<>();

  private MenuItem lastSelectedItem;

  private final static Pattern regexHeader = Pattern.compile("h([1-6])");

  public NavigateableSinglePage()
  {
    iframe.setSizeFull();

    setSpacing(true);
    addComponent(iframe);

    setExpandRatio(iframe, 1.0f);

  }

  private void onScroll(String headerID)
  {
    if (navigation != null)
    {
      MenuItem navRoot = navigation.getItems().get(0);
      MenuItem toSelect = menuItemRegistry.get(headerID);
      if (toSelect != null)
      {
        navRoot.setText(toSelect.getText());
        toSelect.setStyleName("huge-selected");
        if (lastSelectedItem != null && lastSelectedItem != toSelect)
        {
          lastSelectedItem.setStyleName("huge");
        }
        lastSelectedItem = toSelect;
      }
    }
  }

  public void setSource(String source)
  {
    iframe.getState().setSource(source);
    if (navigation != null)
    {
      removeComponent(navigation);
    }
    menuItemRegistry.clear();
    navigation = createMenubarFromHTML(source, menuItemRegistry);
    addComponent(navigation, 0);
  }

  private MenuBar createMenubarFromHTML(String source,
    Map<String, MenuItem> idToMenuItem)
  {

    MenuBar mbNavigation = new MenuBar();
    mbNavigation.setStyleName("huge");
    MenuItem navRoot = mbNavigation.addItem("Choose topic", null);
    navRoot.setStyleName("huge");

    try
    {
      Document doc = Jsoup.connect(source).get();

      ArrayList<MenuItem> itemPath = new ArrayList<>();
      // find all headers that have an ID
      for (Element e : doc.getElementsByAttribute("id"))
      {
        Matcher m = regexHeader.matcher(e.tagName());
        if (m.matches())
        {
          int level = Integer.parseInt(m.group(1)) - 1;

          // decide wether to expand the path (one level deeper) or to truncate
          if (level == 0)
          {
            itemPath.clear();
          }
          else if (itemPath.size() >= level)
          {
            // truncate
            itemPath = new ArrayList<>(itemPath.subList(0, level));
          }

          if (itemPath.isEmpty() && level > 0)
          {
            // fill the path with empty elements
            for (int i = 0; i < level; i++)
            {
              itemPath.add(createItem(navRoot, itemPath, "<empty>", null));
            }
          }
          MenuItem item = createItem(navRoot, itemPath, e.text(), e.id());
          itemPath.add(item);
          idToMenuItem.put(e.id(), item);
        }
      }
    }
    catch (IOException ex)
    {
      log.error("Could not parse iframe source", ex);
    }
    return mbNavigation;
  }

  private MenuItem createItem(MenuItem rootItem, List<MenuItem> path,
    String caption, String id)
  {
    MenuItem parent;

    if (path.isEmpty())
    {
      parent = rootItem;
    }
    else
    {
      parent = path.get(path.size() - 1);

    }
    MenuItem child = parent.addItem(caption, new IDSelectionCommand(id));
    child.setStyleName("huge");
    return child;
  }

  private class IDSelectionCommand implements MenuBar.Command
  {

    private final String id;

    public IDSelectionCommand(String id)
    {
      this.id = id;
    }

    @Override
    public void menuSelected(MenuItem selectedItem)
    {
      if (id != null)
      {
        iframe.scrollToElement(id);
      }
    }

  }

  @JavaScript(
    {
      "vaadin://jquery.js", "navigateablesinglepage.js"
    })
  private class IFrameComponent extends AbstractJavaScriptComponent
  {

    public IFrameComponent()
    {
      addFunction("scrolled", new JavaScriptFunction()
      {

        @Override
        public void call(JSONArray arguments) throws JSONException
        {
          onScroll(arguments.getString(0));
        }
      });
    }
    
    public void scrollToElement(String id)
    {
      callFunction("scrollToElement", id);
    }

    @Override
    public final IframeState getState()
    {
      return (IframeState) super.getState();
    }
  }
}
