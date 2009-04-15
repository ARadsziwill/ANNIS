package paulaReader_1_0.reader;

import java.io.File;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import paulaReader_1_0.PAULAMapperInterface;


/**
 * This reader parses a file from type AudioData, these are paula documents
 * with same structure as annoData, except the AudioData-documents have 
 * references to audio-files.
 * 
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class AudioDataReader extends PAULAReader 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 		"AnnoDataReader";		//Name dieses Tools
	private static final String VERSION=		"1.0";				//Version des Tools
	private static final String PAULA_VERSION=	"1.0";			//Unterst�tzter PAULA-Standard
	private static final boolean DEBUG=			true;			//DEBUG-Schalter
	
	private static final String KW_REF_EMPTY=	"none";		//Inhalt von value, wenn Dateireferenz leer
	
	private String paulaID= null;					//Paula_id
	private String xmlBase= null;					//Dokument auf das sich dieses bezieht
	private String paulaType= null;					//Paula-Typ des aktuellen Dokumentes
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_STD_XML=		MSG_ERR + "An error occurs while parsing document: ";
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt den Toolnamen dieser Klasse zur�ck. dies ist ein abstrakter Name, der nicht 
	 * dem Namen der Klasse gleichen muss.
	 * @return Toolname dieser Klasse 
	 */
	public static String getToolName()
		{ return(TOOLNAME); }
	
	/**
	 * Gibt die Version dieser Klasse zur�ck.
	 * @return Version dieser Klasse 
	 */
	public static String getVersion()
		{ return(VERSION); }
	
	/**
	 * Gibt die von dieser Klasse unterst�tzte PAULA-Version zur�ck.
	 * @return Version dieser Klasse 
	 */
	public static String getPAULAVersion()
		{ return(PAULA_VERSION); }

//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein Reader Objekt und setzt den logger zur Nachrichtenausgabe. 
	 * W�hrend des Lesens wird der �bergebene Mapper �ber ein Callback aufgerufen um
	 * die entsprechenden Daten zu verarbeiten.
	 * @param mapper PAULAMapperInterface - Der Mapper, der f�r das Callback aufgerufen wird
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - zu parsende PAULA-Datei
	 * @param logger Logger - Logger zur Nachrichtenausgabe
	 */
	public AudioDataReader(	PAULAMapperInterface mapper,
								String korpusPath, 
								File paulaFile,
								Logger logger) throws Exception
	{
		super(TOOLNAME, VERSION, mapper, korpusPath, paulaFile, logger);
		if (this.logger!= null) this.logger.debug(MSG_STD + "object initialized: " + this);
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
		retStr= 	"tool: " + TOOLNAME + "\tversion: " + 
					VERSION + "\t supported PAULA version: "+ PAULA_VERSION;
		return(retStr);
	}
//	 --------------------------- SAX Methoden ---------------------------
	public void startDocument()	throws SAXException
	{
		try
		{
			//Mapper Bescheid geben, dass das Parsen beginnt
			this.mapper.startDocument(this, this.paulaFile, this.korpusPath);
			if (this.logger != null) this.logger.debug(MSG_STD + "reading document: "+ this.paulaFile.getCanonicalPath());
		}
		catch (Exception e)
		{
			if (DEBUG) e.printStackTrace();
			throw new SAXException(ERR_STD_XML + e.getMessage());
		}
	}
	
	public void endDocument()	throws SAXException
	{
		try
		{
			//Mapper Bescheid geben, dass das Parsen beginnt
			this.mapper.endDocument(this, this.paulaFile, this.korpusPath);
		}
		catch (Exception e)
		{
			if (DEBUG) e.printStackTrace();
			throw new SAXException(ERR_STD_XML+ e.getMessage());
		}
	}
	
	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(	String uri,
            					String localName,
            					String qName,
            					Attributes attributes) throws SAXException
    {
		super.startElement(uri, localName, qName, attributes);
		try
		{
			//TAG HEADER gefunden
			if (this.isTAGorAttribute(qName, TAG_HEADER))
			{
				for(int i= 0; i < attributes.getLength(); i++)
				{	
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_HEADER_PAULA_ID))
						this.paulaID= attributes.getValue(i);
				}
			}
			//Tag FEATLIST gefunden
			else if (this.isTAGorAttribute(qName, TAG_FEAT_FEATLIST))
			{
				for(int i= 0; i < attributes.getLength(); i++)
				{	
					//Attribut MARKLIST.BASE gefunden
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEATLIST_BASE))
						this.xmlBase= attributes.getValue(i);
					//Attribut MARKLIST.TYPE gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEATLIST_TYPE))
						this.paulaType= attributes.getValue(i);
				}
			}
			//Tag FEAT gefunden
			else if (this.isTAGorAttribute(qName, TAG_FEAT_FEAT))
			{
				String featID= null;	//feat.id-Wert
				String featHref= null;	//feat.href-Wert
				String featTar= null;	//feat.target-Wert
				String audioRefStr= null;	//feat.value-Wert
				String featDesc= null;	//feat.description-Wert
				String featExp= null;	//feat.example-Wert
				
				for(int i= 0; i < attributes.getLength(); i++)
				{	
					//Attribut FEAT.ID gefunden
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_ID))
						featID= attributes.getValue(i);
					//Attribut FEAT.HREF gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_HREF))
						featHref= attributes.getValue(i);
					//Attribut FEAT.TARGET gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_TAR))
						featTar= attributes.getValue(i);
					//Attribut FEAT.VALUE gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_VAL))
						audioRefStr= attributes.getValue(i);
					//Attribut FEAT.DESCRIPTION gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_DESC))
						featDesc= attributes.getValue(i);
					//Attribut FEAT.EXAMPLE gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_EXP))
						featExp= attributes.getValue(i);
				}
				File audioRef= null;
				try
				{
					//wenn Audio-Datei-Referenz leer ist
					if ((audioRefStr== null) || (audioRefStr.equalsIgnoreCase("")) ||
							(audioRefStr.equalsIgnoreCase(KW_REF_EMPTY)))
						audioRef= null;
					else	audioRef= new File(this.paulaFile.getParent()+ "/"+audioRefStr);
				}
				catch (Exception e)
				{}
				//das PAULAMapperInterface aufrufen
				this.mapper.audioDataConnector(	this.korpusPath, this.paulaFile, 
												this.paulaID, this.paulaType, this.xmlBase, 
												featID, featHref, featTar, featDesc, featExp, audioRef);
			}
		}
		catch (Exception e)
		{
			if (DEBUG) 
			{
				e.printStackTrace();
				throw new SAXException(e.getMessage());
			}
			else throw new SAXException(e.getMessage());
		}
    }
	
	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(	String uri,
            				String localName,
            				String qName) throws SAXException
    {
    }
}
