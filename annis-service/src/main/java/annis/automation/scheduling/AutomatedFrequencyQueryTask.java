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
import annis.automation.ResultWrapper;
import annis.service.objects.FrequencyTable;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andreas
 */
public class AutomatedFrequencyQueryTask extends AutomatedQueryTask
{
    private final Logger log = LoggerFactory.getLogger(AutomatedFrequencyQueryTask.class);

    public AutomatedFrequencyQueryTask(AutomatedQuery query)
    {
        super(query);
    }

    @Override
    protected ResultWrapper doExecute(TaskExecutionContext tec) {
        if(tec.getScheduler() instanceof AnnisScheduler)
        {
            AnnisScheduler scheduler = (AnnisScheduler) tec.getScheduler();
            FrequencyTable freqs = scheduler.getAnnisDao().frequency(this.calculateQueryData(tec));
            
            return new ResultWrapper<>(freqs);
        }
        throw new RuntimeException("Wrong kind of Scheduler. "
                + "Must be an an Implementation of AnnisScheduler");
    }
}
