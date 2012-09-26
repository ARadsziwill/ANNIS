/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.visualizers.component.grid;

import annis.CommonHelper;
import static annis.model.AnnisConstants.*;

import annis.gui.visualizers.AbstractVisualizer;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.widgets.AnnotationGrid;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class GridVisualizer extends AbstractVisualizer<GridVisualizer.GridVisualizerComponent>
{

  @Override
  public String getShortName()
  {
    return "grid";
  }

  @Override
  public GridVisualizerComponent createComponent(VisualizerInput visInput)
  {
    GridVisualizerComponent component = new GridVisualizerComponent(visInput);
    return component;
  }

  public static class GridVisualizerComponent extends Panel
  {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GridVisualizerComponent.class);
    private AnnotationGrid grid;

    public enum ElementType
    {

      begin,
      end,
      middle,
      single,
      noEvent
    }

    public GridVisualizerComponent(VisualizerInput input)
    {
      setWidth("100%");
      setHeight("-1");
      ((VerticalLayout) getContent()).setSizeUndefined();
      
      grid = new AnnotationGrid();
      grid.addStyleName("partitur_table");
      addComponent(grid);
      
      SDocumentGraph graph = input.getDocument().getSDocumentGraph();
     
      List<String> annos = new LinkedList<String>(getAnnotationLevelSet(graph, 
        input.getNamespace()));
      
      EList<SToken> token = graph.getSortedSTokenByText();
      long startIndex = token.get(0).getSFeature(ANNIS_NS, FEAT_TOKENINDEX).getSValueSNUMERIC();
      long endIndex = token.get(token.size()-1).getSFeature(ANNIS_NS, FEAT_TOKENINDEX).getSValueSNUMERIC();
      
      LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation = 
        parseSalt(input.getDocument().getSDocumentGraph(), annos, 
          (int) startIndex, (int) endIndex);
      
      // add tokens as row
      Row tokenRow = new Row();
      for(SToken t : token)
      {
        long idx = t.getSFeature(ANNIS_NS, FEAT_TOKENINDEX).getSValueSNUMERIC()
          - startIndex;
        String text = CommonHelper.getSpannedText(t);
        tokenRow.addEvent(new GridEvent((int) idx, (int) idx, text));
      }
      ArrayList<Row> tokenRowList = new ArrayList<Row>();
      tokenRowList.add(tokenRow);
      
      rowsByAnnotation.put("tok", tokenRowList);
      
      grid.setRowsByAnnotation(rowsByAnnotation);
      
    }
    
    private long clip(long value, long min, long max)
    {
      if(value > max)
      {
        return max;
      }
      else if(value < min)
      {
        return min;
      }
      else
      {
        return value;
      }
      
    }
    
    private Set<String> getAnnotationLevelSet(SDocumentGraph graph, String namespace)
    {
      Set<String> result = new TreeSet<String>();

      if(graph != null)
      {
        for(SSpan n : graph.getSSpans())
        {
          for(SLayer layer : n.getSLayers())
          {
            if(namespace.equals(layer.getSName()))
            {
              for (SAnnotation anno : n.getSAnnotations())
              {
                result.add(anno.getQName());
              }
              // we got all annotations of this node, jump to next span
              break;
            } // end if namespace equals layer name
          } // end for each layer
        } // end for each span
      }

      return result;
    }
    
    /**
     * Converts Salt document graph to rows.
     * 
     * @param graph
     * @param annotationNames
     * @param startTokenIndex token index of the first token in the match
     * @param endTokenIndex  token index of the last token in the match
     * @return 
     */
    private LinkedHashMap<String, ArrayList<Row>> parseSalt(SDocumentGraph graph,
      List<String> annotationNames, int startTokenIndex, int endTokenIndex)
    {
      // only look at annotations which were defined by the user
      LinkedHashMap<String, ArrayList<Row>> rowsByAnnotation = 
        new LinkedHashMap<String, ArrayList<Row>>();
      
      for(String anno : annotationNames)
      {
        rowsByAnnotation.put(anno, new ArrayList<Row>());
      }
      
      EList<STYPE_NAME> types = new BasicEList<STYPE_NAME>();
      types.add(STYPE_NAME.SSPANNING_RELATION);
      types.add(STYPE_NAME.STEXTUAL_RELATION);
      types.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
      types.add(STYPE_NAME.SSEQUENTIAL_RELATION);
      for(SSpan span : graph.getSSpans())
      {  
        // calculate the left and right values of a span
        // TODO: howto get these numbers with Salt?
        long leftLong = span.getSFeature(ANNIS_NS, FEAT_LEFTTOKEN).getSValueSNUMERIC();
        long rightLong = span.getSFeature(ANNIS_NS, FEAT_RIGHTTOKEN).getSValueSNUMERIC();
        
        leftLong = clip(leftLong, startTokenIndex, endTokenIndex);
        rightLong = clip(rightLong, startTokenIndex, endTokenIndex);
        
        int left = ((int) leftLong) - startTokenIndex;
        int right = ((int) rightLong) - startTokenIndex;
        
        for(SAnnotation anno : span.getSAnnotations())
        {
          ArrayList<Row> rows = rowsByAnnotation.get(anno.getQName());
          if(rows != null)
          {
            // only do something if the annotation was defined before

            // 1. give each annotation of each span an own row
            Row r = new Row();
            r.addEvent(new GridEvent(left, right, anno.getSValueSTEXT()));
            rows.add(r);
          }
        } // end for each annotation of span
      } // end for each span
      
      // 2. merge rows when possible
      for(Map.Entry<String, ArrayList<Row>> e : rowsByAnnotation.entrySet())
      {
        mergeAllRowsIfPossible(e.getValue());
      }
      
      return rowsByAnnotation;
    }
    
    /**
     * Merges the rows.
     * This function uses a heuristical approach that guarantiess to merge 
     * all rows into one if there is no conflict at all. If there are conflicts
     * the heuristic will be best-efford but with linear runtime (given a a number
     * of rows).
     * @param rows Will be altered, if no conflicts occcured this wil have only
     *              one element.
     */
    private void mergeAllRowsIfPossible(ArrayList<Row> rows)
    {
      // use fixed seed in order to get consistent results (with random properties)
      Random rand = new Random(5711l);
      int tries = 0;
      // this should be enough to be quite sure we don't miss any optimalization
      // possibility
      final int maxTries = rows.size()*2;
      
      // do this loop until we successfully merged everything into one row
      // or we give up until too much tries
      while(rows.size() > 1 && tries < maxTries)
      {
        // choose two random entries
        int oneIdx = rand.nextInt(rows.size());
        int secondIdx = rand.nextInt(rows.size());
        if(oneIdx == secondIdx)
        {
          // try again if we choose the same rows by accident
          continue;
        }
        
        Row one = rows.get(oneIdx);
        Row second = rows.get(secondIdx);
        
        if(one.merge(second))
        {
          // remove the second one since it is merged into the first
          rows.remove(secondIdx);
          
          // success: reset counter
          tries = 0;
        }
        else
        {
          // increase counter to avoid endless loops if no improvement is possible
          tries++;
        }
      }
    }
  } // end GridVisualizerComponent

}
