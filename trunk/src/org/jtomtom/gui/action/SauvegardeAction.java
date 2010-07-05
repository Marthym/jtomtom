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
import java.io.FilenameFilter;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.jtomtom.GlobalPositioningSystem;
import org.jtomtom.GpsMap;
import org.jtomtom.JTomtom;
import org.jtomtom.JTomtomException;
import org.jtomtom.gui.PatienterDialog;
import org.jtomtom.gui.TabSauvegarde;

import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ConfigException;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660Directory;
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
	private static final Logger LOGGER = Logger.getLogger(SauvegardeAction.class);
	
	private PatienterDialog m_waitingDialog = null;
	private String m_fichierDestination;
	private boolean m_makeTestISO = false;

	public SauvegardeAction (String p_label) {
		super(p_label);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (JButton.class.isAssignableFrom(event.getSource().getClass())) {
			JButton bp = (JButton)event.getSource();
			if (TabSauvegarde.class.isAssignableFrom(bp.getParent().getClass())) {
				TabSauvegarde tabSauvegarde = (TabSauvegarde)bp.getParent();
				m_fichierDestination = tabSauvegarde.getFichierDestination();
				m_makeTestISO = tabSauvegarde.getMakeTestISO();
			} 
		}
		
        SwingWorker<ActionResult, Void> worker = new SwingWorker<ActionResult, Void>() {

			@Override
			protected ActionResult doInBackground() throws Exception {
            	ActionResult result = new ActionResult(); 
                try {
                	String mountPoint = JTomtom.getTheGPS().getMountedPoint(false);
                	if (mountPoint.isEmpty()) {
                		throw new JTomtomException("org.jtomtom.errors.gps.nomountpoint");
                	}
                	result.status = createGpsBackup(JTomtom.getTheGPS());
					
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
		m_fichierDestination = p_fichier;
	}
	
    /**
     * Crée un fichier ISO du GPS
     * @param p_mountPoint	Point de montage du GPS, source de l'ISO
     * @param p_forTest		Vrai si l'ISO est un fichier pour test (option caché coché), il sera alors réduit
     * @return				true si OK
     * @throws JTomtomException
     * 			Retourne une exception si la source n'existe pas
     * 			Si la création de l'ISO ne se passe pas bien
     */
    public boolean createGpsBackup(GlobalPositioningSystem p_GPS, boolean p_forTest) throws JTomtomException {
    	File gpsDir = new File(p_GPS.getMountedPoint(false));
    	if (!gpsDir.exists() || !gpsDir.canRead()) {
    		throw new JTomtomException("org.jtomtom.errors.gps.directorycannotberead");
    	}
    	ISO9660RootDirectory isoRoot = new ISO9660RootDirectory();
    	try {
    		if (!p_forTest) {
    			isoRoot.addContentsRecursively(gpsDir);
    			
    		} else {
    			LOGGER.debug("Restriction du contenu de l'ISO pour fichier de test");
    			// - Si on fait un ISO pour test, on ne mets que le nécessaire dedans ...
    			java.util.Map<String, GpsMap> mapsList = p_GPS.getAllMaps();
    			isoRoot.addDirectory(gpsDir);
    			for (String currFileName: gpsDir.list()) {
    				File current = new File(gpsDir, currFileName);
    				
    				if (mapsList.containsKey(current.getName())) {
    					LOGGER.debug("Suppression des fichiers DAT de la carte "+current.getName());
    					// On nettoie les répertoires de cartes pour enlever le gros fichiers
    					ISO9660Directory isoCurrDir = isoRoot.addDirectory(current);
    					String[] mapFileList = current.list(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								return !name.endsWith(".dat");
							}
						});
    					
    					for (String currMapFileName : mapFileList) {
    						isoCurrDir.addRecursively(new File(current, currMapFileName));
    					}
    					
    				} else if ("voices".equalsIgnoreCase(current.getName()) ) {
    					LOGGER.debug("Suppression des fichiers CHK du répertoire voices");
    					// On enlève les fichiers chk du répertoire des voies
    					ISO9660Directory isoCurrDir = isoRoot.addDirectory(current);
    					String[] notchkFileList = current.list(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								return !name.endsWith(".chk");
							}
						});
    					
    					for (String currMapFileName : notchkFileList) {
    						isoCurrDir.addRecursively(new File(current, currMapFileName));
    					}
    					
    				} else if ("helpme".equalsIgnoreCase(current.getName()) ) {
    					LOGGER.debug("Vidage du répertoire helpme");
    					// On vire carrément le contenu du répertoire d'aide
    					// on le laisse quand même à vide
    					isoRoot.addDirectory(current);
    					
    				} else {
    					isoRoot.addRecursively(current);
    				}
    			} // for (String currFileName: gpsDir.list())
    			
    		} // end if (!p_forTest)
			
		} catch (HandlerException e) {
			throw new JTomtomException(e);
		}
		
		if (m_fichierDestination == null || m_fichierDestination == null || m_fichierDestination.isEmpty()) {
			throw new JTomtomException("org.jtomtom.errors.backup.destmustexist");
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
		File outputIsoFile = new File(m_fichierDestination);
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
    
    /**
     * Crée un fichier ISO du GPS
     * @param p_mountPoint	Point de montage du GPS, source de l'ISO
     * @return				true si OK
     * @throws JTomtomException
     * 			Retourne une exception si la source n'existe pas
     * 			Si la création de l'ISO ne se passe pas bien
     */
    public boolean createGpsBackup(GlobalPositioningSystem p_GPS) throws JTomtomException {
    	return createGpsBackup(p_GPS, m_makeTestISO);
    }

}
