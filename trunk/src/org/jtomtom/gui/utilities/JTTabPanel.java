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
package org.jtomtom.gui.utilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Classe intermédiaire d'homogénisation des onglets
 * @author marthym
 *
 */
public class JTTabPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JLabel m_image;
	
	private String	m_title;
	
	protected JPanel m_centerPanel;
	protected JPanel m_scrolledPanel;

	public JTTabPanel(String p_title) {
		super();
		m_title = p_title;
	}
	
	/**
	 * Construit l'onglet vide
	 * @param p_imageUrl	Image de gauche de l'onglet
	 */
	protected void build(URL p_imageUrl) {
		setLayout(new BorderLayout());
		
		// Création du panneau de gauche
		m_image = new JLabel(new ImageIcon(p_imageUrl, p_imageUrl.getFile()));
		add(m_image, BorderLayout.LINE_START);
		
		// Création du paneau central
		m_centerPanel = new JPanel();
		m_centerPanel.setLayout(new BoxLayout(m_centerPanel, BoxLayout.PAGE_AXIS));
		
		// Entête de l'onglet
		m_centerPanel.setBorder(new HeaderTitleBorder(m_title));

		// Création du paneau central
		m_scrolledPanel = new JPanel();
		m_scrolledPanel.setLayout(new BoxLayout(m_scrolledPanel, BoxLayout.PAGE_AXIS));
		m_centerPanel.add(m_scrolledPanel);
		
		add(m_centerPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Ajoute un composant dans le panel principal
	 * @param p_component	Composant à rajouter
	 */
	public Component add(Component p_component) {
		m_scrolledPanel.add(p_component);
		return m_scrolledPanel;
	}
	
	/**
	 * Ajoute le bouton d'action principal de l'onglet
	 * @param p_button	Bouton à ajouter
	 */
	public void addActionButton(JButton p_button) {
		add(p_button, BorderLayout.PAGE_END);
	}
	
	/**
	 * Ajoute un ascenseur vertical à à l'onglet
	 */
	public void addScrollVerticalBar() {
		JScrollPane scroll = new JScrollPane(m_scrolledPanel, 
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		//scroll.getVerticalScrollBar().setUnitIncrement(3); // Je le laisse là au cas où ...
		m_centerPanel.add(scroll, BorderLayout.CENTER);
		
		// On retaille le panel pour inclure la scrollbar
		Dimension dim = m_scrolledPanel.getPreferredSize();
		m_scrolledPanel.setPreferredSize(new Dimension((int)dim.getWidth()-20, (int)dim.getHeight()));
	}
}
