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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.Date;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.GlobalPositioningSystem;
import org.jtomtom.JTomtom;
import org.jtomtom.JTomtomException;
import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.connector.radars.PdisDotEs;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;


public class TestPdisDotEs {
	@BeforeClass
	public static void initLogger() {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
			BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	@Test
	public void testConnexion() {
		RadarsConnector radars = new PdisDotEs();
		Proxy proxy = JTomtom.getApplicationProxy();
		assertFalse(radars.connexion(proxy, "martm", "myhtram"));
		assertFalse(radars.connexion(proxy, "marthym", "myhtram"));
		assertTrue(radars.connexion(proxy, "jtomFrederic", "jtomtom159"));
	}
	
	@Test
	public void testGetRemoteDbInfos() {
		RadarsConnector radars = new PdisDotEs();
		Proxy proxy = JTomtom.getApplicationProxy();
		assertTrue(radars.connexion(proxy, "jtomFrederic", "jtomtom159"));
		
		POIsDbInfos infos;
		infos = radars.getRemoteDbInfos(proxy);
		
		assertNotNull(infos);
		assertTrue(infos.getLastUpdateDate() != new Date(0));
		assertTrue(infos.getPoisNumber() >= 0);
		assertFalse(infos.getDbVersion().equals(POIsDbInfos.UNKNOWN));
	}
	
	@Test
	public void testGetLocalDbInfos() {
		RadarsConnector radars = new PdisDotEs();
		
		POIsDbInfos infos = null;
		try {
			infos = radars.getLocalDbInfos((new GlobalPositioningSystem()).getAllMaps().get("Espagne").getPath());
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		
		assertNotNull(infos);
		assertTrue(infos.getLastUpdateDate() != new Date(0));
		assertTrue(infos.getPoisNumber() >= 0);
		assertFalse(infos.getDbVersion().equals(POIsDbInfos.UNKNOWN));
	}
	
	@Test
	public void testGetConnectionForUpdate() {
		RadarsConnector radars = new PdisDotEs();
		HttpURLConnection conn = radars.getConnectionForUpdate();
		assertNull(conn);
		
		radars.connexion(JTomtom.getApplicationProxy(), "jtomFrederic", "jtomtom159");
		conn = radars.getConnectionForUpdate();
		
		assertNotNull(conn);
		
		int http_code = 0;
		int http_size = 0;
		try {
			conn.connect();
			http_code = conn.getResponseCode();
			http_size = conn.getContentLength();
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		assertEquals(HttpURLConnection.HTTP_OK, http_code);
		assertTrue(http_size > 0);
	}
	
	@Test
	public void testGetConnectionForInstall() {
		RadarsConnector radars = new PdisDotEs();
		HttpURLConnection conn = radars.getConnectionForUpdate();
		assertNull(conn);
		
		radars.connexion(JTomtom.getApplicationProxy(), "jtomFrederic", "jtomtom159");
		conn = radars.getConnectionForInstall();
		
		assertNotNull(conn);
		
		int http_code = 0;
		int http_size = 0;
		try {
			conn.connect();
			http_code = conn.getResponseCode();
			http_size = conn.getContentLength();
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		assertEquals(HttpURLConnection.HTTP_OK, http_code);
		assertTrue(http_size > 0);
	}
	
	@Test
	public void testToString() {
		RadarsConnector radars = new PdisDotEs();
		assertNotNull(radars.toString());
		assertTrue(radars.toString().length() > 0);
	}
}
