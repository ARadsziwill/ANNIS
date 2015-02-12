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

import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskTable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for managing AutomatedQueryTask in a file specified in the constructor
 * @author Andreas
 */
public class AutomatedQueryTaskCollector implements TaskCollector
{
    private final static Logger log = LoggerFactory.getLogger(AutomatedQueryTaskCollector.class);
    
    private final File resourceFile;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final Map<UUID, AutomatedQueryTask> tasks;
    
    private final TaskTable cache;
    private Date lastTimeReloaded = null;
       
    public AutomatedQueryTaskCollector(File resourceFile)
    {
        cache = new TaskTable();
        tasks = new TreeMap<>();
        this.resourceFile = resourceFile;
    }
    
    @Override
    //Todo add and use a cache
    public TaskTable getTasks() {
       checkConfiguration();
       return cache;
    }

    private void checkConfiguration() {
        boolean reload = false;
        lock.readLock().lock();
        try {
            if (resourceFile == null) {
                return;
            } 
            if (lastTimeReloaded == null || FileUtils.isFileNewer(resourceFile,
                    lastTimeReloaded))
            {
                reload = true;
            }
        } finally {
            lock.readLock().unlock();
        }
        if (reload)
        {
            readQueriesFromFile();
            renewCache();
        }
    }
    
    private void renewCache()
    {
        lock.writeLock().lock();
        try {
            //clear cache
            while (cache.size() > 0)
            {
                cache.remove(cache.size() - 1);
            }
        for (AutomatedQueryTask task : tasks.values())
            {
                cache.add(new SchedulingPattern(task.getQuery().getSchedulingPattern()), task);
            }
        } 
        finally
        {
            lock.writeLock().unlock();
        }
    }
    
    private void readQueriesFromFile() 
    {
        if (resourceFile != null)
        {
            lock.writeLock().lock();
            try 
            {
                //unmarshall TaskFile
                FileInputStream fis = new FileInputStream(resourceFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                tasks.clear();
                tasks.putAll((Map<UUID, AutomatedQueryTask>) ois.readObject());
                lastTimeReloaded = new Date(resourceFile.lastModified());
                ois.close();
                fis.close();
            }
            catch (EOFException ex)
            {
                log.info("Queries File empty. This schouldn't be a problem", ex);
            }
            catch (IOException | ClassNotFoundException ex)
            {
                log.error(null, ex);
            }
            finally 
            {
                lock.writeLock().unlock();
            }
        }
    }
    
    public boolean addTask(AutomatedQueryTask task)
    {
        if (resourceFile != null)
        {
            lock.writeLock().lock();
            try
            {
                readQueriesFromFile();
                
                tasks.put(task.getQuery().getId(), task);
                return writeQueriesFile();
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        return false;
    }
    
    public boolean deleteTask(AutomatedQueryTask task)
    {
        if (resourceFile != null)
        {
            lock.writeLock().lock();
            try
            {
                readQueriesFromFile();
                tasks.remove(task.getQuery().getId());
                return writeQueriesFile();
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        return false;
    }
   
    private boolean writeQueriesFile()
    {
        if (resourceFile != null)
        {
            
            lock.writeLock().lock();
            try (ObjectOutputStream oos = 
                    new ObjectOutputStream(new FileOutputStream(resourceFile)))
            
            {
                oos.writeObject(tasks);
                renewCache();
                lastTimeReloaded = new Date(resourceFile.lastModified());
                return true;
            }
            catch (IOException ex)
            {
                log.error("Could not write auto query file: " + 
                        this.resourceFile.getPath(), ex);
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }// end if resourcePath not null
        return false;
    }

    public List<AutomatedQuery> getQueries() 
    {
        checkConfiguration();
        List<AutomatedQuery> result = new ArrayList<>();
        for (AutomatedQueryTask task : tasks.values())
        { 
            result.add(task.getQuery());
        }
        return result;
    }
}
