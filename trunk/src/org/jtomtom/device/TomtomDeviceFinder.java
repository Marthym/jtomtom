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
package org.jtomtom.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jtomtom.JTomtomException;
import org.jtomtom.device.providers.CarminatFilesProvider;
import org.jtomtom.device.providers.TomtomFilesProvider;

/**
 * @author Frédéric Combes
 * 
 * Used for search the Tomtom GPS Device
 */
//TODO: Add timeout diring device search. When we search the good mount point, if one of them is an invalid network mount point jTomtom stay blocked
public class TomtomDeviceFinder {
	private static final Logger LOGGER = Logger.getLogger(TomtomDeviceFinder.class);
	
	private static final String LINUX_MOUNT_START = " on ";
	private static final String LINUX_MOUNT_END = " type ";
	
	/**
	 * Return the mount point of the first Tomtom GPS find
	 * @return					File pointing on the root directory of the GPS
	 */
	public static final File findMountPoint() {
		File[] systemMountPoints = getSystemMountPoints();
		
		for (File currentMountPoints : systemMountPoints) {
			if (isTomtomRootDirectory(currentMountPoints) || isCarminatRootDirectory(currentMountPoints)) {
				LOGGER.info("GPS detected : "+currentMountPoints.getAbsolutePath());
				return currentMountPoints;
			}
		}

		throw new JTomtomException("org.jtomtom.errors.gps.gpsnotfound");
	}

	/**
	 * Retrieve all the mount point of the operating system
	 * @return	List of File pointing on the different directories
	 */
	private static final File[] getSystemMountPoints() {
		File[] systemMountPoints;
		LOGGER.debug("os.name = "+System.getProperty("os.name"));
		if ("Linux".equals(System.getProperty("os.name"))) {
			systemMountPoints = getLinuxMountPoints();
		} else {
			systemMountPoints = getWindowsDrives();
		}
		return systemMountPoints;
	}
	
	/**
	 * Test if a directory is a Tomtom root directory
	 * @param directory	Directory to test
	 * @return
	 */
	private static final boolean isTomtomRootDirectory(File directory) {
		File ttgo = new File(directory, TomtomFilesProvider.FILE_TOMTOM_INFORMATIONS);
		return ttgo.exists() && ttgo.isFile() && ttgo.canRead();
	}
	
	/**
	 * Test if a directory is a Tomtom root directory
	 * @param directory	Directory to test
	 * @return
	 */
	private static final boolean isCarminatRootDirectory(File directory) {
		File ttgo = new File(directory, CarminatFilesProvider.DIR_CARMINAT_LOOPBACK+File.separator+CarminatFilesProvider.FILE_CARMINAT_LOOPBACK);
		return ttgo.exists() && ttgo.isFile() && ttgo.canRead();
	}
	
	/**
	 * Retrieve all mount point of a Linux based system from the "mount" command
	 * @return
	 */
	private static final File[] getLinuxMountPoints() {
		
		List<File> systemMountPoints = new ArrayList<File>();
		try {
			// On exécute la commande système qui liste les points de montages
			Process cmd = Runtime.getRuntime().exec("mount");
			BufferedReader br = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
			
			// Et on en extrait les répertoires de montage
			String line = new String();
			LOGGER.debug("Liste des points de montage :");
			while ((line = br.readLine()) != null) {
				if (line.startsWith("none")) {
					// On vire les périphériques non monté
					continue;
				} 
				
				String pathMountPoint = line.substring(
						line.indexOf(LINUX_MOUNT_START)+LINUX_MOUNT_START.length(), 
						line.lastIndexOf(LINUX_MOUNT_END));
				if (LOGGER.isDebugEnabled()) LOGGER.debug("\t"+pathMountPoint);
				
				systemMountPoints.add(new File(pathMountPoint));
			}
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		} 
		
		return (File[]) systemMountPoints.toArray(new File[systemMountPoints.size()]);
		
	}
	
	/**
	 * Retrieve all drives of a Windows based system
	 * @return
	 */
	private static final File[] getWindowsDrives(){
		return File.listRoots();
	}
	
}
