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
package annis.frontend.servlets.exporter;

import java.io.IOException;
import javax.servlet.http.*;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class GridExporterServlet extends GeneralTextExporter {

List<String> searchedValues = Arrays.asList("tok","LAT","bibl");// Zu Testzwecken erstmal diese 3 Werte, spaeter durch GUI uebergeben.
int counter = 0;

 @Override
public void convertText(AnnisResultSet queryResult, HttpServletResponse response) throws IOException{
        
	for (AnnisResult annisResult : queryResult) {
          Set<Long> matchedNodeIds = annisResult.getGraph().getMatchedNodeIds();

          counter++;
          response.getWriter().append(counter + ". ");
          for (String value : searchedValues){

              if (value.equals("tok")){
                response.getWriter().append("\t "+value+"\t ");
                for (AnnisNode annisNode : annisResult.getGraph().getTokens()) {response.getWriter().append(annisNode.getSpannedText()+" ");}
                response.getWriter().append("\n");
              }else{

                int count = 0;
                for (AnnisNode resolveNode: annisResult.getGraph().getNodes()){
                  
                    for(Annotation resolveAnnotation : resolveNode.getNodeAnnotations()){
                  
                        if(resolveAnnotation.getName().equals(value)) {
                            if (count==0) {
                                response.getWriter().append("\t "+value+"\t ");
                                count++;
                            }
                            response.getWriter().append(resolveAnnotation.getValue()+" ");
                        }
                    }
                }
                if (count!=0) response.getWriter().append("\n");
              }
          }
          
          response.getWriter().append("\n\n");
     }
  }
}
