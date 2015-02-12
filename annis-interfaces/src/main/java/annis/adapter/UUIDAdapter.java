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

import java.util.UUID;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andreas
 */
public class UUIDAdapter extends XmlAdapter<String, UUID>
{
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    UUIDAdapter.class);

  @Override
  public UUID unmarshal(String v) throws Exception
  {
      return UUID.fromString(v);
  }

  @Override
  public String marshal(UUID v) throws Exception
  {
    return v.toString();
  }
  
}
