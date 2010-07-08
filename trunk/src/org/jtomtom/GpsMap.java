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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import net.sf.jcablib.CabFile;

import org.apache.log4j.Logger;

/**
 * Classe de gestion des cartes
 * 
 * @author marthym
 *
 */
public class GpsMap {
	private static final Logger LOGGER = Logger.getLogger(GpsMap.class);
	
	/**
	 * Caractère de commencement du fichier pna des maps
	 */
	public static final byte FS = 0x14;
	
	/**
	 * Lien vers le GPS contenant la map
	 */
	private GlobalPositioningSystem m_gps;
	
	/**
	 * Nom de la carte
	 */
	private String m_name;
	
	/**
	 * Version complète de la carte (majeur.mineure)
	 */
	private String m_version;
	
	/**
	 * Chemin absolue de ma carte
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
	 * Crée une carte correspondant à la carte courante du GPS
	 * @param p_gps	GPS porteur de la carte
	 * @return		Carte
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
			LOGGER.error("Fichier currentmap.dat inexistant !");
			return null;
		}
		
		// Commence par lire le fichier currentmap.dat pour trouver le chemin de la carte
		String gpsMapPath = "";
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(currentMapFile, "r");
			int pathLenght = Integer.reverseBytes(raf.readInt());
			gpsMapPath = CabFile.readCString(raf);
			if (gpsMapPath.length() != (pathLenght-1)) {
				LOGGER.debug("Le fichier "+currentMapFile.getName()+" semble corrompu !");
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
		
		// On récupère ensuite le nom et le path absolue de la carte
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
	 * Retourne la liste des cartes contenues dans un GPS
	 * @param p_gps	GPS à explorer
	 * @return		Liste de carte
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
	 * Crée une carte sans GPS à partie du nom et du chemin de la carte
	 * @param p_name	Nome de la carte
	 * @param p_path	Chemin absolue de la carte
	 * @return			Carte non liée à un GPS
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
			
			version = version.trim() +"."+ build.trim().split("=")[1];	// Ouais c'est presque pas crade ;)
			
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
	 * Relie la carte à un GPS
	 * @param p_gps
	 * @return
	 */
	private GpsMap linkToGps(GlobalPositioningSystem p_gps) {
		// On vérifie quand même que c'est cohérent
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
	 * Lecture des informations relatives aux Radars
	 * @throws JTomtomException
	 */
	public void readRadarsInfos() throws JTomtomException {
		
		// On cherche le répertoire de la carte actuelle
		File mapDirectory = new File(m_path);
		if (!mapDirectory.exists() || !mapDirectory.isDirectory() || !mapDirectory.canRead()) {
			m_radarsDbVersion = -1; // Erreur concernant la carte
			throw new JTomtomException("org.jtomtom.errors.gps.map.notfound", new String[]{m_name});
		}
		
		// On cherche le fichier de mise à jour TomtomMax
		File ttMaxDbFile = new File(mapDirectory, TomTomax.TOMTOMAX_DB_FILE);
		if (!ttMaxDbFile.exists()) {
			LOGGER.info("Les Radars TomtomMax n'ont jamais été installé !");
			return;
		}
		
		// On lit et on parse le fichier
		if (ttMaxDbFile.exists() && ttMaxDbFile.canRead()) {
			BufferedReader buff = null;
			try {
				buff = new BufferedReader(new FileReader(ttMaxDbFile));
				String line;
				while ((line = buff.readLine()) != null) {
					if (line.startsWith("date=")) {
						DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); 
						m_radarsDbDate = formatter.parse(line.substring(5));
						LOGGER.debug("m_radarsDbDate = "+m_radarsDbDate);
						
					} else if (line.startsWith("vers=")) {
						m_radarsDbVersion = Integer.parseInt(line.substring(5));
						LOGGER.debug("m_radarsDbVersion = "+m_radarsDbVersion);
						
					} else if (line.startsWith("radar=")) {
						m_radarsNombre = Integer.parseInt(line.substring(6));
						LOGGER.debug("m_radarsNombre = "+m_radarsNombre);
						
					} else if (line.startsWith("#####")) {
						break;
					}
				}
				
			} catch (FileNotFoundException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
				
			} catch (IOException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
				
			} catch (ParseException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
				
			} finally {
				try {buff.close();}catch(Exception e){}
			}
			
		} // end if (ttMaxDbFile.exists() && ttMaxDbFile.canRead())
	}
	
	/**
	 * Update radar database with a .ov2 files list
	 * @param files	Files need to be installed
	 * @return
	 * @throws JTomtomException
	 */
	public boolean updateRadars(List<File> files) throws JTomtomException {		
		// On cherche le répertoire de la carte actuelle
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
		
		// On déplace les fichiers dans le TT
		for (File current : files) {
			File dest = new File(mapDirectory, current.getName());
			if (JTomTomUtils.copier(current, dest, true)) {
				LOGGER.debug(current.getName()+" copy done.");
			} else {
				throw new JTomtomException("org.jtomtom.errors.gps.radars.installfail", new String[]{current.getName()});
			}
		}
		
		// - Mise à jour des infos pour la carte active
		if (m_name.equals(m_gps.getActiveMapName())) {
			readRadarsInfos();
		}
		
		return true;
	}
	
	public final String getName() {
		return m_name;
	}
	
	public final GlobalPositioningSystem getGPS() {
		return m_gps;
	}
	
	public final String getPath() {
		return m_path;
	}

	public final String getVersion() {
		return m_version;
	}
	
	public final Date getRadarsDbDate() {
		if (m_radarsDbDate == null) {
			try { readRadarsInfos(); } catch (JTomtomException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
		return m_radarsDbDate;
	}
	
	public final int getRadarsDbVersion() {
		if (m_radarsDbVersion == 0) {
			try { readRadarsInfos(); } catch (JTomtomException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
		return m_radarsDbVersion;
	}

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
