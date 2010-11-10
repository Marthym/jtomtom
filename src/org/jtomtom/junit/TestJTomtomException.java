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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.JTomtomException;

//import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;


public class TestJTomtomException {
	private static final Logger LOGGER = Logger.getLogger(TestJTomtomException.class);
	
	@BeforeClass
	public static void initLogger() {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
			BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	@Test
	public void testThrowingException() {
		Exception e = new JTomtomException("Message d'erreur tout con ...");
		LOGGER.info(e.getLocalizedMessage());
		
		e = new JTomtomException("Message avec des %1 un peu %2", new String[]{"trous", "partout"});
		LOGGER.info(e.getLocalizedMessage());
		
		e = new JTomtomException("org.jtomtom.errors.gps.nomountpoint");
		LOGGER.info(e.getLocalizedMessage());
		
		e = new JTomtomException("org.jtomtom.errors.connexion.fail", new String[]{"trous", "partout"});
		LOGGER.info(e.getLocalizedMessage());	
	}
}
