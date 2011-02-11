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
package org.jtomtom.junit;

import static junit.framework.Assert.*;

import java.io.File;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.device.TomtomDeviceFinder;
import org.jtomtom.device.providers.FilesProviderFactory;
import org.jtomtom.device.providers.TomtomFilesProvider;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestTomtomFilesProvider {
	@BeforeClass
	public static void initLogger() {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
			BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	@Test
	public void testGetCurrentMapDat() {
		try {
			TomtomFilesProvider ttFilesProvider = FilesProviderFactory.getFilesProvider(TomtomDeviceFinder.findMountPoint());
			File mapDat = ttFilesProvider.getCurrentMapDat();
			assertNotNull(mapDat);
			assertTrue(mapDat.exists());
			assertTrue("currentmap.dat".equalsIgnoreCase(mapDat.getName())
					|| mapDat.getName().toLowerCase().startsWith("currentmap"));
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testGetEphemeridData() {
		try {
			TomtomFilesProvider ttFilesProvider = new TomtomFilesProvider(TomtomDeviceFinder.findMountPoint());
			Set<File> ephemDat = ttFilesProvider.getEphemeridData();
			assertNotNull(ephemDat);
			assertFalse(ephemDat.isEmpty());
			assertTrue(ephemDat.iterator().next().exists());
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testGetEphemeridMeta() {
		try {
			TomtomFilesProvider ttFilesProvider = new TomtomFilesProvider(TomtomDeviceFinder.findMountPoint());
			File ephemMet = ttFilesProvider.getEphemeridMeta();
			assertNotNull(ephemMet);
			assertTrue(ephemMet.exists());
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
	}
	
}
