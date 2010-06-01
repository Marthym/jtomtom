/**
 *  Copyright (C) 2010  Frédéric Combes
 *  This file is part of jTomtom.
 *
 *  jTomtom is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  jTomtom is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with jTomtom.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Frédéric Combes can be reached at:
 *  <belz12@yahoo.fr> 
 */
package org.jtomtom;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public final class TomTomax {
	private static final Logger LOGGER = Logger.getLogger(TomTomax.class);
	
	/**
	 * URL du fichier de base contennat les informations sur la base courante
	 */
	public static final String TOMTOMAX_DB_URL = "http://www.tomtomax.fr/upload/tomtomax_radars.db";
	public static final String TOMTOMAX_LOGIN_URL = "http://www.tomtomax.fr/forum/ucp.php?mode=login";
	
	public static final String TOMTOMAX_COOKIE_CONNECT = "phpbb3_e1wj8_u";
	
	/**
	 * User Agent de la MaxBox
	 */
	public static final String TOMTOMAX_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)";
	
	/**
	 * Fichier de base présent dans les packages et installé sur le TT
	 */
	public static final String TOMTOMAX_DB_FILE = "maxipoi_radars.db";
	
	/**
	 * Tags contenu dans le fichier pour identifier les différentes informations
	 */
	public static final String TAG_DATE = "[DAT] ";
	public static final String TAG_VERSION = "[VER] ";
	public static final String TAG_RADARS = "[RAD] ";
	public static final String TAG_BASIC = "[UZ1] ";
	public static final String TAG_MEDIUM = "[UZ2] ";
	public static final String TAG_PREMIUM = "[UZ3] ";
	
	
	/**
	 * On interdit l'instanciation
	 */
	private TomTomax(){};
	
	/**
	 * Récupère les informations de la base de Radars courante chez Tomtomax
	 * @param	Proxy à utiliser pour la connexion
	 * @return	Un tableau contenant les informations
	 */
	public static final Map<String, String> getRemoteDbInfos(Proxy proxy) {
		Map<String, String> infos = null;
		HttpURLConnection conn = null;
		try {
			URL tomtomaxUrl = new URL(TOMTOMAX_DB_URL);
			conn = (HttpURLConnection) tomtomaxUrl.openConnection(proxy);
			
			conn.setRequestProperty ( "User-agent", Constant.TOMTOM_USER_AGENT);
	        conn.setUseCaches(false);
	        conn.setReadTimeout(Constant.TIMEOUT); // TimeOut en cas de perte de connexion
	        conn.connect();
	
			if (LOGGER.isDebugEnabled()) LOGGER.debug("conn.getResponseCode() = "+conn.getResponseCode());
			
	        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	        	infos = new HashMap<String, String>(6);
	        	InputStream is = null;
	        	BufferedReader rd = null;
	        	try {
					is = conn.getInputStream();
					rd = new BufferedReader(new InputStreamReader(is));
					String line;
					while((line = rd.readLine()) != null) {
						if (line.startsWith(TAG_DATE)) {
							infos.put(TAG_DATE, line.substring(TAG_DATE.length()).trim());
							if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_DATE+infos.get(TAG_DATE));
							
						} else if (line.startsWith(TAG_VERSION)) {
							infos.put(TAG_VERSION, line.substring(TAG_VERSION.length()).trim());
							if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_VERSION+infos.get(TAG_VERSION));
							
						} else if (line.startsWith(TAG_RADARS)) {
							infos.put(TAG_RADARS, line.substring(TAG_RADARS.length()).trim());
							if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_RADARS+infos.get(TAG_RADARS));
							
						} else if (line.startsWith(TAG_BASIC)) {
							infos.put(TAG_BASIC, line.substring(TAG_BASIC.length()).trim());
							if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_BASIC+infos.get(TAG_BASIC));
							
						} else if (line.startsWith(TAG_MEDIUM)) {
							infos.put(TAG_MEDIUM, line.substring(TAG_MEDIUM.length()).trim());
							if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_MEDIUM+infos.get(TAG_MEDIUM));
							
						} else if (line.startsWith(TAG_PREMIUM)) {
							infos.put(TAG_PREMIUM, line.substring(TAG_PREMIUM.length()).trim());
							if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_PREMIUM+infos.get(TAG_PREMIUM));
							
						}
					}
					
	        	} finally {
	        		try {is.close();} catch (Exception e){}
	        		try {rd.close();} catch (Exception e){}
	        	}
	        } // end if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
	        
		} catch (MalformedURLException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		} 
		
		return infos;
	}
	
	/**
	 * Effectue la connexion au site Tomtomax pour télécharger les mises à jours !
	 * @param p_proxy		Proxy à utiliser
	 * @param p_user		Utilisateur sur forum Tomtomax
	 * @param p_password	Mot de pas de l'utilisateur
	 * @return				True si l'utilisateur à le droit de se connecter
	 */
	public static final boolean connexion(Proxy p_proxy, String p_user, String p_password) {
		Map<String, String> cookies = null;
		String urlParameters = "";
		try {
			urlParameters =
			    "mode=login&username=" + URLEncoder.encode(p_user, "UTF-8") +
			    "&password=" + URLEncoder.encode(p_password, "UTF-8") +
			    "&login=Connexion"+
			    "&redirect=index.php";
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		HttpURLConnection conn = null;
		DataOutputStream wr = null;
		try {
			URL tomtomaxUrl = new URL(TOMTOMAX_LOGIN_URL);
			conn = (HttpURLConnection) tomtomaxUrl.openConnection(p_proxy);
			
			conn.setRequestProperty ( "User-agent", Constant.TOMTOM_USER_AGENT);
	        conn.setUseCaches(false);
	        conn.setReadTimeout(Constant.TIMEOUT); // TimeOut en cas de perte de connexion
	        conn.setRequestMethod("POST");
	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        
	        wr = new DataOutputStream (conn.getOutputStream ());
	        wr.writeBytes(urlParameters);
	        wr.flush ();
	        wr.close ();
	        
	        conn.connect();
	
			if (LOGGER.isDebugEnabled()) LOGGER.debug("conn.getResponseCode() = "+conn.getResponseCode());
			
	        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	        	cookies = new HashMap<String, String>();
	        	String headerName=null;
	        	for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) {
	        	 	if (headerName.equals("Set-Cookie")) {                  
	        	 		String cookie = conn.getHeaderField(i);
	        	 		cookie = cookie.substring(0, cookie.indexOf(";"));
	        	        String cookieName = cookie.substring(0, cookie.indexOf("="));
	        	        String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
	        	 		cookies.put(cookieName, cookieValue);
	        	        
	        	        LOGGER.debug(cookieName+" = "+cookieValue);
	        	 	}
	        	}
	        }
	        
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			
		} finally {
			try {wr.close();} catch (Exception e){}
		}
		
		if (cookies != null && 
				!cookies.isEmpty() && 
				cookies.containsKey(TOMTOMAX_COOKIE_CONNECT) && 
				!cookies.get(TOMTOMAX_COOKIE_CONNECT).equals("1")) {
			return true;
		}
		
		return false;
	}

}
