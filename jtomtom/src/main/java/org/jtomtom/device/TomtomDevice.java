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
package org.jtomtom.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sf.jcablib.CabFile;

import org.apache.log4j.Logger;
import org.jtomtom.JTomtomException;
import org.jtomtom.device.providers.FilesProviderFactory;
import org.jtomtom.device.providers.TomtomFilesProvider;
import org.jtomtom.tools.JTomTomUtils;

/**
 * @author Frédéric Combes
 *
 * Class for Tomtom GPS manipulation
 * 
 */
public class TomtomDevice {
	
	private static final Logger LOGGER = Logger.getLogger(TomtomDevice.class);
	private static final int MAX_EPHEM_FILE_NUMBER = 1;
	
	public TomtomFilesProvider theFiles;
	
	private String deviceUniqueID;
	private String deviceSerialNumber;
	private String name;
	private int applicationVersion;		// Navcore version
	private int systemVersion;
	private String processorVersion;	// GPS Processor version
	private int bootloaderVersion;

	private TomtomMap activeMap;
	private Map<String, TomtomMap> availableMaps;
	
	/**
	 * Quickfix informations
	 * 		this data are not loading at the beginning
	 */
	private Chipset chipset;
	private long quickFixExpiry;
	private long quickFixLastUpdate;

	/**
	 * Create Tomtom device by search the device in the mount points
	 */
	public TomtomDevice() {
		try {
			theFiles = FilesProviderFactory.getFilesProvider( TomtomDeviceFinder.findMountPoint() );
			
		} catch (FileNotFoundException e) {
			throw new JTomtomException("org.jtomtom.errors.gps.incorrectmountpoint", e);
		}
		
		loadInformationsFromBif();
	}
	
	/**
	 * Create Tomtom device with the given mount point
	 */
	public TomtomDevice(File p_moutPoint) {
		try {
			theFiles = FilesProviderFactory.getFilesProvider(p_moutPoint);
		} catch (FileNotFoundException e) {
			throw new JTomtomException("org.jtomtom.errors.gps.incorrectmountpoint", e);
		}
		
		loadInformationsFromBif();
	}
		
	/**
	 * Return the absolute path of the GPS root
	 * @return
	 */
	public String getMountPoint() {
		return theFiles.getRootDirectory().getAbsolutePath();
	}
	
