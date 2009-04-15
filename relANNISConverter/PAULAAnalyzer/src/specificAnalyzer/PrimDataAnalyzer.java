package specificAnalyzer;

import paulaAnalyzer.AnalyzeContainer;

/**
 * Der PrimDataAnalyzer kann Dateien analysieren, die Prim�rdaten enthalten. Das Analyse-
 * ergebniss wird aufgrund des paula-Dateityps getroffen. Ist der Typwert gleich "text",
 * matcht dieser Analyzer. Er gibt das Analyzeergebnis "PrimData" zur�ck.
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class PrimDataAnalyzer extends AbstractAnalyzer 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"PrimDataAnalyzer";	//Name dieses Tools
	private static final String VERSION= 	"1.0";					//Version dieses Tools
	private static final boolean DEBUG=	false;					//Debug-Schalter 
	
	private static final String anaType= "PrimData";			//Name des Analyse Typs
	private static final String comment= "analyzed by: "+ TOOLNAME + " this document contains primary data wich has to be annotated.";			//Kommentar zu diesem Analyse Typ
	
	private static final String[] analyzableDTDs=	{"paula_text.dtd", "sfb632_text.dtd"};			//DTD von der die zu analysierende Datei sein muss
	private static final String[] analyzablePAULATypes= {"text"};		//analysierbare PAULA-Typen
	
	private AnalyzeContainer aCon= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START_FCT=		MSG_STD + "start method: ";
	private static final String MSG_END_FCT=		MSG_STD + "end method: ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_ACON=		MSG_ERR + "No analyze-container-object was given.";
	private static final String ERR_CANNOT_ANALYZE=	MSG_ERR + "Cannot analyze this document: ";
//	 ============================================== Konstruktoren ==============================================
//	 ============================================== private Methoden ==============================================
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Erzeugt ein Objekt vom Typ MetaStructAnalyzer. Dieses kann Dateien vom Typ 
	 * MetaStructData bestimmen.
	 */
	public PrimDataAnalyzer() throws Exception
	{
		super(TOOLNAME, VERSION);
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Pr�ft ob dieser spezielle Analyser eine Datei mit den gegebenen Eigenschaften 
	 * analysieren kann.
	 */
	public boolean canAnalyze(AnalyzeContainer aCon) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "canAnalyze("+aCon+")");
		if (aCon == null) throw new Exception(ERR_NO_ACON);
		//wenn ein Typ existiert und dieser = text
		if ((aCon.getPAULAType()!= null) && (!aCon.getPAULAType().equalsIgnoreCase("")))
		{
			if (this.canAnalyzeValue(aCon.getPAULAType(), analyzablePAULATypes))
			{
				//wenn DTD = paula_text.dtd oder DTD= sfb632_text.dtd
				if (this.canAnalyzeValue(aCon.getDTD().getName(), analyzableDTDs))
					return(true);
			}
		}
		if (DEBUG) System.out.println(MSG_END_FCT + "canAnalyze()--> false");
		return(false);
	}
	
	/**
	 * Startet den AnalyseProzess und �bergibt das zu beschreibende Container-Objekt.
	 * @param aCon AnalyzeContainer - ein Container-Objekt, in dem Dateiinformationen stehen und das Analyseergebniss geschrieben werden kann
	 * @exception Fehler, wenn Methode nicht �berschrieben wurde
	 */
	public void startAnalyze(AnalyzeContainer aCon) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "canAnalyze("+aCon+")");
		if (!this.canAnalyze(aCon)) throw new Exception(ERR_CANNOT_ANALYZE + aCon.getPAULAFile().getAbsolutePath());
		this.aCon= aCon;
		if (DEBUG) System.out.println(MSG_END_FCT + "startAnalyze()");
	}
	
	/**
	 * Wird aufgerufen, wenn der AnalyseProzess zu Ende ist, diese Methode gibt das vorher
	 * �bergebene Objekt um die Eigenschaften Typ und Kommentar erweitert zur�ck.
	 * @exception Fehler, wenn Methode nicht �berschrieben wurde
	 */
	public AnalyzeContainer getResult() throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "getResult()");
		this.aCon.setAnaType(anaType);
		this.aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
		this.aCon.setComment(comment);
		if (DEBUG) System.out.println(MSG_END_FCT + "getResult()");
		return(this.aCon);
	}
}
