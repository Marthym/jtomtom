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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.Constant;
import org.jtomtom.JTomtomException;
import org.jtomtom.JTomtomProperties;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.gui.utilities.JPasswordTableRenderer;
import org.jtomtom.gui.utilities.JTTabPanel;
import org.jtomtom.gui.utilities.SpringUtilities;

/**
 * @author Frédéric Combes
 *
 */
public class TabSettings extends JTTabPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabSettings.class);
	
	private final JTomtomProperties globalProperties = Application.getInstance().getGlobalProperties();
	
	private JTextField 	paramProxyHost;
	private JTextField 	paramProxyPort;
	private JComboBox  	paramProxyType;
	
	private JTable 		poisConnexionTable;
	private JButton		btAddLine;

	
	private JComboBox  	paramLogLevel;
	private JTextField 	paramLogFile;
	
	private JCheckBox	paramCheckUpdate;
	
	private JButton		btSave;
	
	public TabSettings() {
		super(getTabTranslations().getString("org.jtomtom.tab.parameters.title"));
		setPanelLeftImage(getClass().getResource("resources/parametres.png"));
	}
	
	/**
	 * Building the UI
	 */
	public JPanel build() {
		if (isBuild()) return this;
		super.build();
		LOGGER.trace("Building TabSettings ...");
		
		// Proxy server settings
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
		
		// POIs site connexion settings
		buildConnexionFields();
		JPanel tomtomaxPanel = new JPanel(new SpringLayout());
		tomtomaxPanel.setBorder(BorderFactory.createTitledBorder(getTabTranslations().getString("org.jtomtom.tab.parameters.border.poisradars.label")));
		tomtomaxPanel.add(poisConnexionTable.getTableHeader());
		tomtomaxPanel.add(poisConnexionTable);
		tomtomaxPanel.add(Box.createRigidArea(new Dimension(1, 6)));
		btAddLine = new JButton(getTabTranslations().getString("org.jtomtom.tab.parameters.button.addline"));
		btAddLine.addActionListener(this);
		tomtomaxPanel.add(btAddLine);
		SpringUtilities.makeCompactGrid(tomtomaxPanel,
                4, 1, 		 //rows, cols
                6, 6,        //initX, initY
                0, 0);       //xPad, yPad		
		add(tomtomaxPanel);
		
		// Log and trace settings
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

		// Various settings
		buildDummyFields();
		JPanel dummyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		dummyPanel.setBorder(BorderFactory.createTitledBorder(getTabTranslations().getString("org.jtomtom.tab.parameters.border.dummy.label")));
		dummyPanel.add(paramCheckUpdate);
		add(dummyPanel);
		
		// Create button panel
		btSave = new JButton(getTabTranslations().getString("org.jtomtom.tab.parameters.button.save.label"));
		btSave.addActionListener(this);
		addActionButton(btSave);
		
		addScrollVerticalBar();
		
		return this;
	}	
	
	/**
	 * Init proxy server settings
	 */
	private void buildProxyFields() {
		// - Proxy type
		String[] proxyTypeStrings = {"DIRECT", "HTTP", "SOCKS"};
		paramProxyType = new JComboBox(proxyTypeStrings);
		paramProxyType.setSelectedIndex(Arrays.binarySearch(proxyTypeStrings, globalProperties.getUserProperty("net.proxy.type")));
		paramProxyType.setPrototypeDisplayValue("DIRECTI");	// Need for init value list width
		paramProxyType.addActionListener(this);
		
		// - Proxy hostname
		paramProxyHost = new JTextField();
		paramProxyHost.setColumns(15);
		paramProxyHost.setPreferredSize(new Dimension(0,25));
		paramProxyHost.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.proxy.hint"));
		paramProxyHost.setText(globalProperties.getUserProperty("net.proxy.name"));

		// - Proxy port
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
	 * Init POIs site connexion settings
	 */
	private void buildConnexionFields() {
		
		String[] columnNames = {
				getTabTranslations().getString("org.jtomtom.tab.parameters.table.column.pois"),
				getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.user.label"),
				getTabTranslations().getString("org.jtomtom.tab.parameters.textfield.password.label")};

		Map<String, String> userList = Application.getInstance().getGlobalProperties().getUserProperties("org.connector.user");
		Map<String, String> passwordList = Application.getInstance().getGlobalProperties().getUserProperties("org.connector.password");
		Object[][] data = new Object[userList.size()][3];
		
		Iterator<String> it = userList.keySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			String key = it.next();
			String connectorLocale = key.substring(key.lastIndexOf('.')+1);
			RadarsConnector connector = RadarsConnector.getConnectorByLocale(connectorLocale);
			connector = (connector == null)?RadarsConnector.EMPTY_RADAR_CONNECTOR:connector;
			String userdata = userList.get(key);
			userdata = (userdata == null)?"":userdata;
			String passwordData = passwordList.get("org.connector.password."+connectorLocale);
			passwordData = (passwordData == null)?"":passwordData;
			data[i] = new Object[]{connector, userdata, passwordData};
			i++;
		}
		
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		poisConnexionTable = new JTable(model);

		// Build specific column renderer and editor
		TableColumn connectorColumn = poisConnexionTable.getColumnModel().getColumn(0);
		RadarsConnector[] allConnectors = RadarsConnector.getAllRadarsConnectors();
		JComboBox radarSiteList = new JComboBox(allConnectors);
		connectorColumn.setCellEditor(new DefaultCellEditor(radarSiteList));

		TableColumn passwordColumn = poisConnexionTable.getColumnModel().getColumn(2);
		passwordColumn.setCellEditor(new DefaultCellEditor(new JPasswordField()));
		passwordColumn.setCellRenderer(new JPasswordTableRenderer());
		
	}

	/**
	 * Init log & trace settings
	 */
	private void buildLogsFields() {
		// Sort the array in alphabetical order for accept binarySearch
		String[] logLevelStrings = {"DEBUG", "ERROR", "INFO", "WARN"};
		paramLogLevel = new JComboBox(logLevelStrings);
		paramLogLevel.setSelectedIndex(
				Arrays.binarySearch(
						logLevelStrings, 
						Level.toLevel(globalProperties.getUserProperty("org.jtomtom.logLevel")).toString()));
		paramLogLevel.setPrototypeDisplayValue("DEBUGI");
		
		// - Log file
		paramLogFile = new JTextField();
		paramLogFile.setColumns(17);
		paramLogFile.setPreferredSize(new Dimension(0,25));
		paramLogFile.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.parameters.border.log.hint"));
		paramLogFile.setText(globalProperties.getUserProperty("org.jtomtom.logFile"));
	}

	/**
	 * Init various settings
	 */
	private void buildDummyFields() {
		paramCheckUpdate = new JCheckBox(getTabTranslations().getString("org.jtomtom.tab.parameters.checkbox.update.label"));
		paramCheckUpdate.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.parameters.checkbox.update.hint"));
		
		boolean checkUpdate = "true".equals(globalProperties.getUserProperty("org.jtomtom.checkupdate", "true"));
		paramCheckUpdate.setSelected(checkUpdate);
	}

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
				globalProperties.setUserProperty("org.jtomtom.checkupdate", Boolean.toString(paramCheckUpdate.isSelected()));
			
				LOGGER.debug("Add "+poisConnexionTable.getRowCount()+" connexion settings from JTable");
				globalProperties.removeUserProperties("org.connector.user");
				globalProperties.removeUserProperties("org.connector.password");
				for (int i = 0; i < poisConnexionTable.getRowCount(); i++) {
					String userData = (String)poisConnexionTable.getValueAt(i, 1);
					if (userData.isEmpty()) continue;
					
					RadarsConnector connectorData = (RadarsConnector) poisConnexionTable.getValueAt(i, 0);
					String passwordData = (String)poisConnexionTable.getValueAt(i, 2);
					
					globalProperties.setUserProperty("org.connector.user."+connectorData.getLocale(), userData);
					globalProperties.setUserProperty("org.connector.password."+connectorData.getLocale(), passwordData);
				}
				
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
				if (TabBackupDevice.class.isAssignableFrom(currTab.getClass())) {
					((TabBackupDevice)currTab).refreshISOType();
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
			
		} else if (event.getSource() == btAddLine) {
			((DefaultTableModel)poisConnexionTable.getModel()).insertRow(poisConnexionTable.getRowCount(), new Object[]{RadarsConnector.EMPTY_RADAR_CONNECTOR,"",""});
			poisConnexionTable.getParent().doLayout();
			resizeScrolling(poisConnexionTable.getRowHeight());
		}
		
	}
}
