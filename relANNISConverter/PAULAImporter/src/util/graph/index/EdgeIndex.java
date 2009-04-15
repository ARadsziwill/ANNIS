package util.graph.index;

import java.util.Collection;

import util.graph.Edge;

/**
 * Dieses Interface definiert einen Index, der auf den Typen Edge als Indexinhalt
 * zugeschnitten ist.
 * @author Florian Zipser
 *
 */
public interface EdgeIndex extends Index 
{
	/**
	 * Gibt alle Ids zur�ck, die in diesem Index vorhanden sind
	 * @return Collection<String> - alle Ids dieses Indexes
	 */
	public Collection<String> getNodeIds();
	
	/**
	 * Diese Methode f�gt diesem Index eine neue Kantehinzu. Der Eintrag wird
	 * unter dem �bergebenen Identifier abgelegt und kann anschlie�end �ber diesen 
	 * identifiziert werden.
	 * @param id String - eindeutiger Identifier, der die �bergebene Kante identifiziert
	 * @param entry Edge - abzulegende Kante  
	 */
	public void addEntry(String id, Edge entry) throws Exception;
	
	/**
	 * Diese Methode gibt zur�ck, ob es in diesem Index mindestens einen Identifier gibt,
	 * das der �bergebenen id zugeordnet ist.
	 * @param id Object - eindeutiger Identifier, der die �bergebene Kante identifiziert
	 * @return true, wenn es mindestens eine Kante zu der �bergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasId(String id) throws Exception;
	
	/**
	 * Diese Methode gibt zur�ck, ob der �bergebene Eintrag in diesem Index eingetragen ist. 
	 * @param entry Edge - Kante, f�r die gepr�ft werden soll, ob sie in diesem Index enthalten ist
	 * @return true, wenne s mindestens einen Eintrag zu der �bergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasEntry(Edge entry) throws Exception;
	
	/**
	 * Diese Methode entfernt alle Eintr�ge, die mit der �bergebenen id identifiziert werden.
	 * Alle Kanten zu einer id werden als slot bezeichnet.
	 * @param id String -  eindeutiger Identifier, der den zu l�schenden Kante identifiziert
	 */
	public boolean removeSlot(String id) throws Exception;
	
	/**
	 * Diese Methode entfernt die �bergebene Kante aus dem Index. Ist der Slot nach dem
	 * entfernen dieses Eintrages leer, wird der ganze Slot entfernt.
	 */
	public boolean removeEntry(Edge entry) throws Exception;
	
	/**
	 * Diese Methode gibt eine Liste von Kanten, die von der �bergebenen id 
	 * identifiziert werden zur�ck.
	 * @param id String - eindeutiger Identifizierer der gesuchten Kante(n) 
	 * @return Collection<Edge>, passend zu dem �bergebenen Identifizierer
	 */
	public Collection<Edge> getEntry(String id) throws Exception;
}
