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
import org.jtomtom.GlobalPositioningSystem;
import org.jtomtom.GpsMap;
import org.jtomtom.JTomtom;
import org.jtomtom.JTomtomException;
import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.gui.PatienterDialog;
import org.jtomtom.gui.TabRadars;

/**
 * @author marthym
 *
 */
public class MajRadarsAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(MajRadarsAction.class);
	
	private PatienterDialog m_waitingDialog = null;
	private TabRadars m_tabRadars = null;
	
	public MajRadarsAction (String p_label) {
		super(p_label);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// On récupère l'onglet qui a appelé l'action pour le rafraichir plus tard
		if (JButton.class.isAssignableFrom(arg0.getSource().getClass())) {
			JButton btRadars = (JButton)arg0.getSource();
			if (TabRadars.class.isAssignableFrom(btRadars.getParent().getClass())) {
				m_tabRadars = (TabRadars)btRadars.getParent();
			}
		}
		
        SwingWorker<ActionResult, Void> worker = new SwingWorker<ActionResult, Void>() {
            @Override
            public ActionResult doInBackground() {
            	ActionResult result = new ActionResult(); 
                try {
                	result.status = miseAJourRadars(JTomtom.getTheGPS(), m_tabRadars.getMapsCheckList(), m_tabRadars.getSelectedRadarConnector());
					
				} catch (JTomtomException e) {
					LOGGER.error(e.getLocalizedMessage());
					if (LOGGER.isDebugEnabled()) e.printStackTrace();
					result.exception = e;
				}
                
                return result;
            }

            @Override
            public void done() {
            	// Déjà, on ferme la fenêtre d'attente
            	if (m_waitingDialog != null) {
            		m_waitingDialog.dispose();
            	}
            	
            	// Ensuite, on regarde s'il y a eu une erreur
            	ActionResult result = null;
            	try {
            		result = get();
	            	if (!result.status) {
	            		// En cas d'erreur on affiche un message
		            	JOptionPane.showMessageDialog(null, result.exception.getLocalizedMessage(), 
		            			JTomtom.theMainTranslator.getString("org.jtomtom.main.dialog.default.error.title"), JOptionPane.ERROR_MESSAGE);
	            	}
            	} catch (ExecutionException e) {
            		LOGGER.warn(e.getLocalizedMessage());
            	} catch (InterruptedException e) {
            		LOGGER.warn(e.getLocalizedMessage());
				}
            	
            	// Finalement on rafraichit l'onglet
            	if (m_tabRadars != null && (result == null || result.status)) {
            		m_tabRadars.disableRefreshButton();
            		m_tabRadars.loadRadarsInfos();
            	}
            }
        };
        m_waitingDialog = new PatienterDialog(worker);
        m_waitingDialog.setVisible(true);

	}

	public boolean miseAJourRadars(GlobalPositioningSystem theGPS, List<JCheckBox> p_checkList, RadarsConnector p_radars) 
	throws JTomtomException {	
		// Téléchargement du fichier de mise à jour
		LOGGER.info("Téléchargement de la mise à jour Radar ...");
		
		boolean connStatus = p_radars.connexion(
				JTomtom.getApplicationProxy(), 
				JTomtom.theProperties.getUserProperty("org.tomtomax.user"), 
				JTomtom.theProperties.getUserProperty("org.tomtomax.password"));
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
					GpsMap map = theGPS.getAllMaps().get(chk.getText());
					if (map != null && POIsDbInfos.UNKNOWN.equals(map.getRadarsDbVersion())) {
						conn = p_radars.getConnectionForInstall();
					}
				}
			}
						
			if (LOGGER.isDebugEnabled()) LOGGER.debug("RadarsPOIURL = "+conn.getURL());
			
			radarsZipFile = File.createTempFile("tomtomax_radars", ".zip");
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
                    
                    if (m_waitingDialog != null) { // On a besoin de ça pour les tests
                    	m_waitingDialog.refreshProgressBar(currentSize, fileSize);
                    }
                }

            } else {
            	throw new JTomtomException("org.jtomtom.errors.connexion.fail", 
            			new String[]{Integer.toString(conn.getResponseCode()),conn.getResponseMessage()});
            }
			
		} catch (MalformedURLException e) {
			throw new JTomtomException(e);
			
		} catch (IOException e) {
			throw new JTomtomException(e);
			
		} finally {
			LOGGER.debug("Fermeture des tout les flux de téléchargement");
			conn.disconnect();
			try {fout.close();} catch (Exception e){}
			try {is.close();} catch (Exception e){}
		}
		
		if (m_waitingDialog != null) { // On a besoin de ça pour les tests
        	m_waitingDialog.refreshProgressBar(0, 0);
        }
		
		LOGGER.info("Décompression du fichier de radars ...");
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
		
		LOGGER.info("Installation de la mise à jour Tomtomax ...");
		boolean retour = true;
		for (JCheckBox chk : p_checkList) {
			if (chk.isSelected()) {
				GpsMap map = theGPS.getAllMaps().get(chk.getText());
				if (map != null) {
					retour &= map.updateRadars(filesToInstall);
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
