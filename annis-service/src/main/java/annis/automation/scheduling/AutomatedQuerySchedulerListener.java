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

import it.sauronsoftware.cron4j.SchedulerListener;
import it.sauronsoftware.cron4j.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Andreas
 */
public class AutomatedQuerySchedulerListener implements SchedulerListener{

    private final static Logger log = LoggerFactory.getLogger(AutomatedQuerySchedulerListener.class);
    
    @Override
    public void taskLaunching(TaskExecutor te) {
        log.info("Launching Task Executor: " + te.getGuid());
    }

    @Override
    public void taskSucceeded(TaskExecutor te) {
        log.info("Executed successfully: " + te.getGuid());
    }

    @Override
    public void taskFailed(TaskExecutor te, Throwable thrwbl) {
        log.error("TaskExecution failed: ", thrwbl);
    }    
}
