package util.graph.index;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Diese Klasse imlplementiert das Interface IIndex. Die Klasse Index bietet eine 
 * M�glichkeit einen Index f�r bspw. die Klasse Graph zu erstellen. Andere Indizes
 * k�nnen von dieser Klasse abgeleitet werden
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public class IndexImpl implements Index
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IndexImpl";		//Name dieses Tools
	private static final boolean DEBUG= false;
	
	/**
	 * Diese Tabelle ist der Kern des implementierten Index.
	 */
	protected Map<Object, Collection<Object>> idObjectTable= null;	 
	
	/**
	 * Name dieses Indizes
	 */
	protected String idxName= null;
	
	/**
	 * Anzahl der gesamten Eintr�ge dieses Indizes
	 */
	protected Long numOfEntries= null;
	
	/**
	 * Anzahl aller ID-Werte dieses Indizes
	 */
	protected Long numOfIDs= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_NAME=				MSG_ERR + "Cannot create this index, because of the index name is empty.";
	private static final String ERR_EMPTY_ID=				MSG_ERR + "Cannot add the given entry, because the given id is empty.";
	private static final String ERR_EMPTY_ENTRY=			MSG_ERR + "Cannot add the given entry, because the given id is empty.";
	private static final String ERR_EMPTY_ID_REM=			MSG_ERR + "Cannot remove the given entry, because the given id is empty.";
	//private static final String ERR_ID_NOT_EXIST_REM=		MSG_ERR + "Cannot remove the given entry, because there is no slot for the given id.";
	private static final String ERR_EMPTY_ENTRY_HAS=		MSG_ERR + "Cannot search for the given entry, because an empty entry was given.";
	//private static final String ERR_ENTRY_NOT_EXIST_REM=	MSG_ERR + "Cannot remove the given entry, because it doesn�t exists. ";
