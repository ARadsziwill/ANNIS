package relANNIS_2_0;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.relANNISDAO.DBConnector;
import relANNIS_2_0.relANNISDAO.TupleWriter;

import internalCorpusModel.ICMAbstractDN;
import internalCorpusModel.ICMDominanceEdge;

/**
 * Diese Klasse repr�sentiert eine einfache Dominanzkante.
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class DominanceEdge extends ICMDominanceEdge implements RelationalEdge
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"DominanceEdge";		//Name dieses Tools
	private static final String VERSION=	"1.0";				//Version des ineternen KorpusModels
	
	/**
	 * Farbe dieses Knotentyps als DOT-Eintrag
	 */
	protected static final String color= "blue";
	
	protected static Logger logger= null;				//logger f�r log4j
	protected static DBConnector dbConnector= null;		//DB-Verbindungsobjekt
	/**
	 * speichert den abstrakten Namen f�r die Relation, 
	 * in der die Struktur der Kante gespeichert wird (rank)
	 */
	protected static String absRelNameStruct=null;			
	/**
	 * speichert den abstrakten Namen f�r die Relation, 
	 * in der die Annotation der Kante gespeichert wird (rank-anno)
	 */
	protected static String absRelNameAnno=null;			
	protected static boolean factoryInit= false;		//gibt an, ob die Factory initialisiert wurde
	
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_INIT=			MSG_STD + "Factory for "+TOOLNAME+" is initialized.";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_DBCON=		MSG_ERR + "Cannot initialize the Factory, the given db-connector-object is empty.";
	private static final String ERR_EMPTY_KGRAPHMGR=	MSG_ERR + "Cannot initialize the Factory, the given korpus-graph-object is empty.";
	private static final String ERR_FACTORY_NOT_INIT=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the factory is not initilaized. Call "+TOOLNAME+".initFactory() first.";
	private static final String ERR_EMPTY_ABSRELNAME=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the abstract relation name is empty.";	

