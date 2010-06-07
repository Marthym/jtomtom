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
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jtomtom.JTomtom;
import org.jtomtom.JTomtomException;
import org.jtomtom.gui.action.MajQuickFixAction;

/**
 * @author marthym
 *
 */
public class TabQuickFix extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabQuickFix.class);
	
	private JLabel quickFixInfos;
	private JButton quickFixButton;
	
	public TabQuickFix() {
		super();
		
		build();
	}
	
	private void build() {
		setLayout(new BorderLayout());
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));

		JLabel image = new JLabel(new ImageIcon(getClass().getResource("resources/quickfix.png"), "QFx"));
		add(image, BorderLayout.LINE_START);
		
		quickFixInfos = new JLabel("");
		centerPanel.add(quickFixInfos);
		add(centerPanel, BorderLayout.CENTER);
		
		quickFixButton = new JButton(new MajQuickFixAction("Mettre à jour QuickFix"));
		add(quickFixButton, BorderLayout.PAGE_END);
		
	}
	
	/**
	 * Charge les infos nécessaire au QuickFix du GPS et met à jour l'onglet avec
	 */
	public void loadQuickFixInfos() {
		LOGGER.info("Récupération des informations QuickFix");
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
		StringBuffer infos = new StringBuffer();
		try {
			infos.append("<html><h1>Informations QuickFix</h1>");
			infos.append("<table>");
			infos.append("<tr><td>Chipset : </td><td><i>").append(JTomtom.getTheGPS().getChipset()).append("</i></td></tr>");
			infos.append("<tr><td>Dernière MAJ : </td><td><i>")
				.append(dateFormat.format(new Date(JTomtom.getTheGPS().getQuickFixLastUpdate())))
				.append("</i></td></tr>");
			infos.append("<tr><td>Expire le : </td><td><i>")
				.append(dateFormat.format(new Date(JTomtom.getTheGPS().getQuickFixExpiry())))
				.append("</i></td></tr>");
			if ((new Date()).getTime() > JTomtom.getTheGPS().getQuickFixExpiry()) {
				infos.append("<tr><td></td><td><i>L'éphémeride est obsolète !</i></td></tr>");
			} else {
				infos.append("<tr><td></td><td><i>Il n'est pas nécessaire de mettre à jour</i></td></tr>");
			}
			infos.append("</table>");
			infos.append("</html>");
			
		} catch (JTomtomException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			
			JOptionPane.showMessageDialog(this, e.getLocalizedMessage());
			infos.delete(0, infos.length());
			infos.append("<html><h1>Informations QuickFix</h1>");
			infos.append("<p>"+e.getLocalizedMessage()+"</p>");
			infos.append("</html>");
			
			quickFixButton.setEnabled(false);
		}
		
		quickFixInfos.setText(infos.toString());
	}
	
}
