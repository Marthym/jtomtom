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

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.jtomtom.gui.action.IsoBackupAction;
import org.jtomtom.gui.utilities.JTTabPanel;

/**
 * @author Frédéric Combes
 *
 */
public class TabBackupDevice extends JTTabPanel implements MouseListener {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabBackupDevice.class);
	
	private final String DEFAULT_FILE_NAME = "gpsbackup-"+(new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date())+".iso";
	
	private JTextField 	isoFileChooser;
	private JCheckBox 	makeTestISO;

	public TabBackupDevice() {
		super(getTabTranslations().getString("org.jtomtom.tab.backup.title"));
		setPanelLeftImage(getClass().getResource("resources/sauvegarde.png"));
	}
	
	/**
	 * Build UI
	 */
	public JPanel build() {
		if (isBuild()) return this;
		super.build();
		LOGGER.trace("Building TabBackupDevice ...");
		
		add(Box.createRigidArea(new Dimension(0,5)));
		
		// Create presentation text
		JLabel infos = new JLabel(writeBackupInfos().toString());
		add(infos);
		add(Box.createRigidArea(new Dimension(0,20)));
		infos = new JLabel(getTabTranslations().getString("org.jtomtom.tab.backup.label.infos.text"));
		add(infos);
		
		// Create edit field for choose I/O file
		isoFileChooser = new JTextField();
		isoFileChooser.setMaximumSize(new Dimension(Short.MAX_VALUE,25));
		isoFileChooser.setMinimumSize(new Dimension(20,25));
		isoFileChooser.setAlignmentX(LEFT_ALIGNMENT);
		isoFileChooser.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.backup.textfield.filechooser.hint"));
		isoFileChooser.addMouseListener(this);
		isoFileChooser.setText(System.getProperty("user.home")+File.separator+DEFAULT_FILE_NAME);
		add(isoFileChooser);
		
		makeTestISO = new JCheckBox(getTabTranslations().getString("org.jtomtom.tab.backup.checkbox.isotest.label"));
		makeTestISO.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.backup.checkbox.isotest.hint"));
		makeTestISO.setVisible(LOGGER.isDebugEnabled());
		add(makeTestISO);
		add(Box.createRigidArea(new Dimension(0,10)));
		
		// Create button panel
		JButton bouton = new JButton(new IsoBackupAction(getTabTranslations().getString("org.jtomtom.tab.backup.button.createiso.label")));
		addActionButton(bouton);
		
		return this;
	}

	private final static StringBuffer writeBackupInfos() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<html><p>").append(getTabTranslations().getString("org.jtomtom.tab.backup.text.1")).append("</p>");
		buffer.append("<p>").append(getTabTranslations().getString("org.jtomtom.tab.backup.text.2")).append("</p>");
		
		return buffer;
	}
	
	/**
	 * Return text inside the TextField of target path
	 * @return	String
	 */
	public final String getTargetFile() {
		return isoFileChooser.getText();
	}
	
	/**
	 * Return the value of the option generate test ISO file
	 * @return	true or false
	 */
	public final boolean getMakeTestISO() {
		if (makeTestISO == null) return false;
		return makeTestISO.isSelected();
	}
	
	public void refreshISOType() {
		if (makeTestISO == null) return;
		
		if (LOGGER.isDebugEnabled()) {
			makeTestISO.setVisible(true);
		} else {
			makeTestISO.setSelected(false);
			makeTestISO.setVisible(false);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == isoFileChooser) {
			if (e.getClickCount() >= 2) {
				LOGGER.debug("Double-Click on the isoFileChooser");
				// Open file chooser on double-click
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					if (LOGGER.isDebugEnabled()) LOGGER.debug("Chosen file : "+fc.getSelectedFile().getAbsolutePath());
					isoFileChooser.setText(fc.getSelectedFile().getAbsolutePath());
				}

			}
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
}
