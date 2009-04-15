package internalCorpusModel;

import util.graph.Node;


/** 
 * Diese Klasse bildet einen abstrakten Knotentyp, der in den IKMGraph eingef�gt werden kann. 
 */
public class ICMAbstractDN extends Node
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IKMAbstractDN";		//Name dieses Tools
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_OVERRIDE=		MSG_ERR + "This methode has to be overridden: ";
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt die Ebene zur�ck auf der sich dieser Knoten im Internen Korpus Model
	 * befindet.
	 * @return Ebene des interenen Korpus Models
	 */
	public static String getDNLevel() throws Exception
	{
		throw new Exception(ERR_OVERRIDE + "getDNLevel()");
	}
//	 ============================================== Konstruktoren ==============================================
	
	public ICMAbstractDN(String name) throws Exception
	{
		super(name);
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Gibt Informationen �ber dieses Objekt als String zur�ck. 
	 * @return String - Informationen �ber dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		 retStr= "this method isn�t implemented";
		return(retStr);
	}
}
