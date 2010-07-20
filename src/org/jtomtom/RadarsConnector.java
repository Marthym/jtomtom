package org.jtomtom;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.Map;

/**
 * Interface for connect different radars database site 
 * @author marthym
 *
 */
public interface RadarsConnector {
	/**
	 * Tags in the file to identify the different information
	 */
	public static final String TAG_DATE = "[DAT] ";
	public static final String TAG_VERSION = "[VER] ";
	public static final String TAG_RADARS = "[RAD] ";

	/**
	 * Get the radars informations from the GPS connected
	 * @param	String Path of the map
	 * @return	HashMap with informations
	 * @throws JTomtomException 
	 */
	public Map<String, String> getLocalDbInfos(String m_path) throws JTomtomException;
	
	/**
	 * Get the radars information from the remote site database
	 * @param	Proxy to use for connexion
	 * @return	HashMap with informations
	 */
	public Map<String, String> getRemoteDbInfos(Proxy proxy);

	/**
	 * Connect to the remote web site if necessary
	 * @param p_proxy		Proxy to use for connexion
	 * @param p_user		User login
	 * @param p_password	password login
	 * @return				True if the connexion is established
	 */
	public boolean connexion(Proxy p_proxy, String p_user, String p_password);
	
	
	/**
	 * Return the connection needed for download update radars pack
	 * @return
	 */
	public HttpURLConnection getConnectionForUpdate();
	
	/**
	 * Return the connection needed for download installation radars pack
	 * @return
	 */	
	public HttpURLConnection getConnectionForInstall();
	
}
