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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jtomtom.JTomtom;

/**
 * @author marthym
 *
 */
public class TabGeneral extends JPanel implements ActionListener {
	private static final long serialVersionUID = -3308369793048494387L;

	private JButton m_btCopier;
	
	public TabGeneral() {
		super();
		
		build();
	}
	
	private void build() {
		setLayout(new BorderLayout());
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
		
		// Ecriture des informations pour l'onglet General
		StringBuffer infos = new StringBuffer();
		infos.append("<html><h1>Informations sur le GPS</h1>");
		infos.append("<table>");
		infos.append("<tr><td>Nom : </td><td><i>").append(JTomtom.getTheGPS().getDeviceName()).append("</i></td></tr>");
		infos.append("<tr><td>UNID : </td><td><i>").append(JTomtom.getTheGPS().getDeviceUniqueID()).append("</i></td></tr>");
		infos.append("<tr><td>BootLoader : </td><td><i>").append(JTomtom.getTheGPS().getBootloaderVersion()).append("</i></td></tr>");
		infos.append("<tr><td>Version GPS : </td><td><i>").append(JTomtom.getTheGPS().getGpsVersion()).append("</i></td></tr>");
		infos.append("<tr><td>App Version : </td><td><i>").append(JTomtom.getTheGPS().getAppVersion()).append("</i></td></tr>");
		infos.append("<tr><td>Map : </td><td><i>").append(JTomtom.getTheGPS().getMapName()).append("</i></td></tr>");
		infos.append("<tr><td>Map Version : </td><td><i>").append(JTomtom.getTheGPS().getMapVersion()).append("</i></td></tr>");
		infos.append("</table>");
		infos.append("</html>");
		
		JLabel label = new JLabel(infos.toString());
		centerPanel.add(label);
		
		JLabel image = new JLabel(new ImageIcon(getClass().getResource("resources/general.png"), "jTT"));
		
		centerPanel.add(Box.createRigidArea(new Dimension(0, 15)), TOP_ALIGNMENT);
		
		m_btCopier = new JButton("Copier");
		m_btCopier.setToolTipText("Copie les informations ci-dessus dans le presse-papier.");
		m_btCopier.addActionListener(this);
		centerPanel.add(m_btCopier);
		
		add(centerPanel, BorderLayout.CENTER);
		add(image, BorderLayout.LINE_START);		
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
