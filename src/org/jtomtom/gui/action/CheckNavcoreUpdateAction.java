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
package org.jtomtom.gui.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.jtomtom.JTomtom;
import org.jtomtom.JTomtomException;
import org.jtomtom.tools.JarUtils;
import org.jtomtom.tools.NetworkTester;

/**
 * @author Frédéric Combes
 * 
 * Worker pour la vérification de nouvelles version de jTomtom
 */
public class CheckNavcoreUpdateAction extends SwingWorker<ActionResult, Void> {
	
	public static final String TOMTOM_DEVICE_LIST_URL = "http://www.tomtom.com/lib/img/cs/javascript/reset.js";
	public static final String DEVICESN_START = "devicesn=new Array(";
	public static final String DEVICENAME_START = "devicenames=new Array(";
	public static final String DEVICE_ARRAY_END = ");";
	
	private static final Logger LOGGER = Logger.getLogger(CheckNavcoreUpdateAction.class);

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected ActionResult doInBackground() throws Exception {
		
		ActionResult result = new ActionResult();
		
		result.status = false;
		if (!NetworkTester.getInstance().isNetworkAvailable(JTomtom.getApplicationProxy())) {
			result.parameters = new LinkedList<String>();
			result.parameters.add(JTomtom.theMainTranslator.getString("org.jtomtom.errors.network.unavailable"));
		}
		
		
		String message = checkUpdateNow();
		
		if (message != null) {
			result.status = true;
			result.parameters = new LinkedList<String>();
			result.parameters.add(message);
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		try {
			ActionResult result = get();
			if (result.status) {
				LinkedList<String> messages = (LinkedList<String>)result.parameters;
				JOptionPane.showMessageDialog(null, messages.getFirst(), 
						JTomtom.theMainTranslator.getString("org.jtomtom.main.action.checkupdate.updateavailable"), JOptionPane.INFORMATION_MESSAGE);
			}
			
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		}
	}
	
	public static final String checkUpdateNow() {
		LOGGER.debug("Begin to check new Navcore version");
		String message = null;
		
		Map<String, String> deviceMap = createDeviceMap();
		
		return message;
	}
	
	public static final Map<String, String> createDeviceMap() {
		try {
			String[] deviceSN = null;
			String[] deviceNames = null;
			Map<String, String> deviceMap = new HashMap<String, String>();
			
			URL resetJsUrl = new URL(TOMTOM_DEVICE_LIST_URL);
			HttpURLConnection conn = (HttpURLConnection)resetJsUrl.openConnection(JTomtom.getApplicationProxy());
			int response = conn.getResponseCode();
			if (response != HttpURLConnection.HTTP_OK) {
				throw new HTTPException(response);
			}
			
			InputStream is = conn.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = rd.readLine().trim()) != null) {
				if (line.startsWith(DEVICESN_START)) {
					int start = line.indexOf(DEVICESN_START)+DEVICESN_START.length();
					int end = line.indexOf(DEVICE_ARRAY_END, start);
					deviceSN = line.substring(start, end).replaceAll("\"", "").split(",");
				}
				if (line.startsWith(DEVICENAME_START)) {
					int start = line.indexOf(DEVICENAME_START)+DEVICENAME_START.length();
					int end = line.indexOf(DEVICE_ARRAY_END, start);
					deviceNames = line.substring(start, end).replaceAll("\"", "").split(",");
				}
				if (deviceSN != null && deviceNames != null) break;
			}
			
			for (int i = 0; i < deviceSN.length; i++) {
				deviceMap.put(deviceSN[i], deviceNames[i]);
			}
			
			return deviceMap;
			
		} catch (Exception e) {
			throw new JTomtomException("Enable to create device map !", e);
		}
	}

}
