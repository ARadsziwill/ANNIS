/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui;

import annis.gui.servlets.ResourceServlet;
import annis.gui.visualizers.CorefVisualizer;
import annis.gui.visualizers.ExternalFileVisualizer;
import annis.gui.visualizers.OldPartiturVisualizer;
import annis.gui.visualizers.PaulaTextVisualizer;
import annis.gui.visualizers.PaulaVisualizer;
import annis.gui.visualizers.VisualizerPlugin;
import annis.gui.visualizers.dependency.ProielDependecyTree;
import annis.gui.visualizers.dependency.ProielRegularDependencyTree;
import annis.gui.visualizers.dependency.VakyarthaDependencyTree;
import annis.gui.visualizers.graph.DotGraphVisualizer;
import annis.gui.visualizers.gridtree.GridTreeVisualizer;
import annis.gui.visualizers.partitur.PartiturVisualizer;
import annis.gui.visualizers.tree.TigerTreeVisualizer;
import annis.security.AnnisSecurityManager;
import annis.security.AnnisUser;
import com.vaadin.Application;
import com.vaadin.Application.UserChangeListener;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.base.util.uri.ClassURI;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MainApp extends Application implements PluginSystem,
  UserChangeListener
{

  public final static String USER_KEY = "annis.gui.MainApp:USER_KEY";
  private SearchWindow windowSearch;
  
  private PluginManager pluginManager;
  private static final Map<String, VisualizerPlugin> visualizerRegistry =
    Collections.synchronizedMap(new HashMap<String, VisualizerPlugin>());
  private static final Map<String, Date> resourceAddedDate =
    Collections.synchronizedMap(new HashMap<String, Date>());
  
  private CitationWindow windowCitation;

  @Override
  public void init()
  {
    addListener((UserChangeListener) this);
    
    initPlugins();

    setTheme("annis-theme");

    windowSearch = new SearchWindow((PluginSystem) this);
    setMainWindow(windowSearch);
    
    windowCitation = new CitationWindow();
    addWindow(windowCitation);    
    
  }


  @Override
  public void setUser(Object user)
  {
    if(user == null || !(user instanceof AnnisUser))
    {
      try
      {
        user = windowSearch.getSecurityManager().login(AnnisSecurityManager.FALLBACK_USER,
          AnnisSecurityManager.FALLBACK_USER, true);
      }
      catch(Exception ex)
      {
        Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    super.setUser(user);

    windowSearch.updateUserInformation();
  }

  @Override
  public AnnisUser getUser()
  {
    Object u = super.getUser();
    if(u == null)
    {
      return null;
    }
    else
    {
      return (AnnisUser) u;
    }
  }

  
  private void initPlugins()
  {
    Logger log = Logger.getLogger(MainApp.class.getName());


    log.info("Adding plugins");
    pluginManager = PluginManagerFactory.createPluginManager();

    // TODO: package core plugins as extra project/jar and load them as jar
    // add our core plugins by hand
    pluginManager.addPluginsFrom(new ClassURI(CorefVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(DotGraphVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(ExternalFileVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(GridTreeVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(OldPartiturVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(PartiturVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(PaulaTextVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(PaulaVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(ProielDependecyTree.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(ProielRegularDependencyTree.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(ResourceServlet.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(TigerTreeVisualizer.class).toURI());
    pluginManager.addPluginsFrom(new ClassURI(VakyarthaDependencyTree.class).toURI());

    File baseDir = this.getContext().getBaseDirectory();
    File basicPlugins = new File(baseDir, "plugins");
    if(basicPlugins.isDirectory())
    {
      pluginManager.addPluginsFrom(basicPlugins.toURI());
      log.log(Level.INFO, "added plugins from {0}", basicPlugins.getPath());
    }


    String globalPlugins = System.getenv("ANNIS_PLUGINS");
    if(globalPlugins != null)
    {
      pluginManager.addPluginsFrom(new File(globalPlugins).toURI());
      log.log(Level.INFO, "added plugins from {0}", globalPlugins);
    }

    StringBuilder listOfPlugins = new StringBuilder();
    listOfPlugins.append("loaded plugins:\n");
    PluginManagerUtil util = new PluginManagerUtil(pluginManager);
    for(Plugin p : util.getPlugins())
    {
      listOfPlugins.append(p.getClass().getName()).append("\n");
    }
    log.info(listOfPlugins.toString());

    Collection<VisualizerPlugin> visualizers = util.getPlugins(VisualizerPlugin.class);
    for(VisualizerPlugin vis : visualizers)
    {
      visualizerRegistry.put(vis.getShortName(), vis);
      resourceAddedDate.put(vis.getShortName(), new Date());
    }
  }

  

  @Override
  public void close()
  {
    if(pluginManager != null)
    {
      pluginManager.shutdown();
    }

    super.close();
  }

  @Override
  public PluginManager getPluginManager()
  {
    return pluginManager;
  }

  @Override
  public VisualizerPlugin getVisualizer(String shortName)
  {
    return visualizerRegistry.get(shortName);
  }

  @Override
  public void applicationUserChanged(UserChangeEvent event)
  {
    HttpSession session = ((WebApplicationContext) getContext()).getHttpSession();
    session.setAttribute(USER_KEY, event.getNewUser());
  }

  public AnnisSecurityManager getSecurityManager()
  {
    return windowSearch.getSecurityManager();
  }
  
}