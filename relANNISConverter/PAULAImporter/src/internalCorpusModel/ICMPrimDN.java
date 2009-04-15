package internalCorpusModel;

/** 
 * Die Klasse IKMPrimDN ist abgeleitet von der Klasse IKMAbstractDN und dient als 
 * abstrakte Klasse, die in einen IKMGraph eingef�gt werden kann. Diese Klasse dient 
 * der Repr�sentation der Prim�rdatenebene des internen Modells. Alle von dieser 
 * Klasse abgeleiteten Klassen sind Konkretisiereungen von Prim�rdatenknoten.
 * 
 * @author Florian Zisper
 * @version 1.0
 */
public abstract class ICMPrimDN extends internalCorpusModel.ICMAbstractDN 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IKMPrimDN";		//Name dieses Tools
	private static final String VERSION=	"1.0";				//Version des ineternen KorpusModels 
	
	private String text= null;		//Textwert des Knotens
	private long left;				//linke Grenze des Knotens
	private long right;				//rechte Grenze des Knotens
	
	/**
	 * Farbe dieses Knotentyps als DOT-Eintrag
	 */
	protected static final String color= "gold";
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_LEFT_RIGHT=		MSG_ERR + "Cannot return a text, because left border is higher than right border. (left/ right): ";
	private static final String ERR_RIGHT_LENGTH=	MSG_ERR + "Cannot return a text, because right border is higher than textlength. (right/ length): "; 
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt das die Ebene zur�ck auf der sich dieser Knoten im Internen Korpus Model
	 * befindet.
	 * @return Ebene des interenen Korpus Models
	 */
	public static java.lang.String getDNLevel() throws Exception
	{ return("LEVEL_PRIMDATA"); }
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt einen neuen Knoten vom Typ IKMPrimDN. Dieser Knoten rer�sentiert einen 
	 * Prim�rdatenknoten. Es wird der Name des Prim�rdatenknotens und dessen Text gesetzt.
	 * @param name String - Name des Knotens
	 * @param text String - Textwert des Knotens 
	 * @param left long - linke Grenze des Textes
	 * @param right long - rechte Grenze des Textes
	 */
	public ICMPrimDN(	String name,
						String text,
						long left,
						long right) throws Exception
	{
		super(name);
		this.text= text;
		this.left= left;
		this.right= right;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Gibt den Prim�rtext zur�ck, auf den sich dieser Knoten bezieht.
	 * @return Prim�rtext dieses Knotens
	 */
	public String getText()
		{ return(this.text); }
	
	/**
	 * Gibt einen Textauschnitt aus dem gesmaten Text dieses Objektes zur�ck.
	 * Der zur�ckgegebene Text liegt zwischen left und right. Ist der Text k�rzer
	 * als sie rechte Grenze oder liegt die rechte Grenze unterhalb der linken 
	 * wird ein Fehler geworfen. 
	 * @param left
	 * @param right
	 * @return Text zwischen left und right
	 * @exception Ist der Text k�rzer als sie rechte Grenze oder liegt die rechte Grenze unterhalb der linken wird ein Fehler geworfen. 
	 */
	public String getTextInterval(long left, long right) throws Exception
	{
		// Fehler wenn llinker Rand hinter rechtem Rand
		if (left > right) throw new Exception(ERR_LEFT_RIGHT + left + "/" + right);
		//Fehler wenn rechter Rand gr��er als Text
		if (right > text.length()) throw new Exception(ERR_RIGHT_LENGTH + right + "/" + text.length());
		
		int iLeft= (int) left;
		int iRight= (int) right;
		String text= this.text.substring(iLeft, iRight);
		return(text);
	}
	
	/**
	 * Gibt die linke Textgrenze des Textes zur�ck, auf den sich dieser Knoten bezieht.
	 * @return linke Textgrenze
	 */
	public long getLeft()
		{ return(this.left); }
	
	/**
	 * Gibt die rechte Textgrenze des Textes zur�ck, auf den sich dieser Knoten bezieht.
	 * @return rechte Textgrenze
	 */
	public long getRight()
		{ return(this.right); }
	
	/**
	 * Gibt Informationen �ber dieses Objekt als String zur�ck. 
	 * @return String - Informationen �ber dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= "toolname: "+ TOOLNAME + ", version: "+ VERSION+ ", object-name: "+ this.getName();
		return(retStr);
	}
	
	/**
	 * Schreibt diesen Knoten als DOT-Eintrag mit roter Knotenumrandung.
	 * @return Knoten als DOT-Eintrag
	 */
	public String toDOT() throws Exception
	{ return(this.toDOT(color)); }
}
