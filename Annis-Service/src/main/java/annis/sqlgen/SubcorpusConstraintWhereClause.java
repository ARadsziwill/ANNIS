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

import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import annis.model.AnnisNode;
import annis.sqlgen.model.Inclusion;
import annis.sqlgen.model.Join;
import annis.sqlgen.model.LeftAlignment;
import annis.sqlgen.model.LeftOverlap;
import annis.sqlgen.model.Overlap;
import annis.sqlgen.model.Precedence;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author thomas
 */
public class SubcorpusConstraintWhereClause extends BaseNodeSqlGenerator
  implements WhereClauseSqlGenerator
{

  @Override
  public List<String> whereConditions(AnnisNode node, List<Long> corpusList, List<Long> documents)
  {
    return null;
  }

  @Override
  public List<String> commonWhereConditions(List<AnnisNode> nodes, List<Long> corpusList, List<Long> documents)
  {
    LinkedList<String> conditions = new LinkedList<String>();

    conditions.add("-- annotations can always only be inside a subcorpus/document");

    AnnisNode[] copyNodes = nodes.toArray(new AnnisNode[0]);

    HashSet<IdPair> hasAlreadyTextref = calculateTextRefRelations(copyNodes);

    for (int left = 0; left < copyNodes.length; left++)
    {
      for (int right = left + 1; right < copyNodes.length; right++)
      {
        if (!hasAlreadyTextref.contains(new IdPair(copyNodes[left].getId(),copyNodes[right].getId())))
        {
          conditions.add(join("=",
            tables(copyNodes[left]).aliasedColumn(NODE_TABLE, "corpus_ref"),
            tables(copyNodes[right]).aliasedColumn(NODE_TABLE, "corpus_ref")));

          // check if we have to apply this constraint on the facts table as well
          if (tables(copyNodes[left]).usesPartialFacts() && tables(copyNodes[right]).usesPartialFacts())
          {
            conditions.add(join("=",
              tables(copyNodes[left]).aliasedColumn(FACTS_TABLE, "corpus_ref"),
              tables(copyNodes[right]).aliasedColumn(FACTS_TABLE, "corpus_ref")));
          }
        }
      }
    }

    return conditions;
  }

  // TODO: find a way to store this information already in DefaultWhereClauseGeneration
  private HashSet<IdPair> calculateTextRefRelations(AnnisNode[] copyNodes)
  {
    HashSet<IdPair> hasAlreadyTextref = new HashSet<IdPair>();

    for (int left = 0; left < copyNodes.length; left++)
    {
      for (int right = 0; right < copyNodes.length; right++)
      {
        for (Join j : copyNodes[left].getJoins())
        {
          if (j instanceof SameSpan)
          {
            hasAlreadyTextref.add(new IdPair(copyNodes[left].getId(), j.getTarget().getId()));
            hasAlreadyTextref.add(new IdPair(j.getTarget().getId(), copyNodes[left].getId()));
          }
          else if (j instanceof LeftAlignment)
          {
            hasAlreadyTextref.add(new IdPair(copyNodes[left].getId(), j.getTarget().getId()));
            hasAlreadyTextref.add(new IdPair(j.getTarget().getId(), copyNodes[left].getId()));
          }
          else if (j instanceof RightAlignment)
          {
            hasAlreadyTextref.add(new IdPair(copyNodes[left].getId(), j.getTarget().getId()));
            hasAlreadyTextref.add(new IdPair(j.getTarget().getId(), copyNodes[left].getId()));
          }
          else if (j instanceof Inclusion)
          {
            hasAlreadyTextref.add(new IdPair(copyNodes[left].getId(), j.getTarget().getId()));
            hasAlreadyTextref.add(new IdPair(j.getTarget().getId(), copyNodes[left].getId()));
          }
          else if (j instanceof Overlap)
          {
            hasAlreadyTextref.add(new IdPair(copyNodes[left].getId(), j.getTarget().getId()));
            hasAlreadyTextref.add(new IdPair(j.getTarget().getId(), copyNodes[left].getId()));
          }
          else if (j instanceof LeftOverlap)
          {
            hasAlreadyTextref.add(new IdPair(copyNodes[left].getId(), j.getTarget().getId()));
            hasAlreadyTextref.add(new IdPair(j.getTarget().getId(), copyNodes[left].getId()));
          }
          else if (j instanceof RightOverlap)
          {
            hasAlreadyTextref.add(new IdPair(copyNodes[left].getId(), j.getTarget().getId()));
            hasAlreadyTextref.add(new IdPair(j.getTarget().getId(), copyNodes[left].getId()));
          }
          else if (j instanceof Precedence)
          {
            hasAlreadyTextref.add(new IdPair(copyNodes[left].getId(), j.getTarget().getId()));
            hasAlreadyTextref.add(new IdPair(j.getTarget().getId(), copyNodes[left].getId()));
          }
        }
      }
    }
    return hasAlreadyTextref;
  }

  public static class IdPair
  {

    public long id1;
    public long id2;

    public IdPair(long id1, long id2)
    {
      this.id1 = id1;
      this.id2 = id2;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }
      if (getClass() != obj.getClass())
      {
        return false;
      }
      final IdPair other = (IdPair) obj;
      if (this.id1 != other.id1)
      {
        return false;
      }
      if (this.id2 != other.id2)
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 5;
      hash = 97 * hash + (int) (this.id1 ^ (this.id1 >>> 32));
      hash = 97 * hash + (int) (this.id2 ^ (this.id2 >>> 32));
      return hash;
    }
  }
}
