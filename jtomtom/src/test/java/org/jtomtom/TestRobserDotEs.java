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
package org.jtomtom;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.JTomtomException;
import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.connector.radars.RobserEs;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.device.TomtomMap;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class TestRobserDotEs {
	@BeforeClass
	public static void initLogger() {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
			BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	@Test
	public void testConnexion() {
		RadarsConnector radars = new RobserEs();
		Proxy proxy = Application.getInstance().getProxyServer();
		//assertFalse(radars.connexion(proxy, "marthym", "myhtram"));
		assertTrue(radars.connexion(proxy, "jtomFrederic", "jtomtom159"));
	}
	
	@Test
	public void testGetRemoteDbInfos() {
		RadarsConnector radars = new RobserEs();
		Proxy proxy = Application.getInstance().getProxyServer();
		assertTrue(radars.connexion(proxy, "jtomFrederic", "jtomtom159"));
		
		POIsDbInfos infos;
		infos = radars.getRemoteDbInfos(proxy);
		
		assertNotNull(infos);
		assertNotNull(infos.getLastUpdateDate());
		assertNotNull(infos.getNumberOfPOIs());
		assertFalse(infos.getDatabaseVersion().equals(POIsDbInfos.NA));
	}
	
	@Test
	public void testGetLocalDbInfos() {
		RadarsConnector radars = new RobserEs();
		
		POIsDbInfos infos = null;
		TomtomMap iberia = (new TomtomDevice()).getAvailableMaps().get("Iberia_850.2781");
		if (iberia != null) {
			try {
				infos = radars.getLocalDbInfos(iberia.getPath());
			} catch (JTomtomException e) {
				fail(e.getLocalizedMessage());
			}
			
			assertNotNull(infos);
			assertFalse(infos.isEmpty());
		}
	}
	
	@Test
	public void testGetConnectionForUpdate() {
		RadarsConnector radars = new RobserEs();
		HttpURLConnection conn = null;
		try {
			conn = radars.getConnectionForUpdate();
			fail("getConnectionForUpdate without connexion !");
		} catch (JTomtomException e) {}
		assertNull(conn);
		
		radars.connexion(Application.getInstance().getProxyServer(), "jtomFrederic", "jtomtom159");
		conn = radars.getConnectionForUpdate();
		
		assertNotNull(conn);
		
		int http_code = 0;
		int http_size = 0;
		String httpContentDisposition = "";
		try {
			conn.connect();
			http_code = conn.getResponseCode();
			http_size = conn.getContentLength();
			httpContentDisposition = conn.getHeaderField("Content-Disposition");
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		assertEquals(HttpURLConnection.HTTP_OK, http_code);
		assertTrue(http_size > 0);
		assertTrue(httpContentDisposition.trim().startsWith("attachment"));
	}
	
	@Test
	public void testGetConnectionForInstall() {
		RadarsConnector radars = new RobserEs();
		HttpURLConnection conn = null;
		try {
			conn = radars.getConnectionForInstall();
			fail("getConnectionForInstall without connexion !");
		} catch (JTomtomException e) {}
		assertNull(conn);
		
		radars.connexion(Application.getInstance().getProxyServer(), "jtomFrederic", "jtomtom159");
		conn = radars.getConnectionForInstall();
		
		assertNotNull(conn);
		
		int http_code = 0;
		int http_size = 0;
		String httpContentDisposition = "";
		try {
			conn.connect();
			http_code = conn.getResponseCode();
			http_size = conn.getContentLength();
			httpContentDisposition = conn.getHeaderField("Content-Disposition");
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		assertEquals(HttpURLConnection.HTTP_OK, http_code);
		assertTrue(http_size > 0);
		assertTrue(httpContentDisposition.trim().startsWith("attachment"));
	}
	
	@Test
	public void testToString() {
		RadarsConnector radars = new RobserEs();
		assertNotNull(radars.toString());
		assertTrue(radars.toString().length() > 0);
	}
}
