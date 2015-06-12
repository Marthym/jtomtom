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
package org.jtomtom.connector.radars;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Date;

import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.connector.RadarsConnector;

/**
 * @author Frédéric Combes
 *
 * Dummy RadarConnector for unfound connector
 */
public class DummyRadarsConnector extends RadarsConnector {
	private static final String UPDATE_URL = "http://localhost/";
	private static final Date UPDATE_DATE = new Date();
	
	@Override
	public POIsDbInfos getLocalDbInfos(String m_path) {
		POIsDbInfos dbInfos = new POIsDbInfos();
		dbInfos.setDatabaseVersion("1");
		dbInfos.setLastUpdateDate(UPDATE_DATE);
		dbInfos.setNumberOfPOIs(1);
		return dbInfos;
	}

	@Override
	public POIsDbInfos getRemoteDbInfos(Proxy proxy) {
		POIsDbInfos dbInfos = new POIsDbInfos();
		dbInfos.setDatabaseVersion("1");
		dbInfos.setLastUpdateDate(UPDATE_DATE);
		dbInfos.setNumberOfPOIs(1);
		return dbInfos;
	}

	@Override
	public boolean connexion(Proxy p_proxy, String p_user, String p_password) {
		return true;
	}

	@Override
	public HttpURLConnection getConnectionForUpdate() {
		URL myURL = null;
		HttpURLConnection conn = null;
		try {
			myURL = new URL(UPDATE_URL);
			conn = (HttpURLConnection) myURL.openConnection();
		} catch (Exception e) {}
		
		return conn;
	}

	@Override
	public HttpURLConnection getConnectionForInstall() {
		URL myURL = null;
		HttpURLConnection conn = null;
		try {
			myURL = new URL(UPDATE_URL);
			conn = (HttpURLConnection) myURL.openConnection();
		} catch (Exception e) {}
		
		return conn;
	}
	
	@Override
	public String toString() {
		return "No Connector";
	}

	@Override
	public String getConnectorWebsite() {
		return UPDATE_URL;
	}

	@Override
	public String getLocale() {
		return "na_NA";
	}
}
