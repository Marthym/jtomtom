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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import net.sf.jcablib.CabEntry;
import net.sf.jcablib.CabFile;

import org.apache.log4j.Logger;
import org.jtomtom.Constant;
import org.jtomtom.JTomtom;
import org.jtomtom.JTomtomException;
import org.jtomtom.device.Chipset;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.gui.PatienterDialog;
import org.jtomtom.gui.TabQuickFix;
import org.jtomtom.tools.NetworkTester;

/**
 * @author marthym
 *
 */
public class MajQuickFixAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(MajQuickFixAction.class);
	
	private PatienterDialog m_waitingDialog = null;
	private TabQuickFix m_tabQuickFix = null;

	public MajQuickFixAction (String p_label) {
		super(p_label);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// On récupère l'onglet qui a appelé l'action pour le rafraichir plus tard
		if (JButton.class.isAssignableFrom(arg0.getSource().getClass())) {
			JButton btQF = (JButton)arg0.getSource();
			if (TabQuickFix.class.isAssignableFrom(btQF.getParent().getClass())) {
				m_tabQuickFix = (TabQuickFix)btQF.getParent();
			}
		}
		
        SwingWorker<ActionResult, Void> worker = new SwingWorker<ActionResult, Void>() {
            @Override
            public ActionResult doInBackground() {
            	ActionResult result = new ActionResult(); 
                try {
                	NetworkTester.getInstance().validNetworkAvailability(JTomtom.getApplicationProxy());
                	miseAJourQuickFix(JTomtom.getTheGPS());
                	result.status = true;
					
				} catch (JTomtomException e) {
					result.status = false;
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
            	
            	if (m_tabQuickFix != null && (result == null || result.status)) {
            		m_tabQuickFix.loadQuickFixInfos();
            	}
            }
        };
        m_waitingDialog = new PatienterDialog(worker);
        m_waitingDialog.setVisible(true);
	}

	/**
	 * Fonction effectuant la mise à jour du GPS en téléchargeant les fichiers de la mise à jour QuickFix
	 * @param theGPS	Le GPS à mettre à jour
	 * @throws JTomtomException
	 */
	public void miseAJourQuickFix(TomtomDevice theGPS) throws JTomtomException {		
		
		List<URL> filesToDownload = getEphemeridFilesURL(theGPS.getChipset());
		List<File> filesToUncab = downloadEphemeridFiles(filesToDownload);
		
		updateProgressBar(0, 0);
		
		Set<File> filesToInstall = uncabEphemeridFiles(filesToUncab);
		theGPS.updateQuickFix(filesToInstall);
	}
	
	private static final List<URL> getEphemeridFilesURL(Chipset gpsChipset) {
		List<URL> filesUrl = new LinkedList<URL>();
		try {
			if (gpsChipset == Chipset.UNKNOWN) {
				for(Chipset cs : Chipset.available()) {
					filesUrl.add(new URL(Constant.URL_EPHEMERIDE+cs));
				}
			} else {
				filesUrl.add(new URL(Constant.URL_EPHEMERIDE+gpsChipset));
			}
			
		} catch (MalformedURLException e) {
			LOGGER.warn(e);
		}
		return filesUrl;
	}
	
	private final List<File> downloadEphemeridFiles(List<URL> filesLocations) {
		List<File> downloadedFiles = new LinkedList<File>();
		try {
			for (URL oneFileLocation : filesLocations) {
				File ephemFile = File.createTempFile("ephem", ".cab");
				ephemFile.deleteOnExit();
							
				HttpURLConnection conn = (HttpURLConnection) oneFileLocation.openConnection(JTomtom.getApplicationProxy());
				conn.setRequestProperty ( "User-agent", Constant.TOMTOM_USER_AGENT);
				conn.setDoInput(true);
	            conn.setUseCaches(false);
	            conn.setReadTimeout(Constant.TIMEOUT); // TimeOut en cas de perte de connexion
	            conn.connect();
	            
	            int currentSize = 0;
				if (LOGGER.isDebugEnabled()) LOGGER.debug("conn.getResponseCode() = "+conn.getResponseCode());
				
				int connResponseCode = conn.getResponseCode();
				if (connResponseCode != HttpURLConnection.HTTP_OK) {
					throw new JTomtomException("org.jtomtom.errors.connexion.fail", 
	            			new String[]{Integer.toString(connResponseCode),conn.getResponseMessage()});
				}

            	int fileSize = conn.getContentLength();
            	InputStream is = null;
            	FileOutputStream fout = null;
            	try {
            		
	            	is = conn.getInputStream();
	            	fout = new FileOutputStream(ephemFile);
	                byte buffer1[] = new byte[1024*128];
	                int k=0;
	
	                while( (k = is.read(buffer1)) != -1 ){
	                    fout.write(buffer1,0,k);
	                    currentSize += k;
	                    
	                    updateProgressBar(currentSize, fileSize);
	                }
	            	
	                downloadedFiles.add(ephemFile);
	                
            	} finally {
        			try {fout.close();} catch (Exception e){};
        			try {is.close();} catch (Exception e){};
        			conn.disconnect();
            	}
	            	
			} // end for (URL oneFileLocation : filesLocations)
			
		} catch (IOException e) {
			throw new JTomtomException("Enable to download ephemerid files !", e);
			
		} 
		return downloadedFiles;
	}
	
	private final static Set<File> uncabEphemeridFiles(List<File> cabFiles) {
		Set<File> uncompressedFiles = new LinkedHashSet<File>();
		final int BUFFER = 2048;
		
		for (File oneCabinetFile : cabFiles) {
			InputStream cabis = null;
			BufferedOutputStream dest = null;
			FileOutputStream fos = null;
			
			CabFile theCabinet;
			try {
				theCabinet = new CabFile(oneCabinetFile);
				CabEntry[] entries = theCabinet.getEntries();
				
				for (CabEntry entry : entries) {
					int count;
					
					cabis = theCabinet.getInputStream(entry);
		            byte data[] = new byte[BUFFER];
		            
		            File current = new File(oneCabinetFile.getParent(), entry.getName());
		            
		            fos = new FileOutputStream(current);
		            dest = new BufferedOutputStream(fos, BUFFER);
		            while((count = cabis.read(data)) != -1) {
		            	dest.write(data, 0, count);
		            }
		            dest.flush();
		            dest.close();
		            
		            LOGGER.debug("Extract "+current.getName()+" in "+current.getParent());
		            uncompressedFiles.add(current);
				}
				
			} catch (IOException e) {
				throw new JTomtomException(e);
				
			} finally {
				LOGGER.debug("Fermeture des flux de décompression...");
				try {cabis.close();} catch (Exception e){};
				try {dest.close();} catch (Exception e){};
				try {fos.close();} catch (Exception e){};				
			}

		} // end for (File oneCabinetFile : cabFiles)
		
		return uncompressedFiles;
	}
	
	private void updateProgressBar(int current, int max) {
        if (m_waitingDialog != null) { // Need for test case
        	m_waitingDialog.refreshProgressBar(current, max);
        }
	}
}
