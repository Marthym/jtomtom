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
//TODO : Move properties loading in JTomtomProperties class
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
		loadProperties();
		initVersionInformation();
	};
	
	public static Application getInstance() {
		if (instance == null) {
			LOGGER.debug("Create application instance");
			instance = new Application();
		}
		
		return instance;
	}

	public TomtomDevice getTheDevice() {
		if (theGPS == null) {
			initTomtomDevice();
		}
		return theGPS;
	}

	public Proxy getProxyServer() {
		if (globalProperties == null) {
			loadProperties();
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
	 * Charge les propriétées utilisateur depuis le fichier dans le home
	 */
	private void loadProperties() {
		LOGGER.info("Loading properties ...");
		
		// - Chargements des propriétes interne
		globalProperties = new JTomtomProperties();
		try {
			globalProperties.load(Constant.JTOMTOM_PROPERTIES, 
					System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES);
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			SwingUtilities.invokeLater(new InitialErrorRun(e));
		}
		
		// - Mise à jour de la langue par défaut
		if (globalProperties.getUserProperty("org.jtomtom.locale") != null) {
			try {
				String[] jttLocale = globalProperties.getUserProperty("org.jtomtom.locale").split("_");
				Locale.setDefault(new Locale(jttLocale[0], jttLocale[1]));
			} catch (Exception e) {
				// On fait pas dans le détail si ça marche pas on touche à rien
				LOGGER.warn(e.getLocalizedMessage());
			}
		}
		mainTranslator = ResourceBundle.getBundle("org.jtomtom.gui.resources.lang.jTomtom-main", Locale.getDefault());
		
		// - Mise à jour du niveau de log
		Logger.getLogger(JTomtom.class.getPackage().getName()).setLevel(Level.toLevel( globalProperties.getUserProperty("org.jtomtom.logLevel", "INFO") ));
		
		// Suppression des FileAppender potentiellement déjà ajouté
		Enumeration<?> enu = Logger.getLogger(JTomtom.class.getPackage().getName()).getAllAppenders();
		while (enu.hasMoreElements()) {
			Appender logApp = (Appender)enu.nextElement();
			if (FileAppender.class.isAssignableFrom(logApp.getClass())) {
				Logger.getLogger(JTomtom.class.getPackage().getName()).removeAppender(logApp);
			}
		}
		
		// Ajout du FileAppender avec le bon nom si besoin
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
		
		// - Initialisation de la vérification des mises à jour
		if (globalProperties.getUserProperty("org.jtomtom.checkupdate") == null) {
			globalProperties.setUserProperty("org.jtomtom.checkupdate", "true");
		}
		
		// - Ré-initialisation du proxy
		proxyServer = null;
	}

	private void initVersionInformation() {
		try {
			Manifest man = JarUtils.getCurrentJarFile().getManifest();
			if (man.getMainAttributes().getValue("Implementation-Version") != null)
				versionNumber = man.getMainAttributes().getValue("Implementation-Version");
			if (man.getMainAttributes().getValue("Built-Date") != null)
				versionDate = man.getMainAttributes().getValue("Built-Date");
			LOGGER.info("jTomtom v"+versionNumber+" du "+versionDate);
			
		} catch (Exception e) {
			LOGGER.debug("Error reading jTomtom version informations !");
		}
	}
	
	private void initTomtomDevice() {
		theGPS = new TomtomDevice();
	}
	
	public void reloadProperties() {
		loadProperties();
	}
	
	public static final String getUserAgent() {
		return "jTomtom / "+getInstance().getVersionNumber()+" ("+System.getProperty("java.version", "")+")";
	}

	/**
	 * Return the default radar connector create for the default Locale
	 * @return
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
