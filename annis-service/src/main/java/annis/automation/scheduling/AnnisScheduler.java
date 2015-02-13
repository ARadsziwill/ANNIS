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
import it.sauronsoftware.cron4j.Scheduler;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Andreas
 */
public abstract class AnnisScheduler extends Scheduler {
    
    public abstract List<AutomatedQuery> getGroupQueries(String groupname);
    public abstract List<AutomatedQuery> getUserQueries(String username);
    public abstract boolean addAutomatedQuery(AutomatedQuery query);
    public abstract void addResult(AutomatedQueryResult result);
    public abstract AnnisDao getAnnisDao();
    public abstract boolean idExists(UUID id);
    public abstract AutomatedQuery getQuery(UUID id);
    public abstract boolean deleteQuery(AutomatedQuery query);
    public abstract boolean updateAutomatedQuery(AutomatedQuery query, AutomatedQuery old);
    
}
