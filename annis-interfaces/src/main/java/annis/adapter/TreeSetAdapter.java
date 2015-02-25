/*
 * Copyright 2015 SFB 632.
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

package annis.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Andreas
 */
public class TreeSetAdapter extends XmlAdapter<String, TreeSet>
{
  
  @Override
  public TreeSet unmarshal(String v) throws Exception
  {
    TreeSet result = new TreeSet();
    String[] values = StringUtils.split(v, ",");
    for (String string : values)
    {
      result.add(string.trim());
    }
    return result;
  }

  @Override
  public String marshal(TreeSet v) throws Exception
  {
    return StringUtils.join(v, ", ");
  }
}
