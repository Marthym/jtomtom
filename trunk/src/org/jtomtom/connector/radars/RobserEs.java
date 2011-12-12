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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.Date;
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
 */
public class RobserEs extends RadarsConnector {
	private static final Logger LOGGER = Logger.getLogger(RobserEs.class);

	public  static final String PDISES_DATE_FORMAT = "yyyy-MM-dd";
	private static final String PDISES_WEBSITE = "http://www.robser.es/";
	private static final String PDISES_LOGIN_URL = PDISES_WEBSITE+"/foros/login.php?do=login";
	private static final String PDISES_INSTALL_URL = PDISES_WEBSITE+"paginas/descargas/terminos_packs.php";
	private static final String PDISES_DOWN_PACKS = PDISES_WEBSITE+"paginas/descargas/descargas_packs.php?nav=1";
	private static final String PDISES_WHATS_NEW_FILE = "ES_R_AA_Que_hay_de_nuevo_free.txt";
	
	private static final String PDISES_START_DATE = "<span class=\"textobase5\">Fecha de actualización:</span> <span class=\"textobase6\">";
	private static final String PDISES_START_FILEID = "href='terminos_packs.php?fich=";
	private static final String PDISES_END_FILEID = "' title=";
	
	private static final Locale PDISES_COUNTRY = new Locale("es", "ES");
	
	private List<HttpCookie> connexionCookies;
	private String downloadFileId = null;
	
	private Proxy proxy = Proxy.NO_PROXY;
	
	@Override
	public POIsDbInfos getLocalDbInfos(String m_path) {
		
		POIsDbInfos infos = new POIsDbInfos();
		// We search the directory of the current map
		File mapDirectory = new File(m_path);
		
		if (!mapDirectory.exists() || !mapDirectory.isDirectory() || !mapDirectory.canRead()) {
			throw new JTomtomException("org.jtomtom.errors.gps.map.notfound", m_path);
		}
		
		// We looking for the Pdis.es what's new file
		File pdisesWhatsNewFile = new File(mapDirectory, PDISES_WHATS_NEW_FILE);
		if (!pdisesWhatsNewFile.exists()) {
			LOGGER.info("POIs from "+toString()+" has never been installed !");
			infos.setDatabaseVersion(POIsDbInfos.NA);
			infos.setNumberOfPOIs(0);
			return infos;
		}
		
		infos.setLastUpdateDate(new Date(pdisesWhatsNewFile.lastModified()));
		
		// Add dummy value
		infos.setDatabaseVersion(Long.toString(infos.getLastUpdateDate().getTime()));
		infos.setNumberOfPOIs(0);
		
		return infos;
	}

	/**
	 * This class parses the download page on pdis.es to find the date of the last update. 
	 * This is the only solution since no file will list information on packages.
	 */
	@Override
	public POIsDbInfos getRemoteDbInfos(Proxy proxy) {
		POIsDbInfos infos = new POIsDbInfos();
		
		if (connexionCookies == null) {
			LOGGER.error("You are not connected !!");
			return infos;
		}
		
		// Initiate connexion
		HttpURLConnection conn = initDownloadConnection(PDISES_DOWN_PACKS);
		
		// Test connexion response code
		int connResponse = -1;
		try {
			connResponse = conn.getResponseCode();
		} catch (IOException e) {
			return infos;
		}
		
		if (connResponse != HttpURLConnection.HTTP_OK) {
			return infos;
		}
		
		infos.setLastUpdateDate(PDISES_DATE_FORMAT, parseLastUpdateDate(conn));
		infos.setDatabaseVersion(Long.toString(infos.getLastUpdateDate().getTime()));
		infos.setNumberOfPOIs(0);
		
		return infos;
	}
	
