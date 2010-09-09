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
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.JTomtom;
import org.jtomtom.JTomtomException;
import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.device.TomtomDeviceFinder;
import org.jtomtom.device.TomtomMap;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestGpsMap {
	@BeforeClass
	public static void initLogger() {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
			BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	@Test
	public void testReadCurrentMap() {
		try {
			TomtomMap map = TomtomMap.createMapFromPath(TomtomDeviceFinder.findMountPoint()+File.separator+"France");
			
			assertNotNull(map);
			
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testReadRadarInfos() {
		try {
			JTomtom.loadProperties();
			
			TomtomMap map = TomtomMap.createMapFromPath(TomtomDeviceFinder.findMountPoint()+File.separator+"France");
			map.readRadarsInfos();
			assertNotNull(map.getRadarsDbDate());
			assertFalse(map.getRadarsDbVersion().equals(POIsDbInfos.UNKNOWN));
			assertTrue(map.getRadarsNombre() > 0);
			
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testListAllGpsMap() {
		try {
			TomtomDevice myGPS = new TomtomDevice(false);
			List<TomtomMap> map = TomtomMap.listAllGpsMap(myGPS);
			
			assertNotNull(map);
			assertFalse(map.isEmpty());
			
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
}
