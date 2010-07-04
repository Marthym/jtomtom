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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

import net.sf.jcablib.CabFile;

import org.apache.log4j.Logger;

/**
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
		
		File currentMapFile = new File(p_gps.getMountedPoint(false), "currentmap.dat");
		if (!currentMapFile.exists()) {
			LOGGER.error("Fichier currentmap.dat inexistant !");
			return null;
		}
		
		// Commence par lire le fichier currentmap.dat pour trouver le chemin de la carte
		String gpsMapPath = "";
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(currentMapFile, "r");
			if (raf.readByte() == FS) {
				raf.skipBytes(3);
				gpsMapPath = CabFile.readCString(raf);
			}
			LOGGER.debug("path = "+gpsMapPath);
			
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

} 
