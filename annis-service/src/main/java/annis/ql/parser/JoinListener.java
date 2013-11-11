/*
 * Copyright 2013 SFB 632.
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

package annis.ql.parser;

import annis.exceptions.AnnisQLSyntaxException;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.ql.AqlParser;
import annis.ql.AqlParserBaseListener;
import annis.sqlgen.model.CommonAncestor;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Identical;
import annis.sqlgen.model.Inclusion;
import annis.sqlgen.model.Join;
import annis.sqlgen.model.LeftAlignment;
import annis.sqlgen.model.LeftDominance;
import annis.sqlgen.model.LeftOverlap;
import annis.sqlgen.model.Overlap;
import annis.sqlgen.model.PointingRelation;
import annis.sqlgen.model.Precedence;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.Sibling;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class JoinListener extends AqlParserBaseListener
{
  
  private static final Logger log = LoggerFactory.getLogger(JoinListener.class);
  
  private final int precedenceBound;
  /** An array which has an entry for each alternative. 
   *  Each entry maps node variables to a collection of query nodes.
   */
  private final Multimap<String, QueryNode>[] alternativeNodes;
  private int alternativeIndex;
  
  /**
   * Constructor.
   * @param data The {@link QueryData} containing the already parsed nodes.
   * @param precedenceBound  maximal range of precedence
   */
  public JoinListener(QueryData data, int precedenceBound)
  {
    this.precedenceBound = precedenceBound;
    this.alternativeNodes = new Multimap[data.getAlternatives().size()];
    
    int i=0;
    for(List<QueryNode> alternative : data.getAlternatives())
    {
      alternativeNodes[i] = HashMultimap.create();
      for(QueryNode n : alternative)
      {
        alternativeNodes[i].put(n.getVariable(), n);
      }
      i++;
    }
  }

  @Override
  public void enterAndExpr(AqlParser.AndExprContext ctx)
  {
    Preconditions.checkArgument(alternativeIndex < alternativeNodes.length);
  }

  @Override
  public void exitAndExpr(AqlParser.AndExprContext ctx)
  {
    alternativeIndex++;
  }
  
  
  
  @Override
  public void enterRootTerm(AqlParser.RootTermContext ctx)
  {
    Collection<QueryNode> targets = nodesByRef(ctx.left);
    Preconditions.checkArgument(!targets.isEmpty(), errorLHS("root") 
      + ": " + ctx.getText());
    for(QueryNode target : targets)
    {
      target.setRoot(true);
    }
  }

  @Override
  public void enterArityTerm(AqlParser.ArityTermContext ctx)
  {
    Collection<QueryNode> targets = nodesByRef(ctx.left);
    Preconditions.checkArgument(!targets.isEmpty(), errorLHS("arity") 
      + ": " + ctx.getText());
    
    for(QueryNode target : targets)
    {
      target.setArity(annisRangeFromARangeSpec(ctx.rangeSpec()));
    }
  }

  @Override
  public void enterTokenArityTerm(AqlParser.TokenArityTermContext ctx)
  {
    Collection<QueryNode> targets = nodesByRef(ctx.left);
    Preconditions.checkArgument(!targets.isEmpty(), errorLHS("token-arity") 
      + ": " + ctx.getText());
    
    for(QueryNode target : targets)
    {
      target.setTokenArity(annisRangeFromARangeSpec(ctx.rangeSpec()));
    }
  }

  @Override
  public void enterDirectPrecedence(
    AqlParser.DirectPrecedenceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("precendence") 
      + ": " + ctx.getText());
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("precendence")
      + ": " + ctx.getText());
    
    String segmentationName = null;
    if(ctx.layer != null)
    {
      segmentationName=ctx.layer.getText();
    }
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new Precedence(right, 1, segmentationName));
      }
    }
  }

  @Override
  public void enterIndirectPrecedence(
    AqlParser.IndirectPrecedenceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("precendence") 
      + ": " + ctx.getText());
    Preconditions.checkNotNull(!nodesRight.isEmpty(), errorRHS("precendence")
      + ": " + ctx.getText());
    
    String segmentationName = null;
    if(ctx.layer != null)
    {
      segmentationName=ctx.layer.getText();
    }
    
    for (QueryNode left : nodesLeft)
    {
      for (QueryNode right : nodesRight)
      {
        if (precedenceBound > 0)
        {
          left.addJoin(
            new Precedence(right, 1, precedenceBound, segmentationName));
        }
        else
        {
          left.addJoin(new Precedence(right, segmentationName));
        }
      }
    }
  }

  @Override
  public void enterRangePrecedence(AqlParser.RangePrecedenceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkNotNull(!nodesLeft.isEmpty(), errorLHS("precendence") 
      + ": " + ctx.getText());
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("precendence") 
      + ": " + ctx.getText());
    
    QueryNode.Range range = annisRangeFromARangeSpec(ctx.rangeSpec());
    if(range.getMin() == 0 || range.getMax() == 0)
    {
       throw new AnnisQLSyntaxException("Distance can't be 0");
    }
    else
    {
      String segmentationName = null;
      if(ctx.layer != null)
      {
        segmentationName=ctx.layer.getText();
      }
      
      for (QueryNode left : nodesLeft)
      {
        for (QueryNode right : nodesRight)
        {
          left.addJoin(
            new Precedence(right, range.getMin(), range.getMax(),
            segmentationName));
        }
      }
      
    }
  }

  @Override
  public void enterIdenticalCoverage(AqlParser.IdenticalCoverageContext ctx)
  {
    join(ctx, Identical.class);
  }

  @Override
  public void enterLeftAlign(AqlParser.LeftAlignContext ctx)
  {
    join(ctx, LeftAlignment.class);
  }

  @Override
  public void enterRightAlign(AqlParser.RightAlignContext ctx)
  {
    join(ctx, RightAlignment.class);
  }

  @Override
  public void enterInclusion(AqlParser.InclusionContext ctx)
  {
    join(ctx, Inclusion.class);
  }

  @Override
  public void enterOverlap(AqlParser.OverlapContext ctx)
  {
    join(ctx, Overlap.class);
  }

  @Override
  public void enterRightOverlap(AqlParser.RightOverlapContext ctx)
  {
    join(ctx, RightOverlap.class);
  }

  @Override
  public void enterLeftOverlap(AqlParser.LeftOverlapContext ctx)
  {
    join(ctx, LeftOverlap.class);
  }

  @Override
  public void enterDirectDominance(AqlParser.DirectDominanceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("dominance")
     + ": " + ctx.getText());
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("dominance")
     + ": " + ctx.getText());
    
    String layer = ctx.layer == null ? null : ctx.layer.getText();
    
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
    
        if(ctx.anno != null)
        {
          LinkedList<QueryAnnotation> annotations = fromEdgeAnnotation(ctx.anno);
          for (QueryAnnotation a : annotations)
          {
            right.addEdgeAnnotation(a);
          }
        }

        if(ctx.LEFT_CHILD() != null)
        {
          left.addJoin(new LeftDominance(right, layer));
        }
        else if(ctx.RIGHT_CHILD() != null)
        {
          left.addJoin(new RightDominance(right, layer));
        }
        else
        {
          left.addJoin(new Dominance(right, layer, 1));
        }
      }
    }
  }

  @Override
  public void enterIndirectDominance(AqlParser.IndirectDominanceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("dominance")
      + ": " + ctx.getText());
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("dominance")
      + ": " + ctx.getText());
    
    String layer = ctx.layer == null ? null : ctx.layer.getText();
   
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new Dominance(right, layer));
      }
    }
  }

  @Override
  public void enterRangeDominance(AqlParser.RangeDominanceContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("dominance")
      + ": " + ctx.getText());
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("dominance")
      + ": " + ctx.getText());
    
    String layer = ctx.layer == null ? null : ctx.layer.getText();
   
    QueryNode.Range range = annisRangeFromARangeSpec(ctx.rangeSpec());
    Preconditions.checkArgument(range.getMax() != 0, "Distance can't be 0");
    Preconditions.checkArgument(range.getMin() != 0, "Distance can't be 0");
    
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new Dominance(right, layer, range.getMin(), range.getMax()));
      }
    }
  }

  @Override
  public void enterDirectPointing(AqlParser.DirectPointingContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("pointing")
      + ": " + ctx.getText());
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("pointing")
      + ": " + ctx.getText());
    
    String label = ctx.label == null ? null : ctx.label.getText();
    
    for (QueryNode right : nodesRight)
    {
      if (ctx.anno != null)
      {
        LinkedList<QueryAnnotation> annotations = fromEdgeAnnotation(ctx.anno);
        for (QueryAnnotation a : annotations)
        {
          right.addEdgeAnnotation(a);
        }
      }
      
      for (QueryNode left : nodesLeft)
      {
        left.addJoin(new PointingRelation(right, label, 1));
      }
    }
  }

  @Override
  public void enterIndirectPointing(AqlParser.IndirectPointingContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("pointing")
      + ": " + ctx.getText());
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("pointing")
      + ": " + ctx.getText());
    
    String label = ctx.label == null ? null : ctx.label.getText();
   
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new PointingRelation(right, label));
      }
    }
  }

  @Override
  public void enterRangePointing(AqlParser.RangePointingContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("pointing")
      + ": " + ctx.getText());
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("pointing")
      + ": " + ctx.getText());
    
    String label = ctx.label == null ? null : ctx.label.getText();
   
    QueryNode.Range range = annisRangeFromARangeSpec(ctx.rangeSpec());
    Preconditions.checkArgument(range.getMax() != 0, "Distance can't be 0");
    Preconditions.checkArgument(range.getMin() != 0, "Distance can't be 0");
    
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new PointingRelation(right, label, range.getMin(), range.getMax()));
      }
    }
  }

  @Override
  public void enterCommonParent(AqlParser.CommonParentContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("common parent")
      + ": " + ctx.getText());
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("common parent")
      + ": " + ctx.getText());
    
    String label = ctx.label == null ? null : ctx.label.getText();
    
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new Sibling(right, label));
      }
    }
  }

  @Override
  public void enterCommonAncestor(AqlParser.CommonAncestorContext ctx)
  {
    Collection<QueryNode> nodesLeft = nodesByRef(ctx.left);
    Collection<QueryNode> nodesRight = nodesByRef(ctx.right);
    Preconditions.checkArgument(!nodesLeft.isEmpty(), errorLHS("common ancestor")
      + ": " + ctx.getText());
    Preconditions.checkArgument(!nodesRight.isEmpty(), errorRHS("common ancestor")
      + ": " + ctx.getText());
    
    String label = ctx.label == null ? null : ctx.label.getText();
    
    for(QueryNode left : nodesLeft)
    {
      for(QueryNode right : nodesRight)
      {
        left.addJoin(new CommonAncestor(right, label));
      }
    }
  }
  
  @Override
  public void enterIdentity(AqlParser.IdentityContext ctx)
  {
    join(ctx, Identical.class);
  }
  
  /**
   * Automatically create a join from a node and a join class.
   *
   * This will automatically get the left and right hand refs
   * and will construct a new join specified by the type using reflection.
   *
   * @node
   * @type00
   */
  private void join(ParserRuleContext ctx, Class<? extends Join> type)
  {
    Collection<QueryNode> leftNodes = nodesByRef(ctx.getToken(AqlParser.REF, 0).getSymbol());
    Collection<QueryNode> rightNodes = nodesByRef(ctx.getToken(AqlParser.REF, 1).getSymbol());

    Preconditions.checkArgument(!leftNodes.isEmpty(), errorLHS(type.getSimpleName())
      + ": " + ctx.getText());
    Preconditions.checkNotNull(!rightNodes.isEmpty(), errorRHS(type.getSimpleName())
      + ": " + ctx.getText());
    
    for (QueryNode left : leftNodes)
    {
      for (QueryNode right : rightNodes)
      {
        try
        {
          Constructor<? extends Join> c = type.getConstructor(QueryNode.class);
          Join newJoin = c.newInstance(right);
          left.addJoin(newJoin);
        }
        catch (NoSuchMethodException ex)
        {
          log.error(null, ex);
        }
        catch (InstantiationException ex)
        {
          log.error(null, ex);
        }
        catch (IllegalAccessException ex)
        {
          log.error(null, ex);
        }
        catch (InvocationTargetException ex)
        {
          log.error(null, ex);
        }
      }
    }
  }
  
  
  private QueryNode.Range annisRangeFromARangeSpec(
    AqlParser.RangeSpecContext spec)
  {
    String min = spec.min.getText();
    String max = spec.max != null ? spec.max.getText() : null;
    if (max == null)
    {
      return new QueryNode.Range(Integer.parseInt(min), Integer.parseInt(min));
    }
    else
    {
      return new QueryNode.Range(Integer.parseInt(min), Integer.parseInt(max));
    }
  }
  
  private LinkedList<QueryAnnotation> fromEdgeAnnotation(
    AqlParser.EdgeSpecContext ctx)
  {
    LinkedList<QueryAnnotation> annos = new LinkedList<QueryAnnotation>();
    for(AqlParser.EdgeAnnoContext annoCtx : ctx.edgeAnno())
    {
      String namespace = annoCtx.qName().namespace == null
        ? null : annoCtx.qName().namespace.getText();
      String name = annoCtx.qName().name.getText();
      String value = QueryNodeListener.textFromSpec(annoCtx.value);
      QueryNode.TextMatching matching = QueryNodeListener.textMatchingFromSpec(
        annoCtx.value, annoCtx.NEQ() != null);
      
      annos.add(new QueryAnnotation(namespace, name, value, matching));
      
    }
    return annos;
  }
  
  private Collection<QueryNode> nodesByRef(Token ref)
  {
    return alternativeNodes[alternativeIndex].get("" + ref.getText().substring(1));
  }
  
  
  private String errorLHS(String function)
  {
    return function + " operator needs a left-hand-side";
  }

  private String errorRHS(String function)
  {
    return function + " operator needs a right-hand-side";
  }
  
}
