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
package org.jtomtom.junit;

import static junit.framework.Assert.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.JTomtomException;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.device.TomtomMap;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestGPS {
	@BeforeClass
	public static void initLogger() {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
			BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testGetMountedPoint() {
		TomtomDevice myGPS = new TomtomDevice(false);
		assertNotNull(myGPS);
		
		String mountPoint = null;
		try {
			mountPoint = myGPS.getMountPoint(true);
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(mountPoint);
		assertFalse(mountPoint.isEmpty());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testReadInformations() {
		TomtomDevice myGPS = new TomtomDevice(false);
		assertNotNull(myGPS);
		try {
			myGPS.loadInformationsFromBif();
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(myGPS.getSerialNumber());
		assertEquals("AK9AG BJKUJ", myGPS.getSerialNumber());
		System.out.println(myGPS.getAppVersion());
	}
	
	@Test
	public void testGetChipset() {
		try {
			TomtomDevice myGPS = new TomtomDevice();
			String chipset = myGPS.getChipset();
			assertEquals("globalLocate", chipset);
			assertTrue(myGPS.getQuickFixLastUpdate() != 0);
			
			System.out.println(new java.util.Date(myGPS.getQuickFixLastUpdate()));
			
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testGetQuickFixExpiry() {
		try {
			TomtomDevice myGPS = new TomtomDevice();
			long expiry = myGPS.getQuickFixExpiry();
			assertTrue(expiry != 0);
						
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
	}
		
	@Test
	public void testGetMapsList() {
		try {
			TomtomDevice myGPS = new TomtomDevice();
			java.util.Map<String, TomtomMap> maps = myGPS.getAvailableMaps();
			assertNotNull(maps);
			assertFalse(maps.isEmpty());
			
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
}
