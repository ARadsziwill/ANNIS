package util.graph.index;

import java.util.Collection;

import util.graph.Node;

/**
 * Dieses Interface definiert einen Index, der auf den Typen Node als Indexinhalt
 * zugeschnitten ist.
 * @author Florian Zipser
 *
 */
public interface NodeIndex extends Index 
{
	/**
	 * Gibt alle Ids zur�ck, die in diesem Index vorhanden sind
	 * @return Collection<String> - alle Ids dieses Indexes
	 */
	public Collection<String> getNodeIds();
	
	/**
	 * Diese Methode f�gt diesem Index einen neuen Eintrag hinzu. Der Eintrag wird
	 * unter dem �bergebenen Identifier abgelegt und kann anschlie�end �ber diesen 
	 * identifiziert werden.
	 * @param id Object - eindeutiger Identifier, der den �bergebenen Eintrag identifiziert
	 * @param entry Object - abzulegender Eintrag  
	 */
	public void addEntry(String id, Node entry) throws Exception;
	
	/**
	 * Diese Methode gibt zur�ck, ob es in diesem Index mindestens ein Objekt gibt,
	 * das der �bergebenen id zugeordnet ist.
	 * @param id Object - eindeutiger Identifier, der den �bergebenen Eintrag identifiziert
	 * @return true, wenne s mindestens einen Eintrag zu der �bergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasId(String id) throws Exception;
	
	/**
	 * Diese Methode gibt zur�ck, ob der �bergebene Eintrag in diesem Index eingetragen ist. 
	 	 * @param entry Node - Knoten, f�r den gepr�ft werden soll, ob sie in diesem Index enthalten ist
	 * @return true, wenne s mindestens einen Eintrag zu der �bergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasEntry(Node entry) throws Exception;
	
	/**
	 * Diese Methode entfernt alle Eintr�ge, die mit der �bergebenen id identifiziert werden.
	 * Alle Eintr�ge zu einer id werden als slot bezeichnet.
	 * @param id Object -  eindeutiger Identifier, der den zu l�schenden Eintrag identifiziert
	 */
	public boolean removeSlot(String id) throws Exception;
	
	/**
	 * Diese Methode entfernt den �bergebenen Eintrag aus dem Index. Ist der Slot nach dem
	 * entfernen dieses Eintrages leer, wird der ganze slot entfernt.
	 */
	public boolean removeEntry(Node entry) throws Exception;
	
	/**
	 * Diese Methode gibt eine Liste von Eintr�gen, die von der �bergebenen id 
	 * identifiziert werden zur�ck. 
	 * @param id String - eindeutiger Identifizierer des gesuchten Knoten(s) 
	 * @return Collection<Node>, passend zu dem �bergebenen Identifizierer
	 */
	public Collection<Node> getEntry(String id) throws Exception;
}
