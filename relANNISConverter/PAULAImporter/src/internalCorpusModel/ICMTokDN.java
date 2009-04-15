package internalCorpusModel;

/** 
 * Die Klasse IKMTokDN ist abgeleitet von der Klasse IKMAbstractDN und dient als abstrakte 
 * Klasse, die in einen IKMGraph eingef�gt werden kann. Diese Klasse dient der 
 * Repr�sentation der Tokendatenebene des internen Modells. Alle von dieser Klasse 
 * abgeleiteten Klassen sind Konkretisiereungen von Tokendatenknoten.
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public abstract class ICMTokDN extends internalCorpusModel.ICMAbstractDN 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IKMTokDN";		//Name dieses Tools
	private static final String VERSION=	"1.0";				//Version des ineternen KorpusModels 
	
	protected String text= null;		//Textwert des Knotens
	protected Long left= null;			//linke Grenze des Knotens
	protected Long right= null;			//rechte Grenze des Knotens
	protected Long pos= null;			//Position dieses Tokens in den Prim�rdaten
	
	/**
	 * Farbe dieses Knotentyps als DOT-Eintrag
	 */
	protected static final String color= "lightsalmon";
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	//private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt das die Ebene zur�ck auf der sich dieser Knoten im Internen Korpus Model
	 * befindet.
	 * @return Ebene des internen Korpus Models
	 */
	public static java.lang.String getDNLevel() throws Exception
	{ return("LEVEL_TOKDATA"); }
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt einen neuen Knoten vom Typ IKMPrimDN. Dieser Knoten rer�sentiert einen 
	 * Prim�rdatenknoten. Es wird der Name des Prim�rdatenknotens und dessen Text gesetzt.
	 * @param name String - Name des Knotens
	 * @param text String - Textwert des Knotens 
	 * @param left long - linke Grenze des Textes
	 * @param right long - rechte Grenze des Textes
	 * @param pos long -	Position dieses Tokens in den Prim�rdaten
	 */
	public ICMTokDN(	String name,
						String text,
						long left,
						long right,
						long pos) throws Exception
	{
		super(name);
		this.text= text;
		this.left= left;
		this.right= right;
		this.pos= pos;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Gibt den Prim�rtext zur�ck, auf den sich dieser Knoten bezieht.
	 * @return Prim�rtext dieses Knotens
	 */
	public String getText() throws Exception
		{ return(this.text); }
	
	/**
	 * Gibt die linke Textgrenze des Textes zur�ck, auf den sich dieser Knoten bezieht.
	 * @return linke Textgrenze
	 */
	public Long getLeft() throws Exception
		{ return(this.left); }
	
	/**
	 * Gibt die rechte Textgrenze des Textes zur�ck, auf den sich dieser Knoten bezieht.
	 * @return rechte Textgrenze
	 */
	public Long getRight() throws Exception
		{ return(this.right); }
	
	/**
	 * Gibt die Position dieses Tokens in den Prim�rdaten an.
	 * @return Position dieses Tokens in den Prim�rdaten
	 * @throws Exception
	 */
	public Long getPos() throws Exception
		{ return(this.pos); }
	
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
