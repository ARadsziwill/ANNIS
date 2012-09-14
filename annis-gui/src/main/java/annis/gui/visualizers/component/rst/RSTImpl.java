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
package annis.gui.visualizers.component.rst;

import annis.gui.visualizers.VisualizerInput;
import annis.gui.widgets.JITWrapper;
import annis.gui.widgets.gwt.client.VJITWrapper;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class RSTImpl extends Panel
{
  
  private final JITWrapper jit;
  private final Logger log = LoggerFactory.getLogger(RSTImpl.class);

  public RSTImpl(VisualizerInput visInput)    
  {
    jit = new JITWrapper();
    log.debug("initialize RSTImpl");
  }
}
