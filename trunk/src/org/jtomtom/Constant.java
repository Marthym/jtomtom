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
package org.jtomtom;

/**
 * @author marthym
 *
 */
public interface Constant {
	public static final String LOGGER_PROPERTIES = "conf/logger.properties";
	public static final String JTOMTOM_PROPERTIES = "conf/jtomtom.properties";
	public static final String JTOMTOM_USER_PROPERTIES = ".jtomtom.properties";
	
	public static final String URL_EPHEMERIDE = "http://home.tomtom.com/download/Ephemeris.cab?type=ephemeris&devicecode=1&eeProvider=";
	
	public static final String TOMTOM_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; fr-FR; rv:1.9.1b4) Gecko/20090605 TomTomHOME/2.7.3.1894";
	public static final int TIMEOUT = 60000;	// 60s
}
