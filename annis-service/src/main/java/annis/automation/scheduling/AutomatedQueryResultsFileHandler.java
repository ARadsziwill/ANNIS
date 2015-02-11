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

import com.google.common.collect.ImmutableList;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andreas Radsziwill <radsziwill@stud.tu-darmstadt.de
 */
public class AutomatedQueryResultsFileHandler {
    
    private final static Logger log = LoggerFactory.getLogger(
            AutomatedQueryResultsFileHandler.class);
    
    private final File file;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final List<AutomatedQueryResult> cache = new LinkedList<>();
    private Date lastTimeReloaded = null;
    
    public AutomatedQueryResultsFileHandler(String parentDir, String filename)
    {
        this.file = checkFile(parentDir, filename);
    }
    
    private File checkFile(String parentDir, String filename)
    {
        File tmp = new File(parentDir, filename);
        try
        {
            if(tmp.isFile() || tmp.createNewFile()) 
            {
                return tmp;
            }
        } 
        catch(IOException ex)
        {
            log.error(null, ex);
        }
        return null;
    }        
            
    public ImmutableList<AutomatedQueryResult> getResults()
    {
        checkConfiguration();
        return ImmutableList.copyOf(cache);
    }
        
    public boolean addResult(AutomatedQueryResult result)
    {
        if (file != null)
        {
            lock.writeLock().lock();
            try
            {
                readFromFile();
                cache.add(result);
                return writeToFile();
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        return false;
    }
    
    public boolean deleteResult(AutomatedQueryResult result)
    {
        if (file != null)
        {
            lock.writeLock().lock();
            try 
            {
                readFromFile();
                cache.remove(result);
                return writeToFile();
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        return false;
    }
    
    private void checkConfiguration() 
    {
        boolean reload = false;
        
        lock.readLock().lock();
        try
        {
            if (file == null)
            {
                return;
            }
            if (lastTimeReloaded == null || FileUtils.isFileNewer(file,
                    lastTimeReloaded))
            {
                reload = true;
            }
        } finally
        {
            lock.readLock().unlock();
        }
        if (reload)
        {
            readFromFile();
        }
    }
    
    private void readFromFile()
    {
        if (file != null)
        {
            lock.writeLock().lock();
            
            try(ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(file)))
            {
                cache.clear();
                cache.addAll((List<AutomatedQueryResult>) ois.readObject());
                lastTimeReloaded = new Date(file.lastModified());
            }
            catch (EOFException ex)
            {
                log.info("Results File empty. This schouldn't be a problem", ex);
            }            
            catch (ClassNotFoundException | IOException ex)
            {
                log.error(null, ex);
            }
            finally 
            {
                lock.writeLock().unlock();
            }                
        }
    }
    
    private boolean writeToFile()
    {
        if (file != null)
        {
            lock.writeLock().lock();
            try(ObjectOutputStream oos = 
                    new ObjectOutputStream(new FileOutputStream(file)))
            {
                oos.writeObject(cache);
                lastTimeReloaded = new Date(file.lastModified());
                return true;
            }
            catch (IOException ex)
            {
                log.error("Could not write file: " + file.getPath(), ex);
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        return false;
    }
}
