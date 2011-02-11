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
package org.jtomtom.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.device.Chipset;

/**
 * @author Frédéric Combes
 *
 */
public class ChooseChipsetDialog extends JDialog implements ActionListener {
	private static final Logger LOGGER = Logger.getLogger(ChooseChipsetDialog.class);
	private static final long serialVersionUID = 1L;
	private static final String QUICK_FIX_MOREINFO_URL = "http://jtomtom.sourceforge.net/?q=doc/utilisation/gpsquickfix#nochipset";
	
	private final ResourceBundle theTranslator = Application.getInstance().getMainTranslator();
	
	private JButton selectButton;
	private JButton cancelButton;
	private JButton moreInformationsButton;
	private JComboBox availableChipset;
	
	private Chipset selectedChipset = null;
	
	public ChooseChipsetDialog() {
		super();
		
		build();
	}
	
	private void build() {
		// - Define properties
		setTitle(theTranslator.getString("org.jtomtom.main.dialog.choosechipset.title"));
		setSize(400, 120);
		setModal(true);
		setLocationRelativeTo(null);
		setResizable(false);
		
		// - Define content
		setContentPane(buildContentPane());
	}
	
	private JPanel buildContentPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		
		JLabel label = new JLabel(theTranslator.getString("org.jtomtom.main.dialog.choosechipset.message"));
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		panel.add(label, BorderLayout.PAGE_START);
		
		availableChipset = new JComboBox(Chipset.available());
		panel.add(availableChipset, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		cancelButton = new JButton(theTranslator.getString("org.jtomtom.main.dialog.choosechipset.button.cancel.label"));
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		
		moreInformationsButton = new JButton(theTranslator.getString("org.jtomtom.main.dialog.choosechipset.button.infos.label"));
		moreInformationsButton.addActionListener(this);
		buttonPanel.add(moreInformationsButton);
		
		selectButton = new JButton(theTranslator.getString("org.jtomtom.main.dialog.choosechipset.button.select.label"));
		selectButton.addActionListener(this);
		buttonPanel.add(selectButton);
		
		panel.add(buttonPanel, BorderLayout.PAGE_END);
		
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent p_event) {
		if (p_event.getSource() == selectButton) {
			selectedChipset = (Chipset)availableChipset.getSelectedItem();
			this.dispose();
			
		} else if (p_event.getSource() == moreInformationsButton) {
			try { java.awt.Desktop.getDesktop().browse(java.net.URI.create(QUICK_FIX_MOREINFO_URL));} 
			catch (IOException e) { LOGGER.warn(e.getLocalizedMessage()); }
			
		} else if (p_event.getSource() == cancelButton) {
			selectedChipset = Chipset.UNKNOWN;
			this.dispose();
		}
	}
	
	public Chipset getSelectedChipset() {
		return selectedChipset;
	}
}
