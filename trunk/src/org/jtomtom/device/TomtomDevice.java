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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.jcablib.CabFile;

import org.apache.log4j.Logger;
import org.jtomtom.JTomTomUtils;
import org.jtomtom.JTomtomException;

/**
 * @author Frédéric Combes
 *
 * Class for Tomtom GPS manipulation
 * 
 */
public class TomtomDevice {
	
	private static final Logger LOGGER = Logger.getLogger(TomtomDevice.class);
	
	public TomtomFilesProvider theFiles;
	
	private String serialNumber;
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
	private String chipset;
	private long quickFixExpiry;
	private long quickFixLastUpdate;

	/**
	 * Create Tomtom device by search the device in the mount points
	 * @throws JTomtomException 
	 */
	public TomtomDevice() throws JTomtomException {
		try {
			theFiles = 
				new TomtomFilesProvider( TomtomDeviceFinder.findMountPoint() );
		} catch (FileNotFoundException e) {
			throw new JTomtomException("org.jtomtom.errors.gps.incorrectmountpoint", e);
		}
		
		loadInformationsFromBif();
	}
	
	/**
	 * Create Tomtom device with the given mount point
	 * @throws JTomtomException 
	 */
	public TomtomDevice(File p_moutPoint) throws JTomtomException {
		try {
			theFiles = new TomtomFilesProvider(p_moutPoint);
		} catch (FileNotFoundException e) {
			throw new JTomtomException("org.jtomtom.errors.gps.incorrectmountpoint", e);
		}
		
		loadInformationsFromBif();
	}
	
