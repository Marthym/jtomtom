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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.jtomtom.JTomtom;
import org.jtomtom.tools.JarUtils;

/**
 * @author Frédéric Combes
 * 
 * Worker pour la vérification de nouvelles version de jTomtom
 */
public class CheckUpdateAction extends SwingWorker<ActionResult, Void> {
	
	public static final String REMOTE_JTOMTOM_URL = "http://downloads.sourceforge.net/project/jtomtom/jTomtom.jar";
	
	private static final Logger LOGGER = Logger.getLogger(CheckUpdateAction.class);

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected ActionResult doInBackground() throws Exception {
		
		String message = checkUpdateNow();
		
		ActionResult result = new ActionResult();
		result.status = false;
		if (message != null) {
			result.status = true;
			result.parameters = new LinkedList<String>();
			result.parameters.add(message);
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		try {
			ActionResult result = get();
			if (result.status) {
				LinkedList<String> messages = (LinkedList<String>)result.parameters;
				JOptionPane.showMessageDialog(null, messages.getFirst(), 
						JTomtom.theMainTranslator.getString("org.jtomtom.main.action.checkupdate.updateavailable"), JOptionPane.INFORMATION_MESSAGE);
			}
			
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		}
	}
	
	public static final String checkUpdateNow() {
		LOGGER.debug("Lancement de la vérification de version");
		String message = null;
		
		URL jarUrl = null;
		try {
			LOGGER.debug("URL de la version courante : "+REMOTE_JTOMTOM_URL);
			jarUrl = new URL(REMOTE_JTOMTOM_URL);
			
		} catch (MalformedURLException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			return null;
		}
		
		try {
			URLConnection conn = jarUrl.openConnection(JTomtom.getApplicationProxy());
			
			// On trouve le nom du fichier jar de jTomtom
			File jttJarFile = JarUtils.getCurrentFile();
			if (jttJarFile == null || !jttJarFile.exists()) {
				LOGGER.warn("Fichier jar introuvable ! Vérification de MAJ annulé !");
				return null;
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Version installé : "+new Date(jttJarFile.lastModified()));
				LOGGER.debug("Dernière version : "+new Date(conn.getLastModified()));
			}
			
			if (conn.getLastModified()/1000 >= jttJarFile.lastModified()/1000) {
				message = JTomtom.theMainTranslator.getString("org.jtomtom.main.action.checkupdate.newversion")+new Date(conn.getLastModified());
			}
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		}
		
		if (message == null) {
			LOGGER.info("Pas de nouvelle version trouvé.");
		} else {
			LOGGER.info(message);
		}
		
		return message;
	}

}
