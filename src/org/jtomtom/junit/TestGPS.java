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
import org.jtomtom.GlobalPositioningSystem;
import org.jtomtom.GpsMap;
import org.jtomtom.JTomtomException;
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
		GlobalPositioningSystem myGPS = new GlobalPositioningSystem(false);
		assertNotNull(myGPS);
		
		String mountPoint = null;
		try {
			mountPoint = myGPS.getMountedPoint(true);
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(mountPoint);
		assertFalse(mountPoint.isEmpty());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testReadInformations() {
		GlobalPositioningSystem myGPS = new GlobalPositioningSystem(false);
		assertNotNull(myGPS);
		try {
			myGPS.readGPSInformations();
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(myGPS.getDeviceUniqueID());
		assertEquals("AK9AG BJKUJ", myGPS.getDeviceUniqueID());
		System.out.println(myGPS.getAppVersion());
	}
	
	@Test
	public void testGetChipset() {
		try {
			GlobalPositioningSystem myGPS = new GlobalPositioningSystem();
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
			GlobalPositioningSystem myGPS = new GlobalPositioningSystem();
			long expiry = myGPS.getQuickFixExpiry();
			assertTrue(expiry != 0);
						
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
	}
		
	@Test
	public void testGetMapsList() {
		try {
			GlobalPositioningSystem myGPS = new GlobalPositioningSystem();
			java.util.Map<String, GpsMap> maps = myGPS.getAllMaps();
			assertNotNull(maps);
			assertFalse(maps.isEmpty());
			
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
}
