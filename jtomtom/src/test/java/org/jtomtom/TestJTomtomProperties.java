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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class TestJTomtomProperties {
	private static final Logger LOGGER = Logger.getLogger(TestJTomtomProperties.class);
	
	@BeforeClass
	public static void initLogger() {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
			BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
		LOGGER.info("===== TestJTomtomProperties =====");
	}
	
	@Test
	public void testLoadProperties() {
		JTomtomProperties jttProps = new JTomtomProperties();
		try {
			jttProps.load(Constant.JTOMTOM_PROPERTIES, System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES);
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		
		assertFalse(jttProps.isEmpty());
	}
	
	@Test
	public void testGetApplicationProperty() {
		JTomtomProperties jttProps = new JTomtomProperties();
		try {
			jttProps.load(Constant.JTOMTOM_PROPERTIES, System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES);
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		assertEquals("DIRECT", jttProps.getApplicationProperty("net.proxy.type"));
		assertNull(jttProps.getApplicationProperty("dummy"));
		assertEquals("DIRECT", jttProps.getApplicationProperty("net.proxy.type", "HTTP"));
		assertEquals("HTTP", jttProps.getApplicationProperty("dummy", "HTTP"));
	}
	
	@Test
	public void testGetApplicationProperties() {
		JTomtomProperties jttProps = new JTomtomProperties();
		try {
			jttProps.load(Constant.JTOMTOM_PROPERTIES, System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES);
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		Map<String, String> proxyProps = jttProps.getApplicationProperties("net.proxy");
		assertNotNull(proxyProps);
		assertFalse(proxyProps.isEmpty());
		assertEquals("DIRECT", proxyProps.get("net.proxy.type"));
	}
	
	@Test
	public void testGetUserProperty() {
		JTomtomProperties jttProps = new JTomtomProperties();
		try {
			jttProps.load(Constant.JTOMTOM_PROPERTIES, System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES);
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		assertEquals("DEBUG", jttProps.getUserProperty("org.jtomtom.logLevel"));
		assertNull(jttProps.getUserProperty("dummy"));
		assertEquals("DEBUG", jttProps.getUserProperty("org.jtomtom.logLevel", "TRACE"));
		assertEquals("TRACE", jttProps.getUserProperty("dummy", "TRACE"));
	}
	
	@Test
	public void testSetUserProperty() {
		JTomtomProperties jttProps = new JTomtomProperties();
		try {
			jttProps.load(Constant.JTOMTOM_PROPERTIES, System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES);
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		assertEquals("Nimbus", jttProps.getUserProperty("org.jtomtom.lookandfeel"));
		jttProps.setUserProperty("org.jtomtom.lookandfeel", "Metal");
		assertEquals("Metal", jttProps.getUserProperty("org.jtomtom.lookandfeel"));		
	}

	@Test
	public void testGetUserProperties() {
		JTomtomProperties jttProps = new JTomtomProperties();
		try {
			jttProps.load(Constant.JTOMTOM_PROPERTIES, System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES);
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		Map<String, String> tomtomProps = jttProps.getUserProperties("org.jtomtom");
		assertNotNull(tomtomProps);
		assertFalse(tomtomProps.isEmpty());
		assertEquals("DEBUG", tomtomProps.get("org.jtomtom.logLevel"));
	}
	
	@Test
	public void testStoreUserProperties() {
		JTomtomProperties jttProps = new JTomtomProperties();
		try {
			jttProps.load(Constant.JTOMTOM_PROPERTIES, System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES);
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		assertEquals("DEBUG", jttProps.getUserProperty("org.jtomtom.logLevel"));
		
		File testFile = null;
		try { testFile = File.createTempFile("test", ".properties"); } 
		catch (Exception e) { fail(e.getLocalizedMessage()); }
		
		try { jttProps.storeUserProperties(testFile); } 
		catch (Exception e) { fail(e.getLocalizedMessage()); }
		
		assertTrue(testFile.exists());
		assertTrue(testFile.length() > 0);
	}

}
