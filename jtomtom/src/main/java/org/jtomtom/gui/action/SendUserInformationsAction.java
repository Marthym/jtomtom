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
package org.jtomtom.gui.action;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.Constant;
import org.jtomtom.JTomtomException;
import org.jtomtom.JTomtomProperties;
import org.jtomtom.device.Chipset;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.tools.NetworkTester;

/**
 * @author Frédéric Combes
 * 
 * Worker for sendback user informations to jTomtom server 
 */
public class SendUserInformationsAction extends SwingWorker<Void, Void> {
	
	public static final String TOMTOM_SENDBACK_INFORMATIONS_URL = "http://jtomtom.sourceforge.net/scripts/insertGpsInfos.php";
	
	private static final Logger LOGGER = Logger.getLogger(SendUserInformationsAction.class);
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		
		if (!NetworkTester.getInstance().isNetworkAvailable(Application.getInstance().getProxyServer())) {
			throw new JTomtomException("org.jtomtom.errors.network.unavailable");
		}
		
		try {
			
			final TomtomDevice theDevice = Application.getInstance().getTheDevice();
			boolean isChipsetAlreadyInDatabase = Chipset.getPreconizedChipset(theDevice.getDeviceSerialNumber().substring(0, 2)) != Chipset.UNKNOWN;
			if (!isChipsetAlreadyInDatabase && isUserAgreeToSendInformations()) {
				sendInformationsNow();
			}
		
		} finally {
			disableSendbackProperty();
		}
		
		return null;
		
	}

	private final boolean isUserAgreeToSendInformations() {
		ResourceBundle theTranslator = Application.getInstance().getMainTranslator();
		int answer = 
			JOptionPane.showConfirmDialog(
				    null,
				    theTranslator.getString("org.jtomtom.main.action.sendback.askforpermission"),
				    theTranslator.getString("org.jtomtom.main.action.sendback.title"),
				    JOptionPane.YES_NO_OPTION
			);
		if (answer == JOptionPane.YES_OPTION) {
			return true;
		}
		
		return false;
	}

	public static final void sendInformationsNow() {
		final TomtomDevice theDevice = Application.getInstance().getTheDevice(); 
		try {
			StringBuffer queryString = new StringBuffer("?");
			queryString.append("gps_sn=").append(URLEncoder.encode(theDevice.getDeviceSerialNumber().substring(0, 2), "UTF-8"));
			queryString.append("&chipset=").append(URLEncoder.encode(theDevice.getChipset().toString(), "UTF-8"));
			LOGGER.debug("queryString = "+queryString.toString());
			
			URL sendbackInfosURL = new URL(TOMTOM_SENDBACK_INFORMATIONS_URL+queryString.toString());
			HttpURLConnection conn = (HttpURLConnection) sendbackInfosURL.openConnection(Application.getInstance().getProxyServer());
			conn.setRequestProperty("USER-AGENT", Application.getUserAgent() );
			int response = conn.getResponseCode();
			if (response != HttpURLConnection.HTTP_OK) {
				throw new HTTPException(response);
			}
			
		} catch (Exception e) {
			LOGGER.warn(e.getLocalizedMessage());
		}
	}
	
	private static final void disableSendbackProperty() {
		try {
			JTomtomProperties globalProperties = Application.getInstance().getGlobalProperties();
			
			globalProperties.setUserProperty("org.jtomtom.sendbackinformations", "false");
			globalProperties.storeUserProperties(
					new File(System.getProperty("user.home"), Constant.JTOMTOM_USER_PROPERTIES));
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
		}
	}
	
}
