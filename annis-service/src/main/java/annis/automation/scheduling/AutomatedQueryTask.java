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
import annis.ql.parser.QueryData;
import annis.utils.Utils;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import org.joda.time.DateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class can be extended to implement specific Annis related tasks
 * 
 * @author Andreas
 */
public abstract class AutomatedQueryTask extends Task implements Serializable {

    private final static Logger log = LoggerFactory.getLogger(
            AutomatedQueryTask.class);
    private final AutomatedQuery query;
        
    /**
     *
     * @param query
     */
    public AutomatedQueryTask(AutomatedQuery query)
    {
        this.query = query;
    }
    
    public AutomatedQuery getQuery()
    {
        return this.query;
    }

    /**
     *  This method is final to ensure subclasses of  AutomatedQueryTask only 
     *  call doExecute when the associated query is active.
     *  Instead of overriding Task.execute() Subclasses should override doExecute.
     * @param tec
     * @throws RuntimeException
     */
    @Override
    public final void execute(TaskExecutionContext tec) throws RuntimeException {
        if (query.getIsActive())
        {
            //radomize execution start so queryResults are distinguishable by time
            long delay = (long) Utils.getRandomInt(5000);
            log.info("Excuting Query:" + this.getQuery().getId() + " in " + delay +"ms");
            try {
                Thread.sleep(delay); 
            }
            catch (InterruptedException ex)
            {
                ;
            }
            String result = doExecute(tec);
            if (tec.getScheduler() instanceof AnnisScheduler)
            {
                AnnisScheduler scheduler = (AnnisScheduler) tec.getScheduler();
                scheduler.addResult(new AutomatedQueryResult(query, result, new DateTime()));
            }
        }
    }
    
    abstract String doExecute(TaskExecutionContext tec);
    
    QueryData calculateQueryData(TaskExecutionContext tec) 
    {
        if (tec.getScheduler() instanceof AnnisScheduler)
            {
                AnnisScheduler scheduler = (AnnisScheduler) tec.getScheduler();
                List<Long> corpusIDs = scheduler.getAnnisDao().
                        mapCorpusNamesToIds(new ArrayList<>(getQuery().getCorpora()));
                if (corpusIDs.size() != getQuery().getCorpora().size())
                {
                    log.warn("One or more corpora are unknown to the system. " +
                            "Maybe their name has changed?");
                }
                return scheduler.getAnnisDao().parseAQL(getQuery().getQuery(), corpusIDs);
            }        
        throw new RuntimeException("Wrong kind of Scheduler. "
                + "Must be an an Implementation of AnnisScheduler");
    }
}