	/**
	 * Read the GPS informations in the ttgo.bif file at the root directory
	 */
	public void loadInformationsFromBif() {
		
		Properties props = new Properties();
		try { 
			File ttgo = theFiles.getTomtomInformations();
			props.load(new FileInputStream(ttgo));
			
		} catch (FileNotFoundException e) {
			throw new JTomtomException(e);
		} catch (IOException e) {
			throw new JTomtomException(e);
		}
		
		name = props.getProperty("DeviceName");
		applicationVersion = Integer.parseInt(props.getProperty("ApplicationVersionVersionNumber"));
		bootloaderVersion = Integer.parseInt(props.getProperty("BootLoaderVersion"));
		deviceSerialNumber = props.getProperty("DeviceSerialNumber");
		deviceUniqueID = props.getProperty("DeviceUniqueID");
		processorVersion = props.getProperty("GPSFirmwareVersion");
		systemVersion = Integer.parseInt(props.getProperty("LinuxVersion"));
		
		// Some debug informations
		LOGGER.info("Loading of "+name);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("name = "+name);
			LOGGER.debug("applicationVersion = "+applicationVersion);
			LOGGER.debug("bootloaderVersion = "+bootloaderVersion);
			LOGGER.debug("deviceSerialNumber = "+deviceSerialNumber);
			LOGGER.debug("deviceUniqueID = "+deviceUniqueID);
			LOGGER.debug("ActiveMapName = "+getActiveMap().getName());
			LOGGER.debug("ActiveMmapVersion = "+getActiveMap().getVersion());
			LOGGER.debug("systemVersion = "+systemVersion);
		}
	}

	/**
	 * Read the currentmap.dat for finding the path of the active map in the given GPS
	 * @param gpsMountPoint	Root of the GPS device
	 * @return				Path of the active map
	 */
	private final String getCurrentMapPath() {
		RandomAccessFile raf = null;
		try {
			
			String activeMapPath = "";
			File currentMapFile = theFiles.getCurrentMapDat();
			
			raf = new RandomAccessFile(currentMapFile, "r");
			int pathLenght = Integer.reverseBytes(raf.readInt());
			activeMapPath = CabFile.readCString(raf);
			
			if (activeMapPath.length() != (pathLenght-1)) 
				throw new JTomtomException("File "+currentMapFile.getName()+" seems to be corrupted !");
			
			LOGGER.debug("activeMapPath = "+activeMapPath);
			
			return theFiles.getRootDirectory()+File.separator+(new File(activeMapPath)).getName();
		
		} catch (FileNotFoundException e) {
			LOGGER.warn("No active map found, assume the first map found is the current...");
			return getAvailableMaps().values().iterator().next().getPath();
			
		} catch (IOException e) {
			throw new JTomtomException(e);
			
		} finally {
			try {raf.close();} catch (Exception e) {};
		}
	}

	/**
	 * Find and return the Chipset type of the GPS
	 * @return
	 */
	public Chipset getChipset() {
		try {
			
			if (chipset == null) {
				Set<File> ephemFiles = theFiles.getEphemeridData();
				
				if (ephemFiles.isEmpty()) {
					throw new FileNotFoundException("No file found in ephem directory !");
					
				} else if (ephemFiles.size() > MAX_EPHEM_FILE_NUMBER) {
					chipset = Chipset.UNKNOWN;
					
				} else {
					File theEphemFile = ephemFiles.iterator().next();
					if (TomtomFilesProvider.FILE_GLOBAL_LOCATE.equalsIgnoreCase(theEphemFile.getName())) {
						chipset = Chipset.globalLocate;
						
					} else if (TomtomFilesProvider.FILE_SIRFSTAR_III.equalsIgnoreCase(theEphemFile.getName())) {
						chipset = Chipset.SiRFStarIII;
					}
				}
				
				if (chipset == null) chipset = Chipset.UNKNOWN;
			}
			
			return chipset;
			
		} catch (FileNotFoundException e) {
			throw new ChipsetNotFoundException(e);
		}
	}
	
	public void forceChipset(Chipset chipset) {
		this.chipset = chipset;
	}
	
	/**
	 * Find and return the last update date of the quickfix ephemeride on the GPS
	 * @return 	Timespamp of the date
	 */
	public long getQuickFixLastUpdate() {
		try {
			
			if (quickFixLastUpdate == 0) {
				Set<File> ephemFiles = theFiles.getEphemeridData();
				if (!ephemFiles.isEmpty()) {
					File ephemeride = theFiles.getEphemeridData().iterator().next();
					quickFixLastUpdate = ephemeride.lastModified();
				}
			}
			
			return quickFixLastUpdate;
			
		} catch (FileNotFoundException e) {
			return 0;
		}
	}
	
	/**
	 * Find and return the expirency date of the ephemeride informations on the GPS
	 * @return	Timespamp of the date
	 */
	public long getQuickFixExpiry() {
		BufferedReader buff = null;
		try {
			
			if (quickFixExpiry == 0) {
				
				File metaFile = theFiles.getEphemeridMeta();
				
				buff = new BufferedReader(new FileReader(metaFile));
				String line;
				while ((line = buff.readLine()) != null) {
					if (line.startsWith("Expiry=")) {
						quickFixExpiry = Long.parseLong(line.substring(7))*1000;
					}
				}
							
			} // end if (m_quickFixExpiry == 0)
			
			return quickFixExpiry;
			
		} catch (IOException e) { return 0;
			
		} finally {
			try {buff.close();}catch(Exception e){}
		}
	}
	
	/**
	 * Install downloaded QuickFix files inside the GPS
	 * Reset quickfix informations for the device
	 * @param ephemFiles		List of the files to install in the GPS
	 */
	public void updateQuickFix(Collection<File> ephemFiles) {
		String destDir = getMountPoint()+File.separator+"ephem"+File.separator;
		
		File ephemDir = new File(destDir);
		if (!ephemDir.exists()) ephemDir.mkdir();
		
		LOGGER.debug("Copy files in "+destDir);
		for (File current : ephemFiles) {
			File destination = new File(ephemDir, current.getName());
			
			if (!JTomTomUtils.move(current, destination, true))
				throw new JTomtomException("org.jtomtom.errors.gps.ephem.copyerror");
			
			LOGGER.debug("Copy of "+current.getName()+" ... OK");
		} // end for (File current : ephemFiles)
		
		LOGGER.debug("Reset quickfix informations");
		quickFixExpiry = 0;
		quickFixLastUpdate = 0;
	}
	
	/**
	 * Return the active map of the gps
	 * @return
	 */
	public final TomtomMap getActiveMap() {
		if (activeMap == null)
			activeMap = TomtomMap.createMapFromPath(getCurrentMapPath());
		
		return activeMap;
	}
	
	/**
	 * Return the list of all available map on the Tomtom Device
	 * @return	List of map order by map name
	 */
	public Map<String, TomtomMap> getAvailableMaps() {
		if (availableMaps == null) 
			listAvailableMaps();
		
		return availableMaps;
	}
	
	/**
	 * Read device directory and looking for map directory
	 * update avaiblableMaps with all find map.
	 */
	private final void listAvailableMaps() {
		availableMaps = new HashMap<String, TomtomMap>();
		
		for (File currentFile : theFiles.getRootDirectory().listFiles()) {
			if (TomtomMap.isMapDirectory(currentFile))
				availableMaps.put(currentFile.getName(), TomtomMap.createMapFromPath(currentFile.getAbsolutePath()));
		}

	}
	
	/**
	 * Delete Quickfix files
	 */
	public void resetQuickfixData() {
		theFiles.resetEphemeridData();
		LOGGER.debug("Reset quickfix informations");
		quickFixExpiry = 0;
		quickFixLastUpdate = 0;
	}

	public final String getName() {
		return name;
	}
	
	public final String getDeviceUniqueID() {
		return deviceUniqueID;
	}
	
	public final String getDeviceSerialNumber() {
		return deviceSerialNumber;
	}

	public final String getAppVersion() {
		if (applicationVersion != 0) {
			return Float.toString((float)applicationVersion/1000);
		}
		return Integer.toString(applicationVersion);
	}

	public final String getSystemVersion() {
		return Integer.toString(systemVersion);
	}

	public final String getProcessorVersion() {
		return processorVersion;
	}

	public final String getBootloaderVersion() {
		return Integer.toString(bootloaderVersion);
	}
}
