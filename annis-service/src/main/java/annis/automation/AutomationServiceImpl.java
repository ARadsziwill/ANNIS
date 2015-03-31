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

import annis.automation.scheduling.AnnisScheduler;
import annis.automation.scheduling.AnnisSchedulerImpl;
import annis.automation.scheduling.AutomatedQuerySchedulerListener;
import annis.security.ANNISSecurityManager;
import annis.security.ANNISUserConfigurationManager;
import it.sauronsoftware.cron4j.SchedulingPattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class handles automated queries.
 * 
 * 
 * 
 * @author Andreas Radsziwill <radsziwill@stud.tu-darmstadt.de>
 */

@Component 
@Path("annis/automation")
public class AutomationServiceImpl /*implements AutomationService */
{
    
   private final static Logger log = LoggerFactory.getLogger(
           AutomationServiceImpl.class);
   
   private AnnisSchedulerImpl scheduler; 
   private AutomatedQuerySchedulerListener listener;
       
   public void init() 
   {
       // add AutomatedQuerySchedulerListener
       scheduler.addSchedulerListener(listener);
       scheduler.setDaemon(true);
       scheduler.start();
       
       log.info("ANNIS AutomationService loaded.");
    } 
   
   /**
    * API - get a list of all User's personal queries
    * @return 
    */
   @GET
   @Path("scheduledQueries")
   @Produces("application/xml")
   public List<AutomatedQuery> getAutoQueries() 
   {
       Subject user = SecurityUtils.getSubject();
       user.checkPermission("schedule:read:user");
       String userName = (String) user.getPrincipal();
       //personal queries or an empty list
       List<AutomatedQuery> queries = scheduler.getUserQueries(userName);
       //group queries
       ANNISUserConfigurationManager confManager = getConfManager();
       if (confManager != null){    
       
            for (String group : confManager.getGroups().keySet())
            {
                if (user.isPermitted("schedule:readgroup:"+group))
                {
                    queries.addAll(scheduler.getGroupQueries(group));
                }
            }
       }
       
       return queries;
   }
  
