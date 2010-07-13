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
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

public final class JTomTomUtils {
	public static final Logger LOGGER = Logger.getLogger(JTomTomUtils.class);
	
	/**
	 * Copie un fichier dans un autre peut importe leur emplacement sur les disques physiques
	 * @param source		Fichier source
	 * @param destination	Fichier destination
	 * @param ecrase		True s'il faut écraser le fichier destination s'il existe
	 * @return				True si la copie est effectuée
	 */
	public static final boolean copier(File source, File destination, boolean ecrase) {
		if (!ecrase && destination.exists()) {
			LOGGER.error("Le fichier destination existe et il n'est pas parmis de l'écraser !");
			return false;
		}
		
		FileChannel in = null;
		FileChannel out = null;
		 
		try {
		  in = new FileInputStream(source).getChannel();
		  out = new FileOutputStream(destination).getChannel();
		 
		  // Copie depuis le in vers le out
		  in.transferTo(0, in.size(), out);
		  
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return false;
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return false;
			
		} finally { 
		  	try {in.close();} catch (Exception e) {}
		  	try {out.close();} catch (Exception e) {}
		}
		return true;
	} 
	
	/**
	 * Copie un fichier dans un autre peut importe leur emplacement sur les disques physiques
	 * @param source		Fichier source
	 * @param destination	Fichier destination
	 * @return				True si la copie est effectuée
	 */
	public static final boolean copier(File source, File destination) {
		return copier(source, destination, false);
	}
	
	/**
	 * Déplace un fichier dans un autre peut importe leur emplacement sur les disques physiques
	 * @param source		Fichier source
	 * @param destination	Fichier destination
	 * @param ecrase		True s'il faut écraser le fichier destination s'il existe
	 * @return				True si la copie est effectuée
	 */
	public static final boolean deplacer (File source, File destination, boolean ecrase) {
		boolean result = copier(source, destination, ecrase);
		
		// il faut en plus effacer la source
		result &= source.delete();
		
		return result;
	}
	
	/**
	 * Déplace un fichier dans un autre peut importe leur emplacement sur les disques physiques
	 * @param source		Fichier source
	 * @param destination	Fichier destination
	 * @return				True si la copie est effectuée
	 */
	public static final boolean deplacer (File source, File destination) {
		return deplacer(source, destination, false);
	}

	/**
	 * Instantiate the Radar connector class for the current Locale
	 * @return		An intance of RadarsConnector
	 * @throws JTomtomException
	 */
	public static final RadarsConnector instantiateRadarConnector() throws JTomtomException {
		return instantiateRadarConnector(Locale.getDefault());
	}
	
	/**
	 * Instantiate the Radar connector class for the specified Locale
	 * @param	Locale	Locale of the country represented by the map
	 * @return			An intance of RadarsConnector
	 * @throws JTomtomException
	 */
	public static final RadarsConnector instantiateRadarConnector(Locale localeSite) throws JTomtomException {
		Class<?> connector;
		RadarsConnector radars;
		try {
			LOGGER.debug("Create connector instante for Locale "+localeSite);
			connector = Class.forName(JTomtom.getApplicationPropertie("org.jtomtom.radars.connector."+localeSite));
			
			if (RadarsConnector.class.isAssignableFrom(connector)) {
				radars = (RadarsConnector) connector.newInstance();
			} else {
				throw new JTomtomException(new ClassCastException());
			}
			
		} catch (ClassNotFoundException e) {
			throw new JTomtomException(e);
			
		} catch (InstantiationException e) {
			throw new JTomtomException(e);
			
		} catch (IllegalAccessException e) {
			throw new JTomtomException(e);
		}
		
		return radars;
	}
	
	public static final RadarsConnector[] getAllRadarsConnectors() {
		//TODO Ca marche pas encore !!!
		// Little bit of introspection
		String packPath = "org/jtomtom/radars";
		List<RadarsConnector> result = new LinkedList<RadarsConnector>();
		try {
			Enumeration<URL> listConnector = RadarsConnector.class.getClassLoader().getResources(packPath);
			while (listConnector.hasMoreElements()) {
				URL connector = listConnector.nextElement();
				try {
					Class<?> connectorClass = Class.forName(connector.getPath().replace('/', '.'));
					
					if (RadarsConnector.class.isAssignableFrom(connectorClass)) {
						result.add((RadarsConnector) connectorClass.newInstance());
					} 
					
				} catch (ClassNotFoundException e) {
					
					
				} catch (InstantiationException e) {
					
					
				} catch (IllegalAccessException e) {
					
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
