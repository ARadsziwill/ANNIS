package relANNIS_2_0;

public interface RelationalDN 
{

	/**
	 * Gibt die relationale ID dieses Knotens zur�ck, sofern es eine gibt. Es wird
	 * null zur�ckgegeben, wenn keine relationale ID vorhanden.
	 * @return relationale ID
	 */
	public Long getRelID() throws Exception;
}
