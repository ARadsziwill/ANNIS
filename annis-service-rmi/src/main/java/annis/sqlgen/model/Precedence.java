/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
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
package annis.sqlgen.model;

import annis.model.QueryNode;

public class Precedence extends RangedJoin
{

  private String segmentationName;
  
  public Precedence(QueryNode target)
  {
    this(target, 0, 0);
  }
  
  public Precedence(QueryNode target, String segmentationName)
  {
    this(target, 0, 0, segmentationName);
  }

  public Precedence(QueryNode target, int distance)
  {
    this(target, distance, distance);
  }
  
  public Precedence(QueryNode target, int distance, String segmentationName)
  {
    this(target, distance, distance, segmentationName);
  }
  
  public Precedence(QueryNode target, int minDistance, int maxDistance)
  {
    this(target, minDistance, maxDistance, null);
  }
  
  public Precedence(QueryNode target, int minDistance, int maxDistance, String segmentationName)
  {
    super(target, minDistance, maxDistance);
    this.segmentationName = segmentationName;
  }

  public String getSegmentationName()
  {
    return segmentationName;
  }

  public void setSegmentationName(String segmentationName)
  {
    this.segmentationName = segmentationName;
  }
  
  
  @Override
  public String toString()
  {
    return "precedes node " + target.getId() + " (" +  segmentationName + " " 
      + minDistance + ", "
      + maxDistance + ")";
  }
}
