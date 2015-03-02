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

import annis.adapter.DateTimeAdapter;
import java.io.Serializable;
import org.joda.time.DateTime;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This class represents the result of an automated query execution. 
 * 
 * 
 * 
 * @author Andreas
 */
 @XmlRootElement
public class AutomatedQueryResult implements Serializable{
   
    @XmlElement(required = true)
    private final AutomatedQuery query;
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private final DateTime executed;
    @XmlElement(required = true)
    private final String result;

    private AutomatedQueryResult()
    {
        this(null, null, null);
    }
            
    public AutomatedQueryResult(AutomatedQuery query, String result, DateTime date) {
        this.executed = date;
        this.query = query;
        this.result = result;
    }

    public AutomatedQuery getQuery() {
        return query;
    }

    public DateTime getExecuted() {
        return executed;
    }

    public String getResult() {
        return result;
    }  
    
    @Override
    public boolean equals(Object o)
    {
      if (o instanceof AutomatedQueryResult)
      {
        return ((AutomatedQueryResult) o).getExecuted().isEqual(this.getExecuted());
      }
      else 
      {
        return false;
      }
    }
}
