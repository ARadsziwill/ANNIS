//*********************************************** XMLPrinter ******************************************

package importer.paula.paula10.util.toolDescriptor;

/**
 * <p> 
 * 	Das Modul CFlag bietet die M�glichkeit Eigenschaften zu einem Programaufruf flag zu speichern und zu lesen.
 * 	Dies ist der Name des Flags, die Angabe ob es optional ist und eine Beschreibung des Flags
 * </p>
 * @author Flo
 * @version 1.0
 */
public class CFlag 
{
//	 ============================================== private Variablen ==============================================

	/**
	 * Speichert den Namen des Flags
	 */
	private String name= "";
	
	/**
	 * Speichert Anh�nge wie Dateinamen
	 */
	private String appendix= "";
	
	/**
	 * Speichert die Beschreibung des Flags
	 */
	private String desc= "";
	/**
	 * Gibt an ob das Flag optional ist
	 */
	private boolean isOtional= false;	
	
	//	 *************************************** Meldungen ***************************************
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt ein CFlag Objekt und initialisiert es mit dem Namen.
	 * @param name String - des Flags
	 */
	public CFlag(String name)
	{ this.name= name; }
	
	/**
	 * Erzeugt ein CFlag Objekt und initialisiert es mit dem Namen, 
	 * der Optionalit�t und der Beschreibung.
	 * @param name - Name des Flags
	 * @param isOptional - wenn true, Falg ist optional
	 * @param desc - Beschreibung des Flags
	 */
	public CFlag(String name, boolean isOptional, String desc)
	{
		this(name);
		this.isOtional= isOptional;
		this.desc= desc;
	}
	
	/**
	 * Erzeugt ein CFlag Objekt und initialisiert es mit dem Namen, 
	 * der Optionalit�t und der Beschreibung.
	 * @param name - Name des Flags
	 * @param isOptional - wenn true, Falg ist optional
	 * @param appendix - Anh�nge wie Dateinamen
	 * @param desc - Beschreibung des Flags
	 */
	public CFlag(String name, String appendix, boolean isOptional, String desc)
	{
		this(name, isOptional, desc);
		this.appendix= appendix;
	}
	
//	 ============================================== private Methoden ==============================================
//	 ============================================== �ffentliche Methoden ==============================================	

	/**
	 * Gibt den Namen des Flags zur�ck
	 * @return - Name des Flags
	 */
	public String getName()
		{ return(this.name); }
	
	/**
	 * Gibt Anh�nge des Flags wie Dateinamen zur�ck
	 * @return - Anh�nge des Flags
	 */
	public String getAppendix()
		{ return(this.appendix); }
	
	/**
	 * Setzt den Parameter appendix auf den �bergebenen Wert.
	 * @param appendix - String - Anhang ean ein Flag wie z.B. Dateinamen.
	 */
	public void setAppendix(String appendix)
		{ this.appendix= appendix; }
	
	/**
	 * Gibt die Optionalit�t des Flags zur�ck
	 * @return - Optionalit�t des Flags
	 */
	public boolean isOptional()
		{ return(this.isOtional); }
	
	/**
	 * Setzt den Parameter isOptional auf den �bergebenen Wert.
	 * @param isOptional - boolean gibt an ob das Flag optional ist
	 */
	public void setIsOptional(boolean isOptional)
		{this.isOtional= isOptional;}
	
	/**
	 * Gibt die Beschreibung des Flags zur�ck
	 * @return - Beschreibung des Flags
	 */
	public String getDesc()
		{ return(this.desc); }
	
	/**
	 * Setzt den Parameter Desc auf den �bergebenen Wert.
	 * @param desc String - Beschreibung des Flags
	 */
	public void setDesc(String desc)
		{ this.desc= desc; }
	
	/**
	 * Gibt dieses Flag-Objekt als String zur�ck.
	 * @return Flag als String
	 */
	public String toString()
	{
		String retStr= "";
		retStr= retStr + this.name + " "+ this.appendix + "\t" + this.desc;
		if (this.isOtional)
			retStr= retStr + "[optional]";
		
		return(retStr);
	}
//	 ============================================== main Methode ==============================================	

}
