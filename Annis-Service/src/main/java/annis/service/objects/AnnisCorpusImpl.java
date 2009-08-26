package annis.service.objects;

import java.io.Serializable;

import annis.service.ifaces.AnnisCorpus;
import annis.service.ifaces.JSONAble;

public class AnnisCorpusImpl implements Serializable, JSONAble, AnnisCorpus {
	private static final long serialVersionUID = 7622346856447696397L;
	
	private long id;
	private String name;
	private int textCount, tokenCount;
	
	public AnnisCorpusImpl(long id, String name, int textCount, int tokenCount) {
		this.id = id;
		this.textCount = textCount;
		this.tokenCount = tokenCount;
		this.name = name;
	}


	public AnnisCorpusImpl() {
		this(0, null, 0, 0);
	}
	
	/* (non-Javadoc)
	 * @see annisservice.objects.AnnisCorpus#getId()
	 */
	public long getId() {
		return id;
	}
	/* (non-Javadoc)
	 * @see annisservice.objects.AnnisCorpus#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}
	/* (non-Javadoc)
	 * @see annisservice.objects.AnnisCorpus#getName()
	 */
	public String getName() {
		return name;
	}
	/* (non-Javadoc)
	 * @see annisservice.objects.AnnisCorpus#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}
	/* (non-Javadoc)
	 * @see annisservice.objects.AnnisCorpus#getTextCount()
	 */
	public int getTextCount() {
		return textCount;
	}
	/* (non-Javadoc)
	 * @see annisservice.objects.AnnisCorpus#setTextCount(int)
	 */
	public void setTextCount(int textCount) {
		this.textCount = textCount;
	}
	/* (non-Javadoc)
	 * @see annisservice.objects.AnnisCorpus#getTokenCount()
	 */
	public int getTokenCount() {
		return tokenCount;
	}
	/* (non-Javadoc)
	 * @see annisservice.objects.AnnisCorpus#setTokenCount(int)
	 */
	public void setTokenCount(int tokenCount) {
		this.tokenCount = tokenCount;
	}
	
	@Override
	public String toString() {
		return String.valueOf("corpus #" + id + ": " + name);
	}
	
	/* (non-Javadoc)
	 * @see annisservice.objects.AnnisCorpus#getJSON()
	 */
	public String getJSON() {
		return "{\"id\":" + id + ",\"name\":\"" + name + "\",\"textCount\":" + textCount + ",\"tokenCount\":" + tokenCount + "}";
	}
}
