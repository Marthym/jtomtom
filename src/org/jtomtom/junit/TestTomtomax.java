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

import java.net.Proxy;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.GlobalPositioningSystem;
import org.jtomtom.JTomtom;
import org.jtomtom.JTomtomException;
import org.jtomtom.RadarsConnector;
import org.jtomtom.radars.Tomtomax;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;


public class TestTomtomax {
	@BeforeClass
	public static void initLogger() {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
			BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	@Test
	public void testConnexion() {
		RadarsConnector radars = new Tomtomax();
		Proxy proxy = JTomtom.getApplicationProxy();
		assertFalse(radars.connexion(proxy, "marthym", "prout"));
		assertFalse(radars.connexion(proxy, "martm", "myhtram"));
		assertTrue(radars.connexion(proxy, "marthym", "myhtram"));
	}
	
	@Test
	public void testGetRemoteDbInfos() {
		RadarsConnector radars = new Tomtomax();
		Proxy proxy = JTomtom.getApplicationProxy();
		
		Map<String, String> infos;
		infos = radars.getRemoteDbInfos(proxy);
		
		assertNotNull(infos);
		assertTrue(infos.containsKey(Tomtomax.TAG_DATE));
		assertTrue(infos.containsKey(Tomtomax.TAG_RADARS));
		assertTrue(infos.containsKey(Tomtomax.TAG_VERSION));
	}
	
	@Test
	public void testGetLocalDbInfos() {
		RadarsConnector radars = new Tomtomax();
		
		Map<String, String> infos = null;
		try {
			infos = radars.getLocalDbInfos((new GlobalPositioningSystem()).getActiveMap().getPath());
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		
		assertNotNull(infos);
		assertTrue(infos.containsKey(Tomtomax.TAG_DATE));
		assertTrue(infos.containsKey(Tomtomax.TAG_RADARS));
		assertTrue(infos.containsKey(Tomtomax.TAG_VERSION));
	}
	
	@Test
	public void testGetInstallURL() {
		RadarsConnector radars = new Tomtomax();
		radars.getRemoteDbInfos(JTomtom.getApplicationProxy());
		assertNotNull(radars.getInstallURL());
		assertTrue(radars.getInstallURL().length() > 0);
	}
	
	@Test
	public void testGetUpdateURL() {
		RadarsConnector radars = new Tomtomax();
		radars.getRemoteDbInfos(JTomtom.getApplicationProxy());
		assertNotNull(radars.getUpdateURL());
		assertTrue(radars.getUpdateURL().length() > 0);
	}
	
	@Test
	public void testToString() {
		RadarsConnector radars = new Tomtomax();
		assertNotNull(radars.toString());
		assertTrue(radars.toString().length() > 0);
	}
}
