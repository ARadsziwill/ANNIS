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

import annis.automation.AutomatedQuery;
import annis.automation.AutomatedQueryResult;
import annis.dao.AnnisDao;
import it.sauronsoftware.cron4j.Scheduler;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.joda.time.DateTime;

/**
 *
 * @author Andreas
 */
public interface AnnisScheduler {
    
    public List<AutomatedQueryResult> getQueryResults(List<String> filters);
    public void addResult(AutomatedQueryResult result);
    public void deleteResults(DateTime date, Set<UUID> ids);
        
    public List<AutomatedQuery> getGroupQueries(String groupname);
    public List<AutomatedQuery> getUserQueries(String username);
    public boolean addAutomatedQuery(AutomatedQuery query);
    
    public boolean idExists(UUID id);
    public AutomatedQuery getQuery(UUID id);
    
    public boolean deleteQuery(AutomatedQuery query);
    public boolean updateAutomatedQuery(AutomatedQuery query, AutomatedQuery old);
 
    public AnnisDao getAnnisDao();
   
}
