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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.gui.utilities.JTTabPanel;

/**
 * @author Frédéric Combes
 *
 */
public class TabGeneral extends JTTabPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabGeneral.class);
	
	private TomtomDevice theDevice = Application.getInstance().getTheDevice();
	
	private JButton m_btCopier;
	
	public TabGeneral() {
		super(getTabTranslations().getString("org.jtomtom.tab.general.title"));
		setPanelLeftImage(getClass().getResource("resources/general.png"));
	}
	
	public JPanel build() {
		super.build();
		LOGGER.trace("Building TabGeneral ...");
		
		add(Box.createRigidArea(new Dimension(0,5)));
		
		// Ecriture des informations pour l'onglet General
		StringBuffer infos = new StringBuffer();
		infos.append("<html><table>");
		infos.append("<tr><td><strong>").append(getTabTranslations().getString("org.jtomtom.tab.general.name")).append(" : </strong></td><td><i>")
			.append(theDevice.getName()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(getTabTranslations().getString("org.jtomtom.tab.general.unid")).append(" : </strong></td><td><i>")
			.append(theDevice.getDeviceUniqueID()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(getTabTranslations().getString("org.jtomtom.tab.general.bootloader")).append(" : </strong></td><td><i>")
			.append(theDevice.getBootloaderVersion()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(getTabTranslations().getString("org.jtomtom.tab.general.gpsversion")).append(" : </strong></td><td><i>")
			.append(theDevice.getProcessorVersion()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(getTabTranslations().getString("org.jtomtom.tab.general.appversion")).append(" : </strong></td><td><i>")
			.append(theDevice.getAppVersion()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(getTabTranslations().getString("org.jtomtom.tab.general.map")).append(" : </strong></td><td><i>")
			.append(theDevice.getActiveMap().getName()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(getTabTranslations().getString("org.jtomtom.tab.general.mapversion")).append(" : </strong></td><td><i>")
			.append(theDevice.getActiveMap().getVersion()).append("</i></td></tr>");
		infos.append("</table>");
		infos.append("</html>");
		
		JLabel label = new JLabel(infos.toString());
		add(label);
		
		add(Box.createRigidArea(new Dimension(0, 15)));
		
		m_btCopier = new JButton(getTabTranslations().getString("org.jtomtom.tab.general.copy.label"));
		m_btCopier.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.general.copy.hint"));
		m_btCopier.addActionListener(this);
		add(m_btCopier);
		
		return this;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		// Action de copier le contenu des infos dans le presse papier
		if (event.getSource() == m_btCopier) {
			StringBuffer infos = new StringBuffer();
			infos.append(getTabTranslations().getString("org.jtomtom.tab.general.name")).append(" : ").append(theDevice.getName()).append("\n");
			infos.append(getTabTranslations().getString("org.jtomtom.tab.general.unid")).append(" : ").append(theDevice.getDeviceUniqueID()).append("\n");
			infos.append(getTabTranslations().getString("org.jtomtom.tab.general.bootloader")).append(" : ").append(theDevice.getBootloaderVersion()).append("\n");
			infos.append(getTabTranslations().getString("org.jtomtom.tab.general.gpsversion")).append(" : ").append(theDevice.getProcessorVersion()).append("\n");
			infos.append(getTabTranslations().getString("org.jtomtom.tab.general.appversion")).append(" : ").append(theDevice.getAppVersion()).append("\n");
			infos.append(getTabTranslations().getString("org.jtomtom.tab.general.map")).append(" : ").append(theDevice.getActiveMap().getName()).append("\n");
			infos.append(getTabTranslations().getString("org.jtomtom.tab.general.mapversion")).append(" : ").append(theDevice.getActiveMap().getVersion()).append("\n");
			
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new StringSelection(infos.toString()), null);
		}
	}
	
	
}
