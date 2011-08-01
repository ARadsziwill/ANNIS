/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
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
package annis.dao;

import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;
import annis.model.Annotation;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 *
 * @author thomas
 */
public class MatrixExtractor implements ResultSetExtractor
{

  private String matchedNodesViewName;

  @Override
  public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
  {
    List<AnnotatedMatch> matches = new ArrayList<AnnotatedMatch>();

    Map<List<Long>, AnnotatedSpan[]> matchesByGroup = new HashMap<List<Long>, AnnotatedSpan[]>();

    while (resultSet.next())
    {
      long id = resultSet.getLong("id");
      String coveredText = resultSet.getString("span");
      
      Array arrayAnnotation = resultSet.getArray("annotations");
      Array arrayMeta = resultSet.getArray("metadata");
      
      List<Annotation> annotations =  extractAnnotations(arrayAnnotation);
      List<Annotation> metaData = extractAnnotations(arrayMeta);

      // create key
      Array sqlKey = resultSet.getArray("key");
      Validate.isTrue(!resultSet.wasNull(), "Match group identifier must not be null");
      Validate.isTrue(sqlKey.getBaseType() == Types.BIGINT,
        "Key in database must be from the type \"bigint\" but was \"" + sqlKey.getBaseTypeName() + "\"");

      Long[] keyArray = (Long[]) sqlKey.getArray();
      int matchWidth = keyArray.length;
      List<Long> key = Arrays.asList(keyArray);
      
      if (!matchesByGroup.containsKey(key))
      {
        matchesByGroup.put(key, new AnnotatedSpan[matchWidth]);
      }
      
      // set annotation spans for *all* positions of the id
      // (node could have matched several times)
      for(int posInMatch=0; posInMatch < key.size(); posInMatch++)
      {
        if(key.get(posInMatch) == id)
        {
          matchesByGroup.get(key)[posInMatch] = new AnnotatedSpan(id, coveredText, annotations, metaData);
        }
      }
    }

    for(AnnotatedSpan[] match : matchesByGroup.values())
    {
      matches.add(new AnnotatedMatch(Arrays.asList(match)));
    }

    return matches;

  }

  public String getMatrixQuery(List<Long> corpusList, int maxWidth)
  {
    StringBuilder keySb = new StringBuilder();
    keySb.append("ARRAY[matches.id1");
    for (int i = 2; i <= maxWidth; ++i)
    {
      keySb.append(",");
      keySb.append("matches.id");
      keySb.append(i);
    }
    keySb.append("] AS key");
    String key = keySb.toString();

    StringBuilder sb = new StringBuilder();

    sb.append("SELECT \n");
    sb.append("\t");
    sb.append(key);
    sb.append(",\nfacts.id AS id,\n");
    sb.append("min(substr(text.text, facts.left+1,facts.right-facts.left)) AS span,\n");
    sb.append("array_agg(DISTINCT coalesce(facts.node_annotation_namespace || ':', '') "
      + "|| facts.node_annotation_name || ':' "
      + "|| encode(facts.node_annotation_value::bytea, 'base64')) AS annotations,\n");
    sb.append("array_agg(DISTINCT coalesce(ca.namespace || ':', '') "
      + "|| ca.name || ':' "
      + "|| encode(ca.value::bytea, 'base64')) AS metadata\n");
    
    sb.append("FROM\n");
    sb.append("\t");
    sb.append(matchedNodesViewName);
    sb.append(" AS matches,\n");

    sb.append("\t\"text\" AS \"text\",\n");
    
    sb.append("\t");
    sb.append(FACTS_TABLE);
    sb.append(" AS facts\n");
    sb.append("\t LEFT OUTER JOIN corpus_annotation AS ca ON (ca.corpus_ref = facts.corpus_ref)\n");

    sb.append("WHERE\n");

    if (corpusList != null)
    {
      sb.append("facts.toplevel_corpus IN (");
      sb.append(corpusList.isEmpty() ? "NULL" : StringUtils.join(corpusList, ","));
      sb.append(") AND\n");
    }
    sb.append("facts.text_ref = text.id AND \n");

    sb.append("(");
    for (int i = 1; i <= maxWidth; i++)
    {
      sb.append("facts.id = matches.id").append(i);
      if (i < maxWidth)
      {
        sb.append(" OR ");
      }
    }
    sb.append(")\n");
    sb.append("GROUP BY key, facts.id, span");

    Logger.getLogger(MatrixExtractor.class).debug("generated SQL for matrix:\n" + sb.toString());

    return sb.toString();
  }
  
  private List<Annotation> extractAnnotations(Array array) throws SQLException
  {
    List<Annotation> result = new ArrayList<Annotation>();
    
    if(array != null)
    {
      String[] arrayLines = (String[]) array.getArray();
      
      for(String line : arrayLines)
      {
        String namespace = null;
        String name = null;
        String value = null;
        
        String[] split = line.split(":");
        if(split.length > 2)
        {
          namespace = split[0];
          name = split[1];
          value = split[2];
        }
        else if(split.length > 1)
        {
          name = split[0];
          value = split[1];
        }
        else
        {
          name = split[0];
        }
        
        if(value != null)
        {
          value = new String(Base64.decodeBase64(value));
        }
        
        result.add(new Annotation(namespace, name, value));
      }
    }
    
    return result;
  }

  public List<AnnotatedMatch> queryMatrix(JdbcTemplate jdbcTemplate, List<Long> corpusList, int maxWidth)
  {
    return (List<AnnotatedMatch>) jdbcTemplate.query(getMatrixQuery(corpusList, maxWidth), this);
  }

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }
}
