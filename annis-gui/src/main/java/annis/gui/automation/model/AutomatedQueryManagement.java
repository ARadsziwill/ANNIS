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

import annis.automation.AutomatedQuery;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Andreas Radsziwill <radsziwill@stud.tu-darmstadt.de>
 */
public class AutomatedQueryManagement
{
  
  private final Logger log = LoggerFactory.getLogger(AutomatedQueryManagement.class);
 
  private WebResource rootResource;
  
  private final Map<UUID, AutomatedQuery> queries = new TreeMap<>();
  
  public void clear()
  {
    queries.clear();
  }
  
  public boolean fetchFromService()
  {
    if (rootResource != null)
    {
      WebResource res = rootResource.path("automation/scheduledQueries");
      queries.clear();
      try
      {
        List<AutomatedQuery> list = res.get(new GenericType<List<AutomatedQuery>>() {});
        
        for(AutomatedQuery q : list)
        {
          queries.put(q.getId(), q);
        }
        return true;
      }
      catch (UniformInterfaceException ex)
      {
        log.error("Could not get the list of queries", ex);
      }
    }
    return false;
  }
  
  public boolean createAutomatedQuery(AutomatedQuery newQuery)
  {
    if(rootResource != null)
    {
      WebResource res = rootResource.path("automation/scheduledQueries");
      try 
      {
        res.post(newQuery);
        queries.put(newQuery.getId(), newQuery);
        
        return true;
      }
      catch(UniformInterfaceException ex)
      {
        log.warn("Could not create automated query: " + ex.getResponse().getEntity(String.class) , ex);
      }
    }
    return false;
  }
  
  public boolean updateAutomatedQuery(AutomatedQuery newQuery)
  {
    if(rootResource != null)
    {
      WebResource res = rootResource.path("automation/scheduledQueries").path(
        newQuery.getId().toString());
      try 
      {
        res.put(newQuery);
        queries.put(newQuery.getId(), newQuery);
        return true;
      }
      catch (UniformInterfaceException ex)
      {
        log.warn("Could not update query", ex);
      }
    }
    return false;
  }
  
  public boolean deleteQuery(UUID queryId)
  {
    if (rootResource != null)
    {
      WebResource res = rootResource.path("automation/scheduledQueries").path(
          queryId.toString());
      try 
      {
      res.delete();
      queries.remove(queryId);
      return true;
      }
      catch (UniformInterfaceException ex)
      {
        log.warn("Could not delete query", ex);
      }
    }
    return false;
  }

  public Collection<AutomatedQuery> getQueries()
  {
    return queries.values();
  }
  
  public WebResource getRootResource()
  {
    return rootResource;
  }

  public void setRootResource(WebResource rootResource)
  {
    this.rootResource = rootResource;
  }
  
}
