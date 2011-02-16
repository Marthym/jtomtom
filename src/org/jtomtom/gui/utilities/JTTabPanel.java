/**
 *  Copyright© 2010, 2011  Frédéric Combes
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
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Classe intermédiaire d'homogénisation des onglets
 * @author Frédéric Combes
 *
 */
public class JTTabPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final ResourceBundle tabTranslations = ResourceBundle.getBundle("org.jtomtom.gui.resources.lang.jTomtom-tab", Locale.getDefault());
	
	private JLabel leftImage;
	
	private String	tabTitle;
	
	private JPanel centralPanel;
	private JPanel scrolledPanel;
	private JScrollPane additiveScrollBar;
	
	private boolean isBuild = false;

	public JTTabPanel(String p_title) {
		super();
		tabTitle = p_title;
	}
	
	/**
	 * Construit l'onglet vide
	 * @param p_imageUrl	Image de gauche de l'onglet
	 */
	public JPanel build() {
		setLayout(new BorderLayout());
		
		if (leftImage != null)
			add(leftImage, BorderLayout.LINE_START);
		
		// Création du paneau central
		centralPanel = new JPanel();
		centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.PAGE_AXIS));
		
		// Entête de l'onglet
		centralPanel.setBorder(new HeaderTitleBorder(tabTitle));

		// Création du paneau central
		scrolledPanel = new JPanel();
		scrolledPanel.setLayout(new BoxLayout(scrolledPanel, BoxLayout.PAGE_AXIS));
		centralPanel.add(scrolledPanel);
		
		add(centralPanel, BorderLayout.CENTER);
		
		isBuild = true;
		return this;
	}
	
	protected void setPanelLeftImage(URL p_imageUrl) {
		leftImage = new JLabel(new ImageIcon(p_imageUrl, p_imageUrl.getFile()));
	}
	
	/**
	 * Ajoute un composant dans le panel principal
	 * @param p_component	Composant à rajouter
	 */
	public Component add(Component p_component) {
		scrolledPanel.add(p_component);
		return scrolledPanel;
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
		additiveScrollBar = new JScrollPane(scrolledPanel, 
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		additiveScrollBar.setBorder(BorderFactory.createEmptyBorder());
		centralPanel.add(additiveScrollBar, BorderLayout.CENTER);
		
		// On retaille le panel pour inclure la scrollbar
		Dimension dim = scrolledPanel.getPreferredSize();
		scrolledPanel.setPreferredSize(new Dimension((int)dim.getWidth()-20, (int)dim.getHeight()));
	}
	
	public static final ResourceBundle getTabTranslations() {
		return tabTranslations;
	}
	
	protected final JPanel getScrollPanel() {
		return scrolledPanel;
	}
	
	protected final boolean isBuild() {
		return isBuild;
	}
	
	protected void resizeScrolling(int height) {
		// Not very clean but I don't know how make an autorisize
		if (additiveScrollBar != null) {
			Dimension dim = scrolledPanel.getPreferredSize();
			scrolledPanel.setPreferredSize(new Dimension((int)dim.getWidth(), (int)dim.getHeight()+height));
			scrolledPanel.revalidate();
		}
	}
}
