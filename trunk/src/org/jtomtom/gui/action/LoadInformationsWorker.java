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

import java.net.Proxy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.JTomtomException;
import org.jtomtom.JTomtomProperties;
import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.gui.TabRadars;
import org.jtomtom.tools.NetworkTester;

/**
 * @author Frédéric Combes
 * 
 * The Worker refresh Radars panel informations
 * Because a part of informations come from the remote POIs web site, the refresh can be long
 * 		and maybe can crash. So the informations was getting by this working outside the EDT
 *
 */
public class LoadInformationsWorker extends SwingWorker<ActionResult, Void> {
	private static final Logger LOGGER = Logger.getLogger(LoadInformationsWorker.class);
	
	private POIsDbInfos remoteRadarsInfos;
	private POIsDbInfos localRadarsInfos;
	
	private RadarsConnector radarConnector;
	private TomtomDevice theDevice;
	
	private TabRadars tabRadars;

	
	public LoadInformationsWorker(TabRadars tabRadars) {
		this.tabRadars = tabRadars;
		this.radarConnector = tabRadars.getSelectedRadarConnector();
	}
	
	public void setTomtomDevice(TomtomDevice theDevice) {
		this.theDevice = theDevice;
	}
	
	@Override
    public ActionResult doInBackground() {
		LOGGER.debug("Enter in LoadInformationsWorker.doInBackground ...");
        return loadInBackground();
    }

	@Override
	protected void done() {
		try {
			ActionResult infos = get();
			tabRadars.refreshDisplay(infos);
			
			LOGGER.debug("Execution of LoadInformationsWorker terminated.");
			
		} catch (InterruptedException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			
		} catch (ExecutionException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		}
	}
	
	/**
	 * Get necessary informations in background process
	 * @return	HTML string containing the informations to display in panel
	 */
	private ActionResult loadInBackground() {
		ActionResult result = new ActionResult();
		SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
		StringBuffer infos = new StringBuffer();
		
		Proxy applicationProxy = Application.getInstance().getProxyServer();
		JTomtomProperties applicationProperties = Application.getInstance().getGlobalProperties();
				
		if (remoteRadarsInfos == null) {
			if (!NetworkTester.getInstance().isNetworkAvailable(applicationProxy)) {
				remoteRadarsInfos = new POIsDbInfos();
			}
		} else {
			if (remoteRadarsInfos.isEmpty() && 
					NetworkTester.getInstance().isNetworkAvailable(applicationProxy)) {
				remoteRadarsInfos = null;
			}
		}
		
		if (remoteRadarsInfos == null) {
			// We try to connect before all
			boolean isConnected = radarConnector.connexion(
									applicationProxy, 
									applicationProperties.getUserProperty("org.connector.user."+radarConnector.getLocale()), 
									applicationProperties.getUserProperty("org.connector.password."+radarConnector.getLocale()));
			if (!isConnected) {
				result.exception = new JTomtomException("org.jtomtom.errors.radars.tomtomax.account");
				LOGGER.debug("Error during connexion to "+radarConnector.toString()+" for getting remote informations about POIs");
				result.status = false;
				return result;
			}
			remoteRadarsInfos = radarConnector.getRemoteDbInfos(applicationProxy);
			if (remoteRadarsInfos == null || remoteRadarsInfos.isEmpty()) {
				result.exception = new JTomtomException("org.jtomtom.errors.radars.tomtomax.getinfo");
				LOGGER.debug("Error during getting information about "+radarConnector.toString());
				result.status = false;
				return result;
			}
		}
		
		if (localRadarsInfos == null) {
			try {
				localRadarsInfos = radarConnector.getLocalDbInfos(theDevice.getActiveMap().getPath());
			} catch (JTomtomException e) {
				result.exception = e;
				result.status = false;
				return result;
			}
		}
		
		boolean isInstalled = !localRadarsInfos.isEmpty();

		infos.append("<html><table>");
		infos.append("<tr><td><strong>").append(TabRadars.getTabTranslations().getString("org.jtomtom.tab.radars.availableupdate")).append(" : </strong></td><td><i>")
				.append(remoteRadarsInfos.getLastUpdateDateForPrint(dateFormat.toPattern()))
				.append("</i></td></tr>");
		if (isInstalled) {
			infos.append("<tr><td><strong>").append(TabRadars.getTabTranslations().getString("org.jtomtom.tab.radars.installedupdate")).append(" : </strong></td><td><i>")
				.append(localRadarsInfos.getLastUpdateDateForPrint(dateFormat.toPattern()))
				.append("</i></td></tr>");
		} else {
			infos.append("<tr><td><strong>").append(TabRadars.getTabTranslations().getString("org.jtomtom.tab.radars.installedupdate")).append(" : </strong></td><td><i>")
				.append(TabRadars.getTabTranslations().getString("org.jtomtom.tab.radars.noversioninstalled")).append("</i></td></tr>");
		}
		infos.append("<tr><td><strong>").append(TabRadars.getTabTranslations().getString("org.jtomtom.tab.radars.radarcount")).append(" : </strong></td><td><i>")
			.append(localRadarsInfos.getNumberOfPOIsForPrint())
			.append("</i>");
		
		if (remoteRadarsInfos.getNumberOfPOIs() != null) {
			infos.append(" [")
				.append(remoteRadarsInfos.getNumberOfPOIs() - localRadarsInfos.getNumberOfPOIs())
				.append(TabRadars.getTabTranslations().getString("org.jtomtom.tab.radars.missingradar")).append("]");
		}
		
		infos.append("</td></tr></table>");
		infos.append("<br/><font size=\"2\"><p><i>").append(TabRadars.getTabTranslations().getString("org.jtomtom.tab.radars.radarprovidedby"))
			.append(" <a href=\"").append(radarConnector.getConnectorWebsite())
			.append("\">").append(radarConnector.toString())
			.append("</a></i></p></font>");
		infos.append("</html>");
		
		if (result.parameters == null) {
			result.parameters = new LinkedList<String>();
		}
		result.parameters.add(infos.toString());
		
		// - Finally we check POIs web site connexion in order to be sure user has an account
		try {NetworkTester.getInstance().validNetworkAvailability(applicationProxy);}
		catch (JTomtomException e) {
			result.status = false;
			result.exception = e;
			return result;
		}
		
		result.status = radarConnector.connexion(
				applicationProxy, 
				applicationProperties.getUserProperty("org.connector.user."+radarConnector.getLocale()), 
				applicationProperties.getUserProperty("org.connector.password."+radarConnector.getLocale()));
		if (!result.status) {
			result.exception = new JTomtomException("org.jtomtom.errors.radars.tomtomax.account");
			LOGGER.debug("POIs web site account error ...");
			return result;
		}
		
		result.status = true;
		return result;
	}

}
