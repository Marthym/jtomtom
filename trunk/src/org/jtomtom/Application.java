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
package org.jtomtom;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.jar.Manifest;

import javax.swing.SwingUtilities;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.tools.JarUtils;


/**
 * @author Frédéric Combes
 *
 * Global Application properties management
 * 
 */
public class Application {
	private static final Logger LOGGER = Logger.getLogger(Application.class);
	
	private static Application instance;
	
	private TomtomDevice theGPS;
	private Proxy proxyServer = null;
	
	private String versionNumber = "0.x";
	private String versionDate = (new java.util.Date()).toString();
		
	private ResourceBundle mainTranslator;
	private JTomtomProperties globalProperties;
	
	private Application() {
		reloadProperties(); 
		initVersionInformation();
	};
	
	public static Application getInstance() {
		if (instance == null) {
			LOGGER.debug("Create application instance");
			instance = new Application();
		}
		
		return instance;
	}

	/**
	 * Reload properties files and dependant application parameters (translations and logs)
	 */
	public void reloadProperties() {
		initProperties();
		initTranslations();
		initLogger(); 
	}
	
	private void initProperties() {
		try {
			LOGGER.info("Loading properties ...");
			
			// - Load properties from the two properties files, internal and external
			globalProperties = new JTomtomProperties();
			globalProperties.load(Constant.JTOMTOM_PROPERTIES, 
					System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES);
			
			// - Reset the proxy setting
			proxyServer = null;
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			SwingUtilities.invokeLater(new InitialErrorRun(e));
		}
	}

	private void initTranslations() {
		LOGGER.debug("Initiate translations ...");

		if (globalProperties.getUserProperty("org.jtomtom.locale") != null) {
			try {
				String[] jttLocale = globalProperties.getUserProperty("org.jtomtom.locale").split("_");
				Locale.setDefault(new Locale(jttLocale[0], jttLocale[1]));
			} catch (Exception e) {
				// If error we don't do anything
				LOGGER.warn(e.getLocalizedMessage());
			}
		}
		mainTranslator = ResourceBundle.getBundle("org.jtomtom.gui.resources.lang.jTomtom-main", Locale.getDefault());
	}

	private void initLogger() {
		LOGGER.debug("Initiate logger ...");
		
		Logger.getLogger(JTomtom.class.getPackage().getName()).setLevel(Level.toLevel( globalProperties.getUserProperty("org.jtomtom.logLevel", "INFO") ));
		
		// - Delete existing appender before create new
		Enumeration<?> enu = Logger.getLogger(JTomtom.class.getPackage().getName()).getAllAppenders();
		while (enu.hasMoreElements()) {
			Appender logApp = (Appender)enu.nextElement();
			if (FileAppender.class.isAssignableFrom(logApp.getClass())) {
				Logger.getLogger(JTomtom.class.getPackage().getName()).removeAppender(logApp);
			}
		}
		
		// - Add the new FileAppender with good name
		if (globalProperties.getUserProperty("org.jtomtom.logFile") != null && !globalProperties.getUserProperty("org.jtomtom.logFile").isEmpty()) {
			try {
				Logger.getLogger(JTomtom.class.getPackage().getName()).addAppender(
						new FileAppender(new PatternLayout("%d{ABSOLUTE} %5p %c{1}:%L %m%n"),  
								globalProperties.getUserProperty("org.jtomtom.logFile")));
			} catch (IOException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
	}

	private void initVersionInformation() {
		try {
			LOGGER.debug("Initiate version informations ...");
			
			Manifest man = JarUtils.getCurrentJarFile().getManifest();
			if (man.getMainAttributes().getValue("Implementation-Version") != null)
				versionNumber = man.getMainAttributes().getValue("Implementation-Version");
			if (man.getMainAttributes().getValue("Built-Date") != null)
				versionDate = man.getMainAttributes().getValue("Built-Date");
			LOGGER.info("jTomtom v"+versionNumber+" on "+versionDate);
			
		} catch (Exception e) {
			LOGGER.debug("Error reading jTomtom version informations !");
		}
	}
	
	/**
	 * Return the GPS object of the application. 
	 * Initiate GPS if no GPS found
	 * @return TomtomDevice
	 */
	public TomtomDevice getTheDevice() {
		if (theGPS == null) {
			theGPS = new TomtomDevice();
		}
		return theGPS;
	}

	/**
	 * Return the proxy configuration of the application
	 * @return Proxy
	 */
	public Proxy getProxyServer() {
		if (globalProperties == null) {
			initProperties();
		}
		
		if (proxyServer == null) {
			String proxyName = globalProperties.getUserProperty("net.proxy.name");
			if (proxyName == null || proxyName.trim().isEmpty()) proxyName = "127.0.0.1";
			
			String proxyPort = globalProperties.getUserProperty("net.proxy.port");
			if (proxyPort == null || proxyPort.trim().isEmpty()) proxyPort = "3128";
			
			if ("HTTP".equals(globalProperties.getUserProperty("net.proxy.type"))) {
				proxyServer = new Proxy(Type.HTTP, 
                        new InetSocketAddress(
                        		proxyName, 
                                Integer.parseInt(proxyPort) ));

			} else if ("SOCKS".equals(globalProperties.getUserProperty("net.proxy.type"))) {
				proxyServer = new Proxy(Type.SOCKS, 
                        new InetSocketAddress(
                        		proxyName, 
                                Integer.parseInt(proxyPort) ));
					
			} else {
				proxyServer = Proxy.NO_PROXY;
			}
		} // end if (m_proxy == null)
			
		return proxyServer;
	}

	/**
	 * Give the HTTP_USER_AGENT for connections
	 * @return	User agent as "jTomtom / {version} ({java version})
	 */
	public static final String getUserAgent() {
		return "jTomtom / "+getInstance().getVersionNumber()+" ("+System.getProperty("java.version", "")+")";
	}

	/**
	 * Return the default radar connector create for the default Locale
	 * @return RadarsConnector
	 */
	public static final RadarsConnector getDefaultRadarConnector() {
		String connectorClassName = getInstance().globalProperties.getApplicationProperty(
										RadarsConnector.RADARS_CONNECTOR_PROPERTIES+"."+Locale.getDefault());
		return RadarsConnector.createFromClass(connectorClassName);
	}
	
	public final String getVersionNumber() {
		return versionNumber;
	}

	public final String getVersionDate() {
		return versionDate;
	}

	public final ResourceBundle getMainTranslator() {
		return mainTranslator;
	}

	public final JTomtomProperties getGlobalProperties() {
		return globalProperties;
	}

}
