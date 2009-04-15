package paulaReader_1_0.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Vector;

import org.apache.log4j.Logger;

import paulaReader_1_0.PAULAMapperInterface;
import paulaReader_1_0.reader.PAULAReader;

/**
 * The PAULAReaderMgr manages all PAULAReaders. At initialization it loads all kinds of 
 * PAULAReader wich could be used for parsing PAULA-Files. The main function is to
 * give back a correct PAULAReader for asked requirements.
 * @author Florian Zipser
 *
 */
public class PAULAReaderMgr
{
	/**
	 * Diese Klasse dient dem Speichern von Informationen zu PAULAReadern.
	 */
	private class PAULAReaderInfo 
	{
		String name= null;			//Name des Verwendeten Readers
		String readerCType= null;	//CTyp zu der dieser Reader geh�rt 
		String className= null;		//Javaklasse dieses Readertyps 
		Double priority= null;		//Gibt die Priorit�t an, mit der der Reader zum Zug kommt
		
		public PAULAReaderInfo(	String name, 
								String readerCType, 
								String className, 
								Double prio)
		{
			this.name= name;
			this.readerCType= readerCType;
			this.className= className;
			this.priority= prio;
		}
		
		/**
		 * Erzeugt einen PAULAReader. Dieser ist ein Objekt der Klasse className. 
		 * @return neuer PAULAReader zu der hier gespeicherten Klasse
		 */
		@SuppressWarnings ("unchecked")
		public PAULAReader getReader(	PAULAMapperInterface pMI,
										String corpusPath,						
										File paulaFile,
										Logger logger) throws Exception
		{	
			//Erzeugen des entsprechenden Readers
			PAULAReader reader= null;
			//if (Class.forName(className)<PAULAReader>)
			Class readerClass= Class.forName(this.className);
			//KonstruktorParameter zusammenbauen
			Class[] paramTypes = new Class[]{PAULAMapperInterface.class,java.lang.String.class, File.class, Logger.class}; 
			//suche passenden Konstruktor
			Constructor<PAULAReader> readerConst= readerClass.getConstructor(paramTypes);
			reader= readerConst.newInstance(pMI, corpusPath, paulaFile, logger);
			return(reader);
		}
	}
	
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 		"PAULAReaderMgr";		//Name dieses Tools
	
	private PAULAReaderInfo currReaderInfo= null;				//aktuell benutzter PAULA-Reader, wird gespeichert um Events entgegen zu nehmen
	private Vector<PAULAReaderInfo> paulaReaderInfoList= null;		//Liste aller benutzbaren PAULAReader
	/**
	 * Logger for log4j
	 */
	protected Logger logger= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_TYPE_NOT_SUPPORTED=		MSG_ERR + "Sorry the given analyze type is not yet supported in "+ TOOLNAME+ ". Analyze type: ";
	
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialize a PAULAReaderMgr-object an initialize all usable PAULAReader.
	 */
	public PAULAReaderMgr(Logger logger) throws Exception
	{
		this.logger= logger;
		
		this.fillPAULAReaderInfoList();
	}

//	 ============================================== private Methoden ==============================================

