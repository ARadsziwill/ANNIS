/*
 *  Copyright 2010 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package annis.sqlgen;

import annis.model.AnnisNode;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author thomas
 */
public class MetaDataAndCorpusWhereClause extends BaseNodeSqlGenerator
  implements WhereClauseSqlGenerator
{

  @Override
  public List<String> whereConditions(AnnisNode node, List<Long> corpusList, List<Long> documents)
  {
    if (documents == null && corpusList == null)
    {
      return null;
    }
    LinkedList<String> conditions = new LinkedList<String>();

    conditions.add("-- select documents by metadata and toplevel corpus");
    if (documents != null)
    {
      if(documents.isEmpty())
      {
        conditions.add("-- WARNING: can't generate any result if empty document list is given");
        conditions.add(in(tables(node).aliasedColumn("node", "corpus_ref"),
          "NULL"));
      }
      else
      {
        conditions.add(in(tables(node).aliasedColumn("node", "corpus_ref"),
          documents));
      }
    }

    if (corpusList != null)
    {
      if(corpusList.isEmpty())
      {
        conditions.add("-- WARNING: can't generate any result if empty corpus list is given");
        conditions.add(in(tables(node).aliasedColumn("node", "toplevel_corpus"),
          "NULL"));
      }
      else
      {
        conditions.add(in(tables(node).aliasedColumn("node", "toplevel_corpus"),
          corpusList));
      }
    }
    return conditions;
  }

  @Override
  public List<String> commonWhereConditions(List<AnnisNode> nodes, List<Long> corpusList, List<Long> documents)
  {
    return null;
  }
}
