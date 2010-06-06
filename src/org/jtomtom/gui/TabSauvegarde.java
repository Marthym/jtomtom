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
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.jtomtom.gui.action.SauvegardeAction;

/**
 * @author marthym
 *
 */
public class TabSauvegarde extends JPanel implements MouseListener {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabSauvegarde.class);
	
	private JTextField m_isoFileChooser;

	public TabSauvegarde() {
		super();
		
		build();
	}
	
	/**
	 * Construction de l'interface graphique
	 */
	private void build() {
		setLayout(new BorderLayout());
		
		JLabel image = new JLabel(new ImageIcon(getClass().getResource("resources/sauvegarde.png"), "Sauvegarde"));
		add(image, BorderLayout.LINE_START);
		
		// Création du paneau central
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
		
		// Création du texte de présentation
		JLabel infos = new JLabel(loadSauvegardeInfos().toString());
		centerPanel.add(infos);
		centerPanel.add(Box.createRigidArea(new Dimension(0,20)));
		infos = new JLabel("Fichiers d'entrée / sortie :");
		centerPanel.add(infos);
		
		// Création du champ de saisie pour le fichier d'entrée/sortie
		m_isoFileChooser = new JTextField();
		m_isoFileChooser.setMaximumSize(new Dimension(Short.MAX_VALUE,23));
		m_isoFileChooser.setMinimumSize(new Dimension(20,23));
		m_isoFileChooser.setAlignmentX(LEFT_ALIGNMENT);
		m_isoFileChooser.setToolTipText("Double-clicker pour afficher une fenêtre de sélection de fichier ...");
		m_isoFileChooser.addMouseListener(this);
		centerPanel.add(m_isoFileChooser);
		centerPanel.add(Box.createRigidArea(new Dimension(0,10)));
		
		// Création du panneau de bouton
		JButton bouton = new JButton(new SauvegardeAction("Sauvegarder le GPS", m_isoFileChooser));
		add(bouton, BorderLayout.PAGE_END);
		
		add(centerPanel, BorderLayout.CENTER);
		
	}

	/**
	 * Séparation de l'écriture du message dans l'onglet
	 * @return
	 */
	private final static StringBuffer loadSauvegardeInfos() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<html><h1>Sauvegarde &amp; Restauration</h1>");
		buffer.append("<p>Cette fonction vous permet de faire une sauvegarde du contenu de votre GPS.</p>");
		buffer.append("<p>La sauvegarde se fait sous la forme d'un fichier ISO standard qu'il vous est possible de graver ");
		buffer.append("sur CD par la suite.</p>");
		
		return buffer;
	}
	
	/**
	 * Retourne le text présent dans le TextField de chemin de destination
	 * @return	Chaine
	 */
	public final String getFichierDestination() {
		return m_isoFileChooser.getText();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == m_isoFileChooser) {
			if (e.getClickCount() >= 2) {
				LOGGER.debug("Double-Click sur le m_isoFileChooser");
				// S'il y a double click, on ouvre un filechooser
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					if (LOGGER.isDebugEnabled()) LOGGER.debug("Fichier choisit : "+fc.getSelectedFile().getAbsolutePath());
					m_isoFileChooser.setText(fc.getSelectedFile().getAbsolutePath());
				}

			}
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
}
