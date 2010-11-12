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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.apache.log4j.Logger;
import org.jtomtom.JTomtom;
import org.jtomtom.gui.utilities.JTTabPanel;
import org.jtomtom.gui.utilities.SpringUtilities;

/**
 * @author Frédéric Combes
 *
 * About tab
 */
public class TabAbout extends JTTabPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logger.getLogger(TabAbout.class);
	
	public static final String LICENSE_URL = "http://www.gnu.org/licenses/gpl.html";
	public static final String WEBSITE_URL = "http://jtomtom.sourceforge.net/";
	public static final String DEVELOPER_URL = "belz12@yahoo.fr";
	
	private JButton openWebsite;
	private JButton sendMeaMail;
	private JButton viewLicense;
	
	/**
	 * Display initialisation
	 */
	public TabAbout() {
		super(m_rbControls.getString("org.jtomtom.tab.about.title"));
		build();
	}
	
	/**
	 * Building UI
	 */
	private void build() {
		super.build(getClass().getResource("resources/apropos.png"));
		add(Box.createRigidArea(new Dimension(0,5)));
		
		add(buildInformationsPanel());
		
		add(buildTranslatorsPanel());
				
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		viewLicense = new JButton(m_rbControls.getString("org.jtomtom.tab.about.license")+" GPLv3");
		viewLicense.addActionListener(this);
		buttonPanel.add(viewLicense);
		add(buttonPanel);
	}
	
	private JComponent buildInformationsPanel() {
		JPanel informationsPanel = new JPanel(new SpringLayout());
		
		informationsPanel.add(new JLabel("<html><strong>"+m_rbControls.getString("org.jtomtom.tab.about.version")+"</strong></html>"));
		informationsPanel.add(new JLabel(JTomtom.getApplicationVersionNumber()));
		informationsPanel.add(new JLabel("<html><strong>"+m_rbControls.getString("org.jtomtom.tab.about.date")+"</strong></html>"));
		informationsPanel.add(new JLabel(JTomtom.getApplicationVersionDate()));
		informationsPanel.add(new JLabel("<html><strong>"+m_rbControls.getString("org.jtomtom.tab.about.developer")+"</strong></html>"));
		
		sendMeaMail = new JButton("<html>Frédéric Combes @ <a href=\"mailto:"+DEVELOPER_URL+"\">"+DEVELOPER_URL+"</a></html>");
		sendMeaMail.setHorizontalAlignment(JButton.LEFT);
		sendMeaMail.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		sendMeaMail.addActionListener(this);
		informationsPanel.add(sendMeaMail);
		
		informationsPanel.add(new JLabel("<html><strong>"+m_rbControls.getString("org.jtomtom.tab.about.website")+"</strong></html>"));
		openWebsite = new JButton("<html><a href=\""+WEBSITE_URL+"\">"+WEBSITE_URL+"</a></html>");
		openWebsite.setHorizontalAlignment(JButton.LEFT);
		openWebsite.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		openWebsite.addActionListener(this);
		informationsPanel.add(openWebsite);
		
		SpringUtilities.makeCompactGrid(informationsPanel,
                4, 2, 		 //rows, cols
                2, 2,        //initX, initY
                2, 2);       //xPad, yPad
		
		return informationsPanel;
	}
	
	private JComponent buildTranslatorsPanel() {
		JPanel translatorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		StringBuffer infos = new StringBuffer();
		infos.append("<html><table>");
		infos.append("<tr><td>Català (ca_ES) :</td><td><strong>Oriol Gonzalez Llobet</strong></td></tr>");
		infos.append("<tr><td>Dutch (nl_NL) :</td><td><strong>Charly Preis</strong></td></tr>");
		infos.append("<tr><td>Español (es_ES) :</td><td><strong>Francisco Luque Contreras</strong></td></tr>");
		infos.append("<tr><td>German (de_DE) :</td><td><strong>Olivier Brügger</strong></td></tr>");
		infos.append("</table>");
		infos.append("</html>");
		JLabel label = new JLabel(infos.toString());
		translatorsPanel.add(label);
		
		JScrollPane scroll = new JScrollPane(translatorsPanel);
		scroll.setBorder(
				BorderFactory.createTitledBorder(
						m_rbControls.getString("org.jtomtom.tab.about.translation")));
		return scroll;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			if (event.getSource() == viewLicense) {
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(LICENSE_URL));
			
			} else if (event.getSource() == openWebsite) {
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(WEBSITE_URL));
				
			} else if (event.getSource() == sendMeaMail) {
				java.awt.Desktop.getDesktop().browse(java.net.URI.create("mailto:"+DEVELOPER_URL));
			}
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) LOGGER.debug(e);
		} 
		
	}
	
}
