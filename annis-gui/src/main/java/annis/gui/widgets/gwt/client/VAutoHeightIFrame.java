package annis.gui.widgets.gwt.client;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.VConsole;

public class VAutoHeightIFrame extends Widget implements Paintable
{

  /** Set the CSS class name to allow styling. */
  public static final String CLASSNAME = "v-autoheightiframe";
  /** The client side widget identifier */
  protected String paintableId;
  /** Reference to the server connection object. */
  ApplicationConnection gClient;
  private IFrameElement iframe;

  /**
   * The constructor should first call super() to initialize the component and
   * then handle any initialization relevant to Vaadin.
   */
  public VAutoHeightIFrame()
  {
    super();

    iframe = Document.get().createIFrameElement();

    setElement(iframe);


    // This method call of the Paintable interface sets the component
    // style name in DOM tree
    setStyleName(CLASSNAME);

    addDomHandler(new LoadHandler()
    {

      @Override
      public void onLoad(LoadEvent event)
      {
        //VConsole.log("loadhandler: " + iframe.getSrc());
        int additional = 50;
        if(!iframe.getSrc().endsWith("empty.html"))
        {
          //VConsole.log("loadhandler: survived first check");
          Document doc = null;
          try
          {
            doc = iframe.getContentDocument();
          }          
          catch(JavaScriptException ex)
          {
            VConsole.log("trying to access iframe source from different domain which is forbidden");
          }
          
          if(doc != null)
          {
            int newHeight = -1;
            String contentType = doc.getDocumentElement().getPropertyString("contentType");
            if(contentType != null && contentType.startsWith("image/"))
            {
              // image
              NodeList<Element> imgList = doc.getElementsByTagName("img");
              if(imgList.getLength() > 0)
              {
                ImageElement img = (ImageElement) imgList.getItem(0);
                newHeight = img.getPropertyInt("naturalHeight");
              }
            }
            else if(doc.getBody().getScrollHeight() > 20)
            {
              // real html page
              newHeight = doc.getBody().getScrollHeight() + additional;
            }
            
            if(newHeight > -1)
            {
              
              //VConsole.log("new height is " + newHeight + " (with additional " + additional + ")");
              gClient.updateVariable(paintableId, "height", newHeight, true);
            }
            
          }

        }
      }
    }, LoadEvent.getType());

    iframe.setFrameBorder(0);
  }

  /**
   * Called whenever an update is received from the server 
   */
  @Override
  public void updateFromUIDL(UIDL uidl, ApplicationConnection client)
  {

    
    // This call should be made first. 
    // It handles sizes, captions, tooltips, etc. automatically.
    if(client.updateComponent(this, uidl, true))
    {
      // If client.updateComponent returns true there has been no changes and we
      // do not need to update anything.
      return;
    }
    
    String url = uidl.getStringAttribute("url");

    if(iframe.getSrc() != null && url != null && iframe.getSrc().equals(url))
    {
      return;
    }
    // Save reference to server connection object to be able to send
    // user interaction later
    this.gClient = client;

    // Save the client side identifier (paintable id) for the widget
    paintableId = uidl.getId();


    final Style style = iframe.getStyle();

    style.setWidth(
      100, Style.Unit.PCT);

    if(url != null)
    {
      url =  client.translateVaadinUri(url);
      
      //VConsole.log("iframe is updated with url " + url );
      iframe.setSrc(url);
    }
  }
}
