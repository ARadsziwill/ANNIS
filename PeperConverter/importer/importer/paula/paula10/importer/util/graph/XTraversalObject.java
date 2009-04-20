package importer.paula.paula10.importer.util.graph;

import importer.paula.paula10.importer.util.graph.Graph.TRAVERSAL_MODE;


/**
 * Dieses Interface muss implementiert werden, wenn die Methode DepthFirst() der Klasse
 * Graph genutzt wird. 
 * @author Florian Zipser
 * @version 1.0
 */
public interface XTraversalObject extends TraversalObject
{
	/**
	 * Diese Funktion wird von der Methode traversalGraph() aufgerufen, die die
	 * Knoten des Graphen traversiert. Dabei k�nnen verschiedene Verfahren angewandt 
	 * werden (wie z.B. DEPTH_FIRST, BOTTOM_UP ...), wodurch die Knoten des graphen in
	 * unterschiedlichen Reihenfolgen traversiert werden. 
	 * Die Methode traversalGraph() erzeugt ein Callback und ruft im ihr �bergebenen
	 * Objekt die Methode checkConstraint() auf. Diese Methode soll dann anhand des 
	 * �bergebenen Knotens entscheiden, ob weiter traversiert werden soll.
	 * @param tMode TRAVERSAL_MODE - Modus der Traversion
	 * @param edge Edge - Kante �ber die dieser Knoten erreicht wurde
	 * @param currNode Node - aktueller zu pr�fender Knoten
	 * @return true, wenn weiter traversiert werden soll, false sonst
	 */
	public boolean checkConstraint(	TRAVERSAL_MODE tMode, 
									Edge edge, 
									Node currNode) throws Exception;
}
