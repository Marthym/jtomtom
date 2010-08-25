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
package org.jtomtom.connector.radars;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jtomtom.Constant;
import org.jtomtom.JTomtomException;
import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.connector.RadarsConnector;

/**
 * @author marthym
 *
 */
public class PdisDotEs implements RadarsConnector {
	private static final Logger LOGGER = Logger.getLogger(PdisDotEs.class);

	private static final String PDISES_LOGIN_URL = "http://www.pdis.es/paginas/login.php";
	private static final String PDISES_UPDATE_URL = "http://www.pdis.es/paginas/terminos_packs.php?fich=2";
	private static final String PDISES_UPDATE_POST = "fichero=actualizacion_radares_tomtom.zip";
	private static final String PDISES_INSTALL_URL = "http://www.pdis.es/paginas/terminos_packs.php?fich=9";
	private static final String PDISES_INSTALL_POST = "fichero=pack_radares_tomtom_v6.zip";
	private static final String PDISES_DOWN_PACKS = "http://www.pdis.es/paginas/down_packs.php";
	
	private static final String PDISES_START_DATE = "<span class=\"textobase5\">&Uacute;ltima actualizaci&oacute;n: </span><span class=\"textobase6\">";
	
	private static final Locale PDISES_COUNTRY = new Locale("es", "ES");
	
	private List<HttpCookie> m_connexionCookies;
	
	private Proxy m_proxy = Proxy.NO_PROXY;
	
	/* (non-Javadoc)
	 * @see org.jtomtom.RadarsConnector#getLocalDbInfos(java.lang.String)
	 */
	@Override
	public POIsDbInfos getLocalDbInfos(String m_path)
			throws JTomtomException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jtomtom.RadarsConnector#getRemoteDbInfos(java.net.Proxy)
	 */
	@Override
	public POIsDbInfos getRemoteDbInfos(Proxy proxy) {
		if (m_connexionCookies == null) {
			LOGGER.error("You must connect before !!");
			return null;
		}
		
		// Initiate connexion
		HttpURLConnection conn = initDownloadConnection(PDISES_DOWN_PACKS, "");
		
		// Test connexion response code
		int connResponse = -1;
		try {
			connResponse = conn.getResponseCode();
		} catch (IOException e) {
			return null;
		}
		
		if (connResponse != HttpURLConnection.HTTP_OK) {
			return null;
		}
		
		// Looking for package date une the response content.
		POIsDbInfos infos = new POIsDbInfos();
		InputStream is = null;
		BufferedReader rd = null;
		try {
			is = conn.getInputStream();
			rd = new BufferedReader(new InputStreamReader(is));
			String line;
			boolean nextLine = false;
			while((line = rd.readLine()) != null) {
				if (line.trim().startsWith(PDISES_START_DATE)) {
					nextLine = true;
					continue;
				}
				if (nextLine) {
					infos.setLastUpdateDate("dd/MM/yyyy", line.replaceAll("\\<.*?>","").replaceAll("\\&.*?;","").trim());
					break;
				}
			}
			
		} catch (IOException e) {
			LOGGER.warn(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) LOGGER.debug(e);
			return null;
		}
		
		return infos;
	}

	/* (non-Javadoc)
	 * @see org.jtomtom.RadarsConnector#connexion(java.net.Proxy, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean connexion(Proxy p_proxy, String p_user, String p_password) {
		m_proxy = p_proxy;
		String urlParameters = "";
		try {
			urlParameters =
			    "recordar=on&usuario=" + URLEncoder.encode(p_user, "UTF-8") +
			    "&pass=" + URLEncoder.encode(p_password, "UTF-8") +
			    "&submit=Login";
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		}
		
		HttpURLConnection conn = null;
		DataOutputStream wr = null;
		try {
			URL pdisesUrl = new URL(PDISES_LOGIN_URL);
			conn = (HttpURLConnection) pdisesUrl.openConnection(m_proxy);
			
			conn.setRequestProperty ("User-agent", Constant.TOMTOM_USER_AGENT);
	        conn.setUseCaches(false);
	        conn.setReadTimeout(Constant.TIMEOUT); // TimeOut en cas de perte de connexion
	        conn.setRequestMethod("POST");
	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        
	        wr = new DataOutputStream (conn.getOutputStream());
	        wr.writeBytes(urlParameters);
	        wr.flush ();
	        wr.close ();
	        conn.setInstanceFollowRedirects(false);
	        conn.connect();
	        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK ||
	        		conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
	        	m_connexionCookies = new LinkedList<HttpCookie>();
	        	String headerName=null;
	        	for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) {
	        	 	if (headerName.equals("Set-Cookie")) {                  
	        	 		String cookie = conn.getHeaderField(i);
	        	 		cookie = cookie.substring(0, cookie.indexOf(";"));
	        	        String cookieName = cookie.substring(0, cookie.indexOf("="));
	        	        String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
	        	        if (!cookieValue.equals("deleted")) {
	        	        	m_connexionCookies.add(new HttpCookie(cookieName, cookieValue));
	        	        	LOGGER.debug(cookieName+" = "+cookieValue);
	        	        }
	        	 	}
	        	}
	        }
	        	        
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			
		} finally {
			try {wr.close();} catch (Exception e){}
		}
		
		if (m_connexionCookies != null && 
				m_connexionCookies.size() > 1) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Init a HttpURLConnection with cookie and all needed parameter for download update and install file
	 * @param p_url		URL of the file to download
	 * @param p_post	POST parameter needed for get the good file
	 * @return			The connection
	 */
	private HttpURLConnection initDownloadConnection(String p_url, String p_post) {
		if (m_connexionCookies == null) {
			LOGGER.error("You must connect before !!");
			return null;
		}
		
		HttpURLConnection conn = null;
		URL updateURL = null;
		try {
			updateURL = new URL(p_url);
			conn = (HttpURLConnection) updateURL.openConnection(m_proxy);

	        // Rewrite the cookie string
	        StringBuffer theCookie = new StringBuffer();
	        for (HttpCookie cookie : m_connexionCookies) {
	        	if (theCookie.length() > 0) theCookie.append(";");
	        	theCookie.append(cookie.getName()).append("=").append(cookie.getValue());
	        }
	        
	        conn.setRequestProperty ("Cookie", theCookie.toString());
			conn.setRequestProperty ("User-agent", Constant.TOMTOM_USER_AGENT);
			conn.setDoInput(true);
	        conn.setUseCaches(false);
	        conn.setReadTimeout(Constant.TIMEOUT); // TimeOut en cas de perte de connexion
	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        conn.setInstanceFollowRedirects(false);
	        
	        DataOutputStream wr = new DataOutputStream (conn.getOutputStream());
	        wr.writeBytes(p_post);
	        wr.flush ();
	        wr.close ();
	        
		} catch (MalformedURLException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return null;
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return null;
		}
				
		return conn;
	}
	
	/* (non-Javadoc)
	 * @see org.jtomtom.RadarsConnector#getConnectionForUpdate()
	 */
	@Override
	public HttpURLConnection getConnectionForUpdate() {
		return initDownloadConnection(PDISES_UPDATE_URL, PDISES_UPDATE_POST);
	}
	
	/* (non-Javadoc)
	 * @see org.jtomtom.RadarsConnector#getConnectionForInstall()
	 */
	@Override
	public HttpURLConnection getConnectionForInstall() {
		return initDownloadConnection(PDISES_INSTALL_URL, PDISES_INSTALL_POST);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.getClass().getSimpleName()+" ["+PDISES_COUNTRY.getCountry()+"]";
	}

}
