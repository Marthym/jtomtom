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
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.gui.JTomtomFenetre;
import org.jtomtom.gui.action.CheckUpdateAction;
import org.jtomtom.gui.utilities.JarUtils;

/**
 * @author marthym
 *
 */
public class JTomtom {
	private static final Logger LOGGER = Logger.getLogger(JTomtom.class);
	
	private static TomtomDevice theGPS;
	private static Proxy m_proxy = null;
		
	private static String m_versionNumber = "0.x";
	private static String m_versionDate = (new java.util.Date()).toString();
	
	public static ResourceBundle theMainTranslator;

	/**
	 * Global application properties
	 */
	public static JTomtomProperties theProperties;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Initialisation des logs
		PropertyConfigurator.configure(JTomtom.class.getResource(Constant.LOGGER_PROPERTIES));
				
		// Chargement des propriétés
		loadProperties();
		
		// Récupération des informations de version
		try {
			Manifest man = JarUtils.getCurrentJarFile().getManifest();
			if (man.getMainAttributes().getValue("Implementation-Version") != null)
				m_versionNumber = man.getMainAttributes().getValue("Implementation-Version");
			if (man.getMainAttributes().getValue("Built-Date") != null)
				m_versionDate = man.getMainAttributes().getValue("Built-Date");
			LOGGER.info("jTomtom v"+m_versionNumber+" du "+m_versionDate);
		} catch (Exception e) {
			LOGGER.debug("Erreur de récupération de version !");
		}
		
		// License
		LOGGER.warn("jTomtom  Copyright (C) 2010  Frédéric Combes");
		LOGGER.warn("This program comes with ABSOLUTELY NO WARRANTY.");
		LOGGER.warn("This is free software, and you are welcome to redistribute it");
		LOGGER.warn("under certain conditions.");
		
