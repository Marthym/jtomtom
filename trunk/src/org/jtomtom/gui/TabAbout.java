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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;

import org.jtomtom.gui.utilities.JTTabPanel;

/**
 * @author marthym
 *
 * Onglet des informations sur jTomtom
 */
public class TabAbout extends JTTabPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Initialise l'affichage et instancie le woker
	 */
	public TabAbout() {
		super("A Propos de jTomtom");
		build();
	}
	
	/**
	 * Fabrication de l'interface
	 */
	private void build() {
		super.build(getClass().getResource("resources/apropos.png"));
		add(Box.createRigidArea(new Dimension(0,5)));
		
		// Ecriture des informations pour l'onglet General
		StringBuffer infos = new StringBuffer();
		infos.append("<html><table>");
		infos.append("<tr><td><strong>Version</strong></td><td>build 01-07-2010</td></tr>");
		infos.append("<tr><td><strong>Développeur</strong></td><td>Frédéric Combes @ <a href=\"mailto:belz12@yahoo.fr\">belz12@yahoo.fr</a></td></tr>");
		infos.append("<tr><td><strong>Site web</strong></td><td><a href=\"http://jtomtom.sourceforge.net\">http://jtomtom.sourceforge.net</a></td></tr>");
		infos.append("<tr><td><strong>Licence</strong></td><td>GPLv3</td></tr>");
		infos.append("</table>");
		infos.append("</html>");
		
		JLabel label = new JLabel(infos.toString());
		add(label);
		
		add(Box.createRigidArea(new Dimension(0, 15)));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
