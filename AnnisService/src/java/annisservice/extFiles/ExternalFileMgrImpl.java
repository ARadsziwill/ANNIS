package annisservice.extFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import annisservice.extFiles.exchangeObj.ExtFileObjectCom;
import annisservice.extFiles.exchangeObj.ExtFileObjectDAO;
import annisservice.extFiles.exchangeObj.ExtFileObjectImpl;
import annisservice.ifaces.AnnisBinary;
import annisservice.objects.AnnisBinaryImpl;

/**
 * This class manages storing and getting external files for the ANNIS 2.0 system.
 * Main functions are putting a file and getting it back. 
 * The manager corresponds the given file to an unique id, which is reference to the file. 
 *
 * @author Florian Zipser
 *
 */
public class ExternalFileMgrImpl implements ExternalFileMgr
{
//	 ============================================== private Variablen ==============================================
	private Logger logger = Logger.getLogger(this.getClass());
	
	// folder in which the external data should be stored
	private String externalDataFolder;
	
	// Database access helper object
	@Autowired private ExternalFileMgrDAO externalFileMgrDao;
		
//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_BRANCH_ALREADY_EXISTS=	"Cannot create new branch, because it already exists: ";
	private static final String ERR_BRANCH_NOT_EXISTS=		"Cannot delete branch, because it does not exists: ";
	private static final String ERR_BRANCH_NO_DIR=			"The given branch name seems to be a filename, i can only work with names for folders: ";
	private static final String ERR_BRANCH_NOT_EMPTY=		"Cannot delete branch, because it is not empty: ";
	private static final String ERR_BRANCH_NOT_EXIST=		"Cannot put external file, because the needed branch does not exist (call createBranch() first): ";
	private static final String ERR_EMPTY_EXTFILE=			"Cannot put external file, because it�s empty."; 
	private static final String ERR_NO_FILE=				"Cannot put external file, because the context file is empty.";
	private static final String ERR_DELETE=					"Cannot delete file: ";

//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Creates a new ExternalFileMgrImpl
	 */
	public ExternalFileMgrImpl()
	{
		logger.debug("external data folder: "+ this.externalDataFolder);
	}
//	 ============================================== �ffentliche Methoden ==============================================	
	//---------------------- start: branch management --------------------------------
	/**
	 * Returns whether a branch with the given name already exists or not.   
	 * @param branch String - name of the branch
	 * @return true if the branch exists
	 * @throws Exception
	 */
	public boolean hasBranch(String branch)
	{
		boolean retVal= false;
		File dstFolder= new File(this.externalDataFolder, branch);
		if ((dstFolder.exists()) && (!dstFolder.isDirectory()))
			throw new ExternalFileMgrException(ERR_BRANCH_NO_DIR + branch);
		retVal= dstFolder.exists();
		return(retVal);
	}
	
	/**
	 * Creates a new branch with the given name, in which the external files would be stored.
	 * @param branch String - name of the new branch
	 * @throws Exception
	 */
	public void createBranch(String branch)
	{
		if (this.hasBranch(branch))
			throw new ExternalFileMgrException(ERR_BRANCH_ALREADY_EXISTS + branch);
		File dstFolder= new File(this.externalDataFolder, branch);
		dstFolder.mkdir();
	}
	
	/**
	 * Creates an existing branch with the given name, in which the external files would be stored.
	 * @param branch String - name of the new branch
	 * @throws Exception
	 */
	public void deleteBranch(String branch)
	{
		this.deleteBranch(branch, false);
	}
	
