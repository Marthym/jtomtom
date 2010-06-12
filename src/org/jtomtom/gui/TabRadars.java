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
package org.jtomtom.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import org.apache.log4j.Logger;
import org.jtomtom.JTomtom;
import org.jtomtom.JTomtomException;
import org.jtomtom.TomTomax;
import org.jtomtom.gui.action.ActionResult;
import org.jtomtom.gui.action.MajRadarsAction;
import org.jtomtom.gui.utilities.JTTabPanel;

/**
 * @author marthym
 *
 * Onglet de mise à jour des radars
 * La mise à jour se fait grâce au site Tomtomax qui fourni les POIs
 */
public class TabRadars extends JTTabPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabRadars.class);
	
	private JLabel radarsInfos;
	private JButton radarsButton;
	private JButton refreshButton;
	
	private Map<String, String> infosTomtomax;

	/**
	 * @author marthym
	 * 
	 * Le Worker qui rafraichit les informations de l'onglet Radar
	 * Vu qu'une des infos est prose sur le site de Tomtomax, la mise à jour peux potentiellement
	 * 		prendre du temps voire même se planter donc on efectue la récupération des informations
	 * 		en dehors de l'EDT
	 *
	 */
	class LoadInformationsWorker extends SwingWorker<ActionResult, Void> {
		@Override
        public ActionResult doInBackground() {
			LOGGER.debug("Enter in LoadInformationsWorker.doInBackground ...");
            return loadInBackground();
        }

		@Override
		protected void done() {
			try {
				ActionResult infos = get();
				if (!infos.status) {
					JOptionPane.showMessageDialog(null, 
							infos.exception.getLocalizedMessage(), 
							"Erreur ! ", JOptionPane.ERROR_MESSAGE);
				} else {
					radarsInfos.setText(infos.parameters.get(0));
					radarsButton.setEnabled(true);
					refreshButton.setEnabled(true);
				}
				LOGGER.debug("Exécution de LoadInformationsWorker terminé.");
				
			} catch (InterruptedException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
				
			} catch (ExecutionException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
	}
	private SwingWorker<ActionResult, Void> m_loadWorker;

	
	/**
	 * Initialise l'affichage et instancie le woker
	 */
	public TabRadars() {
		super("Informations Radars");
		m_loadWorker = new LoadInformationsWorker();
		build();
	}
	
	/**
	 * Fabrication de l'interface
	 */
	private void build() {
		super.build(getClass().getResource("resources/radars.png"));
		add(Box.createRigidArea(new Dimension(0,5)));
		
		radarsInfos = new JLabel("");
		add(radarsInfos);
		
		add(Box.createRigidArea(new Dimension(0, 5)));
		refreshButton = new JButton("Rafraichir");
		refreshButton.setToolTipText("Rafraichir les informations de cette page");
		refreshButton.setEnabled(false);
		refreshButton.addActionListener(this);
		add(refreshButton);
		
		radarsButton = new JButton(new MajRadarsAction("Mettre à jour les radars"));
		radarsButton.setEnabled(false);
		addActionButton(radarsButton);

	}
	
	/**
	 * Récupération et affichage des élements texte de l'interface
	 */
	public void loadRadarsInfos() {
		// Pour ne pas faire ça à chaque passage sur l'onglet
		if (refreshButton.isEnabled()) {
			return;
		}
		LOGGER.info("Récupération des informations sur les Radars");
		
		StringBuffer infos = new StringBuffer();
		infos.append("<html><table>");
		infos.append("<tr><td><strong>Mise à jour disponible  : </strong></td><td><i>Chargement ...</i></td></tr>");
		infos.append("<tr><td><strong>Mise à jour installé : </strong></td><td><i>Chargement...</i></td></tr>");
		infos.append("<tr><td><strong>Nombre de radar : </strong></td><td><i>Chargement...</i></td></tr>");
		infos.append("</table>");
		infos.append("<br/><br/><font size=\"2\"><p><i>Les radars sont fournis par le site <a href=\"http://www.tomtomax.fr/\">&copy;Tomtomax</a></i></p></font>");
		infos.append("</html>");
		radarsInfos.setText(infos.toString());
		
		if (m_loadWorker.getState() != StateValue.STARTED) { // On en lance qu'un à la fois
			LOGGER.debug("Lancement de LoadInformationsWorker ...");
			m_loadWorker = new LoadInformationsWorker();
			m_loadWorker.execute();
		}
	}
	
	/**
	 * Désactive le bouton de refresh et ainsi impose
	 * Un rafraichissment des infos lors de l'appel à loadRadarsInfos
	 */
	public void disableRefreshButton() {
		refreshButton.setEnabled(false);
	}
	
	/**
	 * Récupération en background des éléments nécessaires à l'affichage de l'interface
	 * @return	Chaine HTML à affiché contenant les infos
	 */
	private ActionResult loadInBackground() {
		ActionResult result = new ActionResult();
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
		StringBuffer infos = new StringBuffer();
		
		// Si on a pas déjà les infos, on les demande au serveur TTMax
		if (infosTomtomax == null) {
			infosTomtomax = TomTomax.getRemoteDbInfos(JTomtom.getApplicationProxy());
			if (infosTomtomax == null) {
				result.exception = new JTomtomException("Erreur lors de la récupération des informations Tomtomax !");
				result.status = false;
				return result;
			}
		}
		
		// On formate la date récupérer du server TTM
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); 
		Date remoteDbDate = null;
		try {
			remoteDbDate = formatter.parse(infosTomtomax.get(TomTomax.TAG_DATE));
		} catch (ParseException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
		}
		
		if (JTomtom.getTheGPS().getRadarsDbVersion() >= 0) {
			// Les radars sont déjà installé
			infos.append("<html><table>");
			infos.append("<tr><td><strong>Mise à jour disponible  : </strong></td><td><i>")
					.append(dateFormat.format(remoteDbDate))
					.append("</i></td></tr>");
			if (JTomtom.getTheGPS().getRadarsDbDate() != null) {
				infos.append("<tr><td><strong>Mise à jour installé : </strong></td><td><i>")
					.append(dateFormat.format(JTomtom.getTheGPS().getRadarsDbDate()))
					.append("</i></td></tr>");
			} else {
				infos.append("<tr><td><strong>Mise à jour installé : </strong></td><td><i>Aucune</i></td></tr>");
			}
			infos.append("<tr><td><strong>Nombre de radar : </strong></td><td><i>")
				.append(JTomtom.getTheGPS().getRadarsNombre())
				.append("</i> [")
				.append(Integer.parseInt(infosTomtomax.get(TomTomax.TAG_RADARS)) - JTomtom.getTheGPS().getRadarsNombre())
				.append(" radars manquants]</td></tr>");
			infos.append("</table>");
			infos.append("<br/><br/><font size=\"2\"><p><i>Les radars sont fournis par le site <a href=\"http://www.tomtomax.fr/\">&copy;Tomtomax</a></i></p></font>");
			infos.append("</html>");
			
			if (result.parameters == null) {
				result.parameters = new LinkedList<String>();
			}
			result.parameters.add(infos.toString());
		} else {
			result.exception = new JTomtomException("Une erreur s'est produite à la lecture des informations sur le GPS !");
			result.status = false;
			return result;
		} 
		
		// - Enfin, on teste la connexion à Tomtomax pour vérifier que le user ai un compte
		result.status = TomTomax.connexion(
				JTomtom.getApplicationProxy(), 
				JTomtom.getApplicationPropertie("org.tomtomax.user"), 
				JTomtom.getApplicationPropertie("org.tomtomax.password"));
		if (!result.status) {
			result.exception = new JTomtomException("Votre compte utilisateur Tomtomax n'est pas valide !\nVérifier vos paramètre.\nSi vous n'avez pas de compte, vous devez vous en créer un d'abord !");
			return result;
		}
		
		result.status = true;
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == refreshButton) {
			// Rafraichissement manuel des infos de la page
			refreshButton.setEnabled(false);
			loadRadarsInfos();
		}
		
	}

}