//	 ============================================== statische Methoden ==============================================
	/**
	 * Initialisiert die Factory zum erzeugen von ConstEdge-Objekten.
	 * Gesetzt wird hier:<br/>
	 * <ul>
	 *  <li>der Graph manager, in den dieser Knoten geschrieben werden soll</li>
	 * 	<li>der logger f�r log4j</li>
	 * 	<li>abstrakter Name der Relation, in die der Knoten geschrieben werden soll</li>
	 *  <li>das Datenbankverbindungsobjekt</li>
	 * <ul/>
	 * @param kGraphMgr KorpusGraph - der Graph, in den dieser Knoten geschrieben werden soll
	 * @param dbConnector DBConnector - DB-Verbindungsobjekt
	 * @param absRelNameStruct String - abstrakter Name der Relation, in die die Struktur dieser Kante geschrieben werden soll
	 * @param absRelNameAnno String - abstrakter Name der Relation, in die die Annotation dieser Kante geschrieben werden soll 
	 * @param logger Logger - Logger f�r log4j
	 */
	public static void initFactory(	CorpusGraphMgr kGraphMgr,
									DBConnector dbConnector, 
									String absRelNameStruct,
									String absRelNameAnno,
									Logger logger) throws Exception
	{
		ConstEdge.logger= logger;
		if (logger!= null) logger.debug(MSG_INIT);
		
		//KorpusGraph setzen wenn g�ltig
		if (kGraphMgr== null) throw new Exception(ERR_EMPTY_KGRAPHMGR);
		//ConstEdge.kGraphMgr= kGraphMgr;
		
		//pr�fen ob DB-Verbinder g�ltig ist
		if (dbConnector== null) throw new Exception(ERR_EMPTY_DBCON);
		ConstEdge.dbConnector= dbConnector;
		
		//abstrakten relNamen setzen
		if ((absRelNameStruct== null)||(absRelNameStruct.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		ConstEdge.absRelNameStruct= absRelNameStruct;
		if ((absRelNameAnno== null)||(absRelNameAnno.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		ConstEdge.absRelNameAnno= absRelNameAnno;
		
		ConstEdge.factoryInit= true;
	}
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei einen Namen f�r diese Kante. Au�erdem 
	 * werden die beiden �bergebenen Knoten als Quelle bzw. Ziel gesetzt. 
	 * Weiter kann auch ein oder mehrere Kantenlabels vergeben werden. Diese Kantenlabels
	 * bilden Attribut-Wert-Paare in Form einer Tabelle
	 * @param fromIKMAbstractDN IKMAbstractDN - Quellknoten, von dem aus die Kante geht
	 * @param toIKMAbstractDN IKMAbstractDN -  Zielknoten, zu dem die Kante geht
	 */
	public DominanceEdge(	StructDN fromIKMAbstractDN, 
							ICMAbstractDN toIKMAbstractDN) throws Exception
	{
		super(fromIKMAbstractDN, toIKMAbstractDN);
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!ConstEdge.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
	}
	
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei einen Namen f�r diese Kante. Au�erdem 
	 * werden die beiden �bergebenen Knoten als Quelle bzw. Ziel gesetzt. 
	 * Weiter kann auch ein oder mehrere Kantenlabels vergeben werden. Diese Kantenlabels
	 * bilden Attribut-Wert-Paare in Form einer Tabelle
	 * @param name String - Name dieser Kante
	 * @param fromIKMAbstractDN IKMAbstractDN - Quellknoten, von dem aus die Kante geht
	 * @param toIKMAbstractDN IKMAbstractDN -  Zielknoten, zu dem die Kante geht
	 * @param labels Hashtable<String, String> - Tabelle der Attribut-Wert-Paare
	 */
	public DominanceEdge(	String name,
							StructDN fromIKMAbstractDN, 
							ICMAbstractDN toIKMAbstractDN,
							Hashtable<String, String> labels) throws Exception
	{
		super(name, fromIKMAbstractDN, toIKMAbstractDN, labels);
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!ConstEdge.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
	}
	
//	 ============================================== private Methoden ==============================================
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Schreibt diese Kante auf einen TupleWriter. Dieser wird �ber das 
	 * dbConnector-Objekt ermittelt. Die zu f�llende Tabelle:
	 * <ul>
	 * 	<li>pre					numeric(38)	NOT NULL,</li>
	 * 	<li>post					numeric(38)	NOT NULL,</li>
	 * 	<li>struct_ref			numeric(38)	NOT NULL,</li>
	 * 	<li>parent				numeric(38),</li>
	 * 	<li>dominance			boolean		NOT NULL,</li>
	 * </ul>
	 * @param pre String - Pre-Wert f�r den Quellknoten dieser Kante
	 * @param post String - Post-Wert f�r den Quellknoten dieser Kante 
	 * @param fatherPre String - Zielknoten f�r diese Kante
	 */
	public void toWriter(String pre, String post, String fatherPre) throws Exception
	{
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!ConstEdge.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
	
		//ermittle TupleWriter f�r die Tabelle meta_attribute
		TupleWriter structWriter = ConstEdge.dbConnector.getTWriter(ConstEdge.absRelNameStruct);
		Vector<String> tuple= new Vector<String>();
		tuple.add(pre.toString());
		tuple.add(post.toString());
		tuple.add(((RelationalDN)this.getToNode()).getRelID().toString());
		tuple.add(fatherPre.toString());
		//Flag setzen, dass besagt ob diese Kante eine Dominanzkante ist
		tuple.add(dbConnector.getDBTRUE());
		//tuple in Writer schreiben
		structWriter.addTuple(tuple);
	}
	
	
	/**
	 * Gibt Informationen �ber dieses Objekt als String zur�ck. 
	 * @return String - Informationen �ber dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= "toolname: "+ TOOLNAME + ", version: "+ VERSION;
		try
		{
			retStr= retStr+ ", object-name: "+ this.getName();
		}
		catch (Exception e)
		{
			retStr= retStr+ "null";
		}
		return(retStr);
	}
	
	/**
	 * Schreibt diesen Knoten als DOT-Eintrag mit roter Knotenumrandung.
	 * @return Knoten als DOT-Eintrag
	 */
	public String toDOT() throws Exception
	{ 
		String retStr= "";
		
		retStr= "<" + this.getFromNode().getName() + "> -> <" + this.getToNode().getName() +">";
		
		//Farbe setzen
		retStr= retStr + "[color= " + color +"];"; 
		
		return(retStr);
	}
}
