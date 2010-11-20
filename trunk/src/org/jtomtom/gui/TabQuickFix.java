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
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.JTomtomException;
import org.jtomtom.device.Chipset;
import org.jtomtom.device.ChipsetNotFoundException;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.gui.action.MajQuickFixAction;
import org.jtomtom.gui.utilities.JTTabPanel;

/**
 * @author Frédéric Combes
 *
 */
public class TabQuickFix extends JTTabPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabQuickFix.class);
	
	private final TomtomDevice theDevice = Application.getInstance().getTheDevice();
	private JLabel quickFixInfos;
	private JButton quickFixButton;
	
	public TabQuickFix() {
		super(m_rbControls.getString("org.jtomtom.tab.quickfix.title"));
		
		build();
	}
	
	private void build() {
		super.build(getClass().getResource("resources/quickfix.png"));
		add(Box.createRigidArea(new Dimension(0,5)));
		
		quickFixInfos = new JLabel("");
		add(quickFixInfos);
		
		quickFixButton = new JButton(new MajQuickFixAction(m_rbControls.getString("org.jtomtom.tab.quickfix.button.update.label")));
		addActionButton(quickFixButton);
		
	}
	
	/**
	 * Charge les infos nécessaire au QuickFix du GPS et met à jour l'onglet avec
	 */
	public void loadQuickFixInfos() {
		LOGGER.info("Récupération des informations QuickFix");
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
		StringBuffer infos = new StringBuffer();
		
		try { theDevice.getChipset(); }
		catch (ChipsetNotFoundException e) {
			theDevice.forceChipset(askForChipset());
		}
		
		try {
			infos.append("<html><table>");
			infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.quickfix.chipset")).append(" : </strong></td><td><i>").append(theDevice.getChipset()).append("</i></td></tr>");
			infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.quickfix.lastupdate")).append(" : </strong></td><td><i>")
				.append(dateFormat.format(new Date(theDevice.getQuickFixLastUpdate())))
				.append("</i></td></tr>");
			infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.quickfix.expirency")).append(" : </strong></td><td><i>")
				.append(dateFormat.format(new Date(theDevice.getQuickFixExpiry())))
				.append("</i></td></tr>");
			if ((new Date()).getTime() > theDevice.getQuickFixExpiry()) {
				infos.append("<tr><td></td><td><i>").append(m_rbControls.getString("org.jtomtom.tab.quickfix.ephemtoold")).append("</i></td></tr>");
			} else {
				infos.append("<tr><td></td><td><i>").append(m_rbControls.getString("org.jtomtom.tab.quickfix.ephemuptodate")).append("</i></td></tr>");
			}
			infos.append("</table>");
			infos.append("</html>");
			
		} catch (JTomtomException e) {
			LOGGER.error(e.getLocalizedMessage());
			if (LOGGER.isDebugEnabled()) e.printStackTrace();
			
			JOptionPane.showMessageDialog(this, e.getLocalizedMessage());
			infos.delete(0, infos.length());
			infos.append("<html>");
			infos.append("<p>"+e.getLocalizedMessage()+"</p>");
			infos.append("</html>");
			
			quickFixButton.setEnabled(false);
		}
		
		quickFixInfos.setText(infos.toString());
	}
	
	private Chipset askForChipset() {
		ChooseChipsetDialog chooseChipsetDial = new ChooseChipsetDialog();
		chooseChipsetDial.setVisible(true);

		return chooseChipsetDial.getSelectedChipset();
	}
	
}