		// En premier, on initialise le thème histoire que les messages d'erreur en profitent
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				// Changement du Look And Feel
				changeLookAndFeel(theProperties.getUserProperty("org.jtomtom.lookandfeel", UIManager.getSystemLookAndFeelClassName()));				
			}
		});
		
		// Test de la version de java
		String version = System.getProperty("java.version", "0.0");
		String[] javaVersion = version.split("\\.");
		if (Integer.parseInt(javaVersion[0]) < 1) {
			SwingUtilities.invokeLater(new InitialErrorRun(
					new JTomtomException("Version de Java ("+version+") non compatible ! 1.6 minimum !")));
			return;
		} else if (Integer.parseInt(javaVersion[0]) == 1 && Integer.parseInt(javaVersion[1]) < 6) {
			SwingUtilities.invokeLater(new InitialErrorRun(
					new JTomtomException("Version de Java ("+version+") non compatible ! 1.6 minimum !")));
			return;
		}
		
		// Le premier truc à faire c'est initialiser le GPS
		try {
			theGPS = new TomtomDevice();
			
		} catch (JTomtomException e) {
			// Si on peut même pas faire ça c'est pas la peine de se faire chier
			SwingUtilities.invokeLater(new InitialErrorRun(e));
			
			return;
		}
		
		// Ensuite, on crée et on affiche l'interface
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				JTomtomFenetre fenetre = new JTomtomFenetre();
				fenetre.setVisible(true);
			}
		});

		// On vérifie les mises à jour si nécessaire
		if ("true".equals(theProperties.getUserProperty("org.jtomtom.checkupdate"))) {
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					CheckUpdateAction check = new CheckUpdateAction();
					check.execute();
				}
			});
		}
		
	}
	
	/**
	 * Initialise le thème de l'application
	 * @param p_lafName	Nom du thème à mettre
	 */
	private static void changeLookAndFeel(String p_lafName) {
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if (p_lafName.equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {}
	}

	public static final TomtomDevice getTheGPS() {
		return theGPS;
	}
	
	/**
	 * Récupère et retourne le proxy utilisé pour l'appli
	 * @return
	 */
	public static Proxy getApplicationProxy() {
		if (theProperties == null) {
			loadProperties();
		}
		
		if (m_proxy == null) {
			if ("HTTP".equals(theProperties.getUserProperty("net.proxy.type"))) {
				m_proxy = new Proxy(Type.HTTP, 
                        new InetSocketAddress(
                                        theProperties.getUserProperty("net.proxy.name"), 
                                        Integer.parseInt(theProperties.getUserProperty("net.proxy.port")) ));

			} else if ("SOCKS".equals(theProperties.getUserProperty("net.proxy.type"))) {
				m_proxy = new Proxy(Type.SOCKS, 
                        new InetSocketAddress(
                                        theProperties.getUserProperty("net.proxy.name"), 
                                        Integer.parseInt(theProperties.getUserProperty("net.proxy.port")) ));
					
			} else {
				m_proxy = Proxy.NO_PROXY;
			}
		} // end if (m_proxy == null)
			
		return m_proxy;
	}
	
	/**
	 * Fournit le numéro de version de jTomtom lu dans le manifest
	 * @return	Numero de version/build
	 */
	public static final String getApplicationVersionNumber() {
		return m_versionNumber;
	}
	
	/**
	 * Fournit la date de compilation de jTomtom lu dans le manifest
	 * @return	Date du build
	 */
	public static final String getApplicationVersionDate() {
		return m_versionDate;
	}
	
	/**
	 * Charge les propriétées utilisateur depuis le fichier dans le home
	 */
	public static void loadProperties() {
		LOGGER.info("Loading properties ...");
		
		// - Chargements des propriétes interne
		theProperties = new JTomtomProperties();
		try {
			theProperties.load(Constant.JTOMTOM_PROPERTIES, 
					System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES);
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			SwingUtilities.invokeLater(new InitialErrorRun(e));
		}
				
		// - Mise à jour de la langue par défaut
		if (theProperties.getUserProperty("org.jtomtom.locale") != null) {
			try {
				String[] jttLocale = theProperties.getUserProperty("org.jtomtom.locale").split("_");
				Locale.setDefault(new Locale(jttLocale[0], jttLocale[1]));
			} catch (Exception e) {
				// On fait pas dans le détail si ça marche pas on touche à rien
				LOGGER.warn(e.getLocalizedMessage());
			}
		}
		theMainTranslator = ResourceBundle.getBundle("org.jtomtom.gui.resources.lang.jTomtom-main", Locale.getDefault());
		
		// - Mise à jour du niveau de log
		Logger.getLogger(JTomtom.class.getPackage().getName()).setLevel(Level.toLevel( theProperties.getUserProperty("org.jtomtom.logLevel", "INFO") ));
		
		// Suppression des FileAppender potentiellement déjà ajouté
		Enumeration<?> enu = Logger.getLogger(JTomtom.class.getPackage().getName()).getAllAppenders();
		while (enu.hasMoreElements()) {
			Appender logApp = (Appender)enu.nextElement();
			if (FileAppender.class.isAssignableFrom(logApp.getClass())) {
				Logger.getLogger(JTomtom.class.getPackage().getName()).removeAppender(logApp);
			}
		}
		
		// Ajout du FileAppender avec le bon nom si besoin
		if (theProperties.getUserProperty("org.jtomtom.logFile") != null && !theProperties.getUserProperty("org.jtomtom.logFile").isEmpty()) {
			try {
				Logger.getLogger(JTomtom.class.getPackage().getName()).addAppender(
						new FileAppender(new PatternLayout("%d{ABSOLUTE} %5p %c{1}:%L %m%n"),  
								theProperties.getUserProperty("org.jtomtom.logFile")));
			} catch (IOException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		} 
		
		// - Initialisation de la vérification des mises à jour
		if (theProperties.getUserProperty("org.jtomtom.checkupdate") == null) {
			theProperties.setUserProperty("org.jtomtom.checkupdate", "true");
		}
		
		// - Ré-initialisation du proxy
		m_proxy = null;
	}
	
}
