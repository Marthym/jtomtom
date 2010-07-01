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

import org.jtomtom.JTomtom;
import org.jtomtom.gui.utilities.JTTabPanel;

/**
 * @author marthym
 *
 */
public class TabGeneral extends JTTabPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JButton m_btCopier;
	
	public TabGeneral() {
		super(m_rbControls.getString("org.jtomtom.tab.general.title"));
		
		build();
	}
	
	private void build() {
		super.build(getClass().getResource("resources/general.png"));
		add(Box.createRigidArea(new Dimension(0,5)));
		
		// Ecriture des informations pour l'onglet General
		StringBuffer infos = new StringBuffer();
		infos.append("<html><table>");
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.general.name")).append(" : </strong></td><td><i>")
			.append(JTomtom.getTheGPS().getDeviceName()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.general.unid")).append(" : </strong></td><td><i>")
			.append(JTomtom.getTheGPS().getDeviceUniqueID()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.general.bootloader")).append(" : </strong></td><td><i>")
			.append(JTomtom.getTheGPS().getBootloaderVersion()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.general.gpsversion")).append(" : </strong></td><td><i>")
			.append(JTomtom.getTheGPS().getGpsVersion()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.general.appversion")).append(" : </strong></td><td><i>")
			.append(JTomtom.getTheGPS().getAppVersion()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.general.map")).append(" : </strong></td><td><i>")
			.append(JTomtom.getTheGPS().getActiveMapName()).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.general.mapversion")).append(" : </strong></td><td><i>")
			.append(JTomtom.getTheGPS().getMapVersion()).append("</i></td></tr>");
		infos.append("</table>");
		infos.append("</html>");
		
		JLabel label = new JLabel(infos.toString());
		add(label);
		
		add(Box.createRigidArea(new Dimension(0, 15)));
		
		m_btCopier = new JButton(m_rbControls.getString("org.jtomtom.tab.general.copy.label"));
		m_btCopier.setToolTipText(m_rbControls.getString("org.jtomtom.tab.general.copy.hint"));
		m_btCopier.addActionListener(this);
		add(m_btCopier);	
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		// Action de copier le contenu des infos dans le presse papier
		if (event.getSource() == m_btCopier) {
			StringBuffer infos = new StringBuffer();
			infos.append(m_rbControls.getString("org.jtomtom.tab.general.name")).append(" : ").append(JTomtom.getTheGPS().getDeviceName()).append("\n");
			infos.append(m_rbControls.getString("org.jtomtom.tab.general.unid")).append(" : ").append(JTomtom.getTheGPS().getDeviceUniqueID()).append("\n");
			infos.append(m_rbControls.getString("org.jtomtom.tab.general.bootloader")).append(" : ").append(JTomtom.getTheGPS().getBootloaderVersion()).append("\n");
			infos.append(m_rbControls.getString("org.jtomtom.tab.general.gpsversion")).append(" : ").append(JTomtom.getTheGPS().getGpsVersion()).append("\n");
			infos.append(m_rbControls.getString("org.jtomtom.tab.general.appversion")).append(" : ").append(JTomtom.getTheGPS().getAppVersion()).append("\n");
			infos.append(m_rbControls.getString("org.jtomtom.tab.general.map")).append(" : ").append(JTomtom.getTheGPS().getActiveMapName()).append("\n");
			infos.append(m_rbControls.getString("org.jtomtom.tab.general.mapversion")).append(" : ").append(JTomtom.getTheGPS().getMapVersion()).append("\n");
			
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new StringSelection(infos.toString()), null);
		}
	}
	
	
}
