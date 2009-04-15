package de.deutschdiachrondigital.dddquery.parser.postprocessors;

import java.util.Stack;

import org.apache.log4j.Logger;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.node.AEndMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.AMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.AStartMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.TId;

/**
 * A class that inlines step markers that are specified using the start (#+) and end (#-)
 * syntax of markers.
 * 
 * <p>
 * This class transformes a parse tree that contains start and end markers, so that each
 * step between a start and an end marker has the correct marker set.
 * 
 * <p>
 * I.e., the input <tt>a#+(marker) / b / c#-</tt> is transformed to <tt>a#(marker) / b#(marker) / c#(marker)</tt>.
 * 
 * <p>
 * Start and end specifications of a marker can nest.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
public class MarkerNormalizer extends DepthFirstAdapter {

	private Logger log = Logger.getLogger(this.getClass());
	
	Stack<String> markerStack;
	
	public MarkerNormalizer() {
		markerStack = new Stack<String>();
	}
	
	@Override
	public void inAStartMarkerSpec(AStartMarkerSpec node) {
		String marker = node.getMarker() != null ? node.getMarker().getText() : null;
		
		log.info("found start of marker specification: " + marker);
		
		markerStack.push(marker);
		node.replaceBy(newMarkerSpec(marker));
	}
	
	@Override
	public void inAEndMarkerSpec(AEndMarkerSpec node) {
		String marker = markerStack.pop();

		log.info("found end of marker specification: " + marker);
		
		node.replaceBy(newMarkerSpec(marker));
	}
	
	@Override
	public void inAStep(AStep node) {
		if ( ! (markerStack.isEmpty()) && node.getMarkerSpec() == null) {
			String marker = markerStack.peek();
			
			log.info("inlining marker specification: " + marker);
			
			node.setMarkerSpec(newMarkerSpec(marker));
		}
	}

	private AMarkerSpec newMarkerSpec(String marker) {
		AMarkerSpec n = new AMarkerSpec();
		n.setMarker(((marker == null) ? (TId) null : new TId(marker)));
		return n;
	}
	
}
