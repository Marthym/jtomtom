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
package org.jtomtom.device.providers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;
import org.jtomtom.JTomtomException;
import org.jtomtom.tools.BoyerMoore;

/**
 * @author Frédéric Combes
 *
 * Specific files provider for Carminat Tomtom device
 */
public class CarminatFilesProvider extends TomtomFilesProvider {
	private static final Logger LOGGER = Logger.getLogger(CarminatFilesProvider.class);
	
	public static final String FILE_CARMINAT_LOOPBACK = "ext3_loopback";
	public static final String DIR_CARMINAT_LOOPBACK = "loopdir";
	private static final String TTGOBIF_START_TAG = "[TomTomGo]";
	private static final byte[] CURRENTMAP_START_TAG = new byte[]{20,0,0,0,47,109,110,116,47};

	private File loopback;
	private File carminatInformations;
	private File carminatCurrentMap;
	
	public CarminatFilesProvider(File rootDirectory)
			throws FileNotFoundException {
		super(rootDirectory);
		
		loopback = new File(rootDirectory, DIR_CARMINAT_LOOPBACK+File.separator+FILE_CARMINAT_LOOPBACK);
		if (loopback == null || !loopback.exists() || !loopback.canRead()) {
			throw new FileNotFoundException(loopback.getAbsolutePath());
		}
		
		LOGGER.debug("new CarminatFilesProvider "+loopback.getAbsolutePath());
	}

	@Override
	public File getTomtomInformations() throws FileNotFoundException {
		if (carminatInformations == null || !carminatInformations.exists())
			carminatInformations = extractInformationsFromLoopback();
		
		return carminatInformations;
	}

	private final File extractInformationsFromLoopback() {
		InputStream is = null;
		FileWriter writer = null;
		BufferedWriter out = null;
		File tempInforamtionsFile = null;
		try {
			is = new FileInputStream(loopback);
			int ttgoBifStart = BoyerMoore.findBytes(is, TTGOBIF_START_TAG.getBytes());
			LOGGER.debug("find ttgoBif at "+ttgoBifStart);
			
			tempInforamtionsFile = File.createTempFile("ttgo", ".bif");
			tempInforamtionsFile.deleteOnExit();
			
			writer = new FileWriter(tempInforamtionsFile);
			out = new BufferedWriter(writer);
			
			RandomAccessFile raf = new RandomAccessFile(loopback, "r");
			raf.seek(ttgoBifStart);
			String line;
			while ((line = raf.readLine()) != null) {
				if (line.contains("=") || line.contains("[")) {
					out.write(line);
					out.newLine();
				} else { 
					break;
				}
			}
			out.flush();
			
			LOGGER.debug("write temporary file "+tempInforamtionsFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			throw new JTomtomException(e);
			
		} catch (IOException e) {
			throw new JTomtomException(e);
			
		} finally {
			try { is.close(); } catch (Exception e) {}
			try { writer.close(); } catch (Exception e) {}
			try { out.close(); } catch (Exception e) {}
		}
		
		return tempInforamtionsFile;
	}

	@Override
	public File getCurrentMapDat() throws FileNotFoundException {
		if (carminatCurrentMap == null || !carminatCurrentMap.exists())
			carminatCurrentMap = extractCurrentMapFromLoopback();
		
		return carminatCurrentMap;
	}
	

	private final File extractCurrentMapFromLoopback() {
		InputStream is = null;
		FileOutputStream out = null;
		File tempCurrentMapFile = null;
		try {
			is = new FileInputStream(loopback);
			int ttgoBifStart = BoyerMoore.findBytes(is, CURRENTMAP_START_TAG);
			LOGGER.debug("find CurrentMap.dat at "+ttgoBifStart);
			
			tempCurrentMapFile = File.createTempFile("CurrentMap", ".dat");
			tempCurrentMapFile.deleteOnExit();
			
			out = new FileOutputStream(tempCurrentMapFile);
			
			RandomAccessFile raf = new RandomAccessFile(loopback, "r");
			raf.seek(ttgoBifStart);
			
			byte[] dummyBytes = new byte[4];
			raf.read(dummyBytes);
			out.write(dummyBytes);
			
			String map = raf.readUTF();
			out.write(map.getBytes());
			
			out.write(raf.readByte());
			
			out.flush();
			
			LOGGER.debug("write temporary file "+tempCurrentMapFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			throw new JTomtomException(e);
			
		} catch (IOException e) {
			throw new JTomtomException(e);
			
		} finally {
			try { is.close(); } catch (Exception e) {}
			try { out.close(); } catch (Exception e) {}
		}
		
		return tempCurrentMapFile;
	}

}
