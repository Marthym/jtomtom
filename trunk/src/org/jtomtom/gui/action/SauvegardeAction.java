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
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.jtomtom.JTomtom;
import org.jtomtom.JTomtomException;
import org.jtomtom.gui.PatienterDialog;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ConfigException;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660RootDirectory;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.CreateISO;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.ISO9660Config;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.ISOImageFileHandler;
import de.tu_darmstadt.informatik.rbg.hatlak.joliet.impl.JolietConfig;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.HandlerException;
import de.tu_darmstadt.informatik.rbg.mhartle.sabre.StreamHandler;

/**
 * @author marthym
 *
 */
public class SauvegardeAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(MajRadarsAction.class);
	
	private PatienterDialog m_waitingDialog = null;
	private JTextField m_fichierDestination;

	public SauvegardeAction (String p_label, JTextField p_iso) {
		super(p_label);
		m_fichierDestination = p_iso;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		
        SwingWorker<ActionResult, Void> worker = new SwingWorker<ActionResult, Void>() {

			@Override
			protected ActionResult doInBackground() throws Exception {
            	ActionResult result = new ActionResult(); 
                try {
                	String mountPoint = JTomtom.getTheGPS().getMountedPoint(false);
                	if (mountPoint.isEmpty()) {
                		throw new JTomtomException("Pas de point de montage !");
                	}
                	result.status = createGpsBackup(mountPoint);
					
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
		            	JOptionPane.showMessageDialog(null, result.exception.getLocalizedMessage(), "Erreur ! ", JOptionPane.ERROR_MESSAGE);
	            	}
            	} catch (ExecutionException e) {
            		LOGGER.warn(e.getLocalizedMessage());
            	} catch (InterruptedException e) {
            		LOGGER.warn(e.getLocalizedMessage());
				}
            }
        };
        m_waitingDialog = new PatienterDialog(worker);
        m_waitingDialog.setVisible(true);

	}
	
	/**
	 * Permet d'initialiser le chemin de l'ISO destination de la sauvegarde
	 * C'est surtout pour les tests, elle est pas utilisé dans le programme
	 * @param p_fichier	Chemin complet du fichier destination
	 */
	public void setFichierDestination(String p_fichier) {
		m_fichierDestination.setText(p_fichier);
	}
	
    /**
     * Crée un fichier ISO du GPS
     * @param p_mountPoint	Point de montage du GPS, source de l'ISO
     * @param isoFile		Destination de la sauvegarde, nom du fichier ISO
     * @return				true si OK
     * @throws JTomtomException
     * 			Retourne une exception si la source n'existe pas
     * 			Si la création de l'ISO ne se passe pas bien
     */
    public boolean createGpsBackup(String p_mountPoint) throws JTomtomException {
    	File gpsDir = new File(p_mountPoint);
    	if (!gpsDir.exists() || !gpsDir.canRead()) {
    		throw new JTomtomException("Le répertoire du GPS n'existe pas ou ne peut pas être lu !");
    	}
    	ISO9660RootDirectory isoRoot = new ISO9660RootDirectory();
    	try {
			isoRoot.addContentsRecursively(gpsDir);
		} catch (HandlerException e) {
			throw new JTomtomException(e);
		}
		
		if (m_fichierDestination == null || m_fichierDestination.getText() == null || m_fichierDestination.getText().isEmpty()) {
			throw new JTomtomException("Le chemin du fichier destination est obligatoire !");
		}
    	
		
		// ISO9660 support
		ISO9660Config iso9660Config = new ISO9660Config();
		try {
			iso9660Config.allowASCII(false);
			iso9660Config.setDataPreparer("jTomtom");
			iso9660Config.forceDotDelimiter(true);
			
		} catch (ConfigException e) {
			throw new JTomtomException(e);
		} 
		
		// Joliet support
		JolietConfig jolietConfig = new JolietConfig();
		try {
			jolietConfig.setDataPreparer("jTomtom");
			jolietConfig.forceDotDelimiter(true);
			
		} catch (ConfigException e) {
			throw new JTomtomException(e);
			
		}

		// Create ISO
		File outputIsoFile = new File(m_fichierDestination.getText());
		StreamHandler streamHandler = null;
		try {
			streamHandler = new ISOImageFileHandler(outputIsoFile);
			CreateISO iso = new CreateISO(streamHandler, isoRoot);
			LOGGER.debug("Début de la création du fichier ...");
			iso.process(iso9660Config, null, jolietConfig, null);
			
		} catch (FileNotFoundException e) {
			throw new JTomtomException(e);
			
		} catch (HandlerException e) {
			throw new JTomtomException(e);
			
		} 
		
		LOGGER.debug("Fin de la création de l'ISO.");
		return true;
	}

}
