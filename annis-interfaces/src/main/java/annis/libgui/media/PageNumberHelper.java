/*
 * Copyright 2013 SFB 632.
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
package annis.libgui.media;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class PageNumberHelper
{
  public static final String PAGE_NUMBER_ANNOATION_NAMESPACE = "annis";

  public static final String PAGE_NUMBER_ANNOATATION_NAME = "pageNumber";

  public static String getPageNumberFromAnnotation(SSpan node)
  {
    for (SAnnotation anno : node.getSAnnotations())
    {
      if (getQualifiedPageNumberAnnotationName().equals(anno.getQName()))
      {
        return anno.getSValueSTEXT();
      }
    }

    return null;
  }

  public static String getQualifiedPageNumberAnnotationName()
  {
    return PAGE_NUMBER_ANNOATION_NAMESPACE + "::" + PAGE_NUMBER_ANNOATATION_NAME;
  }
}