	/**
	 * Deletes an existing branch with the given name, in which the external files would be stored.
	 * @param branch String - name of the new branch
	 * @param delRec boolean - if set to true, all entries will be deleted recursivly
	 * @throws Exception Error if branch does not exists, or branch isn�t empty and delRec isn�t set to true
	 */
	public void deleteBranch(String branch, boolean delRec)
	{
		if (!this.hasBranch(branch))
			throw new ExternalFileMgrException(ERR_BRANCH_NOT_EXISTS + branch);
		File dstFolder= new File(this.externalDataFolder, branch);
		//wenn branch nicht leer ist
		File[] subFiles= dstFolder.listFiles();
		if ((subFiles!= null) && (subFiles.length != 0) && (!delRec)) 
			throw new ExternalFileMgrException(ERR_BRANCH_NOT_EMPTY + branch);
		
		if (!dstFolder.delete())
			throw new ExternalFileMgrException("could not delete branch, because it isn�t empty, and this function isn�t implemented");
	}
	
	
	//---------------------- end: branch management --------------------------------
	/**
	 * Returns true, if the eFileMgr has an entry with the given value
	 * @param id Long - reference to the needed file 
	 * @param true, if a value exists, else false 
	 */
	public boolean hasId(long id)
	{ 
		logger.debug("calling hasId("+id+") was called...");
		return(externalFileMgrDao.hasId(id));
	}
	//---------------------- putting a file --------------------------------
	/**
	 * Puts a new file into the storage and returns an unique id to refer it.
	 * @param extFile File - new file which should be inserted.
	 */
	public Long putFile(ExtFileObjectCom extFile)
	{ 
		logger.debug("calling putFile("+extFile.getFile().getName()+") was called...");
		if (extFile== null)
			throw new ExternalFileMgrException(ERR_EMPTY_EXTFILE);
		if (extFile.getFile()== null)
			throw new ExternalFileMgrException(ERR_NO_FILE);
		
		//Datei in den Branch kopieren
		String strBranch= extFile.getBranch();
		File branch= new File(this.externalDataFolder, strBranch);
		if (!branch.exists())
			throw new ExternalFileMgrException(ERR_BRANCH_NOT_EXIST+ branch);
		
		//writing bytes to new file
		File outFile= this.createNewFileName(branch, extFile.getFileName());
		
		try {
			extFile.getFile(outFile.getCanonicalPath());
		} catch (IOException e) {
			throw new ExternalFileMgrException(e);
		}
		
		ExtFileObjectDAO extFileDao= new ExtFileObjectImpl(extFile, outFile.getName());
		return(externalFileMgrDao.putExtFile(extFileDao));
	}
	
	/**
	 * Creates a new filename.
	 * @param branch String - the name of branch, in which file should be stored 
	 * @param fileName String - name of file, as which file should be stored
	 * @return a file object which contains the new created name
	 * @throws Exception
	 */
	private File createNewFileName(File branch, String fileName)
	{
		try {

			String newFileName = branch.getCanonicalFile() + "/"+ fileName;
			File newFile= new File(newFileName);
			//if this filename already exists
			if (newFile.exists())
			{
				String prefix= fileName;
				String suffix= "";
				String[] parts= fileName.split("[.]");
				if (parts.length >1)
				{
					prefix= "";
					for (int i= 0; i < parts.length-1; i++)
					{
						if (i==0) prefix= prefix + parts[i];
						else prefix= prefix + "." + parts[i];
					}
					suffix= "."+parts[parts.length-1];
				}	
				int i= 0;
				//searching for new name
				while(newFile.exists())
				{
					newFileName= branch.getCanonicalFile() + "/"+ prefix+ "_"+i+suffix;
					newFile= new File(newFileName);
					i++;
				}
			}
			return(newFile);
		} catch (IOException e) {
			throw new ExternalFileMgrException(e);
		}
	}
	
	/**
	 * Returns a ExtFileObject-object which corresponds to the given reference. If there�s
	 * no file to the given reference null will be returned.
	 * @param id Long - reference to the needed file 
	 * @return ExtFileObject which corresponds to the given reference
	 * @throws Exception
	 */
	public ExtFileObjectCom getExtFileObj(Long id)
	{ 
		if (this.logger!= null) 
			logger.debug("calling getFile("+id+") was called...");
		
		ExtFileObjectDAO extFileDao= null;
		extFileDao= externalFileMgrDao.getExtFileObj(id);
		
		String fileSrc= this.externalDataFolder + "/"+ extFileDao.getBranch() + "/"+ extFileDao.getFileName();
		
		//ExtFileObjectCom extFileCom= new ExtFileObjectImpl();
		//extFileCom.setFile(new File(fileSrc));
		ExtFileObjectCom extFileCom= new ExtFileObjectImpl(extFileDao, new File(fileSrc));
		return(extFileCom); 
	}
	
