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

import java.io.Serializable;
import java.util.Date;

/**
 * This class represents the result of an automated query execution. 
 * 
 * 
 * 
 * @author Andreas
 */
public class AutomatedQueryResult implements Serializable{
   
    private final AutomatedQuery query;
    private final Date executed;
    private final String result;

    public AutomatedQueryResult(AutomatedQuery query, String result, Date date) {
        this.executed = date;
        this.query = query;
        this.result = result;
    }

    public AutomatedQuery getQuery() {
        return query;
    }

    public Date getExecuted() {
        return executed;
    }

    public String getResult() {
        return result;
    }
    
}