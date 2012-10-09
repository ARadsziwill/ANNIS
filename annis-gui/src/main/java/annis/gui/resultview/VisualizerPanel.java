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

import annis.gui.Helper;
import annis.gui.PluginSystem;
import annis.gui.VisualizationToggle;
import annis.gui.media.MediaControllerFactory;
import annis.gui.media.MediaControllerHolder;
import annis.gui.media.MediaPlayer;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.visualizers.VisualizerPlugin;
import annis.resolver.ResolverEntry;
import annis.visualizers.LoadableVisualizer;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.Application;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the visibility of visualizer plugins and provides some control
 * methods for the media visualizers.
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 *
 */
public class VisualizerPanel extends CustomLayout 
  implements Button.ClickListener, VisualizationToggle
{

  private final Logger log = LoggerFactory.getLogger(VisualizerPanel.class);
  public static final ThemeResource ICON_COLLAPSE = new ThemeResource(
    "icon-collapse.gif");
  public static final ThemeResource ICON_EXPAND = new ThemeResource(
    "icon-expand.gif");
  private ApplicationResource resource = null;
  private Component vis;
  private SDocument result;
  private PluginSystem ps;
  private ResolverEntry entry;
  private Random rand = new Random();
  private Map<SNode, Long> markedAndCovered;
  private List<SToken> token;
  private Map<String, String> markersExact;
  private Map<String, String> markersCovered;
  private Button btEntry;
  private String htmlID;
  private String resultID;;
  private VisualizerPlugin visPlugin;
  private Set<String> visibleTokenAnnos;
  private STextualDS text;
  private String segmentationName;
  private boolean showTextID;
  private final String PERMANENT = "permanent";
  private final String ISVISIBLE = "visible";
  private final String HIDDEN = "hidden";
  private final String PRELOADED = "preloaded";

  private final static String htmlTemplate = 
    "<div id=\":id\"><div location=\"btEntry\"></div>"
    + "<div location=\"iframe\"></div></div>";
    
  /**
   * This Constructor should be used for {@link ComponentVisualizerPlugin}
   * Visualizer.
   *
   */
  public VisualizerPanel(
    final ResolverEntry entry,
    SDocument result,
    List<SToken> token,
    Set<String> visibleTokenAnnos,
    Map<SNode, Long> markedAndCovered,
    @Deprecated Map<String, String> markedAndCoveredMap,
    @Deprecated Map<String, String> markedExactMap,
    STextualDS text,
    String htmlID,
    String resultID,
    SingleResultPanel parent,
    String segmentationName,
    PluginSystem ps,
    boolean showTextID) throws IOException
  {
    super(new ByteArrayInputStream(htmlTemplate.replace(":id", htmlID).getBytes("UTF-8")));

    visPlugin = ps.getVisualizer(entry.getVisType());
    
    this.ps = ps;
    this.entry = entry;
    this.markersExact = markedExactMap;
    this.markersCovered = markedAndCoveredMap;


    this.result = result;
    this.token = token;
    this.visibleTokenAnnos = visibleTokenAnnos;
    this.markedAndCovered = markedAndCovered;
    this.text = text;
    this.segmentationName = segmentationName;
    this.htmlID = htmlID;
    this.resultID = resultID;

    this.showTextID = showTextID;

    this.addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    this.setWidth("100%");
  }

  @Override
  public void attach()
  {

    if (visPlugin == null)
    {
      entry.setVisType(PluginSystem.DEFAULT_VISUALIZER);
      visPlugin = ps.getVisualizer(entry.getVisType());
    }

    if(entry != null)
    {
      
      if(HIDDEN.equalsIgnoreCase(entry.getVisibility()))
      {
        // build button for visualizer
        btEntry = new Button(entry.getDisplayName()
          + (showTextID ? " (" + text.getSName() + ")" : ""));
        btEntry.setIcon(ICON_EXPAND);
        btEntry.setStyleName(ChameleonTheme.BUTTON_BORDERLESS + " "
          + ChameleonTheme.BUTTON_SMALL);
        btEntry.addListener((Button.ClickListener) this);
        addComponent(btEntry, "btEntry");
      }
      else
      {
        
        if ( ISVISIBLE.equalsIgnoreCase(entry.getVisibility())
          || PRELOADED.equalsIgnoreCase(entry.getVisibility()))
        {
          // build button for visualizer
          btEntry = new Button(entry.getDisplayName() 
            + (showTextID ? " (" + text.getSName() + ")" : ""));
          btEntry.setIcon(ICON_COLLAPSE);
          btEntry.setStyleName(ChameleonTheme.BUTTON_BORDERLESS + " "
            + ChameleonTheme.BUTTON_SMALL);
          btEntry.addListener((Button.ClickListener) this);
          addComponent(btEntry, "btEntry");
        }
        
        
        // create the visualizer and calc input
        try
        {
          vis = createComponent();
          vis.setVisible(true);
          addComponent(vis, "iframe");
        }
        catch(Exception ex)
        {
          getWindow().showNotification(
            "Could not create visualizer " + visPlugin.getShortName(), 
            ex.toString(),
            Window.Notification.TYPE_TRAY_NOTIFICATION
          );
          log.error("Could not create visualizer " + visPlugin.getShortName(), ex);
        }
        
        
        if (PRELOADED.equalsIgnoreCase(entry.getVisibility()))
        {
          btEntry.setIcon(ICON_EXPAND);
          vis.setVisible(false);
        }
        
      }
    }

  }
  
  private Component createComponent()
  {
    Application application = getApplication();
    VisualizerInput input = createInput();
    
    Component c = this.visPlugin.createComponent(input, application);
    
    return c;
  }

  private VisualizerInput createInput()
  {
    VisualizerInput input = new VisualizerInput();
    input.setAnnisWebServiceURL(getApplication().getProperty(
      "AnnisWebService.URL"));
    input.setContextPath(Helper.getContext(getApplication()));
    input.setDotPath(getApplication().getProperty("DotPath"));
    
    input.setId(resultID);

    input.setMarkableExactMap(markersExact);
    input.setMarkableMap(markersCovered);
    input.setMarkedAndCovered(markedAndCovered);
    input.setVisPanel(this);

    input.setResult(result);
    input.setToken(token);
    input.setVisibleTokenAnnos(visibleTokenAnnos);
    input.setText(text);
    input.setSegmentationName(segmentationName);

    if (entry != null)
    {
      input.setMappings(entry.getMappings());
      input.setNamespace(entry.getNamespace());
      String template = Helper.getContext(getApplication())
        + "/Resource/" + entry.getVisType() + "/%s";
      input.setResourcePathTemplate(template);
    }

    if (visPlugin.isUsingText()
      && result.getSDocumentGraph().getSNodes().size() > 0)
    {
      SaltProject p = getText(result.getSCorpusGraph().getSRootCorpus().
        get(0).getSName(), result.getSName());

      input.setDocument(p.getSCorpusGraphs().get(0).getSDocuments().get(0));

    }
    else
    {
      input.setDocument(result);
    }

    return input;
  }
  
  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {
    this.visibleTokenAnnos = annos;
    if(visPlugin != null && vis != null)
    {
      visPlugin.setVisibleTokenAnnosVisible(vis, annos);
    }
  }
  
  public void setSegmentationLayer(String segmentationName, Map<SNode, Long> markedAndCovered)
  {
    this.segmentationName = segmentationName;
    this.markedAndCovered = markedAndCovered;
    
    if(visPlugin != null && vis != null)
    {
      visPlugin.setSegmentationLayer(vis, segmentationName,markedAndCovered);
    }
  }

  public ApplicationResource createResource(
    final ByteArrayOutputStream byteStream,
    String mimeType)
  {

    StreamResource r;

    r = new StreamResource(new StreamResource.StreamSource()
    {
      @Override
      public InputStream getStream()
      {
        return new ByteArrayInputStream(byteStream.toByteArray());
      }
    }, entry.getVisType() + "_" + rand.nextInt(Integer.MAX_VALUE), getApplication());
    r.setMIMEType(mimeType);

    return r;
  }

  private SaltProject getText(String toplevelCorpusName, String documentName)
  {
    SaltProject txt = null;
    try
    {
      toplevelCorpusName = URLEncoder.encode(toplevelCorpusName, "UTF-8");
      documentName = URLEncoder.encode(documentName, "UTF-8");
      WebResource annisResource = Helper.getAnnisWebResource(getApplication());
      txt = annisResource.path("graphs").path(toplevelCorpusName).path(
        documentName).get(SaltProject.class);
    }
    catch (Exception e)
    {
      log.error("General remote service exception", e);
    }
    return txt;
  }

  @Override
  public void detach()
  {
    super.detach();

    if (resource != null)
    {
      getApplication().removeResource(resource);
    }
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    toggleVisualizer(true, null);
  }

  /**
   * Opens and closes visualizer.
   *
   * @param collapse when collapse is false, the Visualizer would never be
   * closed
   */
  @Override
  public void toggleVisualizer(boolean collapse, LoadableVisualizer.Callback callback)
  {
    if (btEntry.getIcon() == ICON_EXPAND)
    {

      // check if it's necessary to create input
      if (visPlugin != null && vis == null)
      {
        try
        {
          vis = createComponent();
          addComponent(vis, "iframe");
          
        }
        catch(Exception ex)
        {
          getWindow().showNotification(
            "Could not create visualizer " + visPlugin.getShortName(), 
            ex.toString(),
            Window.Notification.TYPE_WARNING_MESSAGE
          );
          log.error("Could not create visualizer " + visPlugin.getShortName(), ex);
        }
      }
      
      
      if(callback != null && vis instanceof LoadableVisualizer)
      {
        ((LoadableVisualizer) vis).addOnLoadCallBack(callback);
      }

      btEntry.setIcon(ICON_COLLAPSE);
      vis.setVisible(true);
    }
    else if (btEntry.getIcon() == ICON_COLLAPSE && collapse)
    {
      if (vis != null)
      {
        vis.setVisible(false);
      }

      btEntry.setIcon(ICON_EXPAND);
    }
  }

  public String getHtmlID()
  {
    return htmlID;
  }

}
