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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author marthym
 *
 * Classe de manipulation de l'appareil
 * 
 */
public class GlobalPositioningSystem {
	
	private static final Logger LOGGER = Logger.getLogger(GlobalPositioningSystem.class);
	
	/**
	 * Pount de montage ou lecteur du TT
	 */
	private File m_mountPoint;
	
	/**
	 * Numéro de série matériel du GPS connecté
	 */
	private String m_deviceUniqueID;
	
	/**
	 * Nom du TomTom 	
	 */
	private String m_deviceName;
	
	/**
	 * Numéro de version du NavCore
	 */
	private int m_appVersion;
	
	/**
	 * Numéro de version du système
	 */
	private int m_systemVersion;
	
	/**
	 * Numéro de version de la puce GPS
	 */
	private String m_gpsVersion;
	
	/**
	 * Numéro de version du BootLoader
	 */
	private int m_bootloaderVersion;
	
	/**
	 * Map actuellement utilisée
	 */
	private GpsMap m_activeMap;
	private Map<String, GpsMap> m_mapsList;
	
	/**
	 * Informations pour le quickFix 
	 * 		ces données ne sont pas chargées au lancement
	 */
	private String m_chipset;
	private long m_quickFixExpiry;
	private long m_quickFixLastUpdate;

	/**
	 * Constructeur initialisant les variables membre lues dans le TT connecté
	 * @throws JTomtomException 
	 */
	public GlobalPositioningSystem() throws JTomtomException {
		readGPSInformations();
	}
	
	public GlobalPositioningSystem(String p_moutPoint) throws JTomtomException {
		m_mountPoint = new File(p_moutPoint);
		if (m_mountPoint.exists() && m_mountPoint.canRead())
			readGPSInformations();
		
		throw new JTomtomException("org.jtomtom.errors.gps.incorrectmountpoint");
	}
	
	/**
	 * Version NO_ERROR du constructeur juste pour les tests
	 * ne pas utiliser
	 * @param p_init
	 */
	@Deprecated
	public GlobalPositioningSystem(boolean p_init) {
		if (p_init) {
			try { readGPSInformations(); } catch (JTomtomException e) {
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
	public String getMountedPoint(boolean p_forceRefresh) throws JTomtomException {
		if (!p_forceRefresh && m_mountPoint != null) {
			return m_mountPoint.getAbsolutePath();
		}
		
		File[] roots = null;
		
		// Pour obtenir la liste des points de montage c'est différent sous Linux & windows
		LOGGER.debug("os.name = "+System.getProperty("os.name"));
		if ("Linux".equals(System.getProperty("os.name"))) {
			roots = getLinuxMountPoints();
		} else {
			roots = getWindowsMountPoints();
		}
		
		for (int i=0; i < roots.length; i++) {
			// Pour chaque point de montage on test l'existance de ttgo.bif
			File ttgo = new File(roots[i].getPath()+File.separator+"ttgo.bif");
			if (ttgo.exists() && ttgo.isFile() && ttgo.canRead()) {
				m_mountPoint = roots[i];
				LOGGER.info("Détection du GPS : "+m_mountPoint.getAbsolutePath());
				return m_mountPoint.getAbsolutePath();
			}
		}
		
		throw new JTomtomException("org.jtomtom.errors.gps.gpsnotfound");
	}
	
	/**
	 * Read the GPS informations in the ttgo.bif file at the root directory
	 * @throws JTomtomException
	 */
	public void readGPSInformations() throws JTomtomException {
		// On vérifit déjà qu'on ai bien le point de montage et à défaut on va le chercher
		if (m_mountPoint == null) {
			getMountedPoint(false);
		}
		
		// On récupère la fichier d'info du TT
		File ttgo = new File(m_mountPoint.getPath()+File.separator+"ttgo.bif");
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(ttgo));
			
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return;
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return;
		}
		
		// On affecte les propriétés dont on a besoin
		m_deviceName = props.getProperty("DeviceName");
		m_appVersion = Integer.parseInt(props.getProperty("ApplicationVersionVersionNumber"));
		m_bootloaderVersion = Integer.parseInt(props.getProperty("BootLoaderVersion"));
		m_deviceUniqueID = props.getProperty("DeviceUniqueID");
		m_gpsVersion = props.getProperty("GPSFirmwareVersion");
		m_systemVersion = Integer.parseInt(props.getProperty("LinuxVersion"));
		
		m_activeMap = GpsMap.readCurrentMap(this);
		
		// Un petit coup de trace
		LOGGER.info("Chargement du "+m_deviceName);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("m_deviceName = "+m_deviceName);
			LOGGER.debug("m_appVersion = "+m_appVersion);
			LOGGER.debug("m_bootloaderVersion = "+m_bootloaderVersion);
			LOGGER.debug("m_deviceUniqueID = "+m_deviceUniqueID);
			LOGGER.debug("m_mapName = "+getActiveMapName());
			LOGGER.debug("m_mapVersion = "+getActiveMapVersion());
			LOGGER.debug("m_systemVersion = "+m_systemVersion);
		}
	}
	
