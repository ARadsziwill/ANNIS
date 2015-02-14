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

import annis.adapter.UUIDAdapter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A POJO representing an automated Query 
 * @author Andreas
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AutomatedQuery implements Serializable
{
    @XmlAttribute
    private String description;
    @XmlAttribute
    private String owner;
    @XmlAttribute
    private String query;
    @XmlAttribute
    private String schedulingPattern;
    
    @XmlJavaTypeAdapter(UUIDAdapter.class)
    @XmlElement
    private final UUID id;
    @XmlElement(required = true)
    private Boolean isOwnerGroup; 
    @XmlElement(required = true)
    private Boolean isActive;
    
    private List<String> corpora;
    
    
    private AutomatedQuery()
    {
        this(null, null, null, null, null, null, null, null);
    }
    
    public AutomatedQuery(String query,
            List<String> corpora,
            String schedulingPattern,
            String description,
            String owner,
            Boolean isOwnerGroup,
            Boolean isActive,
            UUID id)
    {
      this.query = query;
      this.corpora = corpora;
      this.schedulingPattern = schedulingPattern;
      this.description = description;
      this.owner = owner;
      this.isOwnerGroup = isOwnerGroup;
      this.isActive = isActive;
      this.id = id;
    }

    public AutomatedQuery(String query, List<String> corpusNames, String schedulingPattern, String description, String owner, boolean isOwnerGroup, boolean isActive) {
        this(query, corpusNames, schedulingPattern, description, owner, isOwnerGroup, isActive, UUID.randomUUID());
    }

  public UUID getId()
  {
      return this.id;
  }
  
  public String getQuery()
  {
    return query;
  }

  public void setQuery(String query)
  {
    this.query = query;
  }

  public List<String> getCorpora()
  {
    return corpora;
  }
  
@XmlElement(name="corpora")
  public void setCorpora(List<String> corpora)
  {
    this.corpora = corpora;
  }

  public boolean getIsOwnerGroup()
  {
    return isOwnerGroup;
  }

  public void setIsOwnerGroup(boolean isOwnerGroup)
  {
    this.isOwnerGroup = isOwnerGroup;
  }
    public String getOwner()
  {
    return owner;
  }

  public void setOwner(String owner)
  {
    this.owner = owner;
  }
      
    public String getSchedulingPattern()
    {
      return schedulingPattern;
    }
    
    public void setSchedulingPattern(String pattern)
    {
      this.schedulingPattern = pattern;
    }

    public boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(boolean value)
    {
        this.isActive = value;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("{ ");
        sb.append("Id: " + id.toString());
        sb.append(", query: " + query);
        sb.append(", description: " + description);
        sb.append(", corpora: [");
        Iterator it = corpora.iterator();
        if (it.hasNext())
        {
            sb.append((String) it.next());
        }
        while(it.hasNext())
        {
            sb.append(", " + (String) it.next());
        }
        sb.append("], owner: " + owner);
        sb.append(", isOwnerGroup: " + isOwnerGroup);
        sb.append(", isActive: " + isActive);
        
        sb.append("}");
        return sb.toString();
    }
}
