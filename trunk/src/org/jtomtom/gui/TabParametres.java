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
	
	private JTextField 	paramProxyHost;
	private JTextField 	paramProxyPort;
	private JComboBox  	paramProxyType;
	
	private JTextField 	m_ttmaxUser;
	private JTextField 	m_ttmaxPassword;

	
	private JComboBox  	paramLogLevel;
	private JTextField 	paramLogFile;
	
	private JCheckBox	paramCheckUpdate;
	
	private JButton		btSave;
	
	public TabParametres() {
		super(getTabTranslations().getString("org.jtomtom.tab.parameters.title"));
		setPanelLeftImage(getClass().getResource("resources/parametres.png"));
	}
	
	/**
	 * Construction de l'interface graphique
	 */
	public JPanel build() {
		if (isBuild()) return this;
		super.build();
		LOGGER.trace("Building TabParametres ...");
		
		// Paramètres du proxy
		buildProxyFields();
		JPanel proxyPanel = new JPanel(new SpringLayout());
		proxyPanel.setBorder(BorderFactory.createTitledBorder(getTabTranslations().getString("org.jtomtom.tab.parameters.border.network.label")));
		proxyPanel.add(new JLabel(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.conn.label")));
		proxyPanel.add(paramProxyType);
		proxyPanel.add(new JLabel(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.proxy.label")));
		proxyPanel.add(paramProxyHost);
		proxyPanel.add(new JLabel(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.port.label")));
		proxyPanel.add(paramProxyPort);
		SpringUtilities.makeCompactGrid(proxyPanel,
                3, 2, 		 //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
		add(proxyPanel);
		
		// Paramètres Tomtomax
		buildConnexionFields();
		JPanel tomtomaxPanel = new JPanel(new SpringLayout());
		tomtomaxPanel.setBorder(BorderFactory.createTitledBorder(getTabTranslations().getString("org.jtomtom.tab.parameters.border.poisradars.label")));
		tomtomaxPanel.add(new JLabel(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.user.label")));
		tomtomaxPanel.add(m_ttmaxUser);
		tomtomaxPanel.add(new JLabel(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.password.label")));
		tomtomaxPanel.add(m_ttmaxPassword);
		SpringUtilities.makeCompactGrid(tomtomaxPanel,
                2, 2, 		 //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad

		add(tomtomaxPanel);
		
		// Paramètres des log
		buildLogsFields();
		JPanel logsPanel = new JPanel(new SpringLayout());
		logsPanel.setBorder(BorderFactory.createTitledBorder(getTabTranslations().getString("org.jtomtom.tab.parameters.border.log.label")));
		logsPanel.add(new JLabel(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.label")));
		logsPanel.add(paramLogLevel);
		logsPanel.add(new JLabel(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.logfile.label")));
		logsPanel.add(paramLogFile);
		SpringUtilities.makeCompactGrid(logsPanel,
                2, 2, 		 //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
		add(logsPanel);

		// Paramètres divers
		buildDummyFields();
		JPanel dummyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		dummyPanel.setBorder(BorderFactory.createTitledBorder(getTabTranslations().getString("org.jtomtom.tab.parameters.border.dummy.label")));
		dummyPanel.add(paramCheckUpdate);
		add(dummyPanel);
		
		// Création du panneau de bouton
		btSave = new JButton(getTabTranslations().getString("org.jtomtom.tab.parameters.button.save.label"));
		btSave.addActionListener(this);
		addActionButton(btSave);
		
		addScrollVerticalBar();
		
		return this;
	}	
	
	/**
	 * Initialise les champs de configuration proxy
	 */
	private void buildProxyFields() {
		// - Type de proxy
		String[] proxyTypeStrings = {"DIRECT", "HTTP", "SOCKS"};
		paramProxyType = new JComboBox(proxyTypeStrings);
		paramProxyType.setSelectedIndex(Arrays.binarySearch(proxyTypeStrings, globalProperties.getUserProperty("net.proxy.type")));
		paramProxyType.setPrototypeDisplayValue("DIRECTI");	// Initialise la taille de la combo
		paramProxyType.addActionListener(this);
		
		// - Serveur Proxy
		paramProxyHost = new JTextField();
		paramProxyHost.setColumns(15);
		paramProxyHost.setPreferredSize(new Dimension(0,25));
		paramProxyHost.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.proxy.hint"));
		paramProxyHost.setText(globalProperties.getUserProperty("net.proxy.name"));

		// - Port du proxy
		paramProxyPort = new JTextField();
		paramProxyPort.setColumns(4);
		paramProxyPort.setPreferredSize(new Dimension(0,25));
		paramProxyPort.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.port.hint"));
		paramProxyPort.setText(globalProperties.getUserProperty("net.proxy.port"));
		
		if (paramProxyType.getSelectedItem().equals("DIRECT")) {
			paramProxyHost.setEnabled(false);
			paramProxyPort.setEnabled(false);
		} else {
			paramProxyHost.setEnabled(true);
			paramProxyPort.setEnabled(true);
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
		m_ttmaxUser.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.user.hint"));
		m_ttmaxUser.setText(globalProperties.getUserProperty("org.tomtomax.user"));

		// - Password Tomtomax
		m_ttmaxPassword = new JPasswordField();
		m_ttmaxPassword.setColumns(20);
		m_ttmaxPassword.setPreferredSize(new Dimension(0,25));
		m_ttmaxPassword.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.password.hint"));
		m_ttmaxPassword.setText(globalProperties.getUserProperty("org.tomtomax.password"));
	}

	/**
	 * Initialise les champs de configuration des logs
	 */
	private void buildLogsFields() {
		// Le tableau est trié par ordre alphabétique pour que le binarySearch fonctionne
		String[] logLevelStrings = {"DEBUG", "ERROR", "INFO", "WARN"};
		paramLogLevel = new JComboBox(logLevelStrings);
		paramLogLevel.setSelectedIndex(
				Arrays.binarySearch(
						logLevelStrings, 
						Level.toLevel(globalProperties.getUserProperty("org.jtomtom.logLevel")).toString()));
		paramLogLevel.setPrototypeDisplayValue("DEBUGI");
		
		// - Fichier de log
		paramLogFile = new JTextField();
		paramLogFile.setColumns(17);
		paramLogFile.setPreferredSize(new Dimension(0,25));
		paramLogFile.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.parameters.border.log.hint"));
		paramLogFile.setText(globalProperties.getUserProperty("org.jtomtom.logFile"));
	}

	/**
	 * Crée et initialise les champs de paramètre divers
	 */
	private void buildDummyFields() {
		paramCheckUpdate = new JCheckBox(getTabTranslations().getString("org.jtomtom.tab.parameters.checkbox.update.label"));
		paramCheckUpdate.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.parameters.checkbox.update.hint"));
		
		// Initialisation
		boolean checkUpdate = "true".equals(globalProperties.getUserProperty("org.jtomtom.checkupdate", "true"));
		paramCheckUpdate.setSelected(checkUpdate);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == btSave) {
			try {
				if (!"DIRECT".equals((String)paramProxyType.getSelectedItem())) {
					if (paramProxyHost.getText().trim().isEmpty() || paramProxyPort.getText().trim().isEmpty()) {
						throw new JTomtomException("org.jtomtom.errors.settings.proxy");
					}
				}
				globalProperties.setUserProperty("net.proxy.type", ((String)paramProxyType.getSelectedItem()));
				globalProperties.setUserProperty("net.proxy.name", paramProxyHost.getText());
				globalProperties.setUserProperty("net.proxy.port", paramProxyPort.getText());
				globalProperties.setUserProperty("org.jtomtom.logLevel", ((String)paramLogLevel.getSelectedItem()));
				globalProperties.setUserProperty("org.jtomtom.logFile", paramLogFile.getText());
				globalProperties.setUserProperty("org.tomtomax.user", m_ttmaxUser.getText());
				globalProperties.setUserProperty("org.tomtomax.password", m_ttmaxPassword.getText());
				globalProperties.setUserProperty("org.jtomtom.checkupdate", Boolean.toString(paramCheckUpdate.isSelected()));
			
				globalProperties.storeUserProperties(
						new File(System.getProperty("user.home"), Constant.JTOMTOM_USER_PROPERTIES));
				
			} catch (Exception e) { 
				LOGGER.error("An error occurred while writing properties file ("+System.getProperty("user.home")+File.separator+Constant.JTOMTOM_USER_PROPERTIES+") !");
				JOptionPane.showMessageDialog(this, 
						e.getLocalizedMessage(), 
						getTabTranslations().getString("org.jtomtom.tab.parameters.dialog.save.title"), JOptionPane.ERROR_MESSAGE);
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
					getTabTranslations().getString("org.jtomtom.tab.parameters.dialog.save.message"), 
					getTabTranslations().getString("org.jtomtom.tab.parameters.dialog.save.title"), JOptionPane.INFORMATION_MESSAGE);
			
		} else if (event.getSource() == paramProxyType) {
			
			if (paramProxyType.getSelectedItem().equals("DIRECT")) {
				paramProxyHost.setEnabled(false);
				paramProxyPort.setEnabled(false);
			} else {
				paramProxyHost.setEnabled(true);
				paramProxyPort.setEnabled(true);
			}
		}
		
	}
}
