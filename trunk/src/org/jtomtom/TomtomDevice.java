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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jtomtom.gui.utilities.TomtomDeviceFinder;

/**
 * @author Frédéric Combes
 *
 * Class for Tomtom GPS manipulation
 * 
 */
public class TomtomDevice {
	
	private static final Logger LOGGER = Logger.getLogger(TomtomDevice.class);
	
	/**
	 * Mount point or drive of the device
	 */
	private File mountPoint;
	
	/**
	 * Serial Number of the device
	 */
	private String serialNumber;
	
	/**
	 * Device name
	 */
	private String name;
	
	/**
	 * NavCore application version
	 */
	private int applicationVersion;
	
	/**
	 * Operating System version
	 */
	private int systemVersion;
	
	/**
	 * GPS processor version
	 */
	private String processorVersion;
	
	/**
	 * BootLoader version number
	 */
	private int bootloaderVersion;
	
	/**
	 * Current active map
	 */
	private TomtomMap activeMap;
	
	/**
	 * Available maps on device
	 */
	private Map<String, TomtomMap> availableMaps;
	
	/**
	 * Quickfix informations
	 * 		this data are not loading at the beginning
	 */
	private String chipset;
	private long quickFixExpiry;
	private long quickFixLastUpdate;

	/**
	 * Constructeur initialisant les variables membre lues dans le TT connecté
	 * @throws JTomtomException 
	 */
	public TomtomDevice() throws JTomtomException {
		loadInformationsFromBif();
	}
	
	public TomtomDevice(String p_moutPoint) throws JTomtomException {
		mountPoint = new File(p_moutPoint);
		if (mountPoint.exists() && mountPoint.canRead())
			loadInformationsFromBif();
		
		throw new JTomtomException("org.jtomtom.errors.gps.incorrectmountpoint");
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
	public String getMountPoint(boolean p_forceRefresh) throws JTomtomException {
		if (p_forceRefresh || mountPoint == null)
			mountPoint = TomtomDeviceFinder.findMountPoint();
		
		return mountPoint.getAbsolutePath();
	}
	
	/**
	 * Read the GPS informations in the ttgo.bif file at the root directory
	 * @throws JTomtomException
	 */
	public void loadInformationsFromBif() throws JTomtomException {
		verifyMountPoint();
		
		File ttgo = new File(mountPoint, TomtomDeviceFinder.TOMTOM_INFORMATION_FILE);
		Properties props = new Properties();
		
		try { props.load(new FileInputStream(ttgo)); } 
		catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return;
		}
		
		name = props.getProperty("DeviceName");
		applicationVersion = Integer.parseInt(props.getProperty("ApplicationVersionVersionNumber"));
		bootloaderVersion = Integer.parseInt(props.getProperty("BootLoaderVersion"));
		serialNumber = props.getProperty("DeviceUniqueID");
		processorVersion = props.getProperty("GPSFirmwareVersion");
		systemVersion = Integer.parseInt(props.getProperty("LinuxVersion"));
		
		activeMap = TomtomMap.createActiveMapOfTomtom(this);
		
		// Un petit coup de trace
		LOGGER.info("Chargement du "+name);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("name = "+name);
			LOGGER.debug("applicationVersion = "+applicationVersion);
			LOGGER.debug("bootloaderVersion = "+bootloaderVersion);
			LOGGER.debug("serialNumber = "+serialNumber);
			LOGGER.debug("ActiveMapName = "+getActiveMapName());
			LOGGER.debug("ActiveMmapVersion = "+getActiveMapVersion());
			LOGGER.debug("systemVersion = "+systemVersion);
		}
	}

	/**
	 * Verify if the device has a correct mount point and looking for it if it is not the case
	 * @throws JTomtomException
	 */
	private void verifyMountPoint() throws JTomtomException {
		if (mountPoint == null)
			getMountPoint(false);
	}

////
// LISTE DES GETTERS 
//
	
	/**
	 * Récupère et renvoie le chipset de la puce GPS en fonctoin des fichiers ephemeride
	 * @return
	 * @throws JTomtomException 
	 */
	public String getChipset() throws JTomtomException {
		if (chipset == null) {
			String ephemDir = mountPoint+File.separator+"ephem";
			File ephemFile = new File(ephemDir+File.separator+"lto.dat");
			if (ephemFile.exists()) {
				chipset = "globalLocate";
				quickFixLastUpdate = ephemFile.lastModified();
			}
			
			ephemFile = new File(ephemDir+File.separator+"packedephemeris.ee");
			if (ephemFile.exists()) {
				chipset = "SiRFStarIII";
				quickFixLastUpdate = ephemFile.lastModified();
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
			String ephemDir = mountPoint+File.separator+"ephem";
			File metaFile = new File(ephemDir+File.separator+"ee_meta.txt");
			if (metaFile.exists() && metaFile.canRead()) {
				BufferedReader buff = null;
				try {
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
				
			} // end if (metaFile.exists() && metaFile.canRead())
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
		String destDir = getMountPoint(false)+File.separator+"ephem"+File.separator;
		
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
		
	public final String getDeviceName() {
		return name;
	}
	
	public final String getDeviceUniqueID() {
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

	public final String getGpsVersion() {
		return processorVersion;
	}

	public final String getBootloaderVersion() {
		return Integer.toString(bootloaderVersion);
	}

	public final String getActiveMapName() {
		if (activeMap != null) {
			return activeMap.getName();
		} else {
			return "";
		}
	}

	public final String getActiveMapVersion() {
		if (activeMap != null) {
			return activeMap.getVersion();
		} else {
			return "";
		}
	}
	
	public final TomtomMap getActiveMap() {
		return activeMap;
	}
	
	public Map<String, TomtomMap> getAllMaps() throws JTomtomException {
		if (availableMaps == null) {
			availableMaps = new HashMap<String, TomtomMap>();
			for (TomtomMap map : TomtomMap.listAllGpsMap(this)) {
				availableMaps.put(map.getName(), map);
			}
		}
		return availableMaps;
	}
}
