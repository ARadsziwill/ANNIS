/*
 * Copyright 2014 SFB 632.
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
package annis.sqlgen;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import com.google.common.base.Joiner;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SelectedCorporaWithClauseGenerator 
  implements WithClauseSqlGenerator<QueryData>
{

  @Override
  public List<String> withClauses(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    List<String> tables = new LinkedList<>();
    for(Long corpusID : queryData.getCorpusList())
    {
      tables.add("SELECT * FROM facts_" + corpusID);
    }
    
    String indent2 = indent + AbstractSqlGenerator.TABSTOP;
    
    return Arrays.asList(indent + 
      "selected_facts AS (\n" + indent2 
      + Joiner.on("\n" + indent2 + "UNION ALL\n" + indent2).join(tables) 
      + ")");
  }
  
}
