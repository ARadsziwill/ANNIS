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
package annis.gui.resultview;

import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.gui.PluginSystem;
import annis.gui.Helper;
import annis.gui.paging.PagingCallback;
import annis.gui.paging.PagingComponent;
import annis.security.AnnisUser;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import com.vaadin.addon.chameleon.ChameleonTheme;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class ResultViewPanel extends Panel implements PagingCallback
{
  
  private PagingComponent paging;
  private ResultSetPanel resultPanel;
  private String aql;
  private Set<Long> corpora;
  private int contextLeft, contextRight, pageSize;
  private AnnisResultQuery query;
  private VerticalLayout layout;
  private ScrollPanel scrollPanel;
  private ProgressIndicator progressResult;
  private PluginSystem ps;
  private MenuItem miTokAnnos;

  public ResultViewPanel(String aql, Set<Long> corpora, 
    int contextLeft, int contextRight, int pageSize,
    PluginSystem ps)
  {
    this.aql = aql;
    this.corpora = corpora;
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
    this.pageSize = pageSize;
    this.ps = ps;
    
    setSizeFull();

    VerticalLayout mainLayout = (VerticalLayout) getContent();
    mainLayout.setMargin(false);
    mainLayout.setSizeFull();

    MenuBar mbResult = new MenuBar();
    mbResult.setWidth("100%");
    miTokAnnos = mbResult.addItem("Token Annotations", null);
    
    paging = new PagingComponent(0, pageSize);
    paging.setInfo("Result for query \"" + aql.replaceAll("\n", " ") + "\"");
    paging.addCallback((PagingCallback) this);

    scrollPanel = new ScrollPanel();
    scrollPanel.setSizeFull();
    scrollPanel.addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    layout = (VerticalLayout) scrollPanel.getContent();
    layout.setMargin(false);
    layout.setWidth("100%");
    layout.setHeight("-1px");

    mainLayout.addComponent(mbResult);
    mainLayout.addComponent(paging);
    mainLayout.addComponent(scrollPanel);

    mainLayout.setSizeFull();
    mainLayout.setExpandRatio(scrollPanel, 1.0f);

    progressResult = new ProgressIndicator();
    progressResult.setIndeterminate(true);
    progressResult.setEnabled(false);
    
    layout.addComponent(progressResult);
  }

  @Override
  public void attach()
  {    
    query = new AnnisResultQuery(corpora, aql,
      contextLeft, contextRight, Helper.getService(getApplication(), getWindow()));
    createPage(0, pageSize);
    
    super.attach();
  }

  public void setCount(int count)
  {
    paging.setCount(count, false);
  }

  @Override
  public void createPage(final int start, final int limit)
  {
    
    if(query != null)
    {
      progressResult.setEnabled(true);
      progressResult.setVisible(true);
      if(resultPanel != null )
      {
        resultPanel.setVisible(false);
      }
      
      Runnable r = new Runnable()
      {

        @Override
        public void run()
        {
          try
          {

            AnnisUser user = null;
            if(getApplication() != null)
            {
              user = (AnnisUser) getApplication().getUser();
            }
            AnnisResultSet result = query.loadBeans(start, limit, user);
            
            updateTokenAnnos(result);
            
            if(resultPanel != null)
            {
              layout.removeComponent(resultPanel);
            }
            resultPanel = new ResultSetPanel(result, start, ps);
            
            layout.addComponent(resultPanel);
            resultPanel.setVisible(true);
            
          }
          catch(AnnisQLSemanticsException ex)
          {
            paging.setInfo("Semantic error: " + ex.getLocalizedMessage());
          }
          catch(AnnisQLSyntaxException ex)
          {
            paging.setInfo("Syntax error: " + ex.getLocalizedMessage());
          }
          catch(AnnisCorpusAccessException ex)
          {
            paging.setInfo("Corpus access error: " + ex.getLocalizedMessage());
          }
          catch(Exception ex)
          {
            Logger.getLogger(ResultViewPanel.class.getName()).log(Level.SEVERE, "unknown exception in result view", ex);
            paging.setInfo("unknown exception: " + ex.getLocalizedMessage());
          }
          finally
          {
            progressResult.setVisible(false);
            progressResult.setEnabled(false);            
          }
        }
      };
      Thread t = new Thread(r);
      t.start();
      
    }
  }

  private void updateTokenAnnos(AnnisResultSet resultSet)
  {
    miTokAnnos.removeChildren();
    for(final String a : resultSet.getTokenAnnotationLevelSet())
    {
      MenuItem miSingleTokAnno = miTokAnnos.addItem(a, new MenuBar.Command()
      {
        @Override
        public void menuSelected(MenuItem selectedItem)
        {
          
          if(selectedItem.isChecked())
          {
            resultPanel.setTokenAnnosVisible(a, true);
          }
          else
          { 
            resultPanel.setTokenAnnosVisible(a, false);
          }
        }
      });
      
      miSingleTokAnno.setCheckable(true);
      miSingleTokAnno.setChecked(true);
      
    }
    
  }
  
  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {
    super.paintContent(target);
  }
  
  public class ScrollPanel extends Panel
  {

    @Override
    public void paintContent(PaintTarget target) throws PaintException
    {
      super.paintContent(target);
    }
    
  }
}
