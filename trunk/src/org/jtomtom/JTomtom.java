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

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jtomtom.device.Chipset;
import org.jtomtom.gui.JTomtomFenetre;
import org.jtomtom.gui.action.SendUserInformationsAction;

/**
 * @author Frédéric Combes
 *
 * Main class
 */
// TODO : Add possibility to set more the one login/password information in the settings tab
// TODO : Add experency date in the error message for GPS not found. Save expirency date in properties file. Maybe for more than one device
// TODO : Add Command line argument for start in DEBUG mode
public class JTomtom {
	static final Logger LOGGER = Logger.getLogger(JTomtom.class);
	
	public static void main(String[] args) {
		// Log initialisation
		PropertyConfigurator.configure(JTomtom.class.getResource(Constant.LOGGER_PROPERTIES));
				
		// Show licence message
		LOGGER.warn("jTomtom  Copyright (C) 2010  Frédéric Combes");
		LOGGER.warn("This program comes with ABSOLUTELY NO WARRANTY.");
		LOGGER.warn("This is free software, and you are welcome to redistribute it");
		LOGGER.warn("under certain conditions.");
		
		Application theApp = Application.getInstance();
		
		// First fo all, change Look at feel
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Application theApp = Application.getInstance();
				changeLookAndFeel(theApp.getGlobalProperties().getUserProperty("org.jtomtom.lookandfeel", UIManager.getSystemLookAndFeelClassName()));				
			}
		});
		
		if (!isJavaRuntimeVersionSixOrSuperior())
			return;
		
		// Try to initiate GPS
		try { theApp.getTheDevice(); } 
		catch (JTomtomException e) {
			SwingUtilities.invokeLater(new InitialErrorRun(e));	
			return;
		}
		
		// Initiate UI
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				JTomtomFenetre fenetre = new JTomtomFenetre();
				fenetre.setVisible(true);
			}
		});
		
		// Send information to jTomtom server if necessary
		if (isInformationsMustBeSend()) {
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					SendUserInformationsAction sendBackWorker = new SendUserInformationsAction();
					sendBackWorker.execute();
				}
			});
		}

	}

	private static boolean isJavaRuntimeVersionSixOrSuperior() {
		String version = System.getProperty("java.version", "0.0");
		String[] javaVersion = version.split("\\.");
		LOGGER.debug("Find Java Runtime : "+version);
		if (Integer.parseInt(javaVersion[0]) < 1) {
			SwingUtilities.invokeLater(new InitialErrorRun(
					new JTomtomException("org.jtomtom.errors.javaversion", new String[]{version})));
			return false;
		} else if (Integer.parseInt(javaVersion[0]) == 1 && Integer.parseInt(javaVersion[1]) < 6) {
			SwingUtilities.invokeLater(new InitialErrorRun(
					new JTomtomException("org.jtomtom.errors.javaversion", new String[]{version})));
			return false;
		}
		
		return true;
	}

	/**
	 * Initiate application look at feel
	 * @param p_lafName	Name of the new look
	 */
	private static void changeLookAndFeel(String p_lafName) {
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if (p_lafName.equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {}
	}
	
	private final static boolean isInformationsMustBeSend() {
		boolean sendInformations = true;
		Application theApp = Application.getInstance();
		
		if (!"true".equals(theApp.getGlobalProperties().getUserProperty("org.jtomtom.sendbackinformations", "true"))) {
			sendInformations = false;
		}
		if (sendInformations && theApp.getTheDevice().getChipset() == Chipset.UNKNOWN) {
			sendInformations = false;
		}
		return sendInformations;
	}

}
