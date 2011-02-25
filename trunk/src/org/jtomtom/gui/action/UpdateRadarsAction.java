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
package org.jtomtom.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.JTomtomException;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.device.TomtomMap;
import org.jtomtom.gui.WaitingDialog;
import org.jtomtom.gui.TabRadars;
import org.jtomtom.tools.NetworkTester;

/**
 * @author Frédéric Combes
 *
 */
public class UpdateRadarsAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(UpdateRadarsAction.class);
	
	private WaitingDialog waitingDialog = null;
	private TabRadars tabRadars = null;
	
	public UpdateRadarsAction (String p_label) {
		super(p_label);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// Get panel which call action for refresh later
		if (JButton.class.isAssignableFrom(arg0.getSource().getClass())) {
			JButton btRadars = (JButton)arg0.getSource();
			if (TabRadars.class.isAssignableFrom(btRadars.getParent().getClass())) {
				tabRadars = (TabRadars)btRadars.getParent();
			}
		}
		
        SwingWorker<ActionResult, Void> worker = new SwingWorker<ActionResult, Void>() {
            @Override
            public ActionResult doInBackground() {
            	Application theApp = Application.getInstance();
            	
            	ActionResult result = new ActionResult(); 
                try {
                	NetworkTester.getInstance().validNetworkAvailability(theApp.getProxyServer());
                	result.status = updateRadars(theApp.getTheDevice(), tabRadars.getMapsCheckList(), tabRadars.getSelectedRadarConnector());
					
				} catch (JTomtomException e) {
					result.status = false;
					result.exception = e;
				}
                
                return result;
            }

            @Override
            public void done() {
            	// First of all we close waiting dialog
            	if (waitingDialog != null) {
            		waitingDialog.dispose();
            	}
            	
            	// Second we check there are no error
            	ActionResult result = null;
            	try {
            		result = get();
	            	if (!result.status) {
	            		// If error occured we show message
		            	JOptionPane.showMessageDialog(null, result.exception.getLocalizedMessage(), 
		            			Application.getInstance().getMainTranslator().getString("org.jtomtom.main.dialog.default.error.title"), 
		            			JOptionPane.ERROR_MESSAGE);
	            	}
            	} catch (ExecutionException e) {
            		LOGGER.warn(e.getLocalizedMessage());
            	} catch (InterruptedException e) {
            		LOGGER.warn(e.getLocalizedMessage());
				}
            	
            	// Finally we refresh the tab
            	if (tabRadars != null && (result == null || result.status)) {
            		tabRadars.disableRefreshButton();
            		tabRadars.loadRadarsInfos();
            	}
            }
        };
        waitingDialog = new WaitingDialog(worker);
        waitingDialog.setVisible(true);

	}

	public boolean updateRadars(TomtomDevice theGPS, List<JCheckBox> p_checkList, RadarsConnector p_radars) {	
		LOGGER.info("Download radars update files ...");
		Application theApp = Application.getInstance();
		
		boolean connStatus = p_radars.connexion(
				theApp.getProxyServer(), 
				theApp.getGlobalProperties().getUserProperty("org.connector.user."+p_radars.getLocale()), 
				theApp.getGlobalProperties().getUserProperty("org.connector.password."+p_radars.getLocale()));
		if (!connStatus) {
			throw new JTomtomException("org.jtomtom.errors.radars.tomtomax.account");
		}
		
		HttpURLConnection conn = null;
		FileOutputStream fout = null;
		InputStream is = null;
		File radarsZipFile = null;
		try {
			conn = p_radars.getConnectionForUpdate();
			// We check if we need to download PremiumPack
			for (JCheckBox chk : p_checkList) { 
				if (chk.isSelected()) {
					TomtomMap map = theGPS.getAvailableMaps().get(chk.getText());
					if (map != null && map.getRadarsInfos(p_radars).isEmpty()) {
						conn = p_radars.getConnectionForInstall();
					}
				}
			}
						
			if (LOGGER.isDebugEnabled()) LOGGER.debug("RadarsPOIURL = "+conn.getURL());
			
			radarsZipFile = File.createTempFile("radars", ".zip");
			radarsZipFile.deleteOnExit();
			
            conn.connect();

            int currentSize = 0;
			if (LOGGER.isDebugEnabled()) LOGGER.debug("conn.getResponseCode() = "+conn.getResponseCode());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	int fileSize = conn.getContentLength();
            	is = conn.getInputStream();
                fout = new FileOutputStream(radarsZipFile);
                byte buffer1[] = new byte[1024*128];
                int k=0;

                while( (k = is.read(buffer1)) != -1 ){
                    fout.write(buffer1,0,k);
                    currentSize += k;
                    
                    if (waitingDialog != null) { // Need for junit test
                    	waitingDialog.refreshProgressBar(currentSize, fileSize);
                    }
                }

            } else {
            	throw new JTomtomException("org.jtomtom.errors.connexion.fail", 
            			Integer.toString(conn.getResponseCode()), conn.getResponseMessage());
            }
			
		} catch (MalformedURLException e) {
			throw new JTomtomException(e);
			
		} catch (IOException e) {
			throw new JTomtomException(e);
			
		} finally {
			LOGGER.debug("Close all download stream ...");
			conn.disconnect();
			try {fout.close();} catch (Exception e){}
			try {is.close();} catch (Exception e){}
		}
		
		if (waitingDialog != null) { // Need for junit test
        	waitingDialog.refreshProgressBar(0, 0);
        }
		
		LOGGER.info("Unzip radars update file ...");
		FileInputStream fin = null;
	    ZipInputStream zin = null;
	    ZipEntry ze = null;
	    List<File> filesToInstall = new LinkedList<File>();
	    try {
	    	fin = new FileInputStream(radarsZipFile);
	    	zin = new ZipInputStream(fin);
		    while ((ze = zin.getNextEntry()) != null) {
		    	File currOutput = new File(radarsZipFile.getParent(), ze.getName());
		    	filesToInstall.add(currOutput);
		        fout = new FileOutputStream(currOutput);
		        for (int c = zin.read(); c != -1; c = zin.read()) {
		        	fout.write(c);
		        }
		        zin.closeEntry();
		        fout.close();
		    }
		    zin.close();
		    
	    } catch (FileNotFoundException e) {
			throw new JTomtomException(e);
			
		} catch (IOException e) {
			throw new JTomtomException(e);
			
		} finally {
	    	try {fout.close();} catch (Exception e) {}
	    	try {zin.close();} catch (Exception e) {}
	    	try {fin.close();} catch (Exception e) {}
	    }
		
		LOGGER.info("Install the radars update files ...");
		boolean retour = true;
		for (JCheckBox chk : p_checkList) {
			if (chk.isSelected()) {
				TomtomMap map = theGPS.getAvailableMaps().get(chk.getText());
				if (map != null) {
					retour &= map.updateRadars(filesToInstall, p_radars);
				}
			}
		}
		
		// Now we clean temp directory
		if (retour) {
			radarsZipFile.delete();
			for (File fileToDelete : filesToInstall) {
				fileToDelete.delete();
			}
		}
		return retour;
		
	}
}
