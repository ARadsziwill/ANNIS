package annis.frontend.servlets.visualizers.gridtree;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;

import annis.frontend.servlets.visualizers.WriterVisualizer;

/**
 * 
 * @author benjamin
 * 
 */

public class GridTreeVisualizer extends WriterVisualizer {

	private LinkedList<Span> spans = new LinkedList<GridTreeVisualizer.Span>();

	/**
	 * This helper-class saves the span from a specific Node. The span is
	 * represented as tokenIndex from the most and the most right Token of the
	 * root.
	 * 
	 * @author benjamin
	 * 
	 */
	private class Span implements Comparable<Span>, Cloneable {

		Long left;
		Long right;
		AnnisNode root;
		AnnisNode current;
		int height;

		/**
		 * left and right should be initiate with null, when root is not a
		 * token.
		 * 
		 * @param root
		 * @param r
		 *            must be a sorted List of the result
		 */
		public Span(AnnisNode root) {
			this.root = root;
			this.current = root;
		}

		@Override
		public int compareTo(Span sp) {
			if (this.height < sp.height)
				return 1;
			if (this.height == sp.height)
				return 0;
			return -1;
		}
	}

	@Override
	public void writeOutput(Writer writer) {
		try {
			writer.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
			writer.append("<link href=\""
					+ getContextPath()
					+ "/css/visualizer/partitur.css\" rel=\"stylesheet\" type=\"text/css\" >");
			writer.append("<link href=\""
					+ getContextPath()
					+ "/css/visualizer/gridtree.css\" rel=\"stylesheet\" type=\"text/css\" >");
			writer.append("<body>");
			writer.append("<table class=\"grid-tree partitur_table\">\n");
			writer.append(findAnnotation("cat"));
			writer.append("</table>\n");
			writer.append("</body></html>");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String findAnnotation(String anno) {

		AnnotationGraph graph = getResult().getGraph();
		List<AnnisNode> nodes = graph.getNodes();
		List<AnnisNode> result = graph.getTokens();
		Set<AnnisNode> roots = new HashSet<AnnisNode>();

		for (AnnisNode n : nodes)
			if (hasAnno(n, anno))
				roots.add(n);

		StringBuffer sb = new StringBuffer();

		for (AnnisNode n : roots) {

			// catch the result
			Span span = new Span(n);
			getTokens(span, roots);
			spans.add(span);

		}

		Collections.sort(spans);

		// print result
		htmlTableRow(sb, result, spans, anno);

		htmlTableRow(sb, result);

		return sb.toString();
	};

	/**
	 * Returns the annotation of a {@link AnnisNode}
	 * 
	 * @param n
	 * @param anno
	 * @return null, if the annotation not exists.
	 */
	private String getAnnoValue(AnnisNode n, String anno) {

		for (Annotation a : n.getNodeAnnotations()) {
			if (a.getName().equals(anno))
				return a.getName() + " : " + a.getValue();
		}

		return " ";
	}

	/**
	 * Returns true when the node is annotated with the string. It is not
	 * sensitive to namespaces.
	 * 
	 * @param n
	 *            the node to check
	 * @param annotation
	 *            String to check, without namespaces
	 * @return
	 */
	private boolean hasAnno(AnnisNode n, String annotation) {

		Set<Annotation> annos = n.getNodeAnnotations();

		for (Annotation x : annos)
			if (x.getName().equals(annotation))
				return true;

		return false;
	}

	/**
	 * Steps from the root recursive through all children nodes to find the
	 * tokens. This function is a straight forward DFS-Algorithm. It also
	 * calculate the max-depth with this functional pseudo-algorithm:<br/>
	 * 
	 * data Tree a = Node [Tree a] | Leaf a<br />
	 * 
	 * maxDepth :: Tree a -> Int <br />
	 * maxDepth (Leaf a) = 0 <br />
	 * maxDepth (Node ls) = maximum (map maxDepth ls) + 1
	 * 
	 * @param n
	 *            is the root
	 * @param roots
	 *            List of root nodes
	 * 
	 */
	public void getTokens(Span n, Set<AnnisNode> roots) {
		getTokens(n, roots, 0);
	}

	private void getTokens(Span n, Set<AnnisNode> roots, int height) {
		Set<Edge> edges = n.current.getOutgoingEdges();

		for (Edge e : edges) {

			AnnisNode x;

			if ((x = e.getDestination()).isToken()) {

				Long tokenIndex = x.getTokenIndex();

				if (n.left == null)
					n.left = tokenIndex;
				else
					n.left = Math.min(n.left, tokenIndex);

				if (n.right == null)
					n.right = tokenIndex;
				else
					n.right = Math.max(n.right, tokenIndex);

				n.height = Math.max(n.height, height);

			}

			// recursive step
			else {
				n.current = x;
				getTokens(n, roots, height + 1);
			}
		}
	}

	/**
	 * Build a html-table-row.
	 * 
	 * @param sb
	 *            the html-code, where the row is embedded
	 * @param result
	 *            List of all results, the list must be sorted by the
	 *            token-index
	 * @param spans
	 *            Sorted List of Span objects with left and right limit
	 * @param anno
	 *            the anno, which matches to all Span-Objects
	 */
	private void htmlTableRow(StringBuffer sb, List<AnnisNode> result,
			LinkedList<Span> spans, String anno) {

		for (Span sp : spans) {
			sb.append("<tr>\n");
			sb.append("<th>" + sp.height + "</th>");

			// fill with empty cell
			for (long i = result.get(0).getTokenIndex(); i < sp.left; i++) {
				sb.append("<td> </td>");
			}

			// build table-cell for span
			sb.append("<td colspan=\"" + (Math.abs(sp.right - sp.left) + 1)
					+ "\" class=\"gridtree-result\">"
					+ getAnnoValue(sp.root, anno) + " " + sp.height + "</td>");

			// fill with empty cells
			long lastTokenIndex = result.get(result.size() - 1).getTokenIndex();
			for (long i = sp.right; i < lastTokenIndex; i++) {
				sb.append("<td> </td>");
			}
			sb.append("</tr>\n");
		}
	}

	/**
	 * Build a simple html-row
	 * 
	 * @param sb
	 * @param result
	 */
	private void htmlTableRow(StringBuffer sb, List<AnnisNode> result) {

		sb.append("<tr>\n");
		sb.append("<th> tok </th>");

		for (AnnisNode n : result) {
			sb.append("<td>" + n.getSpannedText() + "</td>");
		}

		sb.append("</tr>\n");
	}
}
