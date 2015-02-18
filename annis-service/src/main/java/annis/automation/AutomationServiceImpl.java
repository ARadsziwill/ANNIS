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
import annis.automation.scheduling.AutomatedCountQueryTask;
import annis.automation.scheduling.AutomatedQueryResult;
import annis.automation.scheduling.AutomatedQuery;
import annis.automation.scheduling.AutomatedQuerySchedulerListener;
import annis.ql.AqlParser;
import annis.security.ANNISSecurityManager;
import annis.security.ANNISUserConfigurationManager;
import annis.security.Group;
import annis.security.User;
import it.sauronsoftware.cron4j.SchedulingPattern;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
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
   
   private AnnisScheduler scheduler; 
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
    * API - test if the automated query, scheduling patterns etc. is legit
    * 
    * @return @class{AutomatedQueryResult} if everything is ok or an error... 
    */
   public Response test () 
   {
       throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
   
   
   /**
    * API - get a list of all User's personal queries
    * @return 
    */
   @GET
   @Path("scheduledQueries")
   @Produces("application/xml")
   public List<AutomatedQuery> getUserAutoQueries() 
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
   public Response createAutoQuery(
        @QueryParam("q") String query,
        @QueryParam("corpora") String rawCorpusNames,
        @QueryParam("schedulingPattern") String schedulingPattern,
        @QueryParam("description") String description,
        @QueryParam("owner") String owner,
        @QueryParam("isGroup") Boolean isGroup,
        @QueryParam("isActive") Boolean isActive) 
   {
       requiredParameter(query, "q", "AnnisQL query");
       requiredParameter(rawCorpusNames, "corpora",
               "comma separated list of corpus names");
       requiredParameter(schedulingPattern, "schedulingPattern",
               "cron-style scheduling pattern");
       
       Subject subject = SecurityUtils.getSubject();
       String username = (String) subject.getPrincipal();
       
       //set defaults
       if (owner == null)
       {
           owner = username;
           isGroup = false;
       }
       isGroup = (isGroup == null)? false : isGroup;
       isActive = (isActive == null)? false : isActive;
       description = (description == null)? "" : description;
       //end defaults
       //security checks
       if (isGroup)
       {
           subject.checkPermission("schedule:write:group:" + owner);
       } else
       {
           subject.checkPermission("schedule:write:user");
           if (!owner.equals(username))
            {
               throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
                        "Owner in query not the same as login name.").build());
            }           
       }
       //end security checks
       if (!SchedulingPattern.validate(schedulingPattern)) {
           throw new WebApplicationException(
                   Response.status(Response.Status.BAD_REQUEST).type(
                   MediaType.TEXT_PLAIN).entity(
                   "Invalid scheduling pattern: " + schedulingPattern).build());
       }
       List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
            
       AutomatedQuery queryData = new AutomatedQuery(query,
            corpusNames,
            schedulingPattern,
            description,
            owner,
            isGroup,
            isActive);
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
       if (!id.equals(query.getId()) || !scheduler.idExists(id))
       {
           throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
           "QueryId in Object is not the same as in path.").build());
       }
       
       AutomatedQuery old = scheduler.getQuery(id);       
       
       //check permissions
       Subject subject = SecurityUtils.getSubject();
       String username = (String) subject.getPrincipal(); 
       log.info(username);
       log.info(query.getOwner());
       if (query.getIsOwnerGroup())
        {
            subject.checkPermission("schedule:writegroup:" + query.getOwner());
            if (!getConfManager().getGroups().containsKey(query.getOwner()))
            {
               throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(
                        "No such group.").build());
            }
        }
        else
        {
            subject.checkPermission("schedule:write:user");
            if (!query.getOwner().equals(username))
            {
              return  Response.status(Response.Status.BAD_REQUEST).entity(
                        "Owner in query not the same as login name.").build();
            }
        }        
        if (old.getIsOwnerGroup())
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
            if (toDelete.getIsOwnerGroup())
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
       try{
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
       catch (NullPointerException nex)
       {
           System.err.println(nex.getMessage());
           nex.printStackTrace();
           return new ArrayList<>();
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
  
    /**
   * Splits a list of corpus names into a proper java list.
   *
   * @param rawCorpusNames The corpus names separated by ",".
   * @return
   */
  private List<String> splitCorpusNamesFromRaw(String rawCorpusNames)
  {
    return Arrays.asList(rawCorpusNames.split(","));
  }
  
  public void setListener(AutomatedQuerySchedulerListener listener)
  {
      this.listener = listener;
  }
  
}
