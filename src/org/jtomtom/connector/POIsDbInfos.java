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
package org.jtomtom.connector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Container of POIs DB Informations
 * @author Frédéric Combes
 *
 */
public class POIsDbInfos {
	public final static Logger LOGGER = Logger.getLogger(POIsDbInfos.class);
	
	/**
	 * Default value for the DB Version
	 */
	public final static String UNKNOWN = "Unknown";
	
	private Date m_lastUpdateDate;
	private int m_poisNumber;
	private String m_dbVersion;
	
	/**
	 * Constructor of initialisation
	 */
	public POIsDbInfos() {
		m_lastUpdateDate = new Date(0);
		m_poisNumber = -1;
		m_dbVersion = UNKNOWN;
	}

	public Date getLastUpdateDate() {
		return m_lastUpdateDate;
	}

	public void setLastUpdateDate(Date m_lastUpdateDate) {
		this.m_lastUpdateDate = m_lastUpdateDate;
	}

	/**
	 * Set the last modified date of the POIs DB
	 * @param p_format	Date format
	 * @param p_date	Date as String
	 */
	public void setLastUpdateDate(String p_format, String p_date) {
		try {
			this.m_lastUpdateDate = (new SimpleDateFormat(p_format)).parse(p_date);
		} catch (ParseException e) {
			LOGGER.warn("Error while parsing last update date of POIs Database !");
			LOGGER.debug(e);
		}
	}

	public int getPoisNumber() {
		return m_poisNumber;
	}

	public void setPoisNumber(int m_poisNumber) {
		this.m_poisNumber = m_poisNumber;
	}

	public String getDbVersion() {
		return m_dbVersion;
	}

	public void setDbVersion(String m_dbVersion) {
		this.m_dbVersion = m_dbVersion;
	}
	
	
}
