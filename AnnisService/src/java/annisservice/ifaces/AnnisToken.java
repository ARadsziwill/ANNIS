package annisservice.ifaces;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a corpus token.
 * 
 * @author k.huetter
 *
 */
public interface AnnisToken extends Map<String, String>, JSONAble, Serializable {

	/**
	 * 
	 * @return Node id of this token.
	 */
	public abstract long getId();

	/**
	 * 
	 * @param id Node id of this token.
	 */
	public abstract void setId(long id);
	
	/**
	 * 
	 * @return Source text (word, etc.) of this token.
	 */
	public abstract String getText();
	
	/**
	 * 
	 * @param Source text (word, etc.) of this token.
	 */
	public abstract void setText(String text);
	
	long getLeft();
	long getRight();
	long getTokenIndex();

}