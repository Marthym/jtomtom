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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Proxy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

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
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.InitialErrorRun;
import org.jtomtom.JTomtomException;
import org.jtomtom.JTomtomProperties;
import org.jtomtom.connector.POIsDbInfos;
import org.jtomtom.connector.RadarsConnector;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.device.TomtomMap;
import org.jtomtom.gui.action.ActionResult;
import org.jtomtom.gui.action.MajRadarsAction;
import org.jtomtom.gui.utilities.JTTabPanel;
import org.jtomtom.gui.utilities.SpringUtilities;
import org.jtomtom.tools.NetworkTester;

/**
 * @author Frédéric Combes
 *
 * Onglet de mise à jour des radars
 * La mise à jour se fait grâce au site Tomtomax qui fourni les POIs
 */
public class TabRadars extends JTTabPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(TabRadars.class);
	
	private final TomtomDevice theDevice = Application.getInstance().getTheGPS();
	
	private JLabel infosHtml;
	private JButton radarsButton;
	private JButton refreshButton;
	private List<JCheckBox> mapsCheckList;
	private JComboBox radarSiteList;
	
	private POIsDbInfos remoteRadarsInfos;
	private POIsDbInfos localRadarsInfos;

	/**
	 * @author marthym
	 * 
	 * Le Worker qui rafraichit les informations de l'onglet Radar
	 * Vu qu'une des infos est prose sur le site de Tomtomax, la mise à jour peux potentiellement
	 * 		prendre du temps voire même se planter donc on efectue la récupération des informations
	 * 		en dehors de l'EDT
	 *
	 */
	class LoadInformationsWorker extends SwingWorker<ActionResult, Void> {
		@Override
        public ActionResult doInBackground() {
			LOGGER.debug("Enter in LoadInformationsWorker.doInBackground ...");
            return loadInBackground();
        }

		@Override
		protected void done() {
			try {
				ActionResult infos = get();
				if (!infos.status) {
					JOptionPane.showMessageDialog(null, 
							infos.exception.getLocalizedMessage(), 
							m_rbControls.getString("org.jtomtom.tab.radars.sw.error.title"), JOptionPane.ERROR_MESSAGE);
					refreshButton.setEnabled(true);
				} else {
					radarsButton.setEnabled(true);
				}
				if (infos.parameters != null)
					infosHtml.setText(infos.parameters.get(0));
				
				refreshButton.setEnabled(true);
				
				LOGGER.debug("Exécution de LoadInformationsWorker terminé.");
				
			} catch (InterruptedException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
				
			} catch (ExecutionException e) {
				LOGGER.error(e.getLocalizedMessage());
				if (LOGGER.isDebugEnabled()) e.printStackTrace();
			}
		}
	}
	private SwingWorker<ActionResult, Void> m_loadWorker;

	
	/**
	 * Initialise l'affichage et instancie le woker
	 */
	public TabRadars() {
		super(m_rbControls.getString("org.jtomtom.tab.radars.title"));
		m_loadWorker = new LoadInformationsWorker();
		build();
	}
	
	/**
	 * Fabrication de l'interface
	 */
	private void build() {
		super.build(getClass().getResource("resources/radars.png"));
		m_scrolledPanel.setLayout(new SpringLayout());	// For better layout we use SpringLayout for this tab
		
		// Add informations about Radars
		infosHtml = new JLabel("");
		add(infosHtml);
		
		// Add Refresh button
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		refreshButton = new JButton(m_rbControls.getString("org.jtomtom.tab.radars.button.refresh.label"));
		refreshButton.setToolTipText(m_rbControls.getString("org.jtomtom.tab.radars.button.refresh.hint"));
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
				chk.setToolTipText(m_rbControls.getString("org.jtomtom.tab.radars.panel.maplist.hint"));
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
		scroll.setToolTipText(m_rbControls.getString("org.jtomtom.tab.radars.panel.maplist.hint"));
		add(scroll);
		
		// Add the action button at the bottom
		radarsButton = new JButton(new MajRadarsAction(m_rbControls.getString("org.jtomtom.tab.radars.button.update.label")));
		radarsButton.setEnabled(false);
		addActionButton(radarsButton);

		// When we finish to add Component, we make a pretty layout
		SpringUtilities.makeCompactGrid(m_scrolledPanel,
				m_scrolledPanel.getComponentCount(), 1, // rows, cols
                0, 0,        							// initX, initY
                0, 3);       							// xPad, yPad
	}
	
	/**
	 * Récupération et affichage des élements texte de l'interface
	 */
	public void loadRadarsInfos() {
		// Pour ne pas faire ça à chaque passage sur l'onglet
		if (refreshButton.isEnabled()) {
			return;
		}
		LOGGER.info("Récupération des informations sur les Radars");
		
		StringBuffer infos = new StringBuffer();
		infos.append("<html><table>");
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.radars.availableupdate"))
			.append(" : </strong></td><td><i>").append(m_rbControls.getString("org.jtomtom.tab.radars.loading")).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.radars.installedupdate"))
			.append(" : </strong></td><td><i>").append(m_rbControls.getString("org.jtomtom.tab.radars.loading")).append("</i></td></tr>");
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.radars.radarcount"))
			.append(" : </strong></td><td><i>").append(m_rbControls.getString("org.jtomtom.tab.radars.loading")).append("</i></td></tr>");
		infos.append("</table>");
		infos.append("<br/><font size=\"2\"><p><i>")
			.append(m_rbControls.getString("org.jtomtom.tab.radars.radarprovidedby"))
			.append(" <a href=\"").append(m_rbControls.getString("org.jtomtom.tab.radars.tomtomax.url")).append("\">")
			.append(m_rbControls.getString("org.jtomtom.tab.radars.tomtomax.label"))
			.append("</a></i></p></font>");
		infos.append("</html>");
		infosHtml.setText(infos.toString());
		
		if (m_loadWorker.getState() != StateValue.STARTED) { // On en lance qu'un à la fois
			LOGGER.debug("Lancement de LoadInformationsWorker ...");
			m_loadWorker = new LoadInformationsWorker();
			m_loadWorker.execute();
		}
	}
	
	/**
	 * Désactive le bouton de refresh et ainsi impose
	 * Un rafraichissment des infos lors de l'appel à loadRadarsInfos
	 */
	public void disableRefreshButton() {
		refreshButton.setEnabled(false);
	}
	
	/**
	 * Récupération en background des éléments nécessaires à l'affichage de l'interface
	 * @return	Chaine HTML à affiché contenant les infos
	 */
	private ActionResult loadInBackground() {
		ActionResult result = new ActionResult();
		SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
		StringBuffer infos = new StringBuffer();
		
		Proxy applicationProxy = Application.getInstance().getProxyServer();
		JTomtomProperties applicationProperties = Application.getInstance().getGlobalProperties();
		
		// Si on a pas déjà les infos, on les demande au serveur TTMax
		RadarsConnector radars;
		radars = (RadarsConnector)radarSiteList.getSelectedItem();
		
		if (remoteRadarsInfos == null) {
			if (!NetworkTester.getInstance().isNetworkAvailable(applicationProxy)) {
				remoteRadarsInfos = new POIsDbInfos();
			}
		} else {
			if (remoteRadarsInfos.isEmpty() && 
					NetworkTester.getInstance().isNetworkAvailable(applicationProxy)) {
				remoteRadarsInfos = null;
			}
		}
		
		if (remoteRadarsInfos == null) {
			// We try to connect before all
			boolean isConnected = radars.connexion(
									applicationProxy, 
									applicationProperties.getUserProperty("org.tomtomax.user"), 
									applicationProperties.getUserProperty("org.tomtomax.password"));
			if (!isConnected) {
				result.exception = new JTomtomException("org.jtomtom.errors.radars.tomtomax.account");
				LOGGER.debug("Erreur lors de la connexion au site "+radars.toString()+" pour récupérer les informations distantes");
				result.status = false;
				return result;
			}
			remoteRadarsInfos = radars.getRemoteDbInfos(applicationProxy);
			if (remoteRadarsInfos == null) {
				result.exception = new JTomtomException("org.jtomtom.errors.radars.tomtomax.getinfo");
				LOGGER.debug("Erreur lors de la récupération des informations Tomtomax ...");
				result.status = false;
				return result;
			}
		}
		
		if (localRadarsInfos == null) {
			try {
				localRadarsInfos = radars.getLocalDbInfos(theDevice.getActiveMap().getPath());
			} catch (JTomtomException e) {
				result.exception = e;
				result.status = false;
				return result;
			}
		}
		
		boolean isInstalled = !localRadarsInfos.isEmpty();

		infos.append("<html><table>");
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.radars.availableupdate")).append(" : </strong></td><td><i>")
				.append(remoteRadarsInfos.getLastUpdateDateForPrint(dateFormat.toPattern()))
				.append("</i></td></tr>");
		if (isInstalled) {
			infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.radars.installedupdate")).append(" : </strong></td><td><i>")
				.append(localRadarsInfos.getLastUpdateDateForPrint(dateFormat.toPattern()))
				.append("</i></td></tr>");
		} else {
			infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.radars.installedupdate")).append(" : </strong></td><td><i>")
				.append(m_rbControls.getString("org.jtomtom.tab.radars.noversioninstalled")).append("</i></td></tr>");
		}
		infos.append("<tr><td><strong>").append(m_rbControls.getString("org.jtomtom.tab.radars.radarcount")).append(" : </strong></td><td><i>")
			.append(localRadarsInfos.getNumberOfPOIsForPrint())
			.append("</i>");
		
		if (remoteRadarsInfos.getNumberOfPOIs() != null) {
			infos.append(" [")
				.append(remoteRadarsInfos.getNumberOfPOIs() - localRadarsInfos.getNumberOfPOIs())
				.append(m_rbControls.getString("org.jtomtom.tab.radars.missingradar")).append("]");
		}
		
		infos.append("</td></tr></table>");
		infos.append("<br/><font size=\"2\"><p><i>").append(m_rbControls.getString("org.jtomtom.tab.radars.radarprovidedby"))
			.append(" <a href=\"").append(m_rbControls.getString("org.jtomtom.tab.radars.tomtomax.url"))
			.append("\">").append(m_rbControls.getString("org.jtomtom.tab.radars.tomtomax.label"))
			.append("</a></i></p></font>");
		infos.append("</html>");
		
		if (result.parameters == null) {
			result.parameters = new LinkedList<String>();
		}
		result.parameters.add(infos.toString());
		
		// - Enfin, on teste la connexion à Tomtomax pour vérifier que le user ai un compte
		try {NetworkTester.getInstance().validNetworkAvailability(applicationProxy);}
		catch (JTomtomException e) {
			result.status = false;
			result.exception = e;
			return result;
		}
		
		result.status = radars.connexion(
				applicationProxy, 
				applicationProperties.getUserProperty("org.tomtomax.user"), 
				applicationProperties.getUserProperty("org.tomtomax.password"));
		if (!result.status) {
			result.exception = new JTomtomException("org.jtomtom.errors.radars.tomtomax.account");
			LOGGER.debug("Erreur de compte Tomtomax ...");
			return result;
		}
		
		result.status = true;
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == refreshButton) {
			remoteRadarsInfos = null;
			localRadarsInfos = null;
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

}