//	 ============================================== Konstruktoren ==============================================
	public IndexImpl(String name) throws Exception
	{
		if ((name== null) || (name.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_NAME);
		this.idxName= name;
		this.init();
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Setzt alle Objekte dieses Objektes auf den Initialzustand.
	 */
	private void init()
	{
		//eigentlichen Index initialisieren
		this.idObjectTable= new Hashtable<Object, Collection<Object>>();
		
		//Anzahlen initialisiern
		this.numOfEntries= 0l;
		this.numOfIDs= 0l;
	}
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Diese Methode gibt den Namen dieses Indizes zur�ck.
	 * @return Name dieses Indizes
	 */
	public String getIdxName()
		{ return(this.idxName); }
	
	
	/**
	 * Gibt alle Ids zur�ck, die in diesem Index vorhanden sind
	 * @return Name dieses Indizes
	 */
	public Collection<Object> getIds()
	{
		return(this.idObjectTable.keySet());
	}
	
	/**
	 * Diese Methode f�gt diesem Index einen neuen Eintrag hinzu. Der Eintrag wird
	 * unter dem �bergebenen Identifier abgelegt und kann anschlie�end �ber diesen 
	 * identifiziert werden. 
	 * @param id Object - eindeutiger Identifier, der den �bergebenen Eintrag identifiziert
	 * @param entry Object - abzulegender Eintrag  
	 */
	public void addEntry(Object id, Object entry) throws Exception
	{
		if (id== null) throw new Exception(ERR_EMPTY_ID);
		if (entry== null) throw new Exception(ERR_EMPTY_ENTRY);
		
		if (DEBUG) System.out.println(MSG_STD + "inserting id: "+ id + ", entry: "+entry);
		
		Collection<Object> slot= this.idObjectTable.get(id);
		//es existiert noch kein Eintrag zu dieser ID
		if (slot== null)
		{	
			//neuen slot zu der gegebenen Id erstellen
			slot= new Vector<Object>();
			slot.add(entry);
			this.idObjectTable.put(id, slot);
			//Anzahl der ID-Eintr�ge erh�hen
			this.numOfIDs++;
		}
		else	slot.add(entry);	
		//Anzahl aller Eintr�ge erhohen
		this.numOfEntries++;
	}
	
	/**
	 * Diese Methode gibt zur�ck, ob es in diesem Index mindestens ein Objekt gibt,
	 * das der �bergebenen id zugeordnet ist.
	 * @param id Object - eindeutiger Identifier, der den �bergebenen Eintrag identifiziert
	 * @return true, wenne s mindestens einen Eintrag zu der �bergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasId(Object id) throws Exception
	{
		if (DEBUG) System.out.print(MSG_STD + "searching for entry with id:" + id);
		if (this.idObjectTable.get(id) != null) 
		{
			if (DEBUG) System.out.println(", result: true");
			return(true);
		}
		else 
		{
			if (DEBUG) System.out.println(", result: false");
			return(false);
		}
	}
	
	/**
	 * Diese Methode gibt zur�ck, ob der �bergebene Eintrag in diesem Index eingetragen ist. 
	 * @param entry Object - eindeutiger Identifier, der den �bergebenen Eintrag identifiziert
	 * @return true, wenne s mindestens einen Eintrag zu der �bergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasEntry(Object entry) throws Exception
	{
		boolean retVal= false;
		
		if (entry== null) throw new Exception(ERR_EMPTY_ENTRY_HAS);
		
		for (Object key: this.idObjectTable.keySet())
		{
			if (this.idObjectTable.get(key).contains(entry))
			{
				retVal= true;
				break;
			}
		}
		
		if (DEBUG) System.out.println(MSG_STD +"searching for entry: "+ entry + ", result: " + retVal);
		
		return(retVal);
	}
	
	/**
	 * Diese Methode entfernt alle Eintr�ge, die mit der �bergebenen id identifiziert werden.
	 * Alle Eintr�ge zu einer id werden als slot bezeichnet.
	 * @param id Object -  eindeutiger Identifier, der den zu l�schenden Eintrag identifiziert
	 * @exception Fehler, wenn kein Slot mit der �bergebenen id vorhanden ist
	 */
	public boolean removeSlot(Object id) throws Exception
	{
		boolean retVal= false;
		if (DEBUG) System.out.println(MSG_STD + "deleting entry with id: " + id);
		if (id== null) throw new Exception(ERR_EMPTY_ID_REM);
		if (this.idObjectTable.remove(id)!= null) 
		{
			retVal= true;
			//Anzahl der Slots verringern
			this.numOfIDs--;
		}
		return(retVal);
	}
	
	/**
	 * Diese Methode entfernt den �bergebenen Eintrag aus dem Index. Ist der Slot nach dem
	 * entfernen dieses Eintrages leer, wird der ganze slot entfernt.
	 */
	public boolean removeEntry(Object entry) throws Exception
	{
		if (entry== null) throw new Exception(ERR_EMPTY_ENTRY_HAS);
		boolean removed= false;
		Object rKey= null;
		Collection<Object> slot= null;
		for (Object key: this.idObjectTable.keySet())
		{
			slot= this.idObjectTable.get(key);
			removed= slot.remove(entry);
			if (removed) 
			{
				rKey= key;
				//Anzahl der Eintr�ge verringern
				this.numOfEntries--;
				break;
			}
		}
		//wenn Slot keinen Eintrag mehr hat nach l�schen des Eintrages, ganzen Slot l�schen
		if ((slot!= null) && (slot.size()== 0))
			this.removeSlot(rKey);
		return(removed);	
	}
	
	/**
	 * Diese Methode entfernt alle Eintr�ge aus dem Index.
	 * @return true, wenn alle Eintr�ge gel�scht werden konnten
	 * @throws Exception
	 */
	public boolean removeAll() throws Exception
	{
		this.init();
		return(true);
	}
	
	/**
	 * Diese Methode gibt eine Liste von Eintr�gen, die von der �bergebenen id 
	 * identifiziert werden zur�ck. 
	 * @return Objekt, passend zu dem �bergebenen Eintrag
	 */
	public Collection<Object> getEntry(Object id) throws Exception
	{
		Collection<Object> entries= this.idObjectTable.get(id);
		if ((entries != null) && (entries.size()!= 0))
			return(entries);
		return(null);
	}
	
	/**
	 * Gibt die Anzahl der in diesem Index enthaltenen Slots, bzw. verschiedenen Id-Werte
	 * zur�ck. 
	 * @return Anzahl der Slots
	 * @throws Exception
	 */
	public long getNumOfIds() throws Exception
	{ return(this.numOfIDs); }
	
	/**
	 * Gibt die Anzahl der in diesem Index enthaltenen Eintr�ge zur�ck.
	 * @return Anzahl der Eintr�ge
	 * @throws Exception
	 */
	public long getNumOfEntries() throws Exception
		{ return(this.numOfEntries); }
	
	/**
	 * Gibt Informationen �ber dieses Objekt als String zur�ck. 
	 * @return String - Informationen �ber dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= MSG_STD + "number of ids: "+ this.numOfIDs +", number of entries: "+ this.numOfEntries; 
		return(retStr);
	}
}
