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
package annis.automation;

import annis.adapter.TreeSetAdapter;
import annis.adapter.UUIDAdapter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.StringUtils;

/**
 * A POJO representing an automated Query 
 * @author Andreas Radsziwill <radsziwill@stud.tu-darmstadt.de>
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
    @XmlElement
    private Type type;
    
    @XmlJavaTypeAdapter(UUIDAdapter.class)
    @XmlElement
    private final UUID id;
    @XmlElement(required = true)
    private Boolean isGroup; 
    @XmlElement(required = true)
    private Boolean isActive;
     
    @XmlJavaTypeAdapter(TreeSetAdapter.class)
    @XmlElement(name="corpora")
    private TreeSet<String> corpora = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    
    
    private AutomatedQuery()
    {
        this(null, null, null, null, null, null, null, null, null);
    }
    
    public AutomatedQuery(String query,
            TreeSet<String> corpora,
            String schedulingPattern,
            String description,
            String owner,
            Boolean isOwnerGroup,
            Boolean isActive,
            Type type,
            UUID id)
    {
      this.query = query;
      this.corpora = corpora;
      this.schedulingPattern = schedulingPattern;
      this.description = description;
      this.owner = owner;
      this.isGroup = isOwnerGroup;
      this.isActive = isActive;
      this.type = type;
      this.id = id;
    }

    public AutomatedQuery(String query, TreeSet<String> corpusNames, String schedulingPattern, String description, String owner, boolean isOwnerGroup, boolean isActive, Type type) {
        this(query, corpusNames, schedulingPattern, description, owner, isOwnerGroup, isActive, type, UUID.randomUUID());
    }
    
    public AutomatedQuery(String query, TreeSet<String> corpusNames)
    {
      this(query, corpusNames, "", "", null, false, false, Type.COUNT);
    }

    public AutomatedQuery(Properties props)
    {
      this.query = props.getProperty("query");
      this.description = props.getProperty("description");
      this.id = UUID.fromString(props.getProperty("id"));
      this.schedulingPattern = props.getProperty("schedulingPattern");
      this.owner = props.getProperty("owner");
      this.isActive = Boolean.parseBoolean(props.getProperty("isActive"));
      this.isGroup = Boolean.parseBoolean(props.getProperty("isGroup"));
      
      String corporaRaw = props.getProperty("corpora");
      for (String c : StringUtils.split(corporaRaw, ","))
      {
        this.corpora.add(c);
      }
      this.type = Type.valueOf(props.getProperty("type"));
      
    }
    
    /**
     * Copy Constructor
     */
  public AutomatedQuery(AutomatedQuery q)
  {
    this(q.getQuery(),
      new TreeSet<>(q.getCorpora()),
      q.getSchedulingPattern(),
      q.getDescription(),
      q.getOwner(),
      q.getIsGroup(),
      q.getIsActive(),
      q.getType(),
      q.getId());
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

  public TreeSet<String> getCorpora()
  {
    return corpora;
  }
 
  public void setCorpora(TreeSet<String> corpora)
  {
    this.corpora = corpora;
  }

  public boolean getIsGroup()
  {
    return isGroup;
  }

  public void setIsGroup(boolean isOwnerGroup)
  {
    this.isGroup = isOwnerGroup;
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
    
    public Type getType()
    {
      return type;
    }
    
    public void setType(Type type)
    {
      this.type = type;
    }
    
    /**
     * Checks for @code{null} properties and sets some default values if necessary
     * 
     * @param username the default username to set
     * @throws IllegalArgumentException when the given username is null or empty
     */
    
    public void setDefaults(String username) throws IllegalArgumentException
    {
      if (username == null || username.isEmpty())
        {
          throw new IllegalArgumentException("The default usernam may not be " +
            "null or empty");
        }
            
      if (owner == null || owner.isEmpty())
      {
           setOwner(username);
           setIsGroup(false);
       }
       isGroup = (isGroup == null)? false : isGroup;
       isActive = (isActive == null)? false : isActive;
       description = (description == null)? "" : description;      
    }
    
    public Properties toProperties()
    {
      Properties props = new Properties();
      if(query != null)
        {
         props.put("query", query);
        }
      if(corpora != null && !corpora.isEmpty())
      {
        props.put("corpora", StringUtils.join(corpora, ","));
      }
      if(description != null)
      {
        props.put("description", description);          
      }
      if(schedulingPattern != null)
      {
        props.put("schedulingPattern", schedulingPattern);
      }
      if(id != null)
      {
        props.put("id", id.toString());
      }
      if(owner != null)
      {
        props.put("owner", owner);
      }
      if(isGroup != null)
      {
        props.put("isGroup", isGroup);
      }
      if(isActive != null)
      {
        props.put("isActive", isActive);
      }
      if(type != null)
      {
        props.put("type", type);
      }
      return props;
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
        sb.append(", isOwnerGroup: " + isGroup);
        sb.append(", isActive: " + isActive);
        
        sb.append("}");
        return sb.toString();
    }
    
    public enum Type 
    {
      COUNT, FIND //, FREQUENCY
    }
}
