/*
 * Copyright 2010 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package annis.sqlgenerator;

import java.util.EnumSet;

/**
 * Interface for a SQLGenerator that transforms an AQL to a SQL query on the
 * relANNIS scheme.
 * @author Thomas Krause
 */
public interface SQLGenerator {

  /**
   * Check if this generator is able to handle a specific AQL query.
   * @param aql The AQL query.
   * @return
   */
  public boolean checkIfApplicable(String aql);

  /**
   * Returns a set of general constraints that must be fullfilled for an AQL
   * query to be executed by this generator.
   */
  public EnumSet<AQLConstraints> getNeededConstraints();

  /**
   * Create a SQL query on the relANNIS scheme from an AQL query.
   * @param aql
   * @return
   */
  public String createSQL(String aql);

}
