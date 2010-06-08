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
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

	public JTTabPanel(String p_title) {
		super();
		m_title = p_title;
	}
	
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

		add(m_centerPanel, BorderLayout.CENTER);
	}
	
	public Component add(Component p_component) {
		m_centerPanel.add(p_component);
		return m_centerPanel;
	}
	
	public void addActionButton(JButton p_button) {
		add(p_button, BorderLayout.PAGE_END);
	}
}
