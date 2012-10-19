/*
 * Copyright 2012 SFB 632.
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
package annis.sqlgen.fullfacts;

import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.model.QueryNode.TextMatching;
import annis.ql.parser.QueryData;
import annis.sqlgen.AnnotationConditionProvider;
import static annis.sqlgen.SqlConstraints.join;
import static annis.sqlgen.SqlConstraints.sqlString;
import annis.sqlgen.TableAccessStrategy;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import java.util.List;


/**
 *
 * @author thomas
 */
public class FfAnnotateConditionProvider implements
  AnnotationConditionProvider
{

  @Override
  public void addAnnotationConditions(List<String> conditions, QueryNode node,
    int index, QueryAnnotation annotation, String table, QueryData queryData,
    TableAccessStrategy tas)
  {
    if (annotation.getNamespace() != null)
    {
      conditions.add(join("=",
        tas.aliasedColumn(table, "namespace", index),
        sqlString(annotation.getNamespace())));
    }
    conditions.add(join("=",
      tas.aliasedColumn(table, "name", index),
      sqlString(annotation.getName())));
    if (annotation.getValue() != null)
    {
      TextMatching textMatching = annotation.getTextMatching();
      conditions.add(join(textMatching.sqlOperator(), tas.aliasedColumn(table,
        "value", index),
        sqlString(annotation.getValue(), textMatching)));
    }
  }

  @Override
  public void addAnnotationsNotEqualConditions(List<String> conditions, QueryNode node, QueryNode target, TableAccessStrategy tasNode,
    TableAccessStrategy tasTarget)
  {
    conditions.add(join("<>", tasNode.aliasedColumn(NODE_ANNOTATION_TABLE, "name"), 
      tasTarget.aliasedColumn(NODE_ANNOTATION_TABLE, "name")));
    conditions.add(join("<>", tasNode.aliasedColumn(NODE_ANNOTATION_TABLE, "namespace"), 
      tasTarget.aliasedColumn(NODE_ANNOTATION_TABLE, "namespace")));
  }

  
  
}