	/**
	 * Returns the file which corresponds to the given reference in byte packages. 
	 * If there�s no file to the given reference null will be returned.
	 * @param id Long - reference to the needed file 
	 * @return file which corresponds to the given reference
	 * @throws Exception
	 */
	public AnnisBinary getBinary(Long id)
	{
		try
		{
			ExtFileObjectCom extFile= this.getExtFileObj(id);
			AnnisBinary aBin= new AnnisBinaryImpl(); 
			
			//Bytes setzen
			File file = extFile.getFile();
			long length = file.length();
			FileInputStream fis = new FileInputStream(file);
			byte[] bytes = new byte[(int)length];
		    int offset = 0;
		    int numRead = 0;
		    while (offset < bytes.length
		               && (numRead=fis.read(bytes, offset, bytes.length-offset)) >= 0) 
		    {
		    	offset += numRead;
		    }
	
		    
		    // Ensure all the bytes have been read in
		    if (offset < bytes.length) {
		    	throw new IOException("Could not completely read file "+file.getName());
		    }
			fis.close();
			aBin.setBytes(bytes);
			aBin.setId(extFile.getID());
			aBin.setFileName(extFile.getFileName());
			aBin.setMimeType(extFile.getMime());
			
			return(aBin);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			throw new ExternalFileMgrException(e);
		}
		/* old Kartsens implementation
		AnnisBinary binary = new AnnisBinaryImpl();
		binary.setId(id);
		binary.setFileName("Nr. 14 c#, op.27 nr 2, presto.mp3");
		binary.setMimeType("audio/mp3");

		try {
		 File file = new File("sample.mp3");
		 long length = file.length();


		 FileInputStream fis = new FileInputStream(file);


		 byte[] bytes = new byte[(int)length];


		     int offset = 0;
		         int numRead = 0;
		         while (offset < bytes.length
		               && (numRead=fis.read(bytes, offset, bytes.length-offset)) >= 0) 
		         {
		             offset += numRead;
		         }

		    
		         // Ensure all the bytes have been read in
		         if (offset < bytes.length) {
		             throw new IOException("Could not completely read file "+file.getName());
		         }

		        
		 fis.close();
		 binary.setBytes(bytes);
		 } catch (IOException e) {
		 e.printStackTrace();
		 } catch (NegativeArraySizeException e) {
		 e.printStackTrace();
		 }
		 return binary;
		 */
	}
	
	/**
	 * Deletes a file from the eFileMgr. The file will be searched by the
	 * given id.
	 * @param id Long - reference to the needed file
	 * @throws Exception
	 */
	public void deleteFile(long id)
	{
		if (this.logger!= null) 
			logger.debug("calling deleteFile("+id+") was called...");
		
		ExtFileObjectDAO extFileDao= externalFileMgrDao.getExtFileObj(id);
		String fileSrc= this.externalDataFolder + "/"+ extFileDao.getBranch() + "/"+ extFileDao.getFileName();
		File dFile= new File(fileSrc);
		if (!dFile.delete())
			throw new ExternalFileMgrException(ERR_DELETE + fileSrc);
		externalFileMgrDao.deleteExtFileObj(id);
	}

	public String getExternalDataFolder() {
		return externalDataFolder;
	}

	public void setExternalDataFolder(String externalDataFolder) {
		this.externalDataFolder = externalDataFolder;
	}
	public ExternalFileMgrDAO getExternalFileMgrDao() {
		return externalFileMgrDao;
	}
	public void setExternalFileMgrDao(ExternalFileMgrDAO externalFileMgrDao) {
		this.externalFileMgrDao = externalFileMgrDao;
	}
}
