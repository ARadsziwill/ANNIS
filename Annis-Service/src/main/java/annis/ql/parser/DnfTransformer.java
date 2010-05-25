package annis.ql.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAndExpr;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AAnyNodeSearchExpr;
import annis.ql.node.ALinguisticConstraintExpr;
import annis.ql.node.AMetaConstraintExpr;
import annis.ql.node.AOrExpr;
import annis.ql.node.ATextSearchExpr;
import annis.ql.node.ATextSearchNotEqualExpr;
import annis.ql.node.PExpr;
import annis.ql.node.Start;


public class DnfTransformer extends DepthFirstAdapter {

	private Logger log = Logger.getLogger(this.getClass());
	
	private SearchExpressionCounter counter;
	
	@Override
	public void caseStart(Start node) {
		counter = new SearchExpressionCounter();
		node.apply(counter);
		PExpr inDnf = normalize(node.getPExpr());
		node.setPExpr(inDnf);
		
		log.debug("dnf is: " + new Ast2String().toString(inDnf));
	}
	
	public PExpr normalize(PExpr expr) {
		if ( expr instanceof AAnnotationSearchExpr || expr instanceof ATextSearchExpr 
      || expr instanceof ATextSearchNotEqualExpr || expr instanceof AAnyNodeSearchExpr
      || expr instanceof AMetaConstraintExpr )
			return clone(expr);
		else if (expr instanceof ALinguisticConstraintExpr)
			return (PExpr) expr.clone();
		else if (expr instanceof AOrExpr)
			return normalize((AOrExpr) expr);
		else if (expr instanceof AAndExpr)
			return normalize((AAndExpr) expr);
		throw new RuntimeException("can't normalize node: " + expr.getClass());
	}
	
	public PExpr clone(PExpr node) {
		PExpr clone = (PExpr) node.clone();
		counter.mapSearchExpressionClone(clone, node);
		return clone;
	}
	
	public PExpr normalize(AOrExpr node) {
		List<PExpr> normalizedChildren = new ArrayList<PExpr>();
		for (PExpr expr : node.getExpr())
			normalizedChildren.add(normalize(expr));
		
		List<PExpr> children = new ArrayList<PExpr>();
		for (PExpr expr : normalizedChildren) {
			if (expr instanceof AOrExpr) {
				log.debug("inlining nested OR expression");

				AOrExpr or = (AOrExpr) expr;
				for (PExpr expr2 : or.getExpr())
					children.add(expr2);
			} else
				children.add(expr);
		}
		
		AOrExpr result = new AOrExpr();
		result.setExpr(children);
		return result;
	}

	public PExpr normalize(AAndExpr node) {
		List<PExpr> normalizedChildren = new ArrayList<PExpr>();
		for (PExpr expr : node.getExpr())
			normalizedChildren.add(normalize(expr));
		
		List<AOrExpr> ors = new ArrayList<AOrExpr>();
		List<PExpr> children = new ArrayList<PExpr>();
		for (PExpr expr : normalizedChildren) {
			if (expr instanceof AAndExpr) {
				log.debug("inlining nested AND expression");

				AAndExpr and = (AAndExpr) expr;
				for (PExpr expr2 : and.getExpr())
					children.add(expr2);
			} else if (expr instanceof AOrExpr) {
				ors.add((AOrExpr) expr);
			} else
				children.add(expr);
		}
		
		AAndExpr result = new AAndExpr();
		result.setExpr(children);
		if (ors.isEmpty())
			return result;
		
		List<AAndExpr> ands = new ArrayList<AAndExpr>();
		ands.add(result);

		List<AAndExpr> distributed = distribute(ors, ands);
		// FIXME: Bug in SableCC: es müsste sein AAndExpr.setExpr(List<? extends PExpr> exprs)
		List<PExpr> alternatives = new ArrayList<PExpr>();
		alternatives.addAll(distributed);
		
		AOrExpr or = new AOrExpr();
		or.setExpr(alternatives);
		return or;
	}
	
	public List<AAndExpr> distribute(List<AOrExpr> ors, List<AAndExpr> ands) {
		List<AAndExpr> res = new ArrayList<AAndExpr>();
		
		AOrExpr or = ors.get(ors.size() - 1);
		ors.remove(or);
		
		log.debug("distributing alternative");
		
		for (PExpr expr : or.getExpr()) {			
			for (AAndExpr and : ands) {
				List<PExpr> alternative = new ArrayList<PExpr>();

				for (PExpr factor : and.getExpr())
					alternative.add(clone(factor));
				
				PExpr clone = (PExpr) expr.clone();
				if (clone instanceof AAndExpr) {
					AAndExpr clonedAnd = (AAndExpr) clone;
					for (int i = 0; i < clonedAnd.getExpr().size(); ++i) 
          {
            PExpr original = ((AAndExpr) expr).getExpr().get(i);
            counter.mapSearchExpressionClone(clonedAnd.getExpr().get(i), original);
					}
				}
				counter.mapSearchExpressionClone(clone, expr);
				alternative.add(clone);
				
				AAndExpr newAnd = new AAndExpr();
				newAnd.setExpr(alternative);
				res.add((AAndExpr) normalize(newAnd));
			}
		}
		
		if (ors.isEmpty())
			return res;
		else
			return distribute(ors, res);
	}
	
	public int getPosition(PExpr expr) {
		return counter.getPosition(expr);
	}

	public SearchExpressionCounter getCounter() {
		return counter;
	}

	public void setCounter(SearchExpressionCounter counter) {
		this.counter = counter;
	}
}
