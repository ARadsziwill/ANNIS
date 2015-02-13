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
package annis.automation.scheduling;

import annis.dao.AnnisDao;
import it.sauronsoftware.cron4j.TaskCollector;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal Scheduling Manager for the AutomationService
 * 
 * @author Andreas
 */
public class AnnisSchedulerImpl extends AnnisScheduler  {
    
    private final static Logger log = LoggerFactory.getLogger(AnnisSchedulerImpl.class);
    private AnnisDao annisDao;
    private final String baseDir;
    
    private File rootDir;
    
    private final AutomatedQueryResultsFileHandler results;
    //maps user or group name to TaskCollector which manages the auto Queries 
    //in one file per user or group
    private final Map<String, AutomatedQueryTaskCollector> userTaskCollectors; 
    private final Map<String, AutomatedQueryTaskCollector> groupTaskCollectors;

    public AnnisSchedulerImpl(String baseDir)
    {
        this.baseDir = baseDir;
        this.results = new AutomatedQueryResultsFileHandler(baseDir, "results.ser");
        this.userTaskCollectors = new HashMap<>();
        this.groupTaskCollectors = new HashMap<>();
    }

    public void init() {
        if (baseDir != null)
        {
            rootDir = new File(baseDir);
            if (rootDir != null && rootDir.isFile())
            {
                log.error("Couldn't open specified directory for automated queries. "
                        + "A file with the same name exists. "
                        + baseDir);
                System.exit(2);
            }
            if (!rootDir.exists()){
                rootDir.mkdirs();
            } 
            if (rootDir.isDirectory()){
                initCollectorsInDir("users", userTaskCollectors);
                initCollectorsInDir("groups", groupTaskCollectors);
            }
        }
        log.info("Annis Scheduler started");
    }
    
    /**
    * Helper method for the initialization of the Automation Service
    * Reads every file in rootDir/dirname and creates a @link{AutomatedQueryTaskCollector} 
    * for it. The filename and collector are then put into the collectorMap
    * 
    * @param dirName
    * @param collectorMap 
    */
   private void initCollectorsInDir(String dirName, Map<String,
           AutomatedQueryTaskCollector> collectorMap)
   {
       File colDir = new File(rootDir, dirName);
       if (colDir.exists() && colDir.isDirectory())
       {
           for (File f: colDir.listFiles())
           {
               if (f.isFile()) 
               {
                   final AutomatedQueryTaskCollector taskCollector = new AutomatedQueryTaskCollector(f);
                   collectorMap.put(f.getName(), taskCollector);
                   addTaskCollector(taskCollector);
               }
           }
       } else
       {
           colDir.mkdir();
       }
   }

   public boolean deleteQuery(AutomatedQuery query)
   {
       AutomatedQueryTaskCollector collector = findCollectorForId(query.getId());
       
       if (collector != null)
       {
           return collector.deleteTask(query.getId());
       }
       return false;
   }
   
   private AutomatedQueryTaskCollector findCollectorForId(UUID id)
   {
       int count = 0;
       AutomatedQueryTaskCollector ref = null;
       
       for (AutomatedQueryTaskCollector col : userTaskCollectors.values())
       {
           if (col.containsId(id))
           {
               count++;
               ref = col;
           }
       }
       for (AutomatedQueryTaskCollector col : groupTaskCollectors.values())
       {
           if (col.containsId(id))
           {
               count++;
               ref = col;
           }
       }
       if (count > 1)
       {
          log.warn("Duplicate detected for Task with Id: " + id);
       }
       return ref;
   }
   
   public AutomatedQuery getQuery(UUID id)
   {
       AutomatedQueryTaskCollector col = findCollectorForId(id);
       if (col != null)
       {
           return col.getTask(id).getQuery();
       }
       throw new IllegalArgumentException("Id does not exist.");
   }
   
   /**
    * 
    * @param id - The id to check
    * @return true if the id is associated with any of the schedulers task collectors
    */
   public boolean idExists(UUID id)
   {
       return (findCollectorForId(id) == null)? false : true;
   }
   
   public List<AutomatedQueryResult> getResults()
   {
       return results.getResults();
   }
           
    @Override
   public void addResult(AutomatedQueryResult result)
   {
       results.addResult(result);
   }
    
   @Override
   public boolean addAutomatedQuery(AutomatedQuery query)
   {
       if (query.getIsOwnerGroup())
       {
           return addAutomatedGroupQuery(query);
       }
       else
       {
           return addAutomatedUserQuery(query);
       }
              
   }
   
