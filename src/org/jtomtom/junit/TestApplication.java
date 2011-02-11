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

import java.net.Proxy;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jtomtom.Application;
import org.jtomtom.connector.RadarsConnector;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestApplication {
	@BeforeClass
	public static void initLogger() {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
			BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	@Test
	public void testGetDefaultRadarConnector() {
		RadarsConnector radar = Application.getDefaultRadarConnector();
		Assert.assertNotNull(radar);
	}
	
	@Test
	public void testGetUserAgent() {
		String userAgent = Application.getUserAgent();
		Assert.assertNotNull(userAgent);
		Assert.assertFalse(userAgent.isEmpty());
	}
	
	@Test
	public void testGetInstance() {
		Application theApp = Application.getInstance();
		Assert.assertNotNull(theApp);
	}
	
	@Test
	public void testGetProxyServer() {
		Proxy proxy = Application.getInstance().getProxyServer();
		
		Assert.assertNotNull(proxy);
	}
	
}
