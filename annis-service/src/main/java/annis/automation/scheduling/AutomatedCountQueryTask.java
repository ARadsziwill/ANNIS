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
import annis.service.objects.MatchAndDocumentCount;
import it.sauronsoftware.cron4j.TaskExecutionContext;

/**
 *
 * @author Andreas
 */
public class AutomatedCountQueryTask extends AutomatedQueryTask{

    public AutomatedCountQueryTask(AutomatedQuery query) {
        super(query);
    }
    
    @Override
    public String doExecute(TaskExecutionContext tec) throws RuntimeException {
        if (tec.getScheduler() instanceof AnnisScheduler)
            {
                AnnisScheduler scheduler = (AnnisScheduler) tec.getScheduler();
                MatchAndDocumentCount result = scheduler.getAnnisDao().countMatchesAndDocuments(this.calculateQueryData(tec));
                return result.toString();
            }
        throw new RuntimeException("Wrong kind of Scheduler. "
                + "Must be an an Implementation of AnnisScheduler");
        }
    
}
