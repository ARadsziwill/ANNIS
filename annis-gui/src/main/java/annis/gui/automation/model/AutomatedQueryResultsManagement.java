/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.automation.model;

import com.sun.jersey.api.client.WebResource;
import java.util.Map;
import java.util.TreeMap;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import annis.automation.AutomatedQueryResult;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Andreas Radsziwill <radsziwill@stud.tu-darmstadt.de>
 */
public class AutomatedQueryResultsManagement
{
  
  private final Logger log = LoggerFactory.getLogger(AutomatedQueryResultsManagement.class);
  
  private WebResource rootResource;
  
  private final Map <DateTime, AutomatedQueryResult> results = new TreeMap<>();
  
  public void clear()
  {
    results.clear();
  }
  
  public Collection<AutomatedQueryResult> getResults()
  {
    return results.values();
  }
  
  public boolean fetchFromService()
  {
    if (rootResource != null)
    {
      WebResource res = rootResource.path("automation/results");
      results.clear();
      try
      {
        List<AutomatedQueryResult> list = res.get(new GenericType<List<AutomatedQueryResult>>() {});
        
        for (AutomatedQueryResult r : list)
        {
          results.put(r.getExecuted(), r);
        }
        return true;
      }
      catch (UniformInterfaceException ex)
      {
        log.error("Could not get the list of results:  " + ex.getResponse().getEntity(String.class), ex);
      }
    }
    return false;
  }
  
  public boolean deleteResults(Collection<UUID> ids, DateTime compareDate)
  {
    if (rootResource != null)
    {
      if (compareDate == null)
      {
        compareDate = new DateTime();
      }
      WebResource res = rootResource.path("automation/results/olderThan").queryParam("date", compareDate.toString()).queryParam("ids", StringUtils.join(ids, ","));

      try
      {
        res.delete();
        return true;
      }
      catch (UniformInterfaceException ex)
      {
        log.warn("Could not delete results\n " + ex.getResponse().getEntity(String.class), ex);
      }
    }
    return false;
  }
  
  public void setRootResource(WebResource rootResource)
  {
    this.rootResource = rootResource;
  }

  public boolean deleteResults(Collection<DateTime> dates)
  {
    if (rootResource != null)
    {
      WebResource res = rootResource.path("automation/results").queryParam("dates", StringUtils.join(dates, ","));
      
      try 
      {
        res.delete();
        return true;
      }
      catch (UniformInterfaceException ex)
      {
         log.warn("Could not delete results\n " + ex.getResponse().getEntity(String.class), ex);
      }
    }
    return false;
  }
  
}
