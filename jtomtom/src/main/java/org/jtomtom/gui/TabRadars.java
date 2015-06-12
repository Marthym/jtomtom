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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker.StateValue;

import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.InitialErrorRun;
import org.jtomtom.JTomtomException;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.device.TomtomMap;
import org.jtomtom.gui.action.ActionResult;
import org.jtomtom.gui.action.LoadInformationsWorker;
import org.jtomtom.gui.action.UpdateRadarsAction;
import org.jtomtom.gui.utilities.JTTabPanel;
import org.jtomtom.gui.utilities.SpringUtilities;

/**
 * @author Frédéric Combes
 *
 * Update camera tab
 * Process use some Radars Connectors for update the GPS POIs
 */
public class TabRadars extends JTTabPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabRadars.class);
	
	private final TomtomDevice theDevice = Application.getInstance().getTheDevice();
	
	private JLabel infosHtml;
	private JButton radarsButton;
	private JButton refreshButton;
	private List<JCheckBox> mapsCheckList;
	private JComboBox radarSiteList;
	
	private LoadInformationsWorker loadWorker;

	
	/**
	 * Init display of the tab
	 */
	public TabRadars() {
		super(getTabTranslations().getString("org.jtomtom.tab.radars.title"));
		setPanelLeftImage(getClass().getResource("resources/radars.png"));
	}
	
	/**
	 * Build the UI
	 */
	public JPanel build() {
		if (isBuild()) return this;
		super.build();
		LOGGER.trace("Building TabRadars UI ...");

		getScrollPanel().setLayout(new SpringLayout());	// For better layout we use SpringLayout for this tab
		
		// Add informations about Radars
		infosHtml = new JLabel("");
		add(infosHtml);
		
		// Add Refresh button
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		refreshButton = new JButton(getTabTranslations().getString("org.jtomtom.tab.radars.button.refresh.label"));
		refreshButton.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.radars.button.refresh.hint"));
		refreshButton.setEnabled(false);
		refreshButton.addActionListener(this);
		buttonPanel.add(refreshButton);
		
		RadarsConnector[] allConnectors = RadarsConnector.getAllRadarsConnectors();
		radarSiteList = new JComboBox(allConnectors);
		for (RadarsConnector radar : allConnectors) {
			if (radar.toString().endsWith("["+Locale.getDefault().getCountry()+"]")) {
				radarSiteList.setSelectedItem(radar);
			}
		}
		buttonPanel.add(radarSiteList);
		buttonPanel.setMaximumSize(
				new Dimension(
						(int)buttonPanel.getMaximumSize().getWidth(),
						(int)refreshButton.getPreferredSize().getHeight()));
		radarSiteList.setPreferredSize(
				new Dimension(
						(int)radarSiteList.getPreferredSize().getWidth(), 
						(int)refreshButton.getPreferredSize().getHeight()));
		add(buttonPanel);
		
		// Make list of the maps found in the GPS
		// A list of checkbox with scrollbar
		JPanel checkBoxPane = new JPanel();
		checkBoxPane.setLayout(new BoxLayout(checkBoxPane, BoxLayout.PAGE_AXIS));
		mapsCheckList = new LinkedList<JCheckBox>();
		try {
			Iterator<TomtomMap> it = theDevice.getAvailableMaps().values().iterator();
			while (it.hasNext()) {
				TomtomMap map = it.next();
				JCheckBox chk = new JCheckBox(map.getName());
				chk.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.radars.panel.maplist.hint"));
				if (map.getName().equals(theDevice.getActiveMap().getName())) {
					chk.setSelected(true);
				}
				mapsCheckList.add(chk);
				checkBoxPane.add(chk);
			}
		} catch (JTomtomException e) {
			SwingUtilities.invokeLater(new InitialErrorRun(e));
		}
		JScrollPane scroll = new JScrollPane(checkBoxPane);
		checkBoxPane.setMaximumSize(new Dimension(200, (int)checkBoxPane.getMaximumSize().getHeight()));
		scroll.setMaximumSize(new Dimension(200, (int)scroll.getMaximumSize().getHeight()));
		scroll.setToolTipText(getTabTranslations().getString("org.jtomtom.tab.radars.panel.maplist.hint"));
		add(scroll);
		
		// Add the action button at the bottom
		radarsButton = new JButton(new UpdateRadarsAction(getTabTranslations().getString("org.jtomtom.tab.radars.button.update.label")));
		radarsButton.setEnabled(false);
		addActionButton(radarsButton);

		// When we finish to add Component, we make a pretty layout
		SpringUtilities.makeCompactGrid(getScrollPanel(),
				getScrollPanel().getComponentCount(), 1, // rows, cols
                0, 0,        							// initX, initY
                0, 3);       							// xPad, yPad
		
		return this;
	}
	
	/**
	 * Run a Worker for getting informations on the remote POIs web site
	 */
	public void loadRadarsInfos() {
		// To avoid doing this every time the tab is displayed
		if (refreshButton.isEnabled()) {
			return;
		}
		LOGGER.info("Getting remote POIs informations ...");
		
		StringBuffer infos = new StringBuffer();
		infos.append("<html><table>");
		infos.append("<tr><td><strong>").append(getTabTranslations().getString("org.jtomtom.tab.radars.availableupdate"))
			.append(" : </strong></td><td><i>").append(getTabTranslations().getString("org.jtomtom.tab.radars.loading")).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(getTabTranslations().getString("org.jtomtom.tab.radars.installedupdate"))
			.append(" : </strong></td><td><i>").append(getTabTranslations().getString("org.jtomtom.tab.radars.loading")).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(getTabTranslations().getString("org.jtomtom.tab.radars.radarcount"))
			.append(" : </strong></td><td><i>").append(getTabTranslations().getString("org.jtomtom.tab.radars.loading")).append("</i></td></tr>");
		infos.append("</table>");
		infos.append("<br/><font size=\"2\"><p><i>")
			.append(getTabTranslations().getString("org.jtomtom.tab.radars.radarprovidedby"))
			.append(" <a href=\"").append(RadarsConnector.EMPTY_RADAR_CONNECTOR.getConnectorWebsite()).append("\">")
			.append(RadarsConnector.EMPTY_RADAR_CONNECTOR)
			.append("</a></i></p></font>");
		infos.append("</html>");
		infosHtml.setText(infos.toString());
		
		if (loadWorker == null || loadWorker.getState() != StateValue.STARTED) { // For launch one work at a time
			LOGGER.debug("Launch LoadInformationsWorker ...");
			loadWorker = new LoadInformationsWorker(this);
			loadWorker.setTomtomDevice(theDevice);
			loadWorker.execute();
		}
	}
	
	/**
	 * Disable refresh button and so require refresh of pois remote infos at next tab radar display
	 */
	public void disableRefreshButton() {
		refreshButton.setEnabled(false);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == refreshButton) {
			refreshButton.setEnabled(false);
			loadRadarsInfos();
		}
		
	}
	
	public final List<JCheckBox> getMapsCheckList() {
		return mapsCheckList;
	}
	
	public final RadarsConnector getSelectedRadarConnector() {
		return (RadarsConnector)radarSiteList.getSelectedItem();
	}

	public void refreshDisplay(ActionResult infos) {
		if (!infos.status) {
			JOptionPane.showMessageDialog(null, 
					infos.exception.getLocalizedMessage(), 
					TabRadars.getTabTranslations().getString("org.jtomtom.tab.radars.sw.error.title"), JOptionPane.ERROR_MESSAGE);
			refreshButton.setEnabled(true);
		} else {
			radarsButton.setEnabled(true);
		}
		if (infos.parameters != null)
			infosHtml.setText(infos.parameters.get(0));
		
		refreshButton.setEnabled(true);		
	}

}
