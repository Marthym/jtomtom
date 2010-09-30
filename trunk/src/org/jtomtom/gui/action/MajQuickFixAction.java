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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
		// Téléchargement du fichier de mise à jour
		LOGGER.info("Téléchargement de la mise à jour QuickFix...");
		HttpURLConnection conn = null;
		FileOutputStream fout = null;
		InputStream is = null;
		File ephemFile = null;
		try {
			if (LOGGER.isDebugEnabled()) LOGGER.debug("tomtomQuickFixURL = "+Constant.URL_EPHEMERIDE+theGPS.getChipset());
			URL tomtomQuickFixURL = new URL(Constant.URL_EPHEMERIDE+theGPS.getChipset());
			
			ephemFile = File.createTempFile("ephemeride", ".cab");
			ephemFile.deleteOnExit();
			
			conn = (HttpURLConnection) tomtomQuickFixURL.openConnection(JTomtom.getApplicationProxy());
			
			conn.setRequestProperty ( "User-agent", Constant.TOMTOM_USER_AGENT);
			conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setReadTimeout(Constant.TIMEOUT); // TimeOut en cas de perte de connexion
            conn.connect();

            int currentSize = 0;
			if (LOGGER.isDebugEnabled()) LOGGER.debug("conn.getResponseCode() = "+conn.getResponseCode());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	int fileSize = conn.getContentLength();
            	is = conn.getInputStream();
                fout = new FileOutputStream(ephemFile);
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
		
		// - Extraction de l'archive
		LOGGER.info("Décompression de la mise à jour QuickFix...");
		CabFile ephemCabFile = null;
		try {
			ephemCabFile = new CabFile(ephemFile);
		} catch (IOException e) {
			throw new JTomtomException(e);
		}
		
		List<File> ephemFiles = new ArrayList<File>();
		
		InputStream cabis = null;
		BufferedOutputStream dest = null;
		FileOutputStream fos = null;
		try {
			
			final int BUFFER = 2048;

			CabEntry[] entries = ephemCabFile.getEntries();
			for (int i = 0; i < entries.length; i++) {
				int count;
				CabEntry entry =  entries[i];
				
				cabis = ephemCabFile.getInputStream(entry);
	            byte data[] = new byte[BUFFER];
	            
	            File current = new File(ephemFile.getParent(), entry.getName());
	            ephemFiles.add(current);
	            fos = new FileOutputStream(current);
	            dest = new BufferedOutputStream(fos, BUFFER);
	            while((count = cabis.read(data)) != -1) {
	            	dest.write(data, 0, count);
	            }
	            dest.flush();
	            dest.close();
	            
	            LOGGER.debug("Extraction de "+current.getName()+" dans "+current.getParent());
			}
			
		} catch (FileNotFoundException e) {
			throw new JTomtomException(e);
			
		} catch (IOException e) {
			throw new JTomtomException(e);
			
		} finally {
			LOGGER.debug("Fermeture des flux de décompression...");
			try {cabis.close();} catch (Exception e){};
			try {dest.close();} catch (Exception e){};
			try {fos.close();} catch (Exception e){};
		}
		
		// - Installation des la mise à jour
		LOGGER.info("Installation des ephemerides dans le GPS...");
		theGPS.updateQuickFix(ephemFiles);
	}
}
