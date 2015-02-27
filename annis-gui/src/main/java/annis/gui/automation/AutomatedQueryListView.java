/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.automation;

import annis.automation.AutomatedQuery;
import annis.gui.QueryController;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Andreas
 */
public interface AutomatedQueryListView
{
  public void addListener(Listener listener);
  
  public void setQueryList(Collection<AutomatedQuery> queries);
  public void setAvailableGroups(Collection<String> groups);
  public void setAvailableCorpusNames(Collection<String> corpusNames);
  public void setQueryAndCorpusData(QueryController controller);
  
  public void emptyNewQueryInputFields();
  public void setStatus(String message);
  
  public interface Listener
  {
    public void queryUpdated(AutomatedQuery query);
    
    public void addNewQuery(AutomatedQuery query);
    public void deleteQueries(Set<UUID> queryIds);
  }
}
