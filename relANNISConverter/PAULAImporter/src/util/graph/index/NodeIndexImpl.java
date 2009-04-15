package util.graph.index;


import java.util.Collection;
import java.util.Vector;

import util.graph.Node;

public class NodeIndexImpl extends IndexImpl implements NodeIndex {
//	 ============================================== private Variablen ==============================================
	//private static final String TOOLNAME= 	"NodeIndexImpl";		//Name dieses Tools
	//private static final boolean DEBUG= false;
	
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	//private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== Konstruktoren ==============================================
	public NodeIndexImpl(String name) throws Exception
	{
		super(name);
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Gibt alle Ids zur�ck, die in diesem Index vorhanden sind
	 * @return Collection<String> - alle Ids dieses Indexes
	 */
	public Collection<String> getNodeIds()
	{
		Collection<String> nodeIds= new Vector<String>();
		for (Object obj: super.getIds())
		{
			nodeIds.add((String) obj);
		}
		return(nodeIds);
	}
	
	/**
	 * Diese Methode f�gt diesem Index einen neuen Eintrag hinzu. Der Eintrag wird
	 * unter dem �bergebenen Identifier abgelegt und kann anschlie�end �ber diesen 
	 * identifiziert werden.
	 * @param id Object - eindeutiger Identifier, der den �bergebenen Eintrag identifiziert
	 * @param entry Object - abzulegender Eintrag  
	 */
	public void addEntry(String id, Node entry) throws Exception
	{ super.addEntry(id, entry); }
	
	/**
	 * Diese Methode gibt zur�ck, ob es in diesem Index mindestens ein Objekt gibt,
	 * das der �bergebenen id zugeordnet ist.
	 * @param id Object - eindeutiger Identifier, der den �bergebenen Eintrag identifiziert
	 * @return true, wenne s mindestens einen Eintrag zu der �bergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasId(String id) throws Exception
	{ return(super.hasId(id)); }
	
	/**
	 * Diese Methode gibt zur�ck, ob der �bergebene Eintrag in diesem Index eingetragen ist. 
	 * @param entry Object - eindeutiger Identifier, der den �bergebenen Eintrag identifiziert
	 * @return true, wenne s mindestens einen Eintrag zu der �bergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasEntry(Node entry) throws Exception
	{ return(super.hasEntry(entry)); }
	
	/**
	 * Diese Methode entfernt alle Eintr�ge, die mit der �bergebenen id identifiziert werden.
	 * Alle Eintr�ge zu einer id werden als slot bezeichnet.
	 * @param id Object -  eindeutiger Identifier, der den zu l�schenden Eintrag identifiziert
	 */
	public boolean removeSlot(String id) throws Exception
	{ return(super.removeSlot(id)); }
	
	/**
	 * Diese Methode entfernt den �bergebenen Eintrag aus dem Index. Ist der Slot nach dem
	 * entfernen dieses Eintrages leer, wird der ganze slot entfernt.
	 */
	public boolean removeEntry(Node entry) throws Exception
	{ return(super.removeEntry(entry)); }
	
	/**
	 * Diese Methode gibt eine Liste von Eintr�gen, die von der �bergebenen id 
	 * identifiziert werden zur�ck. 
	 * @return Objekt, passend zu dem �bergebenen Eintrag
	 */
	public Collection<Node> getEntry(String id) throws Exception
	{
		Collection<Node> nodes= null;
		Collection<Object> nodeObjs=  super.getEntry(id);
		if ((nodeObjs != null) && (nodeObjs.size()!= 0))
		{
			nodes= new Vector<Node>();
			for (Object obj: nodeObjs)
			{
				nodes.add((Node) obj);
			}
		}
		return(nodes);
	}
}