	/**
	 * Erstellt eine Liste mit Informationen zu allen Verf�gbaren PAULAReadern.
	 * Diese Liste ist this.PAULAReaderInfoList.
	 */
	//TODO Diese Methode muss durch das EInlesen aus einer XML-Datei erstezt werden
	private void fillPAULAReaderInfoList() throws Exception
	{
		paulaReaderInfoList= new Vector<PAULAReaderInfo>();
		PAULAReaderInfo pReader= null;
		
		//PrimDataReader(Name, CType, ClassName, Priorit�t)
		pReader= new PAULAReaderInfo("PrimDataReader", "PrimData", "paulaReader_1_0.reader.PrimDataReader", 1.0);
		this.paulaReaderInfoList.add(pReader);
		
		//TokDataReader(Name, CType, ClassName, Priorit�t)
		pReader= new PAULAReaderInfo("TokDataReader", "TokData", "paulaReader_1_0.reader.TokDataReader", 1.0);
		this.paulaReaderInfoList.add(pReader);
		
		//StructDataReader(Name, CType, ClassName, Priorit�t)
		pReader= new PAULAReaderInfo("StructDataReader", "StructData", "paulaReader_1_0.reader.StructDataReader", 1.0);
		this.paulaReaderInfoList.add(pReader);
		
		//StructEdgeDataReader(Name, CType, ClassName, Priorit�t)
		pReader= new PAULAReaderInfo("StructEdgeDataReader", "StructEdgeData", "paulaReader_1_0.reader.StructEdgeDataReader", 1.0);
		this.paulaReaderInfoList.add(pReader);
		
		//AnnoDataReader(Name, CType, ClassName, Priorit�t)
		pReader= new PAULAReaderInfo("AnnoDataReader", "AnnoData", "paulaReader_1_0.reader.AnnoDataReader", 1.0);
		this.paulaReaderInfoList.add(pReader);	
		
		//MultiFeatDataReader(Name, CType, ClassName, Priorit�t)
		pReader= new PAULAReaderInfo("MultiFeatDataReader", "MultiFeatData", "paulaReader_1_0.reader.MultiFeatDataReader", 1.0);
		this.paulaReaderInfoList.add(pReader);	
		
		//ComplexAnnoDataReader(Name, CType, ClassName, Priorit�t)
		pReader= new PAULAReaderInfo("ComplexAnnoDataReader", "ComplexAnnoData", "paulaReader_1_0.reader.ComplexAnnoDataReader", 1.0);
		this.paulaReaderInfoList.add(pReader);
		
		//MetaStructDataReader(Name, CType, ClassName, Priorit�t)
		pReader= new PAULAReaderInfo("MetaStructDataReader", "MetaStructData", "paulaReader_1_0.reader.MetaStructDataReader", 1.0);
		this.paulaReaderInfoList.add(pReader);
		
		//MetaAnnoDataReader(Name, CType, ClassName, Priorit�t)
		pReader= new PAULAReaderInfo("MetaAnnoDataReader", "MetaAnnoData", "paulaReader_1_0.reader.MetaAnnoDataReader", 1.0);
		this.paulaReaderInfoList.add(pReader);
		
		//AudioAnnoDataReader(Name, CType, ClassName, Priorit�t)
		pReader= new PAULAReaderInfo("AudioDataReader", "AudioData", "paulaReader_1_0.reader.AudioDataReader", 1.0);
		this.paulaReaderInfoList.add(pReader);
	}
	
//	 ============================================== �ffentl. Methoden ==============================================

	/**
	 * Sucht einen zu dem Paula-Dokument-Type (PDDesc.getPDType()) passenden Reader und 
	 * gibt diesen zur�ck.
	 * @param cType String type of classification of the given paula file (result of PAULAAnalyzer)
	 * @param paulaFile File - a file object for the current paula file  
	 * @param corpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param mapper PAULAMapperInterface mapper - the mapper wich has to be invoked by the PAULAReader
	 * @return zu dem Dokument Typ passender Reader
	 * @throws Exception
	 */
	public PAULAReader getPAULAReader(	String cType,
												File paulaFile,
												String corpusPath,
												PAULAMapperInterface mapper) throws Exception
	{
		this.currReaderInfo= null;
		Double prio= null;
		//System.out.println("searched cType: " + cType);
		for (PAULAReaderInfo readerInfo : this.paulaReaderInfoList)
		{
			//System.out.println("found cType: " + readerInfo.readerCType);
			//wenn passender Reader gefunden wurde
			if (cType.equalsIgnoreCase(readerInfo.readerCType))
			{	
				//wenn die Priorit�t besser ist als die bereits gefundene, sofern eine gefunden wurde
				//prio ist besser, wenn sie kleiner ist
				if ((prio == null) || ((prio!= null) && (prio > readerInfo.priority)))
				{
					this.currReaderInfo= readerInfo;
					prio= readerInfo.priority;
				}
			}	
		}
		//pr�fe ob ein Reader gefunden wurde
		if (this.currReaderInfo== null)
			throw new Exception(ERR_TYPE_NOT_SUPPORTED + cType);
		return(this.currReaderInfo.getReader(mapper, corpusPath, paulaFile, this.logger));
	}
}
