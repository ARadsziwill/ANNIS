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
package annis.gui.precedencequerybuilder;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.Set;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.AbstractSelect;
import org.apache.commons.lang3.StringUtils;//levenshtein
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;

/**
 *
 * @author tom
 */
public class SearchBox extends Panel implements Button.ClickListener, FieldEvents.TextChangeListener
{
  
  private int id;
  private Button btClose;
  private VerticalNode vn;
  private String ebene;
  private SensitiveComboBox cb;  
  private CheckBox reBox;//by Martin, tick for regular expression
  private Collection<String> annonames;//added by Martin, necessary for rebuilding the list of cb-Items
  private PrecedenceQueryBuilder sq;
  
  public static final String BUTTON_CLOSE_LABEL = "Close";
  private static final String SB_CB_WIDTH = "140px";
  

  public SearchBox(String ebene, PrecedenceQueryBuilder sq, VerticalNode vn)
  {
    
    this.vn = vn;
    this.ebene = ebene;
    this.sq = sq;
    
    VerticalLayout sb = new VerticalLayout();
    sb.setImmediate(true);
    
    // searchbox values for ebene
    Collection<String> annonames = new TreeSet<String>();
    for(String a :sq.getAvailableAnnotationLevels(ebene))
    {
      annonames.add(a);
    }
    this.annonames = annonames;//by Martin
    this.cb = new SensitiveComboBox();
    cb.setCaption(ebene);
    cb.setInputPrompt(ebene);
    cb.setWidth(SB_CB_WIDTH);
    // configure & load content
    cb.setImmediate(true);
    for (String annoname : annonames) 
    {
      cb.addItem(annoname);
    }
    cb.setFilteringMode(Filtering.FILTERINGMODE_OFF);//necessary?
    cb.addListener((FieldEvents.TextChangeListener)this);
    sb.addComponent(cb);
    
    HorizontalLayout sbtoolbar = new HorizontalLayout();
    sbtoolbar.setSpacing(true);
     
    // searchbox tickbox for regex
    CheckBox tb = new CheckBox(SpanBox.REBOX_LABEL);
    tb.setDescription(SpanBox.REBOX_DESCRIPTION);
    tb.setImmediate(true);
    sbtoolbar.addComponent(tb);
    tb.addListener((Button.ClickListener) this);
    reBox = tb;
    
    // close the searchbox
    btClose = new Button(BUTTON_CLOSE_LABEL, (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    sbtoolbar.addComponent(btClose);
    
    sb.addComponent(sbtoolbar);
    
    addComponent(sb);    

  } 
 
  @Override
  public void buttonClick(Button.ClickEvent event)
  {    
    if(event.getButton() == btClose)
    {
      vn.removeSearchBox(this);      
    }
    
    else if(event.getComponent()==reBox)
    {
      boolean r = reBox.booleanValue();
      cb.setNewItemsAllowed(r);
      if(!r)
      {         
        SpanBox.buildBoxValues(cb, ebene, sq);
      }
      else if(cb.getValue()!=null)
      {
        String escapedItem = sq.escapeRegexCharacters(cb.getValue().toString());
        cb.addItem(escapedItem);
        cb.setValue(escapedItem);         
      }
    }
  }
  
  @Override
  public void textChange(TextChangeEvent event)
  {    
    String txt = event.getText();
    if (!txt.equals(""))
    {
      cb.removeAllItems();
      for(String s : annonames)
      {
        if((StringUtils.getLevenshteinDistance(txt, s)<txt.length()/2+1) | (StringUtils.startsWith(s, txt)))
        {
          cb.addItem(s);
        }
      }
    }
    else
    {
      SpanBox.buildBoxValues(cb, ebene, sq);
    }
  }
  
  public String getAttribute()
  {
    return ebene;
  }
  
  public String getValue()
  {
    return cb.toString();
  }
  
  public boolean isRegEx()
  {
    return reBox.booleanValue();
  }
  
  
}
