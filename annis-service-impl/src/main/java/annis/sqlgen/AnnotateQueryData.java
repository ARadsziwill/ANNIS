/*
 * Copyright 2012 SFB 632.
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
package annis.sqlgen;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author benjamin
 */
public class AnnotateQueryData
{

  private int left;
  private int right;
  private String segmentationLayer;

  public AnnotateQueryData(int left, int right)
  {
    this(left, right, null);
  }
  
  public AnnotateQueryData(int left, int right, String segmentationLayer)
  {
    super();
    this.left = left;
    this.right = right;
    this.segmentationLayer = segmentationLayer;
  }

  public int getLeft()
  {
    return left;
  }

  public int getRight()
  {
    return right;
  }

  public String getSegmentationLayer()
  {
    return segmentationLayer;
  }
  
  

  @Override
  public String toString()
  {
    List<String> fields = new ArrayList<String>();

    if (left > 0)
    {
      fields.add("left = " + left);
    }
    if (right > 0)
    {
      fields.add("right = " + right);
    }
    if(segmentationLayer != null)
    {
      fields.add("segLayer = " + segmentationLayer);
    }
    return StringUtils.join(fields, ", ");
  }
}
