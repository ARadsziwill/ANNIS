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
package annis.sqlgen.extensions;

import annis.sqlgen.FrequencySqlGenerator;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This is an extension to be used as argument for {@link FrequencySqlGenerator}
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class FrequencyTableQueryData extends ArrayList<FrequencyTableQueryData.Entry>
{
  public static enum Type
  {
    span, annotation
  }
  
  public static class Entry implements Serializable
  {
    private Type type;
    private String key;
    private int referencedNode;

    public Type getType()
    {
      return type;
    }

    public void setType(Type type)
    {
      this.type = type;
    }

    public String getKey()
    {
      return key;
    }

    public void setKey(String key)
    {
      this.key = key;
    }

    public int getReferencedNode()
    {
      return referencedNode;
    }

    public void setReferencedNode(int referencedNode)
    {
      this.referencedNode = referencedNode;
    }
    
  }
  
}
