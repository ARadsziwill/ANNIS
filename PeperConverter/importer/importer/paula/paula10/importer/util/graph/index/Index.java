package importer.paula.paula10.importer.util.graph.index;

import java.util.Collection;

/**
 * Dieses Interface benennt alle Methoden, die ein Objekt vom Typ Index ben�tigt, um
 * von einem IndexManager angesprochen werden zu k�nnen. Dieses Interface kann von
 * anderen Klassen implementiert werden und somit einen Index bereitstellen. 
 * Vornehmlich wird dieses Interface und ein IndexMgr f�r die Klasse Graph
 * geschrieben. 
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public interface Index 
{
	/**
	 * Diese Methode gibt den Namen dieses Indizes zur�ck.
	 * @return Name dieses Indizes
	 */
	public String getIdxName();
	
	/**
	 * Gibt alle Ids zur�ck, die in diesem Index vorhanden sind
	 * @return Name dieses Indizes
	 */
	public Collection<Object> getIds();
	
	/**
	 * Diese Methode f�gt diesem Index einen neuen Eintrag hinzu. Der Eintrag wird
	 * unter dem �bergebenen Identifier abgelegt und kann anschlie�end �ber diesen 
	 * identifiziert werden.
	 * @param id Object - eindeutiger Identifier, der den �bergebenen Eintrag identifiziert
	 * @param entry Object - abzulegender Eintrag  
	 */
	public void addEntry(Object id, Object entry) throws Exception;
	
	/**
	 * Diese Methode gibt zur�ck, ob es in diesem Index mindestens ein Objekt gibt,
	 * das der �bergebenen id zugeordnet ist.
	 * @param id Object - eindeutiger Identifier, der den �bergebenen Eintrag identifiziert
	 * @return true, wenne s mindestens einen Eintrag zu der �bergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasId(Object id) throws Exception;
	
	/**
	 * Diese Methode gibt zur�ck, ob der �bergebene Eintrag in diesem Index eingetragen ist. 
	 * @param entry Object - eindeutiger Identifier, der den �bergebenen Eintrag identifiziert
	 * @return true, wenne s mindestens einen Eintrag zu der �bergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasEntry(Object entry) throws Exception;
	
	/**
	 * Diese Methode entfernt alle Eintr�ge, die mit der �bergebenen id identifiziert werden.
	 * Alle Eintr�ge zu einer id werden als slot bezeichnet.
	 * @param id Object -  eindeutiger Identifier, der den zu l�schenden Eintrag identifiziert
	 */
	public boolean removeSlot(Object id) throws Exception;
	
	/**
	 * Diese Methode entfernt den �bergebenen Eintrag aus dem Index. Ist der Slot nach dem
	 * entfernen dieses Eintrages leer, wird der ganze slot entfernt.
	 */
	public boolean removeEntry(Object entry) throws Exception;
	
	/**
	 * Diese Methode entfernt alle Eintr�ge aus dem Index.
	 * @return true, wenn alle Eintr�ge gel�scht werden konnten
	 * @throws Exception
	 */
	public boolean removeAll() throws Exception;
	
	/**
	 * Diese Methode gibt eine Liste von Eintr�gen, die von der �bergebenen id 
	 * identifiziert werden zur�ck. 
	 * @return Objekt, passend zu dem �bergebenen Eintrag
	 */
	public Collection<Object> getEntry(Object id) throws Exception;
	
	/**
	 * Gibt die Anzahl der in diesem Index enthaltenen Slots, bzw. verschiedenen Id-Werte
	 * zur�ck. 
	 * @return Anzahl der Slots
	 * @throws Exception
	 */
	public long getNumOfIds() throws Exception;
	
	/**
	 * Gibt die Anzahl der in diesem Index enthaltenen Eintr�ge zur�ck.
	 * @return Anzahl der Eintr�ge
	 * @throws Exception
	 */
	public long getNumOfEntries() throws Exception;
}
