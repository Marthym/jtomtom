package org.jtomtom;

import java.net.Proxy;
import java.util.Map;

public interface RadarsConnector {
	/**
	 * Tags contenu dans le fichier pour identifier les différentes informations
	 */
	public static final String TAG_DATE = "[DAT] ";
	public static final String TAG_VERSION = "[VER] ";
	public static final String TAG_RADARS = "[RAD] ";

	/**
	 * Récupère les informations de la base de Radars courante sur le Tomtom connecté
	 * @param	String Chemin de la carte
	 * @return	Un tableau contenant les informations
	 * @throws JTomtomException 
	 */
	public Map<String, String> getLocalDbInfos(String m_path) throws JTomtomException;
	
	/**
	 * Récupère les informations de la base de Radars courante chez Tomtomax
	 * @param	Proxy à utiliser pour la connexion
	 * @return	Un tableau contenant les informations
	 */
	public Map<String, String> getRemoteDbInfos(Proxy proxy);

	/**
	 * Effectue la connexion au site Tomtomax pour télécharger les mises à jours !
	 * @param p_proxy		Proxy à utiliser
	 * @param p_user		Utilisateur sur forum Tomtomax
	 * @param p_password	Mot de pas de l'utilisateur
	 * @return				True si l'utilisateur à le droit de se connecter
	 */
	public boolean connexion(Proxy p_proxy, String p_user, String p_password);
	
	
	/**
	 * Return the URL for download update radars pack
	 * @return
	 */
	public String getUpdateURL();
	
	/**
	 * Return the URL for download installation radars pack
	 * @return
	 */	public String getInstallURL();
}
