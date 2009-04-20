package importer.paula.paula10.analyzer.paulaAnalyzer;

import importer.paula.paula10.analyzer.specificAnalyzer.AbstractAnalyzer;

public class AnalyzerParams 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"";		//Name dieses Tools
	
	protected String name= null;		//Name des Analysers
	protected String className= null;	//Name der Klassendatei des Analysers 
	protected double priority= 0;		//Priorit�t, mit der der Analyser analysiert
	protected double order= 0;			//Klasse der Reihenfolge, in die die analysierte Datei geh�rt
	protected AbstractAnalyzer analyzer= null;	//eigentlicher Analysierer
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Initialisiert ein neues AnalyzerParams-Objekt.
	 * @param name String - Name des Analyzers
	 * @param className String - Name der Klassendatei des Analyzers
	 * @param priority double - Priorit�t des Analyzers
	 * @param order double- Reihenfolgewert des Analyzers
	 */
	public AnalyzerParams(	String name, 
							String className, 
							double priority, 
							double order)
	{
		this.name= name;
		this.className= className;
		this.priority= priority;
		this.order= order;
	}

//	 ============================================== private Methoden ==============================================
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Gibt den Namen dieses Analyzers zur�ck
	 */
	public String getName()
		{ return(this.name);}
	
	/**
	 * Gibt den Namen der Klassendatei des Analyzers zur�ck.
	 */
	public String getClassName()
		{ return(this.className);}
	
	/**
	 * Gibt die Priorit�t dieses Analyzers zur�ck
	 */
	public double getPriority()
		{ return(this.priority);}
	/**
	 * Gibt den Reihenfolgewert dieses Analyzers zur�ck
	 */
	public double getOrder()
		{ return(this.order);}
	
	/**
	 * Gibt den konkreten Analyzer zur�ck.
	 */
	public AbstractAnalyzer getAnalyzer()
		{ return(this.analyzer);}
	
	/**
	 * Setzt den konkreten Analyzer zur�ck.
	 */
	public void setAnalyzer(AbstractAnalyzer analyzer)
		{ this.analyzer = analyzer;}
	
	
	/**
	 * Gibt Informationen �ber dieses Objekt als String zur�ck. 
	 * @return String - Informationen �ber dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= "name: " + this.getName() + "; classname: "  + this.getClassName() + 
				"; priority: " + this.getPriority() + "; order: " + this.getOrder(); 
		return(retStr);
	}
}
