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
package org.jtomtom.tools;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

/**
 * Some tools for getting jar files from classpath
 * @author Frédéric Combes
 *
 */
public class JarUtils {
	private static final Logger LOGGER = Logger.getLogger(JarUtils.class);
	
	/**
	 * Give the JarFile object containing JarUtils.class
	 * @return JarFile if it's possible, null otherwise
	 */
	public static final JarFile getCurrentJarFile() {
		return getJarFileFromClass(JarUtils.class);
	}
	
	/**
	 * Give the JarFile object containing the class file of the specified Class
	 * @param p_class	Class to search
	 * @return JarFile if it's possible, null otherwise
	 */
	public static final JarFile getJarFileFromClass(Class<?> p_class) {
		JarFile jarfile = null;
		try {
			jarfile = new JarFile(getFileFromClass(p_class));
		} catch (Exception e) {
			LOGGER.warn(e.getLocalizedMessage());
		}
		return jarfile;

	}
	
	/**
	 * Give the File object containing JarUtils.class
	 * @return JarFile if it's possible, null otherwise
	 */
	public static final File getCurrentFile() {
		return getFileFromClass(JarUtils.class);
	}
	
	/**
	 * Give the File object containing the class file of the specified Class
	 * @param p_class	Class to search
	 * @return JarFile if it's possible, null otherwise
	 */
	public static final File getFileFromClass(Class<?> p_class) {
		LOGGER.debug("Recherche de "+p_class.getCanonicalName());
		String jarFileName = null;
		try {
			jarFileName = URLDecoder.decode(
					ClassLoader.getSystemResource(p_class.getCanonicalName().replace('.', '/')+".class").toString(), 
					"UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn(e.getLocalizedMessage());
			return null;
		}
		
		if (jarFileName.indexOf("!/") <= 0) {
			LOGGER.debug("URL anormale, sans doute en train de tester ?");
			return null;
		}
		
		jarFileName = jarFileName.substring(jarFileName.lastIndexOf(":")+1, jarFileName.indexOf("!/"));
		File jttJarFile = null;
		jttJarFile = new File(jarFileName);
		
		return jttJarFile;
	}
}
