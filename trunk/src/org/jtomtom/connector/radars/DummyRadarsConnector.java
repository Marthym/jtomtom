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
package org.jtomtom.connector.radars;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import org.jtomtom.JTomtomException;
import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.connector.RadarsConnector;

/**
 * Dummy RadarConnector for unfound connector
 * @author Frédéric Combes
 *
 */
public class DummyRadarsConnector extends RadarsConnector {

	/* (non-Javadoc)
	 * @see org.jtomtom.connector.RadarsConnector#getLocalDbInfos(java.lang.String)
	 */
	@Override
	public POIsDbInfos getLocalDbInfos(String m_path) throws JTomtomException {
		return new POIsDbInfos();
	}

	/* (non-Javadoc)
	 * @see org.jtomtom.connector.RadarsConnector#getRemoteDbInfos(java.net.Proxy)
	 */
	@Override
	public POIsDbInfos getRemoteDbInfos(Proxy proxy) {
		return new POIsDbInfos();
	}

	/* (non-Javadoc)
	 * @see org.jtomtom.connector.RadarsConnector#connexion(java.net.Proxy, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean connexion(Proxy p_proxy, String p_user, String p_password) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.jtomtom.connector.RadarsConnector#getConnectionForUpdate()
	 */
	@Override
	public HttpURLConnection getConnectionForUpdate() {
		URL myURL = null;
		HttpURLConnection conn = null;
		try {
			myURL = new URL("http://localhost/");
			conn = (HttpURLConnection) myURL.openConnection();
		} catch (Exception e) {}
		
		return conn;
	}

	/* (non-Javadoc)
	 * @see org.jtomtom.connector.RadarsConnector#getConnectionForInstall()
	 */
	@Override
	public HttpURLConnection getConnectionForInstall() {
		URL myURL = null;
		HttpURLConnection conn = null;
		try {
			myURL = new URL("http://localhost/");
			conn = (HttpURLConnection) myURL.openConnection();
		} catch (Exception e) {}
		
		return conn;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "No Connector";
	}

}
