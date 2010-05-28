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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import org.apache.log4j.Logger;
import org.jtomtom.JTomtom;
import org.jtomtom.TomTomax;
import org.jtomtom.gui.action.MajRadarsAction;

/**
 * @author marthym
 *
 * Onglet de mise à jour des radars
 * La mise à jour se fait grâce au site Tomtomax qui fourni les POIs
 */
public class TabRadars extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabRadars.class);
	
	private JLabel radarsInfos;
	private JButton radarsButton;
	
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
	class LoadInformationsWorker extends SwingWorker<StringBuffer, Void> {
		@Override
        public StringBuffer doInBackground() {
			LOGGER.debug("Enter in LoadInformationsWorker.doInBackground ...");
            return loadInBackground();
        }

		@Override
		protected void done() {
			try {
				StringBuffer infos = get();
				if (infos == null) {
					JOptionPane.showMessageDialog(null, 
							"Une erreur s'est produite à la lecture des informations.\n"+
							"Cette fonction n'est pas accessible.\n\n"+
							"Vérifier vos paramètres de configuration", 
							"Erreur ! ", JOptionPane.ERROR_MESSAGE);
				} else {
					radarsInfos.setText(infos.toString());
					radarsButton.setEnabled(true);
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
	private SwingWorker<StringBuffer, Void> m_loadWorker;

	
	/**
	 * Initialise l'affichage et instancie le woker
	 */
	public TabRadars() {
		super();
		m_loadWorker = new LoadInformationsWorker();
		build();
	}
	
	/**
	 * Fabrication de l'interface
	 */
	private void build() {
		setLayout(new BorderLayout());
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout());

		JLabel image = new JLabel(new ImageIcon(getClass().getResource("resources/radars.png"), "Rds"));
		add(image, BorderLayout.LINE_START);
		
		radarsInfos = new JLabel("");
		centerPanel.add(radarsInfos);
		add(centerPanel, BorderLayout.CENTER);
		
		radarsButton = new JButton(new MajRadarsAction("Mettre à jour les radars"));
		radarsButton.setEnabled(false);
		add(radarsButton, BorderLayout.PAGE_END);

	}
	
	/**
	 * Récupération et affichage des élements texte de l'interface
	 */
	public void loadRadarsInfos() {
		LOGGER.info("Récupération des informations sur les Radars");
		
		StringBuffer infos = new StringBuffer();
		infos.append("<html><h1>Informations Radars</h1>");
		infos.append("<table>");
		infos.append("<tr><td>Mise à jour disponible  : </td><td><i>Chargement ...</i></td></tr>");
		infos.append("<tr><td>Mise à jour installé : </td><td><i>Chargement...</i></td></tr>");
		infos.append("<tr><td>Nombre de radar : </td><td><i>Chargement...</i></td></tr>");
		infos.append("</table>");
		infos.append("<br/><br/><br/><font size=\"2\"><p><i>Les radars sont fournis par le site <a href=\"http://www.tomtomax.fr/\">&copy;TomtomMax</a></i></p></font>");
		infos.append("</html>");
		radarsInfos.setText(infos.toString());
		
		if (m_loadWorker.getState() != StateValue.STARTED) { // On en lance qu'un à la fois
			LOGGER.debug("Lancement de LoadInformationsWorker ...");
			m_loadWorker = new LoadInformationsWorker();
			m_loadWorker.execute();
		}
	}
	
	/**
	 * Récupération en background des éléments nécessaires à l'affichage de l'interface
	 * @return	Chaine HTML à affiché contenant les infos
	 */
	private StringBuffer loadInBackground() {
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
		StringBuffer infos = new StringBuffer();
		
		// Si on a pas déjà les infos, on les demande au serveur TTMax
		if (infosTomtomax == null) {
			infosTomtomax = TomTomax.getRemoteDbInfos(JTomtom.getApplicationProxy());
			if (infosTomtomax == null) {
				LOGGER.error("Erreur lors de la récupération des informations Tomtomax !");
				return null;
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
			infos.append("<html><h1>Informations Radars</h1>");
			infos.append("<table>");
			infos.append("<tr><td>Mise à jour disponible  : </td><td><i>")
					.append(dateFormat.format(remoteDbDate))
					.append("</i></td></tr>");
			if (JTomtom.getTheGPS().getRadarsDbDate() != null) {
				infos.append("<tr><td>Mise à jour installé : </td><td><i>")
					.append(dateFormat.format(JTomtom.getTheGPS().getRadarsDbDate()))
					.append("</i></td></tr>");
			} else {
				infos.append("<tr><td>Mise à jour installé : </td><td><i>Aucune</i></td></tr>");
			}
			infos.append("<tr><td>Nombre de radar : </td><td><i>")
				.append(JTomtom.getTheGPS().getRadarsNombre())
				.append("</i> [")
				.append(Integer.parseInt(infosTomtomax.get(TomTomax.TAG_RADARS)) - JTomtom.getTheGPS().getRadarsNombre())
				.append(" radars manquants]</td></tr>");
			infos.append("</table>");
			infos.append("<br/><br/><br/><font size=\"2\"><p><i>Les radars sont fournis par le site <a href=\"http://www.tomtomax.fr/\">&copy;TomtomMax</a></i></p></font>");
			infos.append("</html>");
			
		} else {
			LOGGER.error("Une erreur s'est produite à la lecture des informations sur le GPS !");
			return null;
		} 
		
		return infos;
	}

}
