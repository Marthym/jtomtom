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
package org.jtomtom.device;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jtomtom.Application;
import org.jtomtom.JTomtomException;
import org.jtomtom.tools.NetworkTester;

/**
 * @author Frédéric Combes
 *
 */
public enum Chipset {
	SiRFStarIII, 
	globalLocate,
	UNKNOWN;
	
	private static final Logger LOGGER = Logger.getLogger(Chipset.class);
	private static final String TOMTOM_INFORMATIONS_URL = "http://jtomtom.sourceforge.net/scripts/getGpsInfos.php";
	
	public static Chipset[] available() {
		Chipset[] allChipset = Chipset.values();
		return Arrays.copyOf(allChipset, allChipset.length -1);
	}
	
	/**
	 * Get the proconized Chipset found in the jTomtom Db
	 * @return
	 */
	public static Chipset getPreconizedChipset(String deviceSerialNumber) {
		if (!NetworkTester.getInstance().isNetworkAvailable(Application.getInstance().getProxyServer())) {
			throw new JTomtomException("org.jtomtom.errors.network.unavailable");
		}
		
		try {
			LOGGER.debug("Get proconized chipset on line for "+deviceSerialNumber);
			
			StringBuffer queryString = new StringBuffer("?");
			queryString.append("serial=").append(URLEncoder.encode(deviceSerialNumber, "UTF-8"));
			LOGGER.debug("queryString = "+queryString.toString());
			
			URL infosRequestURL = new URL(TOMTOM_INFORMATIONS_URL+queryString.toString());
			HttpURLConnection conn = (HttpURLConnection) infosRequestURL.openConnection(Application.getInstance().getProxyServer());
			conn.setRequestProperty("USER-AGENT", Application.getUserAgent() );
			int response = conn.getResponseCode();
			if (response != HttpURLConnection.HTTP_OK) {
				throw new HTTPException(response);
			}
			
			String jsonHeaderData = conn.getHeaderField("X-JSON");
			LOGGER.debug("Read JSON data : "+jsonHeaderData);
			JSONObject json = new JSONObject(jsonHeaderData);
			LOGGER.debug("Preconized chipset found : "+json.getString("CHIPSET"));
			
			return Chipset.valueOf(json.getString("CHIPSET"));
			
		} catch (IllegalArgumentException e) {
			// Case of serial was not found in Db
			LOGGER.debug(e.getLocalizedMessage());
			return UNKNOWN;
			
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			return UNKNOWN;
		}
	}
}
