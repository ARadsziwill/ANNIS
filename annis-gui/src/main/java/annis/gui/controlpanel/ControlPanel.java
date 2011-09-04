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

import annis.gui.MainApp;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class ControlPanel extends Panel
{
  private QueryPanel queryPanel;
  private CorpusListPanel corpusList;
  
  public ControlPanel(MainApp app)
  {
    super("Search Form");
    
    addStyleName("control");
    
    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setHeight(100f, UNITS_PERCENTAGE);
    
    Accordion accordion = new Accordion();
    accordion.setHeight(100f, Layout.UNITS_PERCENTAGE);
    
    corpusList = new CorpusListPanel(this);
    accordion.addTab(corpusList, "Corpus List", null);
    accordion.addTab(new SearchOptionsPanel(), "Search Options", null);
    accordion.addTab(new ExportPanel(), "Export", null);
    
    queryPanel = new QueryPanel(app, corpusList);
    queryPanel.setHeight(18f, Layout.UNITS_EM);
    
    addComponent(queryPanel);
    addComponent(accordion);
    
    
    layout.setExpandRatio(accordion, 1.0f);       
  }
  
  public void setQuery(String query, Set<Long> corpora)
  {
    if(queryPanel != null && corpusList != null)
    {
      queryPanel.setQuery(query);
      corpusList.selectCorpora(corpora);
    }
  }
}
