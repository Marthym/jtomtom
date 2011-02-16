/**
 *  Copyright© 2010, 2011  Frédéric Combes
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jtomtom.JTomtomException;
import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.tools.HttpUtils;

/**
 * @author Frédéric Combes
 *
 * Manage radars POIs from Tomtomax (http://www.tomtomax.fr/)
 */
public final class Tomtomax extends RadarsConnector {
	private static final Logger LOGGER = Logger.getLogger(Tomtomax.class);
	
	private static final String TOMTOMAX_WEBSITE = "http://www.tomtomax.fr/";
	private static final String TOMTOMAX_DB_URL = TOMTOMAX_WEBSITE+"upload/tomtomax_radars.db";
	private static final String TOMTOMAX_LOGIN_URL = TOMTOMAX_WEBSITE+"forum/ucp.php?mode=login";
	
	private static final String TOMTOMAX_COOKIE_CONNECT = "phpbb3_e1wj8_u";
	
	public static final String TOMTOMAX_DB_FILE = "maxipoi_radars.db";
	
	private static final Locale TOMTOMAX_COUNTRY = Locale.FRANCE;
	
	private static final String TAG_BASIC = "[UZ1] ";
	private static final String TAG_PREMIUM = "[UZ3] ";
	private URL urlBasicUpdate;
	private URL urlPremiumUpdate;
	
	private POIsDbInfos mapLocalInformations;
	private POIsDbInfos poisRemoteInformations;
	
	private boolean connected = false;
	private Proxy proxy = Proxy.NO_PROXY;
	
	public final POIsDbInfos getRemoteDbInfos(Proxy proxy) {
		if (poisRemoteInformations != null)
			return poisRemoteInformations;
		
		HttpURLConnection conn = null;
		try {
			poisRemoteInformations = new POIsDbInfos();
			
			conn = HttpUtils.createDefaultConnection(TOMTOMAX_DB_URL, proxy);
	        conn.connect();
	        int httpResponseCode = conn.getResponseCode();
	        
			if (LOGGER.isDebugEnabled()) 
				LOGGER.debug("conn.getResponseCode() = "+httpResponseCode);
			
	        if (httpResponseCode == HttpURLConnection.HTTP_OK) {
	        	parseTomtomaxRemoteDbFile(conn);
	        }
	        
	        conn.disconnect();
	        
	        return poisRemoteInformations;
		} catch (IOException e) {
			throw new JTomtomException(e);
		} 
	        		
	}
	
	public final POIsDbInfos getLocalDbInfos(String m_path) {
		mapLocalInformations = new POIsDbInfos();
		
		// Get the directory of the current map
		File mapDirectory = new File(m_path);
		if (!mapDirectory.exists() || !mapDirectory.isDirectory() || !mapDirectory.canRead()) {
			throw new JTomtomException("org.jtomtom.errors.gps.map.notfound", m_path);
		}
		
		// Looking for the Tomtomax database file
		File ttMaxDbFile = new File(mapDirectory,TOMTOMAX_DB_FILE);
		if (!ttMaxDbFile.exists()) {
			LOGGER.info("POIs from "+toString()+" has never been installed !");
			mapLocalInformations.setDatabaseVersion(POIsDbInfos.NA);
			mapLocalInformations.setNumberOfPOIs(0);
			return mapLocalInformations;
		}
		
		// We read and parse the file
		if (ttMaxDbFile.exists() && ttMaxDbFile.canRead()) {
			parseTomtomaxLocalDbFile(ttMaxDbFile);	
		}
		
		return mapLocalInformations;
	}
	
