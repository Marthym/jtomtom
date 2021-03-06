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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.JTomtomException;
import org.jtomtom.tools.JarUtils;
import org.jtomtom.tools.NetworkTester;

/**
 * @author Frédéric Combes
 * 
 * Worker for checking jTomtom new version disponibility
 */
public class CheckUpdateAction extends SwingWorker<ActionResult, Void> {
	
	public static final String REMOTE_JTOMTOM_URL = "http://downloads.sourceforge.net/project/jtomtom/jTomtom.jar";
	
	private static final Logger LOGGER = Logger.getLogger(CheckUpdateAction.class);
	private JLabel displayedMessage;

	public CheckUpdateAction(JLabel message) {
		super();
		this.displayedMessage = message;
	}
	
	@Override
	protected ActionResult doInBackground() throws Exception {
		
		ActionResult result = new ActionResult();
		
		result.status = false;
		if (!NetworkTester.getInstance().isNetworkAvailable(Application.getInstance().getProxyServer())) {
			result.parameters = new LinkedList<String>();
			result.parameters.add(new JTomtomException("org.jtomtom.errors.network.unavailable").getLocalizedMessage());
			return result;
		}
		
		
		String message = checkUpdateNow();
		
		if (message != null) {
			result.status = true;
			result.parameters = new LinkedList<String>();
			result.parameters.add(message);
		}
		
		return result;
	}

	@Override
	protected void done() {
		try {
			ActionResult result = get();
			LinkedList<String> messages = (LinkedList<String>)result.parameters;
			if (result.status) {
				displayedMessage.setText(messages.getFirst());
			} else {
				if (messages != null && !messages.isEmpty())
					LOGGER.warn(messages.getFirst());
			}
			
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		}
	}
	
	public static final String checkUpdateNow() {
		LOGGER.debug("Run to check new version availability ...");
		String message = null;
		
		URL jarUrl = null;
		try {
			LOGGER.debug("URL of the last version available : "+REMOTE_JTOMTOM_URL);
			jarUrl = new URL(REMOTE_JTOMTOM_URL);
			
		} catch (MalformedURLException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return null;
		}
		
		try {
			URLConnection conn = jarUrl.openConnection(
					Application.getInstance().getProxyServer());
			
			// Found jTomtom jar file
			File jttJarFile = JarUtils.getCurrentFile();
			if (jttJarFile == null || !jttJarFile.exists()) {
				LOGGER.warn("Jar file not found ! Check update canceled !");
				return null;
			}
			
			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
			String lastModifiedFormatedDate = dateFormat.format(new Date(conn.getLastModified()));
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Installed version : "+lastModifiedFormatedDate);
				LOGGER.debug("Last available version : "+lastModifiedFormatedDate);
			}
			
			if (conn.getLastModified()/1000 >= jttJarFile.lastModified()/1000) {
				message = Application.getInstance().getMainTranslator().getString("org.jtomtom.main.action.checkupdate.newversion")
							+lastModifiedFormatedDate;
			}
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		}
		
		if (message == null) {
			LOGGER.info("No new version found");
		} else {
			LOGGER.info(message);
		}
		
		return message;
	}

}
