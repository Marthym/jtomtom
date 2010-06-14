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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Level;
import org.jtomtom.JTomtom;
import org.jtomtom.gui.utilities.JTTabPanel;

/**
 * @author marthym
 *
 */
public class TabParametres extends JTTabPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	//private static final Logger LOGGER = Logger.getLogger(TabParametres.class);
	
	private JTextField 	m_proxyHost;
	private JTextField 	m_proxyPort;
	private JComboBox  	m_proxyType;
	
	private JTextField 	m_ttmaxUser;
	private JTextField 	m_ttmaxPassword;

	
	private JComboBox  	m_logLevel;
	private JTextField 	m_logFile;
	
	private JButton		m_enregistrer;
	
	public TabParametres() {
		super("Paramètres");
		build();
	}
	
	/**
	 * Construction de l'interface graphique
	 */
	private void build() {
		super.build(getClass().getResource("resources/parametres.png"));
		
		// Paramètres du proxy
		buildProxyFields();
		JPanel proxyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		proxyPanel.setBorder(BorderFactory.createTitledBorder("Réseau"));
		proxyPanel.add(new JLabel("Proxy :"));
		proxyPanel.add(m_proxyType);
		proxyPanel.add(m_proxyHost);
		proxyPanel.add(new JLabel(":"));
		proxyPanel.add(m_proxyPort);
		add(proxyPanel);
		
		// Paramètres des log
		buildLogsFields();
		JPanel logsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		logsPanel.setBorder(BorderFactory.createTitledBorder("Traces"));
		logsPanel.add(new JLabel("Niveau de log :"));
		logsPanel.add(m_logLevel);
		logsPanel.add(m_logFile);
		add(logsPanel);

		// Paramètres des log
		buildConnexionFields();
		JPanel tomtomaxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tomtomaxPanel.setBorder(BorderFactory.createTitledBorder("Tomtomax"));
		tomtomaxPanel.add(new JLabel("User :"));
		tomtomaxPanel.add(m_ttmaxUser);
		tomtomaxPanel.add(new JLabel("  Password :"));
		tomtomaxPanel.add(m_ttmaxPassword);
		add(tomtomaxPanel);
		
		// Création du panneau de bouton
		m_enregistrer = new JButton("Enregistrer");
		m_enregistrer.addActionListener(this);
		addActionButton(m_enregistrer);
	}	
	
	/**
	 * Initialise les champs de configuration proxy
	 */
	private void buildProxyFields() {
		// - Type de proxy
		String[] proxyTypeStrings = {"DIRECT", "HTTP", "SOCKS"};
		m_proxyType = new JComboBox(proxyTypeStrings);
		m_proxyType.setSelectedIndex(Arrays.binarySearch(proxyTypeStrings, JTomtom.getApplicationPropertie("net.proxy.type")));

		// - Serveur Proxy
		m_proxyHost = new JTextField();
		m_proxyHost.setColumns(16);
		m_proxyHost.setPreferredSize(new Dimension(0,25));
		m_proxyHost.setToolTipText("Nom du serveur ou adresse IP");
		m_proxyHost.setText(JTomtom.getApplicationPropertie("net.proxy.name"));

		// - Port du proxy
		m_proxyPort = new JTextField();
		m_proxyPort.setColumns(5);
		m_proxyPort.setPreferredSize(new Dimension(0,25));
		m_proxyPort.setToolTipText("Port d'écoute du proxy");
		m_proxyPort.setText(JTomtom.getApplicationPropertie("net.proxy.port"));
	}
	
	/**
	 * Initialise les champs de connexion à Tomtomax
	 */
	private void buildConnexionFields() {
		// - User tomtomax
		m_ttmaxUser = new JTextField();
		m_ttmaxUser.setColumns(11);
		m_ttmaxUser.setPreferredSize(new Dimension(0,25));
		m_ttmaxUser.setToolTipText("Utilisateur Tomtomax");
		m_ttmaxUser.setText(JTomtom.getApplicationPropertie("org.tomtomax.user"));

		// - Password Tomtomax
		m_ttmaxPassword = new JPasswordField();
		m_ttmaxPassword.setColumns(11);
		m_ttmaxPassword.setPreferredSize(new Dimension(0,25));
		m_ttmaxPassword.setToolTipText("Password Tomtomax");
		m_ttmaxPassword.setText(JTomtom.getApplicationPropertie("org.tomtomax.password"));
	}

	/**
	 * Initialise les champs de configuration des logs
	 */
	private void buildLogsFields() {
		String[] logLevelStrings = {"DEBUG", "ERROR", "INFO", "WARN"};
		m_logLevel = new JComboBox(logLevelStrings);
		m_logLevel.setSelectedIndex(
				Arrays.binarySearch(
						logLevelStrings, 
						Level.toLevel(JTomtom.getApplicationPropertie("org.jtomtom.logLevel")).toString()));

		// - Fichier de log
		m_logFile = new JTextField();
		m_logFile.setColumns(19);
		m_logFile.setPreferredSize(new Dimension(0,25));
		m_logFile.setToolTipText("Fichier de destination des logs, laisser vide si pas de fichier.");
		m_logFile.setText(JTomtom.getApplicationPropertie("org.jtomtom.logFile"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m_enregistrer) {
			JTomtom.setApplicationPropertie("net.proxy.type", ((String)m_proxyType.getSelectedItem()));
			JTomtom.setApplicationPropertie("net.proxy.name", ((String)m_proxyHost.getText()));
			JTomtom.setApplicationPropertie("net.proxy.port", ((String)m_proxyPort.getText()));
			JTomtom.setApplicationPropertie("org.jtomtom.logLevel", ((String)m_logLevel.getSelectedItem()));
			JTomtom.setApplicationPropertie("org.jtomtom.logFile", ((String)m_logFile.getText()));
			JTomtom.setApplicationPropertie("org.tomtomax.user", ((String)m_ttmaxUser.getText()));
			JTomtom.setApplicationPropertie("org.tomtomax.password", ((String)m_ttmaxPassword.getText()));
			
			JTomtom.saveApplicationProperties();
			JTomtom.loadProperties();
			JOptionPane.showMessageDialog(this, 
					"Propriétées enregistrée avec succès !", 
					"Information", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
}