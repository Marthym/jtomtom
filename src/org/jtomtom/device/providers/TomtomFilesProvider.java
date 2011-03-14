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
package org.jtomtom.device.providers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Frédéric Combes
 *
 * Global files provider for all Tomtom device
 * 
 */
public class TomtomFilesProvider {
	private static final Logger LOGGER = Logger.getLogger(TomtomFilesProvider.class);
	
	public static final String FILE_GLOBAL_LOCATE = "lto.dat";
	public static final String FILE_SIRFSTAR_III = "packedephemeris.ee";
	public static final String FILE_QUICKFIX_META = "ee_meta.txt";
	public static final String FILE_CURRENT_MAP = "currentmap.dat";
	public static final String FILE_TOMTOM_INFORMATIONS = "ttgo.bif";
	
	File rootDirectory;
	File sdCardDirectory;
	
	public TomtomFilesProvider(File rootDirectory) throws FileNotFoundException {
		LOGGER.debug("Init TomtomFilesProvider for root : "+rootDirectory);
		this.rootDirectory = rootDirectory;
		
		if (!rootDirectory.exists() || !rootDirectory.canRead())
			throw new FileNotFoundException(rootDirectory.getAbsolutePath());
	}
	
	/**
	 * Looking for the currentmap.dat file. This file is not case sensitive for
	 * 	Tomtom device but it is for Linux system.
	 * @return
	 * @throws FileNotFoundException
	 */
	public File getCurrentMapDat() throws FileNotFoundException {
		
		File[] datFiles = rootDirectory.listFiles(createExtensionFilter("dat"));
		
		File currentMapFile = null;
		for (File aDatFile : datFiles) {
			if (FILE_CURRENT_MAP.equalsIgnoreCase(aDatFile.getName())) {
				currentMapFile = aDatFile;
				break;
			}
		}
		if (currentMapFile == null || !currentMapFile.exists()) {
			throw new FileNotFoundException("File currentmap.dat not found in the GPS root !");
		}
		return currentMapFile;
	}
	
	public Set<File> getEphemeridData() throws FileNotFoundException {
		File ephemDir = new File(rootDirectory, "ephem");
		if (!ephemDir.exists()) throw new FileNotFoundException();
		
		Set<File> dataEphemFiles = new HashSet<File>();
		File[] ephemFiles = ephemDir.listFiles();
		for (File anEphemFile : ephemFiles) {
			if (FILE_GLOBAL_LOCATE.equalsIgnoreCase(anEphemFile.getName()))
				dataEphemFiles.add(anEphemFile);
			
			if (FILE_SIRFSTAR_III.equalsIgnoreCase(anEphemFile.getName()))
				dataEphemFiles.add(anEphemFile);
		}
		
		return dataEphemFiles;
	}
	
	public File getEphemeridMeta() throws FileNotFoundException {
		File meta = new File(rootDirectory+File.separator+"ephem"+File.separator+FILE_QUICKFIX_META);
		if (!meta.exists() || !meta.canRead()) 
			throw new FileNotFoundException(meta.getAbsolutePath());
			
		return meta;
		
	}
	
	/**
	 * The purpose of this function is to remove all files in the ephem directory
	 * In the case where you have unknown Chipset, you want to change and assign to a know Chipset
	 * You must clean the ephem directory before !
	 */
	public void resetEphemeridData() {
		File ephemDir = new File(rootDirectory, "ephem");
		File[] ephemFiles = ephemDir.listFiles();
		for (File current : ephemFiles) {
			current.delete();
		}
		LOGGER.info("Ephem directory cleaned successfully !");
	}

	public File getTomtomInformations() throws FileNotFoundException {
		File ttgobif = new File(rootDirectory, FILE_TOMTOM_INFORMATIONS);
		if (!ttgobif.exists() || !ttgobif.canRead()) 
			throw new FileNotFoundException(ttgobif.getAbsolutePath());
			
		return ttgobif;
	}

	private static final FilenameFilter createExtensionFilter(final String ext) {
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("."+ext.toLowerCase()) || name.endsWith("."+ext.toUpperCase());
			}
		};
	}
	
	public final File getRootDirectory() {
		return rootDirectory;
	}
}
