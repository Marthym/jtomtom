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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.jtomtom.gui.action.SauvegardeAction;
import org.jtomtom.gui.utilities.JTTabPanel;

/**
 * @author marthym
 *
 */
public class TabSauvegarde extends JTTabPanel implements MouseListener {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabSauvegarde.class);
	
	private JTextField 	m_isoFileChooser;
	private JCheckBox 	m_makeTestISO;

	public TabSauvegarde() {
		super(m_rbControls.getString("org.jtomtom.tab.backup.title"));
		
		build();
	}
	
	/**
	 * Construction de l'interface graphique
	 */
	private void build() {
		super.build(getClass().getResource("resources/sauvegarde.png"));
		add(Box.createRigidArea(new Dimension(0,5)));
		
		// Création du texte de présentation
		JLabel infos = new JLabel(loadSauvegardeInfos().toString());
		add(infos);
		add(Box.createRigidArea(new Dimension(0,20)));
		infos = new JLabel(m_rbControls.getString("org.jtomtom.tab.backup.label.infos.text"));
		add(infos);
		
		// Création du champ de saisie pour le fichier d'entrée/sortie
		m_isoFileChooser = new JTextField();
		m_isoFileChooser.setMaximumSize(new Dimension(Short.MAX_VALUE,25));
		m_isoFileChooser.setMinimumSize(new Dimension(20,25));
		m_isoFileChooser.setAlignmentX(LEFT_ALIGNMENT);
		m_isoFileChooser.setToolTipText(m_rbControls.getString("org.jtomtom.tab.backup.textfield.filechooser.hint"));
		m_isoFileChooser.addMouseListener(this);
		add(m_isoFileChooser);
		
		m_makeTestISO = new JCheckBox(m_rbControls.getString("org.jtomtom.tab.backup.checkbox.isotest.label"));
		m_makeTestISO.setToolTipText(m_rbControls.getString("org.jtomtom.tab.backup.checkbox.isotest.hint"));
		m_makeTestISO.setVisible(LOGGER.isDebugEnabled());
		add(m_makeTestISO);
		add(Box.createRigidArea(new Dimension(0,10)));
		
		// Création du panneau de bouton
		JButton bouton = new JButton(new SauvegardeAction(m_rbControls.getString("org.jtomtom.tab.backup.button.createiso.label")));
		addActionButton(bouton);
				
	}

	/**
	 * Séparation de l'écriture du message dans l'onglet
	 * @return
	 */
	private final static StringBuffer loadSauvegardeInfos() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<html><p>").append(m_rbControls.getString("org.jtomtom.tab.backup.text.1")).append("</p>");
		buffer.append("<p>").append(m_rbControls.getString("org.jtomtom.tab.backup.text.2")).append("</p>");
		
		return buffer;
	}
	
	/**
	 * Retourne le text présent dans le TextField de chemin de destination
	 * @return	Chaine
	 */
	public final String getFichierDestination() {
		return m_isoFileChooser.getText();
	}
	
	/**
	 * Retourne la valeur de l'option de génération d'ISO de test
	 * @return	Vrai ou faux
	 */
	public final boolean getMakeTestISO() {
		return m_makeTestISO.isSelected();
	}
	
	public void refreshISOType() {
		if (LOGGER.isDebugEnabled()) {
			m_makeTestISO.setVisible(true);
		} else {
			m_makeTestISO.setSelected(false);
			m_makeTestISO.setVisible(false);
		}
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
