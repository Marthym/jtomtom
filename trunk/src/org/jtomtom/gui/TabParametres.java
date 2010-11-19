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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.Constant;
import org.jtomtom.JTomtomException;
import org.jtomtom.JTomtomProperties;
import org.jtomtom.gui.utilities.JTTabPanel;
import org.jtomtom.gui.utilities.SpringUtilities;

/**
 * @author Frédéric Combes
 *
 */
public class TabParametres extends JTTabPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabParametres.class);
	
	private final JTomtomProperties globalProperties = Application.getInstance().getGlobalProperties();
	
	private JTextField 	m_proxyHost;
	private JTextField 	m_proxyPort;
	private JComboBox  	m_proxyType;
	
	private JTextField 	m_ttmaxUser;
	private JTextField 	m_ttmaxPassword;

	
	private JComboBox  	m_logLevel;
	private JTextField 	m_logFile;
	
	private JCheckBox	m_checkUpdate;
	
	private JButton		m_enregistrer;
	
	public TabParametres() {
		super(m_rbControls.getString("org.jtomtom.tab.parameters.title"));
		build();
	}
	
	/**
	 * Construction de l'interface graphique
	 */
	private void build() {
		super.build(getClass().getResource("resources/parametres.png"));
		
		// Paramètres du proxy
		buildProxyFields();
		JPanel proxyPanel = new JPanel(new SpringLayout());
		proxyPanel.setBorder(BorderFactory.createTitledBorder(m_rbControls.getString("org.jtomtom.tab.parameters.border.network.label")));
		proxyPanel.add(new JLabel(m_rbControls.getString("org.jtomtom.tab.parameters.textfield.conn.label")));
		proxyPanel.add(m_proxyType);
		proxyPanel.add(new JLabel(m_rbControls.getString("org.jtomtom.tab.parameters.textfield.proxy.label")));
		proxyPanel.add(m_proxyHost);
		proxyPanel.add(new JLabel(m_rbControls.getString("org.jtomtom.tab.parameters.textfield.port.label")));
		proxyPanel.add(m_proxyPort);
		SpringUtilities.makeCompactGrid(proxyPanel,
                3, 2, 		 //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
		add(proxyPanel);
		
		// Paramètres Tomtomax
		buildConnexionFields();
		JPanel tomtomaxPanel = new JPanel(new SpringLayout());
		tomtomaxPanel.setBorder(BorderFactory.createTitledBorder(m_rbControls.getString("org.jtomtom.tab.parameters.border.poisradars.label")));
		tomtomaxPanel.add(new JLabel(m_rbControls.getString("org.jtomtom.tab.parameters.textfield.user.label")));
		tomtomaxPanel.add(m_ttmaxUser);
		tomtomaxPanel.add(new JLabel(m_rbControls.getString("org.jtomtom.tab.parameters.textfield.password.label")));
		tomtomaxPanel.add(m_ttmaxPassword);
		SpringUtilities.makeCompactGrid(tomtomaxPanel,
                2, 2, 		 //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad

		add(tomtomaxPanel);
		
		// Paramètres des log
		buildLogsFields();
		JPanel logsPanel = new JPanel(new SpringLayout());
		logsPanel.setBorder(BorderFactory.createTitledBorder(m_rbControls.getString("org.jtomtom.tab.parameters.border.log.label")));
		logsPanel.add(new JLabel(m_rbControls.getString("org.jtomtom.tab.parameters.textfield.label")));
		logsPanel.add(m_logLevel);
		logsPanel.add(new JLabel(m_rbControls.getString("org.jtomtom.tab.parameters.textfield.logfile.label")));
		logsPanel.add(m_logFile);
		SpringUtilities.makeCompactGrid(logsPanel,
                2, 2, 		 //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
		add(logsPanel);

		// Paramètres divers
		buildDummyFields();
		JPanel dummyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		dummyPanel.setBorder(BorderFactory.createTitledBorder(m_rbControls.getString("org.jtomtom.tab.parameters.border.dummy.label")));
		dummyPanel.add(m_checkUpdate);
		add(dummyPanel);
		
		// Création du panneau de bouton
		m_enregistrer = new JButton(m_rbControls.getString("org.jtomtom.tab.parameters.button.save.label"));
		m_enregistrer.addActionListener(this);
		addActionButton(m_enregistrer);
		
		addScrollVerticalBar();
	}	
	
	/**
	 * Initialise les champs de configuration proxy
	 */
	private void buildProxyFields() {
		// - Type de proxy
		String[] proxyTypeStrings = {"DIRECT", "HTTP", "SOCKS"};
		m_proxyType = new JComboBox(proxyTypeStrings);
		m_proxyType.setSelectedIndex(Arrays.binarySearch(proxyTypeStrings, globalProperties.getUserProperty("net.proxy.type")));
		m_proxyType.setPrototypeDisplayValue("DIRECTI");	// Initialise la taille de la combo
		m_proxyType.addActionListener(this);
		
		// - Serveur Proxy
		m_proxyHost = new JTextField();
		m_proxyHost.setColumns(15);
		m_proxyHost.setPreferredSize(new Dimension(0,25));
		m_proxyHost.setToolTipText(m_rbControls.getString("org.jtomtom.tab.parameters.textfield.proxy.hint"));
		m_proxyHost.setText(globalProperties.getUserProperty("net.proxy.name"));

		// - Port du proxy
		m_proxyPort = new JTextField();
		m_proxyPort.setColumns(4);
		m_proxyPort.setPreferredSize(new Dimension(0,25));
		m_proxyPort.setToolTipText(m_rbControls.getString("org.jtomtom.tab.parameters.textfield.port.hint"));
		m_proxyPort.setText(globalProperties.getUserProperty("net.proxy.port"));
		
		if (m_proxyType.getSelectedItem().equals("DIRECT")) {
			m_proxyHost.setEnabled(false);
			m_proxyPort.setEnabled(false);
		} else {
			m_proxyHost.setEnabled(true);
			m_proxyPort.setEnabled(true);
		}

	}
	
	/**
	 * Initialise les champs de connexion à Tomtomax
	 */
	private void buildConnexionFields() {
		// - User tomtomax
		m_ttmaxUser = new JTextField();
		m_ttmaxUser.setColumns(20);
		m_ttmaxUser.setPreferredSize(new Dimension(0,25));
		m_ttmaxUser.setToolTipText(m_rbControls.getString("org.jtomtom.tab.parameters.textfield.user.hint"));
		m_ttmaxUser.setText(globalProperties.getUserProperty("org.tomtomax.user"));

		// - Password Tomtomax
		m_ttmaxPassword = new JPasswordField();
		m_ttmaxPassword.setColumns(20);
		m_ttmaxPassword.setPreferredSize(new Dimension(0,25));
		m_ttmaxPassword.setToolTipText(m_rbControls.getString("org.jtomtom.tab.parameters.textfield.password.hint"));
		m_ttmaxPassword.setText(globalProperties.getUserProperty("org.tomtomax.password"));
	}

	/**
	 * Initialise les champs de configuration des logs
	 */
	private void buildLogsFields() {
		// Le tableau est trié par ordre alphabétique pour que le binarySearch fonctionne
		String[] logLevelStrings = {"DEBUG", "ERROR", "INFO", "WARN"};
		m_logLevel = new JComboBox(logLevelStrings);
		m_logLevel.setSelectedIndex(
				Arrays.binarySearch(
						logLevelStrings, 
						Level.toLevel(globalProperties.getUserProperty("org.jtomtom.logLevel")).toString()));
		m_logLevel.setPrototypeDisplayValue("DEBUGI");
		
		// - Fichier de log
		m_logFile = new JTextField();
		m_logFile.setColumns(17);
		m_logFile.setPreferredSize(new Dimension(0,25));
		m_logFile.setToolTipText(m_rbControls.getString("org.jtomtom.tab.parameters.border.log.hint"));
		m_logFile.setText(globalProperties.getUserProperty("org.jtomtom.logFile"));
	}

	/**
	 * Crée et initialise les champs de paramètre divers
	 */
	private void buildDummyFields() {
		m_checkUpdate = new JCheckBox(m_rbControls.getString("org.jtomtom.tab.parameters.checkbox.update.label"));
		m_checkUpdate.setToolTipText(m_rbControls.getString("org.jtomtom.tab.parameters.checkbox.update.hint"));
		
		// Initialisation
		boolean checkUpdate = "true".equals(globalProperties.getUserProperty("org.jtomtom.checkupdate"));
		m_checkUpdate.setSelected(checkUpdate);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == m_enregistrer) {
			try {
				if (!"DIRECT".equals((String)m_proxyType.getSelectedItem())) {
					if (m_proxyHost.getText().trim().isEmpty() || m_proxyPort.getText().trim().isEmpty()) {
						throw new JTomtomException("org.jtomtom.errors.settings.proxy");
					}
				}
				globalProperties.setUserProperty("net.proxy.type", ((String)m_proxyType.getSelectedItem()));
				globalProperties.setUserProperty("net.proxy.name", m_proxyHost.getText());
				globalProperties.setUserProperty("net.proxy.port", m_proxyPort.getText());
				globalProperties.setUserProperty("org.jtomtom.logLevel", ((String)m_logLevel.getSelectedItem()));
				globalProperties.setUserProperty("org.jtomtom.logFile", m_logFile.getText());
				globalProperties.setUserProperty("org.tomtomax.user", m_ttmaxUser.getText());
				globalProperties.setUserProperty("org.tomtomax.password", m_ttmaxPassword.getText());
				globalProperties.setUserProperty("org.jtomtom.checkupdate", Boolean.toString(m_checkUpdate.isSelected()));
			
				globalProperties.storeUserProperties(
						new File(System.getProperty("user.home"), Constant.JTOMTOM_USER_PROPERTIES));
				
			} catch (Exception e) { 
				LOGGER.error("An error occurred while writing properties file ("+System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES+") !");
				JOptionPane.showMessageDialog(this, 
						e.getLocalizedMessage(), 
						m_rbControls.getString("org.jtomtom.tab.parameters.dialog.save.title"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			Application.getInstance().reloadProperties();
			for (Component currTab : getParent().getComponents()) {
				if (TabSauvegarde.class.isAssignableFrom(currTab.getClass())) {
					((TabSauvegarde)currTab).refreshISOType();
					break;
				}
			}
			JOptionPane.showMessageDialog(this, 
					m_rbControls.getString("org.jtomtom.tab.parameters.dialog.save.message"), 
					m_rbControls.getString("org.jtomtom.tab.parameters.dialog.save.title"), JOptionPane.INFORMATION_MESSAGE);
			
		} else if (event.getSource() == m_proxyType) {
			
			if (m_proxyType.getSelectedItem().equals("DIRECT")) {
				m_proxyHost.setEnabled(false);
				m_proxyPort.setEnabled(false);
			} else {
				m_proxyHost.setEnabled(true);
				m_proxyPort.setEnabled(true);
			}
		}
		
	}
}
