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
package annis.gui.widgets.gwt.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class VMediaPlayerBase extends Widget implements Paintable
{
  public static final String PLAY = "play";
  public static final String PAUSE = "pause";
  public static final String SOURCE_URL = "url";
  
  private MediaElement media;

  public VMediaPlayerBase(MediaElement media)
  {
    this.media = media;
    setElement(this.media);
    
    media.setControls(true);
    media.setAutoplay(false);
    media.setPreload(MediaElement.PRELOAD_METADATA);
    media.setLoop(false);
  }
  
  @Override
  public void updateFromUIDL(UIDL uidl, ApplicationConnection client)
  {
    if (client.updateComponent(this, uidl, true))
    {
      return;
    }
    
    if(media == null)
    {
      VConsole.error("media not set!!!");
      return;
    }
    
    if(uidl.hasAttribute(SOURCE_URL))
    {
      media.setSrc(uidl.getStringAttribute(SOURCE_URL));      
    }
    
    if(uidl.hasAttribute(PLAY))
    {
      String[] time = uidl.getStringArrayAttribute(PLAY);
      if(time.length == 1)
      {
        media.setCurrentTime(Double.parseDouble(time[0]));
      }
      else if(time.length == 2)
      {
        media.setCurrentTime(Double.parseDouble(time[0]));
        setEndTimeOnce(media, Double.parseDouble(time[1]));
      }
      media.play();
    }
    else if(uidl.hasAttribute(PAUSE))
    {
      media.pause();
    }
  }
  
  public String getMimeType()
  {
    Exception ex = new UnsupportedOperationException(
      "Please overwrite and implement VMediaPlayerBase.getMimeType()");
    VConsole.error(ex);
    return null;
  };
  
  
  private native void setEndTimeOnce(Element elem, double endTime) 
  /*-{
    var media =  $wnd.$(elem); // wrap element with jquery
    var timeHandler = function()
    {
      if (endTime !== null && media[0].currentTime >= endTime)
      {       
        media[0].pause();  
      }    
    };
    media.on("timeupdate", timeHandler);
    media.on("pause", function()
    {
      media[0].off(timeHandler);
    }); 
  }-*/;

  public MediaElement getMedia()
  {
    return media;
  }
  
  
  
}
