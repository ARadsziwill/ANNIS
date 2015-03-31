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

package annis.automation;

import annis.adapter.AnyTypeAdapter;
import annis.service.objects.FrequencyTable;
import annis.service.objects.MatchAndDocumentCount;
import annis.service.objects.MatchGroup;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andreas
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({FrequencyTable.class, MatchAndDocumentCount.class, MatchGroup.class})
public class ResultWrapper<T extends Serializable> implements Serializable 
{

    private final Logger log = LoggerFactory.getLogger(ResultWrapper.class);
    
    @XmlJavaTypeAdapter(AnyTypeAdapter.class)
    private T result;
    
    public T getResult()
    {
      return result;
    }
    
    public void setResult(T result)
    {
      this.result = result;
    }
      
    private ResultWrapper()
    {
      this(null);
    }
    
    public ResultWrapper(T result)
    {
      this.result = result;
    }
    
    @Override
    public String toString()
    {
      return getResult().toString();
    }
}