	private String parseLastUpdateDate(HttpURLConnection conn) {
		String updateDate = null;
		InputStream is = null;
		BufferedReader rd = null;
		String contentEncoding = conn.getContentEncoding();
		if (contentEncoding == null) contentEncoding = "ISO-8859-1";
		try {
			is = conn.getInputStream();
			rd = new BufferedReader(new InputStreamReader(is, contentEncoding));
			String line;

			while((line = rd.readLine()) != null) {
				String trimedLine = line.trim();
				int start = trimedLine.indexOf(PDISES_START_DATE);
				if (start >= 0) {
					updateDate = trimedLine.substring(start+PDISES_START_DATE.length()).replaceAll("\\<.*?>","").replaceAll("\\&.*?;","").trim();
					continue;
				}
				start = trimedLine.indexOf(PDISES_START_FILEID);
				if (start >= 0) {
					downloadFileId = trimedLine.substring(start+PDISES_START_FILEID.length(), trimedLine.indexOf(PDISES_END_FILEID));
					continue;
				}
				if (updateDate != null && downloadFileId != null) break;
			}
			
		} catch (IOException e) {
			LOGGER.warn(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) LOGGER.debug(e);
			
		} finally {
			try {is.close();} catch (Exception e){};
			try {rd.close();} catch (Exception e){};
		}
		
		return updateDate;
	}

	@Override
	public boolean connexion(Proxy p_proxy, String p_user, String p_password) {
		proxy = p_proxy;
		boolean isConnected = false;
		
		if (p_user == null || p_password == null ||
				p_user.isEmpty() || p_password.isEmpty()) {
			return false;
		}
		
		try {
			String urlParameters = createPdisdotesLoginPostData(p_user, p_password);
			HttpURLConnection conn = HttpUtils.createConnectionWithPostData(PDISES_LOGIN_URL, urlParameters, proxy);			
	        conn.setInstanceFollowRedirects(false);
	        
	        conn.connect();
	        int httpResponseCode = conn.getResponseCode();
	        if (httpResponseCode == HttpURLConnection.HTTP_OK ||
	        		httpResponseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
	        	
	        	connexionCookies = HttpUtils.readCookieFromConnection(conn);
	        }
	        conn.disconnect();
	        
	        for (HttpCookie myCookie : connexionCookies) {
	        	if ("PHPSESSID".equals(myCookie.getName())) {
	        		isConnected = true;
	        	}
	        }
			return isConnected;
			
		} catch (IOException e) {
			throw new JTomtomException(e);
		}
	}
	
	private final static String createPdisdotesLoginPostData(String user, String password) {
		try {
			StringBuffer urlParameters = new StringBuffer();
			urlParameters.append("do=login&vb_login_username=").append(URLEncoder.encode(user, "UTF-8"));
			urlParameters.append("&vb_login_password=").append(URLEncoder.encode(password, "UTF-8"));
			
			return urlParameters.toString();
			
		} catch (UnsupportedEncodingException e) {
			throw new JTomtomException(e);
		}
	}

	private final String createCookieString() {
        StringBuffer cookiesString = new StringBuffer();
        for (HttpCookie cookie : connexionCookies) {
        	if (cookiesString.length() > 0) cookiesString.append(";");
        	cookiesString.append(cookie.getName()).append("=").append(cookie.getValue());
        }
        
        return cookiesString.toString();
	}
	
	@Override
	public HttpURLConnection getConnectionForUpdate() {
		if (downloadFileId == null) {
			getRemoteDbInfos(proxy);
		}

		return initDownloadConnection(PDISES_INSTALL_URL);
	}
	
	@Override
	public HttpURLConnection getConnectionForInstall() {
		if (downloadFileId == null) {
			getRemoteDbInfos(proxy);
		}

		return initDownloadConnection(PDISES_INSTALL_URL);
	}
	
	private HttpURLConnection initDownloadConnection(String fileUrl) {
		if (connexionCookies == null) {
			throw new JTomtomException("You must connect before !!");
		}
		
		try {
			HttpURLConnection conn = HttpUtils.createDefaultConnection(fileUrl+"?fich="+downloadFileId, proxy);
	        conn.setRequestProperty ("Cookie", createCookieString());
	        conn.setInstanceFollowRedirects(false);
	        
	        String postData = "fich="+downloadFileId+"&fichero="+downloadFileId+"&aceptar=aceptar";
	        HttpUtils.addPostData(conn, postData);
	        
			return conn;
			
		} catch (MalformedURLException e) {
			throw new JTomtomException(e);
			
		} catch (IOException e) {
			throw new JTomtomException(e);
			
		}
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+" ["+PDISES_COUNTRY.getCountry()+"]";
	}

	@Override
	public String getConnectorWebsite() {
		return PDISES_WEBSITE;
	}

	@Override
	public String getLocale() {
		return PDISES_COUNTRY.toString();
	}

}