	private void parseTomtomaxRemoteDbFile(URLConnection conn) {
    	InputStream is = null;
    	BufferedReader rd = null;
    	try {
			is = conn.getInputStream();
			rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = rd.readLine()) != null) {
				if (line.startsWith(TAG_DATE)) {
					poisRemoteInformations.setLastUpdateDate("dd/MM/yyyy", line.substring(TAG_DATE.length()).trim());
					if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_DATE+poisRemoteInformations.getLastUpdateDateForPrint());
					
				} else if (line.startsWith(TAG_VERSION)) {
					poisRemoteInformations.setDatabaseVersion(line.substring(TAG_VERSION.length()).trim());
					if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_VERSION+poisRemoteInformations.getDatabaseVersionForPrint());
					
				} else if (line.startsWith(TAG_RADARS)) {
					poisRemoteInformations.setNumberOfPOIs(Integer.parseInt(line.substring(TAG_RADARS.length()).trim()));
					if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_RADARS+poisRemoteInformations.getNumberOfPOIsForPrint());

				} else if (line.startsWith(TAG_BASIC)) {
					urlBasicUpdate = new URL(line.substring(TAG_BASIC.length()).trim());
					if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_BASIC+urlBasicUpdate);
					
				} else if (line.startsWith(TAG_PREMIUM)) {
					urlPremiumUpdate = new URL(line.substring(TAG_PREMIUM.length()).trim());
					if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_PREMIUM+urlPremiumUpdate);

				}
			}
			
    	} catch (IOException e) {
    		throw new JTomtomException(e);
			
		} finally {
    		try {is.close();} catch (Exception e){}
    		try {rd.close();} catch (Exception e){}
    	}
	}
	
	private void parseTomtomaxLocalDbFile(File ttLocalDbFile) {
		BufferedReader buff = null;
		try {
			buff = new BufferedReader(new FileReader(ttLocalDbFile));
			String line;
			while ((line = buff.readLine()) != null) {
				if (line.startsWith("date=")) {
					mapLocalInformations.setLastUpdateDate("dd/MM/yyyy", line.substring(5));
					if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_DATE+" = "+mapLocalInformations.getLastUpdateDateForPrint());
					
				} else if (line.startsWith("vers=")) {
					mapLocalInformations.setDatabaseVersion(line.substring(5));
					if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_VERSION+" = "+mapLocalInformations.getDatabaseVersionForPrint());
					
				} else if (line.startsWith("radar=")) {
					mapLocalInformations.setNumberOfPOIs(Integer.parseInt(line.substring(6)));
					if (LOGGER.isDebugEnabled()) LOGGER.debug(TAG_RADARS+" = "+mapLocalInformations.getNumberOfPOIsForPrint());
					
				} else if (line.startsWith("#####")) {
					break;
				}
			}
			
		} catch (FileNotFoundException e) {
			throw new JTomtomException(e);
			
		} catch (IOException e) {
			throw new JTomtomException(e);
			
		} finally {
			try {buff.close();}catch(Exception e){}
		}
	}
	
	public boolean connexion(Proxy p_proxy, String p_user, String p_password) {
		proxy = p_proxy;
		
		if (p_user == null || p_password == null ||
				p_user.isEmpty() || p_password.isEmpty()) {
			return connected;
		}
		
		List<HttpCookie> cookies = null;
		HttpURLConnection conn = null;
		try {
			String urlParameters = createTomtomaxLoginPostData(p_user, p_password);
			conn = HttpUtils.createConnectionWithPostData(TOMTOMAX_LOGIN_URL, urlParameters, proxy);
	        
	        conn.connect();
	        int httpResponseCode = conn.getResponseCode();
			if (LOGGER.isDebugEnabled()) 
				LOGGER.debug("conn.getResponseCode() = "+httpResponseCode);
			
	        if (httpResponseCode == HttpURLConnection.HTTP_OK) {
	        	cookies = HttpUtils.readCookieFromConnection(conn);
	        }
	        
			// Verify connexion cookie
			if (cookies != null && !cookies.isEmpty()) {
				for (HttpCookie cookie : cookies) {
					if (TOMTOMAX_COOKIE_CONNECT.equals(cookie.getName()) && !"1".equals(cookie.getValue())) {
						connected = true;
						break;
					}
				}
			}
		
			return connected;
			
		} catch (IOException e) {
			throw new JTomtomException(e);
			
		}
	}
	
	private final static String createTomtomaxLoginPostData(String user, String password) {
		try {
			StringBuffer urlParameters = new StringBuffer();
			urlParameters.append("mode=login&username=").append(URLEncoder.encode(user, "UTF-8"));
			urlParameters.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
			urlParameters.append("&login=Connexion");
			
			return urlParameters.toString();
		} catch (UnsupportedEncodingException e) {
			throw new JTomtomException(e);
		}
	}

	@Override
	public HttpURLConnection getConnectionForUpdate() {
		try {
			if (!connected) 
				throw new JTomtomException("You must connect before !");
	
			if (urlBasicUpdate == null) 
				getRemoteDbInfos(proxy);
		
			return HttpUtils.createDefaultConnection(urlBasicUpdate.toString(), proxy);
			
		} catch (IOException e) {
			throw new JTomtomException(e);
		}				
	}

	@Override
	public HttpURLConnection getConnectionForInstall() {
		try {
			if (!connected) 
				throw new JTomtomException("You must connect before !");
	
			if (urlPremiumUpdate == null) 
				getRemoteDbInfos(proxy);
		
			return HttpUtils.createDefaultConnection(urlPremiumUpdate.toString(), proxy);
			
		} catch (IOException e) {
			throw new JTomtomException(e);
		}				
	}
		
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+" ["+TOMTOMAX_COUNTRY.getCountry()+"]";
	}
	
	@Override
	public String getConnectorWebsite() {
		return TOMTOMAX_WEBSITE;
	}

	@Override
	public String getLocale() {
		return TOMTOMAX_COUNTRY.toString();
	}

}
