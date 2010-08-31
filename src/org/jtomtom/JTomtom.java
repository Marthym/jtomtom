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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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
import org.jtomtom.gui.JTomtomFenetre;
import org.jtomtom.gui.action.CheckUpdateAction;
import org.jtomtom.gui.utilities.JarUtils;

/**
 * @author marthym
 *
 */
public class JTomtom {
	private static final Logger LOGGER = Logger.getLogger(JTomtom.class);
	
	private static GlobalPositioningSystem theGPS;
	private static Proxy m_proxy = null;
	
	/**
	 * Global application properties
	 */
	private static Properties m_props;
	
	/**
	 * User specific properties save in user home directory
	 */
	private static Properties m_userProps;

	private static String m_versionNumber = "0.x";
	private static String m_versionDate = (new java.util.Date()).toString();
	
	public static ResourceBundle theMainTranslator;

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
				changeLookAndFeel(m_props.getProperty("org.jtomtom.lookandfeel", UIManager.getSystemLookAndFeelClassName()));				
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
			theGPS = new GlobalPositioningSystem();
			
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
		if ("true".equals(m_props.getProperty("org.jtomtom.checkupdate"))) {
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

	public static final GlobalPositioningSystem getTheGPS() {
		return theGPS;
	}
	
	/**
	 * Récupère et retourne le proxy utilisé pour l'appli
	 * @return
	 */
	public static Proxy getApplicationProxy() {
		if (m_props == null) {
			loadProperties();
		}
		
		if (m_proxy == null) {
			if ("HTTP".equals(m_props.getProperty("net.proxy.type"))) {
				m_proxy = new Proxy(Type.HTTP, 
                        new InetSocketAddress(
                                        m_props.getProperty("net.proxy.name"), 
                                        Integer.parseInt(m_props.getProperty("net.proxy.port")) ));

			} else if ("SOCKS".equals(m_props.getProperty("net.proxy.type"))) {
				m_proxy = new Proxy(Type.SOCKS, 
                        new InetSocketAddress(
                                        m_props.getProperty("net.proxy.name"), 
                                        Integer.parseInt(m_props.getProperty("net.proxy.port")) ));
					
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
	//TODO : Déporter la gestion des propriétées dans une classe séparée
	public static void loadProperties() {
		LOGGER.info("Chargement des propriétés");
		
		// - Chargements des propriétes interne
		m_props = new Properties();
		try {
			m_props.load(JTomtom.class.getResourceAsStream(Constant.JTOMTOM_PROPERTIES));
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			SwingUtilities.invokeLater(new InitialErrorRun(e));
		}
		
		// - Chargement des propriétés utilisateur
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Répertoire utilisateur : "+System.getProperty("user.home"));
		
		File userPropertiesFile = new File(System.getProperty("user.home"), Constant.JTOMTOM_USER_PROPERTIES);
		m_userProps = new Properties();
		if (userPropertiesFile.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(userPropertiesFile);
				m_userProps.load(new FileInputStream(userPropertiesFile));
				
			} catch (IOException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
				SwingUtilities.invokeLater(new InitialErrorRun(e));
				
			} finally {
				try { fis.close(); } catch (Exception e) {}
			}
		}
		
		// - Mise à jour de la langue par défaut
		if (getApplicationPropertie("org.jtomtom.locale") != null) {
			try {
				String[] jttLocale = getApplicationPropertie("org.jtomtom.locale").split("_");
				Locale.setDefault(new Locale(jttLocale[0], jttLocale[1]));
			} catch (Exception e) {
				// On fait pas dans le détail si ça marche pas on touche à rien
				LOGGER.warn(e.getLocalizedMessage());
			}
		}
		theMainTranslator = ResourceBundle.getBundle("org.jtomtom.gui.resources.lang.jTomtom-main", Locale.getDefault());
		
		// - Mise à jour du niveau de log
		String myLevel = getApplicationPropertie("org.jtomtom.logLevel");
		Logger.getLogger(JTomtom.class.getPackage().getName()).setLevel(Level.toLevel( (myLevel==null)?"INFO":myLevel) );
		
		// Suppression des FileAppender potentiellement déjà ajouté
		Enumeration<?> enu = Logger.getLogger(JTomtom.class.getPackage().getName()).getAllAppenders();
		while (enu.hasMoreElements()) {
			Appender logApp = (Appender)enu.nextElement();
			if (FileAppender.class.isAssignableFrom(logApp.getClass())) {
				Logger.getLogger(JTomtom.class.getPackage().getName()).removeAppender(logApp);
			}
		}
		
		// Ajout du FileAppender avec le bon nom si besoin
		if (getApplicationPropertie("org.jtomtom.logFile") != null && !getApplicationPropertie("org.jtomtom.logFile").isEmpty()) {
			try {
				Logger.getLogger(JTomtom.class.getPackage().getName()).addAppender(
						new FileAppender(new PatternLayout("%d{ABSOLUTE} %5p %c{1}:%L %m%n"),  
						m_props.getProperty("org.jtomtom.logFile")));
			} catch (IOException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		} 
		
		// - Initialisation de la vérification des mises à jour
		if (getApplicationPropertie("org.jtomtom.checkupdate") == null) {
			m_userProps.setProperty("org.jtomtom.checkupdate", "true");
		}
		
		// - Ré-initialisation du proxy
		m_proxy = null;
	}

	/**
	 * Retourne les paramètres de l'application
	 * @param p_key	Clé de la propriété
	 * @return	Valeur de la propriété
	 */
	public static final String getApplicationPropertie(String p_key) {
		return m_userProps.getProperty(p_key, m_props.getProperty(p_key));
	}
	
	/**
	 * Return all properties wich start with p_key in a Map
	 * @param p_key	Prefix of properties you looking for
	 * @return		Map (key, value) of properties
	 */
	public static final Map<String, String> getApplicationProperties(String p_key) {
		Enumeration<?> keys = m_props.propertyNames(); // Valid because m_userProps is a subset of m_props
		Map<String, String> allProperties = new HashMap<String, String>();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			if (key.startsWith(p_key)) {
				allProperties.put(key, getApplicationPropertie(key));
			}
		}
		return allProperties;
	}
	
	/**
	 * Modifie les paramètres de l'application
	 * @param p_key		Clé de la propriété
	 * @param p_value	Nouvelle valeur de la propriété
	 */
	public static void setApplicationPropertie(String p_key, String p_value) {
		if (LOGGER.isDebugEnabled() && !m_props.getProperty(p_key, "").equals(p_value)) {
			LOGGER.debug(p_key+" = "+p_value);
		}

		m_props.setProperty(p_key, p_value);
	}
	
	/**
	 * Modifie les paramètres de l'application
	 * @param p_key		Clé de la propriété
	 * @param p_value	Nouvelle valeur de la propriété
	 */
	public static void setUserPropertie(String p_key, String p_value) {
		if (LOGGER.isDebugEnabled() && !m_userProps.getProperty(p_key, "").equals(p_value)) {
			LOGGER.debug(p_key+" = "+p_value);
		}

		m_userProps.setProperty(p_key, p_value);
	}
	
	/**
	 * Enregistrement des propriétés dans un fichier du répertoire utilisateur
	 * @return	TRUE si c'est ok
	 */
	public static boolean saveApplicationProperties() {
		File fichierProps = new File(System.getProperty("user.home"), Constant.JTOMTOM_USER_PROPERTIES);
		LOGGER.info("Enregistrement des propriétées dans "+fichierProps.getAbsoluteFile());
		try {
			// We store juste user properties, not application properties
			m_userProps.store(new FileOutputStream(fichierProps), "Propriétées utilisateur de JTomtom");
			
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			SwingUtilities.invokeLater(new InitialErrorRun(e));
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			SwingUtilities.invokeLater(new InitialErrorRun(e));
		} 
		return true;
	}
}
