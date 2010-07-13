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

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.JTomTomUtils;
import org.jtomtom.JTomtomException;
import org.jtomtom.RadarsConnector;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestJTTUtils {
	@BeforeClass
	public static void initLogger() {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
			BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	@Test
	@Ignore
	public void testDeplacer() {
		File source = new File("source.txt");
		File destination = new File("destination.txt");
		assertTrue(source.exists());
		assertTrue(destination.length() != source.length());
		
		JTomTomUtils.copier(source, destination, true);
		assertTrue(destination.exists());
		assertEquals(source.length(), destination.length());
	}

	@Test
	public void testInstantiateRadarsConnector() {
		RadarsConnector radars = null;
		try {
			radars = JTomTomUtils.instantiateRadarConnector();
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(radars);
	}
	
	@Test
	public void testGetAllRadarsConnectors() {
		RadarsConnector[] radars = null;
		radars = JTomTomUtils.getAllRadarsConnectors();
		assertNotNull(radars);
	}

}
