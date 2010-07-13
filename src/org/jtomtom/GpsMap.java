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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.sf.jcablib.CabFile;

import org.apache.log4j.Logger;

/**
 * Class for map management
 * 
 * @author marthym
 *
 */
public class GpsMap {
	private static final Logger LOGGER = Logger.getLogger(GpsMap.class);
		
	/**
	 * Link to the map containing GPS
	 */
	private GlobalPositioningSystem m_gps;
	
	/**
	 * Map name
	 */
	private String m_name;
	
	/**
	 * Full version of the map (major.minor)
	 */
	private String m_version;
	
	/**
	 * Absolute path of the map
	 */
	private String m_path;
	
	/**
	 * Radars informations
	 */
	private Date m_radarsDbDate;
	private int m_radarsDbVersion;
	private int m_radarsNombre;
	
	private GpsMap() {
		m_name = "";
		m_version = "";
		m_path = "";
	}
	
	/**
	 * Create a map corresponding to the current map of GPS
	 * @param p_gps	GPS map containing
	 * @return		Current GPS map
	 * @throws JTomtomException
	 */
	public static GpsMap readCurrentMap(GlobalPositioningSystem p_gps) throws JTomtomException {
		
		// Now we must read root directory for found currentmap.dat
		// We dont know excatly the name because of case sensity
		File mountPoint = new File(p_gps.getMountedPoint(false));
		File[] datFiles = mountPoint.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".dat");
			}
		});
		
		File currentMapFile = null;
		for (int i = 0; i < datFiles.length; i++) {
			if ("currentmap.dat".equalsIgnoreCase(datFiles[i].getName())) {
				currentMapFile = datFiles[i];
				break;
			}
		}
		if (!currentMapFile.exists()) {
			LOGGER.error("File currentmap.dat not found in the GPS root !");
			return null;
		}
		
		// First, we reading the file currentmap.dat for find the map path
		String gpsMapPath = "";
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(currentMapFile, "r");
			int pathLenght = Integer.reverseBytes(raf.readInt());
			gpsMapPath = CabFile.readCString(raf);
			if (gpsMapPath.length() != (pathLenght-1)) {
				LOGGER.debug("File "+currentMapFile.getName()+" seems to be corrupted !");
			}
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return null;
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return null;
			
		} finally {
			try {raf.close();} catch (Exception e) {};
		}
		LOGGER.debug("gpsMapPath = "+gpsMapPath);
		
		// Second, we retrieves the name and absolute path of the map
		String[] cutpath = gpsMapPath.split("/");
		String name = "";
		String path = "";
		if (cutpath.length > 0) {
			name = cutpath[cutpath.length-1];
			path = p_gps.getMountedPoint(false)+File.separator+name;
		}
		
		try {
			return createMapFromPna(name, path).linkToGps(p_gps);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Returns the list of maps in a GPS
	 * @param p_gps	GPS to explore
	 * @return		Map list
	 * @throws JTomtomException
	 */
	public static List<GpsMap> listAllGpsMap(GlobalPositioningSystem p_gps) throws JTomtomException {
		// So we will go through the first level of tree in search of a directory containing file .pna
		File gpsRoot = new File(p_gps.getMountedPoint(false));
		String[] listRootFile = gpsRoot.list();
		List<GpsMap> mapsList = new ArrayList<GpsMap>();
		
		for (String currentFilePath : listRootFile) {
			File currentFile = new File(gpsRoot,currentFilePath);
			if (!currentFile.isDirectory()) continue;
			File pnaFile = new File(currentFile, currentFile.getName()+".pna");
			if (pnaFile.exists() && pnaFile.canRead()) {
				// To save some memory if the card is current, we do not create a new object, we just add the link
				if (p_gps.getActiveMap() != null && p_gps.getActiveMap().getName().equals(currentFile.getName())) {
					mapsList.add(p_gps.getActiveMap());
				} else {
					mapsList.add(createMapFromPna(currentFile.getName(), currentFile.getAbsolutePath()).linkToGps(p_gps));
				}
			}
		}
		
		return mapsList;
	}
	
	/**
	 * Create a map without GPS from the name and path of the map
	 * @param p_name	Map name
	 * @param p_path	Absolute path of the map
	 * @return			Map not linked to a GPS
	 */
	private static GpsMap createMapFromPna(String p_name, String p_path) {
		if (p_name.isEmpty() || p_path.isEmpty()) {
			return null;
		}
		
		File pnaFile = new File(p_path, p_name+".pna");	
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
		
		GpsMap theMap = new GpsMap();
		theMap.m_name = p_name;
		theMap.m_path = p_path;
		theMap.m_version = version;
		
		return theMap;
	}
	
	/**
	 * Link map to a GPS
	 * @param p_gps	GPS to link
	 * @return		The linked map
	 */
	private GpsMap linkToGps(GlobalPositioningSystem p_gps) {
		// We verifies that it is still consistent
		String mp = "";
		try {
			mp = p_gps.getMountedPoint(false);
		} catch (JTomtomException e) {}
		
		if (this.m_path.startsWith(mp)) {
			this.m_gps = p_gps;
		}
		return this;
	}
	
	/**
	 * Reading Radar informations
	 * @throws JTomtomException
	 */
	public void readRadarsInfos() throws JTomtomException {
		RadarsConnector radars = JTomTomUtils.instantiateRadarConnector();
		Map<String, String> infos = radars.getLocalDbInfos(m_path);
		try {
			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			m_radarsDbDate = formatter.parse(infos.get(RadarsConnector.TAG_DATE));
		} catch (ParseException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		}
		
		try { m_radarsDbVersion = Integer.parseInt(infos.get(RadarsConnector.TAG_VERSION));} 
		catch (NumberFormatException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		}
		
		try { m_radarsNombre = Integer.parseInt(infos.get(RadarsConnector.TAG_RADARS)); } 
		catch (NumberFormatException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		}

	}
	
	/**
	 * Update radar database with a .ov2 files list
	 * @param files	Files need to be installed
	 * @return
	 * @throws JTomtomException
	 */
	public boolean updateRadars(List<File> files) throws JTomtomException {		
		// We search the directory of the map
		File mapDirectory = new File(m_path);
		if (!mapDirectory.exists()) {
			mapDirectory = new File(m_path.toLowerCase());
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
		if (m_name.equals(m_gps.getActiveMapName())) {
			readRadarsInfos();
		}
		
		return true;
	}
	
	/**
	 * Retur map name
	 * @return
	 */
	public final String getName() {
		return m_name;
	}
	
	/**
	 * Give the GPS linked to this map
	 * @return
	 */
	public final GlobalPositioningSystem getGPS() {
		return m_gps;
	}
	
	/**
	 * Give the map absolute path
	 * @return
	 */
	public final String getPath() {
		return m_path;
	}

	/**
	 * Give the map version
	 * @return
	 */
	public final String getVersion() {
		return m_version;
	}
	
	/**
	 * Give the installed Radar DB date
	 * @return
	 */
	public final Date getRadarsDbDate() {
		if (m_radarsDbDate == null) {
			try { readRadarsInfos(); } catch (JTomtomException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
		return m_radarsDbDate;
	}
	
	/**
	 * Give the installed Radar version date
	 * @return
	 */
	public final int getRadarsDbVersion() {
		if (m_radarsDbVersion == 0) {
			try { readRadarsInfos(); } catch (JTomtomException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
		return m_radarsDbVersion;
	}

	/**
	 * Give the installed radar count
	 * @return
	 */
	public final int getRadarsNombre() {
		if (m_radarsNombre == 0) {
			try { readRadarsInfos(); } catch (JTomtomException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
		return m_radarsNombre;
	}

} 
