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
package org.jtomtom.connector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * @author Frédéric Combes
 *
 * Container of POIs DB Informations
 */
public class POIsDbInfos {
	public final static Logger LOGGER = Logger.getLogger(POIsDbInfos.class);
	
	public static final String NA = "N/A";
	
	private Date lastUpdateDate;
	private Integer numberOfPOIs;
	private String databaseVersion;
	
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}
	
	public String getLastUpdateDateForPrint(String dateFormat) {
		if (lastUpdateDate == null) {
			return NA;
		} else {
			return (new SimpleDateFormat(dateFormat)).format(lastUpdateDate);
		}
	}
	
	public String getLastUpdateDateForPrint() {
		LOGGER.debug("Default date format : "+new SimpleDateFormat().toPattern());
		return getLastUpdateDateForPrint(new SimpleDateFormat().toPattern());
	}

	public void setLastUpdateDate(Date m_lastUpdateDate) {
		this.lastUpdateDate = m_lastUpdateDate;
	}

	/**
	 * Set the last modified date of the POIs DB
	 * @param p_format	Date format
	 * @param p_date	Date as String
	 */
	public void setLastUpdateDate(String dateFormat, String p_date) {
		try {
			this.lastUpdateDate = (new SimpleDateFormat(dateFormat)).parse(p_date);
		} catch (ParseException e) {
			LOGGER.warn("Error while parsing last update date of POIs Database !");
			LOGGER.debug(e);
		}
	}

	public Integer getNumberOfPOIs() {
		return numberOfPOIs;
	}
	
	public String getNumberOfPOIsForPrint() {
		if (numberOfPOIs == null) return "0";
		else return numberOfPOIs.toString();
	}

	public void setNumberOfPOIs(int m_poisNumber) {
		this.numberOfPOIs = m_poisNumber;
	}

	public String getDatabaseVersion() {
		return databaseVersion;
	}
	
	public String getDatabaseVersionForPrint() {
		if (databaseVersion == null)
			return NA;
		else
			return databaseVersion;
	}

	public void setDatabaseVersion(String m_dbVersion) {
		this.databaseVersion = m_dbVersion;
	}
	
	public boolean isEmpty() {
		return (lastUpdateDate == null);
	}
	
}
