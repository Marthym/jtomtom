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
package org.jtomtom.tools;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.JTomtomException;

public final class HttpUtils {
	public static final Logger LOGGER = Logger.getLogger(HttpUtils.class);

	public static final int TIMEOUT = 60000;	// 60s

	/**
	 * Transform connection cookies in HttpCookie and add them in a list
	 * @param conn	Connection which contain cookie after request be done
	 * @return		The list of HttpCookie
	 */
	public static final List<HttpCookie> readCookieFromConnection(URLConnection conn) {
		List<HttpCookie> cookies = new LinkedList<HttpCookie>();
		String headerName=null;
		for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) {
		 	if (headerName.equals("Set-Cookie")) {                  
		 		String cookie = conn.getHeaderField(i);
		 		cookie = cookie.substring(0, cookie.indexOf(";"));
		        String cookieName = cookie.substring(0, cookie.indexOf("="));
		        String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
		        if (!cookieValue.equals("deleted")) {
		        	cookies.add(new HttpCookie(cookieName, cookieValue));
		        	JTomTomUtils.LOGGER.debug(cookieName+" = "+cookieValue);
		        }
		 	}
		}
		return cookies;
	}

	public static final HttpURLConnection createConnectionWithPostData(String url, String postData, Proxy proxy) throws IOException {
		HttpURLConnection conn = HttpUtils.createDefaultConnection(url, proxy);
		addPostData(conn, postData);
		
		return conn;			
	}
	
	public static final HttpURLConnection addPostData(HttpURLConnection conn, String postData) 
	throws IOException {
		DataOutputStream wr = null;
		try {
			conn.setRequestMethod("POST");
	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        
	        wr = new DataOutputStream (conn.getOutputStream());
	        wr.writeBytes(postData);
	        wr.flush();
	        
		} finally {
			try {wr.close();} catch (Exception e){}
		}
		
		return conn;		
	}
		
	public static final HttpURLConnection createDefaultConnection(String url, Proxy proxy) throws IOException {
		try {
			if (LOGGER.isTraceEnabled()) LOGGER.trace("Create connection for "+url);
			
			URL tomtomaxUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) tomtomaxUrl.openConnection(proxy);
			
			conn.setRequestProperty ( "User-agent", Application.getUserAgent());
	        conn.setUseCaches(false);
	        conn.setReadTimeout(HttpUtils.TIMEOUT);
	        
	        return conn;
	        
		} catch (MalformedURLException e) {
			throw new JTomtomException(e);
		} 
	}	

}
