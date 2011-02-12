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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.JTomtomException;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.connector.radars.Tomtomax;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.gui.action.CheckUpdateAction;
import org.jtomtom.gui.action.MajQuickFixAction;
import org.jtomtom.gui.action.MajRadarsAction;
import org.jtomtom.gui.action.IsoBackupAction;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestActions {
	@BeforeClass
	public static void initLogger() {
		if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
			BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	@Test
	public void testMajQuickFixAction() {
		try {
			
			TomtomDevice theGPS = new TomtomDevice();
			assertNotNull(theGPS);
			
			removeEphemerideMetaFile(theGPS);
			
			MajQuickFixAction action = new MajQuickFixAction("test QF");
			action.miseAJourQuickFix(theGPS);
	
			assertTrue(theGPS.theFiles.getEphemeridMeta().exists());
			
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		} catch (FileNotFoundException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	private static void removeEphemerideMetaFile(TomtomDevice gps) {
		try {
			File meta = gps.theFiles.getEphemeridMeta();
			meta.delete();
			assertFalse(meta.exists());
		} catch (FileNotFoundException e) {}
	}

	@Test
	public void testMajRadarsTomtomaxAction() {
		
		TomtomDevice theGPS = null;
		try {
			theGPS = new TomtomDevice();
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(theGPS);
		
		// - We delete TTMax DB file for be sure that the update is good
		File ttmaxDbFile = new File(theGPS.getActiveMap().getPath(), Tomtomax.TOMTOMAX_DB_FILE);
		if (ttmaxDbFile.exists() && ttmaxDbFile.canWrite()) ttmaxDbFile.delete();
		
		MajRadarsAction action = new MajRadarsAction("test Tomtomax");
		RadarsConnector ttMaxRadars = RadarsConnector.createFromClass(Tomtomax.class);
		try {
			List<JCheckBox> chkList = new LinkedList<JCheckBox>();
			chkList.add(new JCheckBox(theGPS.getActiveMap().getName(), true));
			
			action.miseAJourRadars(theGPS, chkList, ttMaxRadars);
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		
		assertTrue(ttmaxDbFile.exists());
		
		assertFalse(theGPS.getActiveMap().getRadarsInfos(ttMaxRadars).isEmpty());
	}
	
	@Test
	public void testCreateBackupAction() {
		TomtomDevice theGPS = null;
		try {
			theGPS = new TomtomDevice();
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(theGPS);
		
		IsoBackupAction action = new IsoBackupAction("test TTM");
		action.setFichierDestination("/tmp/testgpsbackup.iso");
		try {
			action.createGpsBackup(theGPS);
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		assertTrue((new File("/tmp/testgpsbackup.iso")).exists());
	}
	
	@Test
	public void testCreateBackupfortestAction() {
		TomtomDevice theGPS = null;
		try {
			theGPS = new TomtomDevice();
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(theGPS);
		
		IsoBackupAction action = new IsoBackupAction("test TTM");
		action.setFichierDestination("/tmp/testgpsbackup.iso");
		try {
			action.createGpsBackup(theGPS, true);
		} catch (JTomtomException e) {
			fail(e.getLocalizedMessage());
		}
		assertTrue((new File("/tmp/testgpsbackup.iso")).exists());
	}
	
	@Test
	public void testCheckUpdateAction() {
		String message = null;
		message = CheckUpdateAction.checkUpdateNow();
		assertNull(message);
	}

	@AfterClass
	public static void nettoyage() {
		// Nettoyage des fichiers laissé par testMajRadarsAction
		File currDir = new File("/", "tmp");
		String[] filesToDelete = currDir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return (name.endsWith(".ov2"));
			}
		});
		
		for (String current : filesToDelete) {
			File fDelete = new File(current);
			fDelete.delete();
		}
		
		File fDelete = new File("0 version_radars_Tomtomax.bmp");
		fDelete.delete();
		fDelete = new File("maxipoi_radars.db");
		fDelete.delete();
		
		fDelete = new File("/tmp/testgpsbackup.iso");
		fDelete.delete();
	}

}