   @Override
   public boolean updateAutomatedQuery(AutomatedQuery query, AutomatedQuery old)
   {
       if ((!query.getIsOwnerGroup() ^ old.getIsOwnerGroup()) &&    //!xor  
               old.getOwner().equals(query.getOwner()))
            // both are user or both are group query  AND
            // both owners are the same
       {
           log.info("Simple update.");
           return updateAutomatedQuery(query);
       }
       else //xor
            // one is user and one is group query  OR 
            // owners are not the same
       {
           log.info("Update as Delete and Add.");
           return updateAsDeleteAndAdd(query, old);
       }
   }
   
   private boolean updateAsDeleteAndAdd(AutomatedQuery query, AutomatedQuery old)
   {
       AutomatedQueryTaskCollector oldCollector = findCollectorForId(old.getId());
               
       try
           {
               if(oldCollector.deleteTask(old.getId()) && addAutomatedQuery(query))
                    {
                        return true;
                    }
                    else throw new Exception();
            }
            catch (Exception ex)
            {
                //try to revert
                oldCollector.addTask(new AutomatedCountQueryTask(old));
                deleteQuery(query);
                return false;
            }
   }

   
   private boolean updateAutomatedQuery(AutomatedQuery query)
   {
       if (query.getIsOwnerGroup())
       {
           return updateAutomatedGroupQuery(query);
       }
       else
       {
           return updateAutomatedUserQuery(query);
       }
   }
   
   private boolean updateAutomatedUserQuery(AutomatedQuery query)
   {
       AutomatedQueryTaskCollector collector = userTaskCollectors.get(
       query.getOwner());
       
       if (collector != null)
       {
           return collector.addTask(new AutomatedCountQueryTask(query));
       }
       return false;
   }
   
   private boolean updateAutomatedGroupQuery(AutomatedQuery query)
   {
       AutomatedQueryTaskCollector collector = groupTaskCollectors.get(
       query.getOwner());
       
       if (collector != null)
       {
           return collector.addTask(new AutomatedCountQueryTask(query));
       }
       return false;
   }
   
   private boolean addAutomatedUserQuery(AutomatedQuery query) {
       final String owner = query.getOwner();
       AutomatedQueryTaskCollector collector = userTaskCollectors.get(
               owner);
       if (collector == null) 
       {
           //First query for this owner
           if (rootDir != null && rootDir.isDirectory())
           {
                File userFile = new File(rootDir, "users/" + owner);
                try 
                {
                    userFile.createNewFile();
                    collector = new AutomatedQueryTaskCollector(userFile);
                    addTaskCollector(collector);
                    userTaskCollectors.put(owner, collector);
                }
                catch (IOException ex)
                {
                    log.error(null, ex);
                }
           } else
           {
                log.warn("Automated query data directory setup incorrectly!");
                return false;
           }
       }
          return collector.addTask(new AutomatedCountQueryTask(query));       
     }

    private boolean addAutomatedGroupQuery(AutomatedQuery query) {
        final String owner = query.getOwner();
        AutomatedQueryTaskCollector collector = groupTaskCollectors.get(
                owner);
        if (collector == null) //First query for this owner
       {
            //First query for this owner
           if (rootDir != null && rootDir.isDirectory())
           {
                File groupFile = new File(rootDir, "groups/" + owner);
                try 
                {
                    groupFile.createNewFile();
                    collector = new AutomatedQueryTaskCollector(groupFile);
                    addTaskCollector(collector);
                    groupTaskCollectors.put(owner, collector);
                }
                catch (IOException ex)
                {
                    log.error(null, ex);
                }
           } else
           {
                log.warn("Automated query data directory setup incorrectly!");
                return false;
           }
       }
       return collector.addTask(new AutomatedCountQueryTask(query));  
    }
        
    
    @Override
    public List<AutomatedQuery> getUserQueries(String username) {
        AutomatedQueryTaskCollector collector = userTaskCollectors.get(username);
        if (collector == null)
        {
            return new LinkedList<>();
        }
        return  collector.getQueries();
    }
    
    @Override
    public List<AutomatedQuery> getGroupQueries(String username)
    {
        AutomatedQueryTaskCollector collector = groupTaskCollectors.get(username);
        if (collector == null)
        {
            return new LinkedList<>();
        }
        return collector.getQueries();
    }
    
    public void setAnnisDao(AnnisDao annisDao)
    {
        this.annisDao = annisDao;
    }

    @Override
    public AnnisDao getAnnisDao()
    {
        return annisDao;
    } 

    public String getBaseDir() {
        return baseDir;
    }
}