	/**
	 * Extrait la liste des points de montage de la commande mount
	 * @return
	 */
	private static final File[] getLinuxMountPoints() {
		
		List<File> roots = new ArrayList<File>();
		try {
			// On exécute la commande système qui liste les point de montages
			Process cmd = Runtime.getRuntime().exec("mount");
			BufferedReader br = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
			
			// Et on en extrait les répertoire de montage
			String line = new String();
			LOGGER.debug("Liste des pounts de montage :");
			while ((line = br.readLine()) != null) {
				if (line.startsWith("none")) {
					// On vire les périphériques non monté
					continue;
				} 
				
				final String START = " on ", END = " type ";
				String pathMountPoint = line.substring(
						line.indexOf(START)+START.length(), 
						line.lastIndexOf(END));
				if (LOGGER.isDebugEnabled()) LOGGER.debug("\t"+pathMountPoint);
				
				roots.add(new File(pathMountPoint));
			}
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		} 
		
		return (File[]) roots.toArray(new File[0]);
		
	}
	
	/**
	 * Liste les lecteurs
	 * @return
	 */
	private static final File[] getWindowsMountPoints(){
		return File.listRoots();
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
		if (m_chipset == null) {
			String ephemDir = m_mountPoint+File.separator+"ephem";
			File ephemFile = new File(ephemDir+File.separator+"lto.dat");
			if (ephemFile.exists()) {
				m_chipset = "globalLocate";
				m_quickFixLastUpdate = ephemFile.lastModified();
			}
			
			ephemFile = new File(ephemDir+File.separator+"packedephemeris.ee");
			if (ephemFile.exists()) {
				m_chipset = "SiRFStarIII";
				m_quickFixLastUpdate = ephemFile.lastModified();
			}
			
			if (m_chipset == null)
				throw new JTomtomException("org.jtomtom.errors.gps.unknownchipset");
		}
		
		return m_chipset;
	}
	
	/**
	 * Récupère et retourne la date de la dernière mise à jour du QF
	 * @return 	Timespamp de la date
	 */
	public long getQuickFixLastUpdate() {
		if (m_quickFixLastUpdate == 0) {
			try {
				getChipset();
			} catch (JTomtomException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
		
		return m_quickFixLastUpdate;
	}
	
	/**
	 * Récupère et retourne la date d'expiration de l'éphéméride installé
	 * @return	Timespamp de la date
	 */
	public long getQuickFixExpiry() {
		if (m_quickFixExpiry == 0) {
			String ephemDir = m_mountPoint+File.separator+"ephem";
			File metaFile = new File(ephemDir+File.separator+"ee_meta.txt");
			if (metaFile.exists() && metaFile.canRead()) {
				BufferedReader buff = null;
				try {
					buff = new BufferedReader(new FileReader(metaFile));
					String line;
					while ((line = buff.readLine()) != null) {
						if (line.startsWith("Expiry=")) {
							m_quickFixExpiry = Long.parseLong(line.substring(7))*1000;
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
		
		return m_quickFixExpiry;
	}
	
	/**
	 * Installe les fichiers QuickFix téléchargés dans le GPS
	 * Ré-initialise les informations relatives au QuickFix
	 * @param ephemFiles		Liste des fichiers à installé pour le QF
	 * @return					True si l'installation s'est bien passé
	 * @throws JTomtomException	Si la copie des fichiers à échouée un exception est levé
	 */
	public boolean updateQuickFix(List<File> ephemFiles) throws JTomtomException {
		String destDir = getMountedPoint(false)+File.separator+"ephem"+File.separator;
		
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
		m_quickFixExpiry = 0;
		m_quickFixLastUpdate = 0;
		m_chipset = null;
		
		return true;
	}
		
	public final String getDeviceName() {
		return m_deviceName;
	}
	
	public final String getDeviceUniqueID() {
		return m_deviceUniqueID;
	}

	public final String getAppVersion() {
		if (m_appVersion != 0) {
			return Float.toString((float)m_appVersion/1000);
		}
		return Integer.toString(m_appVersion);
	}

	public final String getSystemVersion() {
		return Integer.toString(m_systemVersion);
	}

	public final String getGpsVersion() {
		return m_gpsVersion;
	}

	public final String getBootloaderVersion() {
		return Integer.toString(m_bootloaderVersion);
	}

	public final String getActiveMapName() {
		if (m_activeMap != null) {
			return m_activeMap.getName();
		} else {
			return "";
		}
	}

	public final String getActiveMapVersion() {
		if (m_activeMap != null) {
			return m_activeMap.getVersion();
		} else {
			return "";
		}
	}
	
	public final GpsMap getActiveMap() {
		return m_activeMap;
	}
	
	public Map<String, GpsMap> getAllMaps() throws JTomtomException {
		if (m_mapsList == null) {
			m_mapsList = new HashMap<String, GpsMap>();
			for (GpsMap map : GpsMap.listAllGpsMap(this)) {
				m_mapsList.put(map.getName(), map);
			}
		}
		return m_mapsList;
	}
}
