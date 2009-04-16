package annis.frontend.servlets.visualizers.partitur;

import annis.frontend.servlets.visualizers.Visualizer;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;

/**
 *
 * @author thomas
 */
public class PartiturVisualizer extends Visualizer
{

  public enum ElementType
  {
    begin,
    end,
    middle,
    single,
    noEvent
  }

  @Override
  public void writeOutput(Writer writer)
  {
    try
    {
      // get partitur
      Document jdomDoc = getPaulaJDOM();
      PartiturParser partitur = new PartiturParser(jdomDoc, namespace);

      List<String> tierNames = new LinkedList<String>(partitur.getKnownTiers());
      Collections.sort(tierNames);

      writer.append("<html><head>");
      writer.append("<link href=\"" + contextPath + "/css/visualizer/partitur.css\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<link href=\"" + contextPath + "/javascript/extjs/resources/css/ext-all.css\" rel=\"stylesheet\" type=\"text/css\" >");
      writer.append("<script type=\"text/javascript\" src=\"" + contextPath + "/javascript/extjs/adapter/ext/ext-base.js\"></script>");
      writer.append("<script type=\"text/javascript\" src=\"" + contextPath + "/javascript/extjs/ext-all.js\"></script>");

      writer.append("<script>\nvar levelNames = [");
      int i = 0;
      for(String levelName : tierNames)
      {
        writer.append((i++ > 0 ? ", " : "") + "\"" + levelName + "\"");
      }
      writer.append("];\n</script>");
      writer.append("<script type=\"text/javascript\" src=\"" + contextPath + "/javascript/annis/visualizer/PartiturVisualizer2.js\"></script>");

      writer.append("</head>");
      writer.append("<body>\n");

      writer.append("<div id=\"toolbar\"></div>");
      writer.append("<div id=\"partiture\" style=\"position: absolute; top: 30px; left: 0px;\">");
      writer.append("<table class=\"partitur_table\">\n");

      for(String tier : tierNames)
      {
        writer.append("<tr id=\"level_" + tier + "\">");

        writer.append("<th>" + tier + "</th>");

        Iterator<PartiturParser.Token> itToken = partitur.getToken().iterator();
        while(itToken.hasNext())
        {
          PartiturParser.Token token = itToken.next();
          
          String val = "";
          String color = "";
          String styleClass = "";
          
          StringBuffer tokenIdsArray = new StringBuffer();
          StringBuffer eventIdsArray = new StringBuffer();

          String quicktip = "";
          int colspan=1;

          ElementType type = getTypeForToken(token, tier);
          PartiturParser.Event event = token.getTier2Event().get(tier);

          if(event != null && type != ElementType.noEvent)
          {
            styleClass = "single_event";
            quicktip = "ext:qtip=\"" + partitur.namespaceForTier(tier) + ":" + tier + " = " + event.getValue() + "\" ";
            
            // "eat up" token and use this info for colspan
            ElementType tmpType = type;
            
            while(itToken.hasNext() &&
              tmpType != ElementType.end && tmpType != ElementType.single)
            {
              PartiturParser.Token tmpToken = itToken.next();
              tmpType = getTypeForToken(tmpToken, tier);
              colspan++;
            }

            color = "black";
            if(markableMap.containsKey("" + event.getId()))
            {
              color = markableMap.get("" + event.getId());
            }
            val = event.getValue();
          
            // token ids containing the same event: for Javascript highlighting

            // current token is included in every case
            tokenIdsArray.append("" + token.getId());
            eventIdsArray.append(tier + "_" + token.getId());

            // go to the end of the event
            PartiturParser.Token tmp = token.getAfter();
            boolean proceed = true;
           
            while(tmp != null && proceed)
            {
              PartiturParser.Event tmpEvent = tmp.getTier2Event().get(tier);
              if(tmpEvent != null && tmpEvent.getId() == event.getId())
              {
                tokenIdsArray.append("," + tmp.getId());
                eventIdsArray.append("," + tier + "_" + tmp.getId());
              }
              else
              {
                proceed = false;
              }

              tmp = tmp.getAfter();
            }

          }

          writer.append("<td class=\"" + styleClass + "\" "
            + "id=\"event_" + tier + "_" + token.getId() + "\" "
            + "style=\"color:" + color + ";\" "
            + "colspan=" + colspan + " "
            + "annis:tokenIds=\"" + tokenIdsArray + "\" "
            + "annis:eventIds=\"" + eventIdsArray + "\" "
            + quicktip + " "
            + "onMouseOver=\"toggleAnnotation(this, true);\" "
            + "onMouseOut=\"toggleAnnotation(this, false);\"" +
            ">" + val + "</td>");

        }
        writer.append("</tr>");

      }


      // add token itself
      writer.append("<tr><th>tok</th>");


      for(PartiturParser.Token token : partitur.getToken())
      {
        String color = "black";

        if(markableMap.containsKey("" + token.getId()))
        {
          color = markableMap.get("" + token.getId());
        }
        writer.append("<td class=\"tok\" style=\"color:" + color + ";\" " +
          "id=\"token_" + token.getId() + "\" " +
          ">" + token.getValue() + "</td>");
      }
      writer.append("</tr>");


      writer.append("</table>\n");
      writer.append("</div>\n");
      writer.append("</body></html>");

    }
    catch(Exception ex)
    {
      Logger.getLogger(PartiturVisualizer.class.getName()).log(Level.SEVERE, null, ex);
      try
      {
        writer.append("<html><body>Error occured</body></html>");
      }
      catch(IOException ex1)
      {
        Logger.getLogger(PartiturVisualizer.class.getName()).log(Level.SEVERE, null, ex1);
      }
    }
  }

  private ElementType getTypeForToken(PartiturParser.Token token, String tier)
  {
    // get token before and after
    PartiturParser.Token beforeToken = token.getBefore();
    PartiturParser.Token afterToken = token.getAfter();

    PartiturParser.Event event = token.getTier2Event().get(tier);

    if(event != null)
    {
      PartiturParser.Event beforeEvent =
        beforeToken == null ? null : beforeToken.getTier2Event().get(tier);
      PartiturParser.Event afterEvent =
        afterToken == null ? null : afterToken.getTier2Event().get(tier);

      boolean left = false;
      boolean right = false;
      // check if the events left and right have the same
      // id (and are the same event)
      if(beforeEvent != null && beforeEvent.getId() == event.getId())
      {
        left = true;
      }
      if(afterEvent != null && afterEvent.getId() == event.getId())
      {
        right = true;
      }

      if(left && right)
      {
        return ElementType.middle;
      }
      else if(left)
      {
        return ElementType.end;
      }
      else if(right)
      {
        return ElementType.begin;
      }
      else
      {
        return ElementType.single;
      }
    }

    return ElementType.noEvent;
  }
}



