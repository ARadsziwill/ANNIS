/*
 * Copyright 2014 SFB 632.
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

package annis.visualizers.component.kwic;

import annis.libgui.media.MediaController;
import annis.libgui.media.PDFController;
import annis.libgui.visualizers.VisualizerInput;
import annis.visualizers.component.grid.GridComponent;

/**
 * A component to visualize matched token and their context as "Keyword in context"
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class KWICComponent extends GridComponent
{
  public KWICComponent(VisualizerInput input, 
    MediaController mediaController, PDFController pdfController)
  {
    super(input, mediaController, pdfController);
  }

  @Override
  protected boolean isShowingTokenAnnotations()
  {
    // always show token annnotations
    return true;
  }

  @Override
  protected boolean isShowingSpanAnnotations()
  {
    // never show span annotations
    return false;
  }
  
  
  
}