   /**
    * Api - add a Query for the User
    * 
    */
   @POST
   @Path("scheduledQueries")
   @Consumes("application/xml")
   public Response createAutoQuery(AutomatedQuery queryData) 
   {
       Subject subject = SecurityUtils.getSubject();
       String username = (String) subject.getPrincipal();
       
       queryData.setDefaults(username);
       
       //security checks
       if (queryData.getIsGroup())
       {
           subject.checkPermission("schedule:write:group:" + queryData.getOwner());
       } else
       {
           subject.checkPermission("schedule:write:user");
           if (!queryData.getOwner().equals(username))
            {
               throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
                        "Owner in query not the same as login name.").build());
            }           
       }
       //end security checks
       if (!SchedulingPattern.validate(queryData.getSchedulingPattern())) {
           throw new WebApplicationException(
                   Response.status(Response.Status.BAD_REQUEST).type(
                   MediaType.TEXT_PLAIN).entity(
                   "Invalid scheduling pattern: " + queryData.getSchedulingPattern()).build());
       }
       
      if (scheduler.addAutomatedQuery(queryData))
      {
         return Response.created(null).build();
      }
      throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
              "Could not save Query").build());
   }
   
   /**
    * API - change an existing query 
    */
   @PUT
   @Path("scheduledQueries/{queryId}")
   @Consumes("application/xml")
   public Response updateAutoQuery(
           AutomatedQuery query,
   @PathParam("queryId") UUID id)
   {   
       //sanitycheck sameId?
       if (!id.equals(query.getId()))
       {
           throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
           "QueryId in Object is not the same as in path.").build());
       } 
       if (!scheduler.idExists(id)) {
           throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
                   "Query to edit does not exist").build());
       }
       
       AutomatedQuery old = scheduler.getQuery(id);       
       
       //check permissions
       Subject subject = SecurityUtils.getSubject();
       String username = (String) subject.getPrincipal();
       
       if (query.getIsGroup())
        {
            subject.checkPermission("schedule:writegroup:" + query.getOwner());
            if (!getConfManager().getGroups().containsKey(query.getOwner()))
            {
               throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
                        "No such group.").build());
            }
        }
       else //personal query owner is probably null
        {
            if (query.getOwner() == null)
            {
                query.setOwner(username);
            }
            subject.checkPermission("schedule:write:user");
            if (!query.getOwner().equals(username))
            {
              return  Response.status(Response.Status.BAD_REQUEST).entity(
                        "Owner in query not the same as login name.").build();
            }
        }        
        if (old.getIsGroup())
        {
            subject.checkPermission("schedule:writegroup:" + old.getOwner());
        }
        else 
        {            
            if (!old.getOwner().equals(username))
            {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
                        "Can't change other users's query").build());
            }
        }
                
       if (scheduler.updateAutomatedQuery(query, old))
       {
           return Response.ok().build();
       }
       return Response.notModified().entity("Query not updated").build();
   }
   
   /**
    * API - delete a query from the list
    */
   @DELETE
   @Path("scheduledQueries/{queryId}")
   public Response deleteAutoQuery(@PathParam("queryId") UUID queryId)
   {
       if (scheduler.idExists(queryId))
       {
           //sanity check, allowed to delete the query?
           AutomatedQuery toDelete = scheduler.getQuery(queryId);
           
           Subject subject = SecurityUtils.getSubject();
           String username = (String) subject.getPrincipal();
            if (toDelete.getIsGroup())
            {
                subject.checkPermission("schedule:writegroup:" + toDelete.getOwner());
            } else
            {
                subject.checkPermission("schedule:write:user");
                if (!toDelete.getOwner().equals(username))
                {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
                        "Can't delete other users's query").build());
                }
            }
            if (scheduler.deleteQuery(toDelete))
            {
                return Response.ok().build();
            }
       }
       throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
               "Could not delete query").build());
   }
   
   @GET
   @Path("results")
   @Produces("application/xml")
   public List<AutomatedQueryResult> getAutoQueryResults()
   {
        List<String> filters = new ArrayList<>();

        Subject subject = SecurityUtils.getSubject();
        String username = (String) subject.getPrincipal();

        filters.add(username);

        ANNISUserConfigurationManager confManager = getConfManager();
        if (confManager != null && confManager.getUser(username) != null){    

             for (String group : confManager.getUser(username).getGroups())
             {
                 if (subject.isPermitted("schedule:readResult:"+group))
                 {
                     filters.add(group);
                 }
             }
        }

        return scheduler.getQueryResults(filters);
    }
   
   @DELETE
   @Path("results")
   public Response deleteResults(
   @QueryParam("dates") String exeDates)
   {
       requiredParameter(exeDates, "dates", "The execution dates of results that should be deleted");
       
       Subject subject = SecurityUtils.getSubject();
       String username = (String) subject.getPrincipal();
       
       try
       {
           Set<String> dates = splitDatesFromRaw(exeDates);
           List<AutomatedQueryResult> toDelete = new LinkedList<>(scheduler.getResults());
           
           //filter 
           Iterator<AutomatedQueryResult> it = toDelete.iterator();
           boolean notAuthorized = false;
           
           while (it.hasNext())
           {
               AutomatedQueryResult aqr = it.next();
               
               if (!dates.contains(aqr.getExecuted().toString()))
               {
                   it.remove();
               }
               else
               {
                    //security
                    if (aqr.getQuery().getIsGroup())
                    {
                        if (!subject.isPermitted("schedule:deleteResult:" + aqr.getQuery().getOwner()))
                        {
                            it.remove();
                            notAuthorized = true;
                        }
                    }
                    else //personal query
                    {
                        if (!username.equals(aqr.getQuery().getOwner()))
                        {
                            it.remove();
                            notAuthorized = true;
                        }
                    }
               }
           }
           scheduler.deleteResults(toDelete);
           return notAuthorized? Response.ok("Some Results haven't been deleted.").build() : Response.ok().build();
       }
       catch(IllegalArgumentException ex) 
       {
           throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
       }      
   }
   
   /**   
    * Deletes all AutomatedQueryResults whose "executed" date is older than the 
    * specified date. Only deletes results of AutomatedQueries whose ids are 
    * contained in the queryIds
    * 
    * @param compareDate The date to compare with
    * @param queryIds The AutomatedQuery IDs whose results should be deleted
    */
   
   @DELETE
   @Path("results/olderThan")
   public Response deleteResults(
   @QueryParam("date") String compareDate,
   @QueryParam("ids") String queryIds)
   {
       requiredParameter(compareDate, "date", "The date with which to compare");  
       requiredParameter(queryIds, "ids", "The AutomatedQuery IDs whose results should be deleted");
       Subject subject = SecurityUtils.getSubject();
       String username = (String) subject.getPrincipal();
       try
       {
            DateTime date = DateTime.parse(compareDate);
            Set<UUID> ids = splitIdsFromRaw(queryIds);
            
            Set<String> allowedGroups = new TreeSet<>();
            
            boolean notAuthorized = false;
            
            Iterator<UUID> it = ids.iterator();
            while (it.hasNext())
            {
                AutomatedQuery q = scheduler.getQuery(it.next());
                if(!q.getIsGroup())
                {
                    if(!username.equals(q.getOwner()))
                    {
                        it.remove();    //Not my Task
                        notAuthorized = true;
                    }   
                }
                else
                {
                    if(subject.isPermitted("schedule:deleteResult:" + q.getOwner()))
                    {
                        allowedGroups.add(q.getOwner());
                    }
                    else
                    {
                        it.remove();  //Not allowed in this group
                        notAuthorized = true;
                    }
                }
            }
            scheduler.deleteResults(date, ids);          
            return notAuthorized? Response.ok("Some Results haven't been deleted.").build() : Response.ok().build();
       }
       catch (IllegalArgumentException ex)
       {
           log.error("Ids: ", ex);
           throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
       }       
   }
     
   private ANNISUserConfigurationManager getConfManager()
   {
    if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager)
    {
      ANNISUserConfigurationManager confManager
        = ((ANNISSecurityManager) SecurityUtils.getSecurityManager()).
        getConfManager();
      return confManager;
    }
    return null;
   }

    public AnnisScheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(AnnisSchedulerImpl scheduler) {
        this.scheduler = scheduler;
    }
    
   /**
   * Throw an exception if the parameter is missing.
   *
   * @param value Value which is checked for null.
   * @param name The short name of parameter.
   * @param description A one line description of the meaing of the parameter.
   */
  private void requiredParameter(String value, String name, String description)
    throws WebApplicationException
  {
    if (value == null)
    {
      throw new WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required parameter '" + name + "' (" + description + ")").
        build());
    }
  }
    
  private Set<UUID> splitIdsFromRaw(String rawIds)
  {
      List<String> tmp = Arrays.asList(rawIds.split(","));
      Set<UUID> result = new HashSet<>();
      for(String id : tmp)
      {
          result.add(UUID.fromString(id));
      }
      return result;
  }
  
  private Set<String> splitDatesFromRaw(String rawDates)
  {
      return new HashSet<>(Arrays.asList(rawDates.split(",")));
  }
  
  public void setListener(AutomatedQuerySchedulerListener listener)
  {
      this.listener = listener;
  }
  
}
