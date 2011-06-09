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
package annis.frontend.servlets.visualizers.dependency;

import annis.frontend.servlets.visualizers.WriterVisualizer;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.Edge;
import annis.service.ifaces.AnnisResult;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author thomas
 */
public class VakyarthaDependencyTree extends WriterVisualizer
{

  private Writer theWriter;

  @Override
  public void writeOutput(Writer writer)
  {
    theWriter = writer;
    try
    {
      AnnisResult result = getResult();

      println("<html>");
      println("<head>");

      println("<script type=\"text/javascript\" src=\"" + getContextPath() + "/javascript/annis/visualizer/VakyarthaDependencyTree/jquery-1.4.2.min.js\"></script>");
      println("<script type=\"text/javascript\" src=\"" + getContextPath() + "/javascript/annis/visualizer/VakyarthaDependencyTree/raphael.js\"></script>");
      println("<script type=\"text/javascript\" src=\"" + getContextPath() + "/javascript/annis/visualizer/VakyarthaDependencyTree/vakyarthaDependency.js\"></script>");

      // output the data for the javascript
      println("<script type=\"text/javascript\">");
      println("fcolors={};");
      println("shownfeatures=[\"t\"];");
      println("tokens=new Object();");


      HashMap<Long,Integer> id2pos = new HashMap<Long, Integer>();
      int counter = 0;
      for(AnnisNode tok : result.getGraph().getTokens())
      {
        id2pos.put(tok.getId(), counter);
        counter++;
      }


      counter = 0;
      for(AnnisNode tok : result.getGraph().getTokens())
      {
        JSONObject o = new JSONObject();
        o.put("t", tok.getSpannedText());

        JSONObject govs = new JSONObject();
        Set<Edge> edges = tok.getIncomingEdges();
        for(Edge e : edges)
        {
          if(e.getEdgeType() == Edge.EdgeType.POINTING_RELATION)
          {
            String label = "";
            for(Annotation anno : e.getAnnotations())
            {
              if(anno.getNamespace() != null && anno.getNamespace().equals(anno.getNamespace()))
              {
                label = anno.getValue();
                break;
              }
            }

            if(e.getSource() == null)
            {
              govs.put("root", label);
            }
            else
            {
              govs.put("" + id2pos.get(e.getSource().getId()), label);
            }
          }
        }
        o.put("govs", govs);

        JSONObject attris = new JSONObject();

        JSONObject tAttris = new JSONObject();
        String tokenColor = "black";
        if(getMarkableExactMap().containsKey("" + tok.getId()))
        {
          tokenColor = getMarkableExactMap().get("" + tok.getId());
        }
        tAttris.put("fill", tokenColor);
        tAttris.put("font", "11px Arial,Tahoma,Helvetica,Sans-Serif");

        attris.put("t", tAttris);
        o.put("attris", attris);

        theWriter.append("tokens[").append("" + counter).append("]=");
        theWriter.append(o.toString().replaceAll("\n", " "));
        theWriter.append(";\n");
        counter++;
      }

      println("</script>");

      println("</head>");
      println("<body>");

      // the div to render the javascript to
      println("<div id=\"holder\" style=\"background:white; position:relative;\"> </div>");

      println("</body>");
      println("</html>");
    }

    catch (JSONException ex)
    {
      Logger.getLogger(VakyarthaDependencyTree.class.getName()).log(Level.SEVERE, null, ex);
    }    catch (IOException ex)
    {
      Logger.getLogger(VakyarthaDependencyTree.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void println(String s) throws IOException
  {
    println(s, 0);
  }

  private void println(String s, int indent) throws IOException
  {
    for (int i = 0; i < indent; i++)
    {
      theWriter.append("\t");
    }
    theWriter.append(s);
    theWriter.append("\n");
  }
}
