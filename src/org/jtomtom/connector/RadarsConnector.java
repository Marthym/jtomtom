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
package org.jtomtom.connector;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.connector.radars.DummyRadarsConnector;
import org.jtomtom.tools.JTomTomUtils;

/**
 * Abstract Class with factory for connect different radars database site 
 * @author Frédéric Combes
 *
 */
public abstract class RadarsConnector {
	private static final Logger LOGGER = Logger.getLogger(RadarsConnector.class);
	
	/**
	 * Tags in the file to identify the different information
	 */
	public static final String TAG_DATE = "[DAT] ";
	public static final String TAG_VERSION = "[VER] ";
	public static final String TAG_RADARS = "[RAD] ";
	
	public static final String RADARS_CONNECTOR_PROPERTIES = "org.jtomtom.radars.connector";
	
	/**
	 * Empty Radar connector used when no connector was find
	 */
	public static final RadarsConnector EMPTY_RADAR_CONNECTOR = new DummyRadarsConnector();

	protected RadarsConnector() {}
	
	/**
	 * Create RadarConnector from the Class name of the connector
	 * @param connectorClassName	Class name of the connector
	 * @return	RadarConnector
	 */
	public static final RadarsConnector createFromClass(String connectorClassName) {
		if (connectorClassName == null || connectorClassName.isEmpty())
			return EMPTY_RADAR_CONNECTOR;
		
		Class<?> connectorClass = null;
		try {
			connectorClass = (Class<?>) Class.forName(connectorClassName);
			
		} catch (ClassNotFoundException e) {
			LOGGER.error(e.getLocalizedMessage());
			return EMPTY_RADAR_CONNECTOR;
		}
		
		return createFromClass(connectorClass);
	}
	
	/**
	 * Create RadarConnector from the Class of the connector
	 * @param connectorClass	Class of the connector
	 * @return RadarConnector
	 */
	public static final RadarsConnector createFromClass(Class<?> connectorClass) {
		RadarsConnector radars = null;
		try {
			radars = (RadarsConnector) connectorClass.newInstance();
			
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) LOGGER.debug(e);
			return EMPTY_RADAR_CONNECTOR;
		} 

		return radars;
	}

	/**
	 * Get the radars informations from the GPS connected
	 * @param	String Path of the map
	 * @return	HashMap with informations
	 */
	public abstract POIsDbInfos getLocalDbInfos(String m_path);
	
	/**
	 * Get the radars information from the remote site database
	 * @param	Proxy to use for connexion
	 * @return	HashMap with informations
	 */
	public abstract POIsDbInfos getRemoteDbInfos(Proxy proxy);

	/**
	 * Connect to the remote web site if necessary
	 * @param p_proxy		Proxy to use for connexion
	 * @param p_user		User login
	 * @param p_password	password login
	 * @return				True if the connexion is established
	 */
	public abstract boolean connexion(Proxy p_proxy, String p_user, String p_password);
	
	
	/**
	 * Return the connection needed for download update radars pack
	 * @return
	 */
	public abstract HttpURLConnection getConnectionForUpdate();
	
	/**
	 * Return the connection needed for download installation radars pack
	 * @return
	 */	
	public abstract HttpURLConnection getConnectionForInstall();

	/**
	 * Get an array of all radarconnector declared in properties file
	 * @return	Array of RadarsConnector
	 */
	public static final RadarsConnector[] getAllRadarsConnectors() {
		Application theApp = Application.getInstance();
		
		Map<String, String> connectorList = theApp.getGlobalProperties().getApplicationProperties(RADARS_CONNECTOR_PROPERTIES);
		RadarsConnector[] result = new RadarsConnector[connectorList.size()+1];
		result[0] = EMPTY_RADAR_CONNECTOR;
		int i = 1;
		
		Iterator<String> it = connectorList.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
	
			Class<?> connector = null;
			try {
				connector = Class.forName(connectorList.get(key));
			} catch (ClassNotFoundException e) {
				JTomTomUtils.LOGGER.debug(e.getLocalizedMessage());
				continue;
			}
			
			try {
				if (RadarsConnector.class.isAssignableFrom(connector)) {
					result[i++] = (RadarsConnector) connector.newInstance();
				}
			} catch (Exception e) {
				JTomTomUtils.LOGGER.debug(e.getLocalizedMessage());
				continue;
			}
		}
		
		return result;
	}
	
}