	/**
	 * Version NO_ERROR du constructeur juste pour les tests
	 * ne pas utiliser
	 * @param p_init
	 */
	@Deprecated
	public TomtomDevice(boolean p_init) {
		if (p_init) {
			try { loadInformationsFromBif(); } catch (JTomtomException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
	}
	
	/**
	 * Détecte le point de montage ou le lecteur qui correspond au TomTom
	 * La détection se base sur l'existence ou non du fichier ttgo.bif
	 * @param p_forceRefresh	Force la recherche du point de montage si true,
	 * 							sinon on ne fait que refournir le paramètre qu'on a déjà recherché
	 * @return					Chemin du TomTom ou chaine vide si erreur
	 * @throws JTomtomException 
	 */
	public String getMountPoint() {
		return theFiles.getRootDirectory().getAbsolutePath();
	}
	
	/**
	 * Read the GPS informations in the ttgo.bif file at the root directory
	 * @throws JTomtomException
	 */
	public void loadInformationsFromBif() throws JTomtomException {
		
		Properties props = new Properties();
		try { 
			File ttgo = theFiles.getTomtomInformations();
			props.load(new FileInputStream(ttgo));
			
		} catch (Exception e) {
			throw new JTomtomException(e);
		}
		
		name = props.getProperty("DeviceName");
		applicationVersion = Integer.parseInt(props.getProperty("ApplicationVersionVersionNumber"));
		bootloaderVersion = Integer.parseInt(props.getProperty("BootLoaderVersion"));
		serialNumber = props.getProperty("DeviceUniqueID");
		processorVersion = props.getProperty("GPSFirmwareVersion");
		systemVersion = Integer.parseInt(props.getProperty("LinuxVersion"));
		
		// Un petit coup de trace
		LOGGER.info("Chargement du "+name);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("name = "+name);
			LOGGER.debug("applicationVersion = "+applicationVersion);
			LOGGER.debug("bootloaderVersion = "+bootloaderVersion);
			LOGGER.debug("serialNumber = "+serialNumber);
			LOGGER.debug("ActiveMapName = "+getActiveMap().getName());
			LOGGER.debug("ActiveMmapVersion = "+getActiveMap().getVersion());
			LOGGER.debug("systemVersion = "+systemVersion);
		}
	}

	/**
	 * Read the currentmap.dat for finding the path of the active map in the given GPS
	 * @param gpsMountPoint	Root of the GPS device
	 * @return				Path of the active map
	 * @throws JTomtomException 
	 */
	private final String readCurrentMapPath() throws JTomtomException {

		String activeMapPath = "";
		RandomAccessFile raf = null;
		try {
			
			File currentMapFile = theFiles.getCurrentMapDat();
			
			raf = new RandomAccessFile(currentMapFile, "r");
			int pathLenght = Integer.reverseBytes(raf.readInt());
			activeMapPath = CabFile.readCString(raf);
			
			if (activeMapPath.length() != (pathLenght-1)) {
				LOGGER.debug("File "+currentMapFile.getName()+" seems to be corrupted !");
			}
			
		} catch (Exception e) { throw new JTomtomException(e);
		} finally {
			try {raf.close();} catch (Exception e) {};
		}
		LOGGER.debug("activeMapPath = "+activeMapPath);
	
		// TODO : To be refactor, that's bullshit
		return theFiles.getRootDirectory()+File.separator+(new File(activeMapPath)).getName();
	}
	
	/**
	 * Récupère et renvoie le chipset de la puce GPS en fonctoin des fichiers ephemeride
	 * @return
	 * @throws JTomtomException 
	 */
	public String getChipset() throws JTomtomException {
		if (chipset == null) {
			File ephemeride = null;
			try {
				ephemeride = theFiles.getEphemeridData();
			} catch (FileNotFoundException e) {
				throw new JTomtomException(e);
			}
			
			if (TomtomFilesProvider.FILE_GLOBAL_LOCATE.equals(ephemeride.getName())) {
				chipset = "globalLocate";
				quickFixLastUpdate = ephemeride.lastModified();
			}
			
			if (TomtomFilesProvider.FILE_SIRFSTAR_III.equals(ephemeride.getName())) {
				chipset = "SiRFStarIII";
				quickFixLastUpdate = ephemeride.lastModified();
			}
			
			if (chipset == null)
				throw new JTomtomException("org.jtomtom.errors.gps.unknownchipset");
		}
		
		return chipset;
	}
	
	/**
	 * Récupère et retourne la date de la dernière mise à jour du QF
	 * @return 	Timespamp de la date
	 */
	public long getQuickFixLastUpdate() {
		if (quickFixLastUpdate == 0) {
			try {
				getChipset();
			} catch (JTomtomException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
		
		return quickFixLastUpdate;
	}
	
	/**
	 * Récupère et retourne la date d'expiration de l'éphéméride installé
	 * @return	Timespamp de la date
	 */
	public long getQuickFixExpiry() {
		if (quickFixExpiry == 0) {
			BufferedReader buff = null;
			try {
				File metaFile = theFiles.getEphemeridMeta();
				
				buff = new BufferedReader(new FileReader(metaFile));
				String line;
				while ((line = buff.readLine()) != null) {
					if (line.startsWith("Expiry=")) {
						quickFixExpiry = Long.parseLong(line.substring(7))*1000;
					}
				}
				
			} catch (FileNotFoundException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
				
			} catch (IOException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
				
			} finally {
				try {buff.close();}catch(Exception e){}
			}
				
		} // end if (m_quickFixExpiry == 0)
		
		return quickFixExpiry;
	}
	
	/**
	 * Installe les fichiers QuickFix téléchargés dans le GPS
	 * Ré-initialise les informations relatives au QuickFix
	 * @param ephemFiles		Liste des fichiers à installé pour le QF
	 * @return					True si l'installation s'est bien passé
	 * @throws JTomtomException	Si la copie des fichiers à échouée un exception est levé
	 */
	public boolean updateQuickFix(List<File> ephemFiles) throws JTomtomException {
		String destDir = getMountPoint()+File.separator+"ephem"+File.separator;
		
		LOGGER.debug("Copie des fichiers dans "+destDir);
		for (File current : ephemFiles) {
			File destination = new File(destDir+current.getName());
			if (!JTomTomUtils.deplacer(current, destination, true)) {
				throw new JTomtomException("org.jtomtom.errors.gps.ephem.copyerror");
			} else {
				LOGGER.debug("Copie de "+current.getName()+" ... OK");
			}
		} // end for (File current : ephemFiles)
		
		// Ré-initialisation des informations QF
		LOGGER.debug("Ré-initialisation des champs QF.");
		quickFixExpiry = 0;
		quickFixLastUpdate = 0;
		chipset = null;
		
		return true;
	}
		
	public final String getName() {
		return name;
	}
	
	public final String getSerialNumber() {
		return serialNumber;
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
	
	public final TomtomMap getActiveMap() {
		if (activeMap == null)
			try {
				activeMap = TomtomMap.createMapFromPath(readCurrentMapPath());
			} catch (JTomtomException e) {}
		
		return activeMap;
	}
	
	public Map<String, TomtomMap> getAvailableMaps() throws JTomtomException {
		if (availableMaps == null) {
			availableMaps = new HashMap<String, TomtomMap>();
			for (TomtomMap map : TomtomMap.listAllGpsMap(this)) {
				availableMaps.put(map.getName(), map);
			}
		}
		return availableMaps;
	}

}
