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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jtomtom.Application;
import org.jtomtom.gui.action.CheckUpdateAction;
import org.jtomtom.gui.action.QuitterAction;
import org.jtomtom.gui.utilities.JTTabPanel;

/**
 * @author Frédéric Combes
 *
 */
public class JTomtomFenetre extends JFrame implements ChangeListener {
	private static final long serialVersionUID = 1L;

	private final Application theApp = Application.getInstance();
	
	private JTabbedPane tabbedPane;	
	private JLabel newVersionMessage;
	
	public JTomtomFenetre() {
		super();
		
		build();
		
		runNewVersionChecking();
	}

	private void runNewVersionChecking() {
		if ("true".equals(theApp.getGlobalProperties().getUserProperty("org.jtomtom.checkupdate", "true"))) {
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					CheckUpdateAction check = new CheckUpdateAction(newVersionMessage);
					check.execute();
				}
			});
		}
	}

	private void build() {
		setTitle("jTomtom - "+theApp.getTheDevice().getName()); 			// Set the application title
		setSize(600,400); 												// Set window size
		setLocationRelativeTo(null); 									// Set window location at the center of the screen
		setResizable(false); 											// Disable window resizing
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 				// Set close window on cross click
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("resources/icon.png"))); 
		
		setContentPane(buildContentPane());
	}

	private JPanel buildContentPane() {
		ResourceBundle theTranslator = theApp.getMainTranslator();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		tabbedPane = new JTabbedPane();
		
		tabbedPane.addTab(theTranslator.getString("org.jtomtom.main.tab.general.label"), new TabGeneral().build());
		tabbedPane.addTab(theTranslator.getString("org.jtomtom.main.tab.quickfix.label"), new TabQuickFix());
		tabbedPane.addTab(theTranslator.getString("org.jtomtom.main.tab.radars.label"), new TabRadars());
		tabbedPane.addTab(theTranslator.getString("org.jtomtom.main.tab.backup.label"), new TabBackupDevice());
		tabbedPane.addTab(theTranslator.getString("org.jtomtom.main.tab.settings.label"), new TabSettings());
		tabbedPane.addTab(theTranslator.getString("org.jtomtom.main.tab.about.label"), new TabAbout());
		tabbedPane.addChangeListener(this);
		
		panel.add(tabbedPane, BorderLayout.CENTER);
		panel.add(buildPageEndPanel(), BorderLayout.PAGE_END);
		
		return panel;
	}
	
	private JPanel buildPageEndPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		newVersionMessage = new JLabel();
		newVersionMessage.setForeground(Color.RED);
		panel.add(newVersionMessage);
		
		JButton bouton = new JButton(new QuitterAction());
		panel.add(bouton);
		
		return panel;
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		if (tabbedPane == event.getSource()) {
			// Build tab UI at last time
			if (JTTabPanel.class.isAssignableFrom(tabbedPane.getSelectedComponent().getClass())) {
				((JTTabPanel)tabbedPane.getSelectedComponent()).build();
			}
			
			// Load tab information just when they are displayed
			if (TabQuickFix.class.isAssignableFrom(tabbedPane.getSelectedComponent().getClass())) {
				TabQuickFix ongletQF = (TabQuickFix)tabbedPane.getSelectedComponent();
				ongletQF.loadQuickFixInfos();
				
			} else if (TabRadars.class.isAssignableFrom(tabbedPane.getSelectedComponent().getClass())) {
				TabRadars ongletRadars = (TabRadars)tabbedPane.getSelectedComponent();
				ongletRadars.loadRadarsInfos();
			}
		}
	}
		
}
