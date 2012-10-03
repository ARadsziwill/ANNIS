package annis.service.objects;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This Class provides the Metadata of a BinaryFile.
 * @author benjamin
 */
@XmlRootElement
public class AnnisBinaryMetaData implements Serializable
{

  private String corpusName;
  private String mimeType;
  private String fileName;
  private int length;

  public String getCorpusName()
  {
    return corpusName;
  }

  public void setCorpusName(String corpusName)
  {
    this.corpusName = corpusName;
  }

  public String getMimeType()
  {
    return mimeType;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  public String getFileName()
  {
    return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public int getLength()
  {
    return length;
  }

  public void setLength(int length)
  {
    this.length = length;
  }

  
  

  
}
