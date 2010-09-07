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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import net.sf.jcablib.CabFile;

import org.apache.log4j.Logger;
import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.connector.RadarsConnector;

/**
 * Class for map management
 * 
 * @author Frédéric Combes
 *
 */
public class TomtomMap {
	private static final Logger LOGGER = Logger.getLogger(TomtomMap.class);
		
	/**
	 * Link to the map containing GPS
	 */
	private TomtomDevice parent;
	
	/**
	 * Map name
	 */
	private String name;
	
	/**
	 * Full version of the map (major.minor)
	 */
	private String version;
	
	/**
	 * Absolute path of the map
	 */
	private String absolutePath;
	
	/**
	 * Radars informations
	 */
	private POIsDbInfos radarsInformations;
	
	/**
	 * FilenameFilter use for finding .pna file inside map directories
	 */
	private static final FilenameFilter PNA_FILE_FILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".pna");
		}
	};
	
	private TomtomMap() {
		name = "";
		version = "";
		absolutePath = "";
	}
	
	/**
	 * Create a map corresponding to the current map of GPS
	 * @param p_gps	GPS map containing
	 * @return		Current GPS map
	 * @throws JTomtomException
	 */
	public static TomtomMap createActiveMapOfTomtom(TomtomDevice p_gps) throws JTomtomException {
		
		String gpsMountPoint = p_gps.getMountPoint(false);
		
		String gpsActiveMapPath = getActiveMapPathFromDeviceRoot(gpsMountPoint);
				
		// Second, we retrieves the name and absolute path of the map
		File activeMapDirectory = new File(gpsActiveMapPath);	
		String path = gpsMountPoint+File.separator+activeMapDirectory.getName();
		try {
			return createMapFromPna(path).linkToGps(p_gps);
		} catch (Exception e) { return null;}
	}

	/**
	 * Read the currentmap.dat for finding the path of the active map in the given GPS
	 * @param gpsMountPoint	Root of the GPS device
	 * @return				Path of the active map
	 */
	public static String getActiveMapPathFromDeviceRoot(String gpsMountPoint) {
		File currentMapFile = getCurrentMapFile(gpsMountPoint);
		if (currentMapFile == null) return null;
		
		String activeMapPath = "";
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(currentMapFile, "r");
			int pathLenght = Integer.reverseBytes(raf.readInt());
			activeMapPath = CabFile.readCString(raf);
			if (activeMapPath.length() != (pathLenght-1)) {
				LOGGER.debug("File "+currentMapFile.getName()+" seems to be corrupted !");
			}
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return null;
			
		} finally {
			try {raf.close();} catch (Exception e) {};
		}
		LOGGER.debug("activeMapPath = "+activeMapPath);

		return activeMapPath;
	}

	/**
	 * Looking for the currentmap.dat file. This file is not case sensitive for
	 * 	Tomtom device but it is for Linux system.
	 * @param p_gps				GPS where looking for
	 * @return					File linking with currentmap.dat
	 * @throws JTomtomException
	 */
	private static File getCurrentMapFile(String p_gpsMountPoint) {
		File mountPoint = new File(p_gpsMountPoint);
		File[] datFiles = mountPoint.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".dat");
			}
		});
		
		File currentMapFile = null;
		for (File aDatFile : datFiles) {
			if ("currentmap.dat".equalsIgnoreCase(aDatFile.getName())) {
				currentMapFile = aDatFile;
				break;
			}
		}
		if (!currentMapFile.exists()) {
			LOGGER.error("File currentmap.dat not found in the GPS root !");
			return null;
		}
		return currentMapFile;
	}
	
	/**
	 * Returns the list of maps in a GPS
	 * @param p_gps	GPS to explore
	 * @return		Map list
	 * @throws JTomtomException
	 */
	public static List<TomtomMap> listAllGpsMap(TomtomDevice p_gps) throws JTomtomException {
		// So we will go through the first level of tree in search of a directory containing file .pna
		File gpsRoot = new File(p_gps.getMountPoint(false));
		String[] listRootFile = gpsRoot.list();
		List<TomtomMap> mapsList = new ArrayList<TomtomMap>();
		
		for (String currentFilePath : listRootFile) {
			File currentFile = new File(gpsRoot,currentFilePath);
			if (!currentFile.isDirectory()) continue;
			String[] pnaFileList = currentFile.list(PNA_FILE_FILTER);
			if (pnaFileList.length <= 0) continue;
			
			File pnaFile = new File(currentFile, pnaFileList[0]);
			if (pnaFile.exists() && pnaFile.canRead()) {
				// To save some memory if the card is current, we do not create a new object, we just add the link
				if (p_gps.getActiveMap() != null && p_gps.getActiveMap().getName().equals(currentFile.getName())) {
					mapsList.add(p_gps.getActiveMap());
				} else {
					mapsList.add(createMapFromPna(currentFile.getAbsolutePath()).linkToGps(p_gps));
				}
			}
		}
		
		return mapsList;
	}
	
	/**
	 * Create a map without GPS from the name and path of the map
	 * @param p_path	Absolute path of the map
	 * @return			Map not linked to a GPS
	 */
	private static TomtomMap createMapFromPna(String p_path) {
		if (p_path.isEmpty()) {
			return null;
		}
		
		// - We looking for pna file
		File mapDirectory = new File(p_path);
		String[] pnaFileList = mapDirectory.list(PNA_FILE_FILTER);
		if (pnaFileList.length <= 0) return null;
		
		File pnaFile = new File(mapDirectory, pnaFileList[0]);
		Scanner sc = null;
		String version = "";
		try {
			sc = new Scanner(pnaFile);
			sc.nextLine();					// ID file name
			sc.nextLine();					// Date
			version = sc.nextLine();		// Major version
			String build = sc.nextLine();	// build
			
			version = version.trim() +"."+ build.trim().split("=")[1];	// Yeah it's almost not dirty ;)
			
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			
		} catch (Exception e) {
			LOGGER.debug(e.getLocalizedMessage());
			return null;
			
		} finally {
			try {sc.close();} catch (Exception e){}
		}
		
		TomtomMap theMap = new TomtomMap();
		theMap.name = mapDirectory.getName();
		theMap.absolutePath = p_path;
		theMap.version = version;
		
		return theMap;
	}
	
	/**
	 * Link map to a GPS
	 * @param p_gps	GPS to link
	 * @return		The linked map
	 */
	private TomtomMap linkToGps(TomtomDevice p_gps) {
		// We verifies that it is still consistent
		String mp = "";
		try {
			mp = p_gps.getMountPoint(false);
		} catch (JTomtomException e) {}
		
		if (this.absolutePath.startsWith(mp)) {
			this.parent = p_gps;
		}
		return this;
	}
	
	/**
	 * Reading Radar informations
	 * @throws JTomtomException
	 */
	public void readRadarsInfos() throws JTomtomException {
		RadarsConnector radars = getDefaultRadarConnector();
		radarsInformations = radars.getLocalDbInfos(absolutePath);
	}
	
	/**
	 * Return the default radar connector create for the default Locale
	 * @return
	 */
	private static final RadarsConnector getDefaultRadarConnector() {
		String connectorClassName = JTomtom.theProperties.getApplicationProperty(
										RadarsConnector.RADARS_CONNECTOR_PROPERTIES+"."+Locale.getDefault());
		return RadarsConnector.createFromClass(connectorClassName);
	}
	
	/**
	 * Update radar database with a .ov2 files list
	 * @param files	Files need to be installed
	 * @return
	 * @throws JTomtomException
	 */
	public boolean updateRadars(List<File> files) throws JTomtomException {		
		// We search the directory of the map
		File mapDirectory = new File(absolutePath);
		if (!mapDirectory.exists()) {
			mapDirectory = new File(absolutePath.toLowerCase());
		}
		
		if (!mapDirectory.exists()) {
			throw new JTomtomException("org.jtomtom.errors.gps.map.directorynotfound");
		}
		
		if (!mapDirectory.canWrite()) {
			throw new JTomtomException("org.jtomtom.errors.gps.map.directoryreadonly", new String[]{mapDirectory.getAbsolutePath()});
		}
		
		// We move file inside the GPS
		for (File current : files) {
			File dest = new File(mapDirectory, current.getName());
			if (JTomTomUtils.copier(current, dest, true)) {
				LOGGER.debug(current.getName()+" copy done.");
			} else {
				throw new JTomtomException("org.jtomtom.errors.gps.radars.installfail", new String[]{current.getName()});
			}
		}
		
		// Update active map infos if needing
		if (name.equals(parent.getActiveMapName())) {
			readRadarsInfos();
		}
		
		return true;
	}
	
	/**
	 * Retur map name
	 * @return
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * Give the GPS linked to this map
	 * @return
	 */
	public final TomtomDevice getGPS() {
		return parent;
	}
	
	/**
	 * Give the map absolute path
	 * @return
	 */
	public final String getPath() {
		return absolutePath;
	}

	/**
	 * Give the map version
	 * @return
	 */
	public final String getVersion() {
		return version;
	}
	
	/**
	 * Give the installed Radar DB date
	 * @return
	 */
	public final Date getRadarsDbDate() {
		if (radarsInformations == null) {
			try { readRadarsInfos(); } catch (JTomtomException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
		return radarsInformations.getLastUpdateDate();
	}
	
	/**
	 * Give the installed Radar version date
	 * @return
	 */
	public final String getRadarsDbVersion() {
		if (radarsInformations == null) {
			try { readRadarsInfos(); } catch (JTomtomException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
		return radarsInformations.getDbVersion();
	}

	/**
	 * Give the installed radar count
	 * @return
	 */
	public final long getRadarsNombre() {
		if (radarsInformations == null) {
			try { readRadarsInfos(); } catch (JTomtomException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
		return radarsInformations.getPoisNumber();
	}

} 
