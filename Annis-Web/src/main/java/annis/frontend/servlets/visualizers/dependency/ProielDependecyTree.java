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
package annis.frontend.servlets.visualizers.dependency;

import annis.frontend.servlets.MatchedNodeColors;
import annis.frontend.servlets.visualizers.AbstractDotVisualizer;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.Edge;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class ProielDependecyTree extends AbstractDotVisualizer
{

  private StringBuilder dot;
  private Set<String> alreadyWrittenEdge;

  @Override
  public void createDotContent(StringBuilder sb)
  {
    this.dot = sb;
    alreadyWrittenEdge = new HashSet<String>();

    w("digraph G {\n");
    w("charset=\"UTF-8\";\n");
    w("graph [truecolor bgcolor=\"#ff000000\"];\n");
    
    writeAllNodes();
    writeAllEdges();

    w("}\n");

  }

  private void writeAllNodes()
  {

    for (AnnisNode n : getResult().getGraph().getNodes())
    {
      boolean isDepNode = false;
      String word = null;
      for (Annotation anno : n.getNodeAnnotations())
      {
        if ("tiger".equals(anno.getNamespace()) && "word".equals(anno.getName()))
        {
          isDepNode = true;
          word = anno.getValue();
          break;
        }
      }

      Vector2 v = new Vector2(n);
      if (isDepNode)
      {
        writeNode(n, word);
      }

    }
  }

  private void writeAllEdges()
  {
    for (Edge e : getResult().getGraph().getEdges())
    {
      boolean isDepEdge = false;
      for (Annotation anno : e.getAnnotations())
      {
        if ("tiger".equals(anno.getNamespace()) && "func".equals(anno.getName()))
        {
          isDepEdge = true;
          break;
        }
      }
      if (isDepEdge)
      {
        writeEdge(e);
      }
    }
  }

  private void writeEdge(Edge e)
  {
    AnnisNode srcNode = e.getSource();
    AnnisNode destNode = e.getDestination();

    if (srcNode == null || destNode == null)
    {
      return;
    }

    String srcId = "" + srcNode.getId();
    String destId = "" + destNode.getId();

    // get the edge annotation
    StringBuilder sbAnno = new StringBuilder();
    for (Annotation anno : e.getAnnotations())
    {
      if ("func".equals(anno.getName()))
      {
        if("--".equals(anno.getValue()))
        {
          return;
        }
        sbAnno.append(anno.getValue());
      }
      break;
    }

    String edgeString = srcId + " -> " + destId
      + "[shape=box, label=\"" + sbAnno.toString() + "\"]";

    if (!alreadyWrittenEdge.contains(edgeString))
    {
      w("  " + edgeString);
      alreadyWrittenEdge.add(edgeString);
    }
  }

  private void writeNode(AnnisNode n, String word)
  {
    if("--".equals(word))
    {
      w("" +  n.getId() + " [fontcolor=\"black\",label=\"\",shape=\"circle\"];\n");
    }
    else
    {
      w("" + n.getId() + "[shape=box, label=\"" + word + "\" ");
    }
    // background color
    w("style=filled, ");
    w("fillcolor=\"");
    String colorAsString = getMarkableExactMap().get(Long.toString(n.getId()));
    if (colorAsString != null)
    {
      MatchedNodeColors color = MatchedNodeColors.valueOf(colorAsString);
      w(color.getHTMLColor());
    }
    else
    {
      w("#ffffff");
    }
    w("\" ];\n");
  }

  private void w(String s)
  {
    dot.append(s);
  }

  private void w(long l)
  {
    dot.append(l);
  }
}
