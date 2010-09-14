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
package org.jtomtom.tools;

import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Test if the network is available
 * @author Frédéric Combes
 *
 */
public class NetworkTester {
	private static final Logger LOGGER = Logger.getLogger(NetworkTester.class);
	private static final int CHECK_DELAY = 60*1000; // in millisecond
	private static final String TEST_URL = "http://jtomtom.sourceforge.net/";
	
	private static NetworkTester instance;
	private long lastCheckTime = 0;
	private boolean lastCheckResult = false;
	
	private NetworkTester() {};
	
	/**
	 * Give the unique instance of NetworkTester
	 * @return NetworkTester
	 */
	public static NetworkTester getInstance() {
		if (instance == null) {
			LOGGER.debug("Create new NetworkTester instance.");
			instance = new NetworkTester();
		}
		
		return instance;
	}
	
	/**
	 * Test the network, only one time per minute
	 * @return	Network availability
	 */
	public boolean isNetworkAvailable() {
		if (new Date().getTime() < (lastCheckTime + CHECK_DELAY)) 
			lastCheckResult = checkNetworkAvailability(Proxy.NO_PROXY);
		
		return lastCheckResult;
	}
	
	/**
	 * Test the network behind a proxy server, only one time per minute
	 * @param proxy	The proxy server to be used
	 * @return Network availability
	 */
	public boolean isNetworkAvailable(Proxy proxy) {
		if (new Date().getTime() > (lastCheckTime + CHECK_DELAY)) 
			lastCheckResult = checkNetworkAvailability(proxy);
		
		return lastCheckResult;
	}
	
	/**
	 * Check availability and update private members
	 * @param proxy The proxy server to be used
	 * @return Network availability
	 */
	private boolean checkNetworkAvailability(Proxy proxy) {
		try {
			LOGGER.debug("Test network with "+TEST_URL);
			lastCheckTime = new Date().getTime();
			
			URL urlForTest = new URL(TEST_URL);
			URLConnection testConnection = urlForTest.openConnection(proxy);
			testConnection.connect();
			
			lastCheckResult = true;
			
		} catch (Exception e) {
			LOGGER.error(e);
			lastCheckResult = false;
		}
		
		return lastCheckResult;
	}
}
