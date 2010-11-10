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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.JTomtom;
import org.jtomtom.JTomtomException;
import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.connector.radars.Tomtomax;
import org.jtomtom.device.TomtomDevice;

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
		
		POIsDbInfos infos;
		infos = radars.getRemoteDbInfos(proxy);
		
		assertNotNull(infos);
		assertNotNull(infos.getLastUpdateDate());
		assertNotNull(infos.getNumberOfPOIs());
		assertFalse(infos.getDatabaseVersion().equals(POIsDbInfos.NA));
	}
	
	@Test
	public void testGetLocalDbInfos() {
		RadarsConnector radars = new Tomtomax();
		
		POIsDbInfos infos = null;
		try {
			infos = radars.getLocalDbInfos((new TomtomDevice()).getActiveMap().getPath());
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		
		assertNotNull(infos);
		assertNotNull(infos.getLastUpdateDate());
		assertNotNull(infos.getNumberOfPOIs());
		assertFalse(infos.getDatabaseVersion().equals(POIsDbInfos.NA));
	}
	
	@Test
	public void testGetConnectionForUpdate() {
		RadarsConnector radars = new Tomtomax();
		HttpURLConnection conn = radars.getConnectionForUpdate();
		assertNull(conn);
		
		radars.connexion(JTomtom.getApplicationProxy(), "marthym", "myhtram");
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
		RadarsConnector radars = new Tomtomax();
		HttpURLConnection conn = radars.getConnectionForUpdate();
		assertNull(conn);
		
		radars.connexion(JTomtom.getApplicationProxy(), "marthym", "myhtram");
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
}
