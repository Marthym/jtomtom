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
		super("Informations sur le GPS");
		
		build();
	}
	
	private void build() {
		super.build(getClass().getResource("resources/general.png"));
		add(Box.createRigidArea(new Dimension(0,5)));
		
		// Ecriture des informations pour l'onglet General
		StringBuffer infos = new StringBuffer();
		infos.append("<html><table>");
		infos.append("<tr><td><strong>Nom : </strong></td><td><i>").append(JTomtom.getTheGPS().getDeviceName()).append("</i></td></tr>");
		infos.append("<tr><td><strong>UNID : </strong></td><td><i>").append(JTomtom.getTheGPS().getDeviceUniqueID()).append("</i></td></tr>");
		infos.append("<tr><td><strong>BootLoader : </strong></td><td><i>").append(JTomtom.getTheGPS().getBootloaderVersion()).append("</i></td></tr>");
		infos.append("<tr><td><strong>Version GPS : </strong></td><td><i>").append(JTomtom.getTheGPS().getGpsVersion()).append("</i></td></tr>");
		infos.append("<tr><td><strong>App Version : </strong></td><td><i>").append(JTomtom.getTheGPS().getAppVersion()).append("</i></td></tr>");
		infos.append("<tr><td><strong>Map : </strong></td><td><i>").append(JTomtom.getTheGPS().getMapName()).append("</i></td></tr>");
		infos.append("<tr><td><strong>Map Version : </strong></td><td><i>").append(JTomtom.getTheGPS().getMapVersion()).append("</i></td></tr>");
		infos.append("</table>");
		infos.append("</html>");
		
		JLabel label = new JLabel(infos.toString());
		add(label);
		
		add(Box.createRigidArea(new Dimension(0, 15)));
		
		m_btCopier = new JButton("Copier");
		m_btCopier.setToolTipText("Copie les informations ci-dessus dans le presse-papier.");
		m_btCopier.addActionListener(this);
		add(m_btCopier);	
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		// Action de copier le contenu des infos dans le presse papier
		if (event.getSource() == m_btCopier) {
			StringBuffer infos = new StringBuffer();
			infos.append("Nom : ").append(JTomtom.getTheGPS().getDeviceName()).append("\n");
			infos.append("UNID : ").append(JTomtom.getTheGPS().getDeviceUniqueID()).append("\n");
			infos.append("BootLoader :").append(JTomtom.getTheGPS().getBootloaderVersion()).append("\n");
			infos.append("Version GPS : ").append(JTomtom.getTheGPS().getGpsVersion()).append("\n");
			infos.append("App Version : ").append(JTomtom.getTheGPS().getAppVersion()).append("\n");
			infos.append("Map : ").append(JTomtom.getTheGPS().getMapName()).append("\n");
			infos.append("Map Version : ").append(JTomtom.getTheGPS().getMapVersion()).append("\n");
			
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new StringSelection(infos.toString()), null);
		}
	}
	
	
}
