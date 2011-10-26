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
package annis.ql.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import annis.model.AnnisNode;
import annis.model.Annotation;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryData implements Cloneable
{
	private List<List<AnnisNode>> alternatives;
	private List<Long> corpusList;
	private List<Long> documents;
	private List<Annotation> metaData;
	private int maxWidth;
	private Set<Object> extensions;

	public QueryData() {
		alternatives = new ArrayList<List<AnnisNode>>();
		corpusList = new ArrayList<Long>();
		documents = new ArrayList<Long>();
		metaData = new ArrayList<Annotation>();
		extensions = new HashSet<Object>();
	}

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    Iterator<List<AnnisNode>> itOr = getAlternatives().iterator();
    while(itOr.hasNext())
    {
      List<AnnisNode> nextNodes = itOr.next();
      Iterator<AnnisNode> itAnd = nextNodes.iterator();
      while(itAnd.hasNext())
      {
        sb.append("\t").append(itAnd.next());
        sb.append("\n");
        if(itAnd.hasNext())
        {
          sb.append("\tAND");
          sb.append("\n");
        }
      }

      if(itOr.hasNext())
      {
        sb.append("OR");
        sb.append("\n");
      }
    }
    Iterator<Annotation> itMeta = getMetaData().iterator();
    if(itMeta.hasNext())
    {
      sb.append("META");
      sb.append("\n");
    }
    while(itMeta.hasNext())
    {
      sb.append("\t").append(itMeta.next().toString());
      sb.append("\n");
    }
    if (! extensions.isEmpty() ) {
    	sb.append("EXTENSIONS\n");
    }
    for (Object extension : extensions) {
		String toString = extension.toString();
		if (! "".equals(toString) )
			sb.append("\t" + toString + "\n");
	}

    return sb.toString();
  }

	public List<List<AnnisNode>> getAlternatives() {
		return alternatives;
	}
	public void setAlternatives(List<List<AnnisNode>> alternatives) {
		this.alternatives = alternatives;
	}
	public List<Long> getCorpusList() {
		return corpusList;
	}
	public void setCorpusList(List<Long> corpusList) {
		this.corpusList = corpusList;
	}
	public List<Annotation> getMetaData() {
		return metaData;
	}
	public void setMetaData(List<Annotation> metaData) {
		this.metaData = metaData;
	}
	public int getMaxWidth() {
		return maxWidth;
	}
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public boolean addAlternative(List<AnnisNode> nodes) {
		return alternatives.add(nodes);
	}

	public boolean addMetaAnnotations(List<Annotation> annotations) {
		return metaData.addAll(annotations);
	}

	// FIXME: warum diese spezielle clone-Funktion?
  @Override
  public QueryData clone()
  {
    try
    {
      return (QueryData) super.clone();
    } catch (CloneNotSupportedException ex)
    {
      Logger.getLogger(QueryData.class.getName()).log(Level.SEVERE, null, ex);
      throw new InternalError("could not clone QueryData");
    }
  }

public List<Long> getDocuments() {
	return documents;
}

public void setDocuments(List<Long> documents) {
	this.documents = documents;
}

public Set<Object> getExtensions() {
	return extensions;
}

public boolean addExtension(Object extension) {
	return extensions.add(extension);
}

}