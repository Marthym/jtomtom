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
package org.jtomtom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Frédéric Combes
 *
 * Management class of jTomtom properties. Allows the separation between user properties and application properties.
 */
public class JTomtomProperties {
	private static final Logger LOGGER = Logger.getLogger(JTomtomProperties.class);
	
	private Properties applicationProperties;
	private Properties userProperties;
	
	public JTomtomProperties() {
		applicationProperties = new Properties();
		userProperties = new Properties();
	}
	
	/**
	 * Load in the same time application and user properties in two differents Properties object
	 * @param p_appPropsFile	Path of the application properties file in the classpath
	 * @param p_userPropsFile	Absolute path of the user properties file in the dd
	 * @throws IOException
	 */
	public void load(String p_appPropsFile, String p_userPropsFile) throws IOException {
		LOGGER.debug("Loading application properties from '"+p_appPropsFile+"'");
		applicationProperties.load(JTomtom.class.getResourceAsStream(p_appPropsFile));
		
		LOGGER.debug("Loading user properties from '"+p_userPropsFile+"'");
		File userPropertiesFile = new File(p_userPropsFile);
		if (userPropertiesFile.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(userPropertiesFile);
				userProperties.load(new FileInputStream(userPropertiesFile));
								
			} finally {
				try { fis.close(); } catch (Exception e) {}
			}
		} // end if (userPropertiesFile.exists())
	}
	
	/**
	 * True if there are no property
	 * @return
	 */
	public final boolean isEmpty() {
		return applicationProperties.isEmpty() && userProperties.isEmpty();
	}
	
	/**
	 * Return the property with de spécified key
	 * This function search just inside application properties.
	 * @param p_key	Key of the property
	 * @return		Valeu of the property
	 */
	public final String getApplicationProperty(String p_key) {
		return applicationProperties.getProperty(p_key);
	}
	
	/**
	 * Return all the properties of a category
	 * This function search just inside application properties.
	 * @param p_key	Categorie of the keys you looking for
	 * @return		A map which contain key/value from the specified catégorie
	 */
	public final Map<String, String> getApplicationProperties(String p_key) {
		Enumeration<?> keys = applicationProperties.propertyNames();
		Map<String, String> allProperties = new HashMap<String, String>();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			if (key.startsWith(p_key)) {
				allProperties.put(key, getApplicationProperty(key));
			}
		}
		return allProperties;
	}
	
	/**
	 * Return the property with de spécified key, if the key does not exist, return a default value
	 * This function search just inside application properties.
	 * @param p_key			Key of the property
	 * @param p_default		Value if the key does not exist
	 * @return				Value of the property or default value
	 */
	public final String getApplicationProperty(String p_key, String p_default) {
		return applicationProperties.getProperty(p_key, p_default);
	}
	
	/**
	 * Return the property with de specified key. 
	 * This function search inside user and application properties, user properties are prior
	 * @param p_key	Key of the property
	 * @return		Valeu of the property
	 */
	public final String getUserProperty(String p_key) {
		return userProperties.getProperty(p_key, applicationProperties.getProperty(p_key));
	}
	
	/**
	 * Return all the properties of a category
	 * This function search inside user and application properties, user properties are prior
	 * @param p_key	Categorie of the keys you looking for
	 * @return		A map which contain key/value from the specified catégorie
	 */
	public final Map<String, String> getUserProperties(String p_key) {
		Enumeration<?> keys = userProperties.propertyNames();
		Map<String, String> allProperties = new HashMap<String, String>();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			if (key.startsWith(p_key)) {
				allProperties.put(key, getUserProperty(key));
			}
		}
		
		return allProperties;
	}
	
	/**
	 * Return the property with de spécified key, if the key does not exist, return a default value
	 * This function search inside user and application properties, user properties are prior
	 * @param p_key			Key of the property
	 * @param p_default		Value if the key does not exist
	 * @return				Value of the property or default value
	 */
	public final String getUserProperty(String p_key, String p_default) {
		return userProperties.getProperty(p_key, applicationProperties.getProperty(p_key, p_default));
	}
	
	/**
	 * Set new value for user property only
	 * @param p_key		Key of the property
	 * @param p_value	New value of the property
	 */
	public void setUserProperty(String p_key, String p_value) {
		userProperties.setProperty(p_key, p_value);
	}
	
	/**
	 * Store user properties in a properties file. Application properties are not save.
	 * @param p_file					Properties file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void storeUserProperties(File p_file) throws FileNotFoundException, IOException {
		LOGGER.info("Save user properties in '"+p_file.getAbsoluteFile()+"'");
		userProperties.store(new FileOutputStream(p_file), "jTomtom user properties file");

	}
}
