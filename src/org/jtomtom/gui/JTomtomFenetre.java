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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jtomtom.JTomtom;
import org.jtomtom.gui.action.QuitterAction;

/**
 * @author marthym
 *
 */
public class JTomtomFenetre extends JFrame implements ChangeListener {
	private static final long serialVersionUID = -6168627345507124480L;
	
	private JTabbedPane tabbedPane;
	
	public JTomtomFenetre() {
		super();
		
		build();
	}

	private void build() {
		setTitle("jTomtom - "+JTomtom.getTheGPS().getDeviceName()); 			//On donne un titre à l'application
		setSize(600,400); 				//On donne une taille à notre fenêtre
		setLocationRelativeTo(null); 	//On centre la fenêtre sur l'écran
		setResizable(false); 			//On interdit la redimensionnement de la fenêtre
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //On dit à l'application de se fermer lors du clic sur la croix
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("resources/icon.png"))); 
		
		setContentPane(buildContentPane());
	}

	private JPanel buildContentPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		tabbedPane = new JTabbedPane();
		
		tabbedPane.addTab(JTomtom.theMainTranslator.getString("org.jtomtom.main.tab.general.label"), new TabGeneral());
		tabbedPane.addTab(JTomtom.theMainTranslator.getString("org.jtomtom.main.tab.quickfix.label"), new TabQuickFix());
		tabbedPane.addTab(JTomtom.theMainTranslator.getString("org.jtomtom.main.tab.radars.label"), new TabRadars());
		tabbedPane.addTab(JTomtom.theMainTranslator.getString("org.jtomtom.main.tab.backup.label"), new TabSauvegarde());
		tabbedPane.addTab(JTomtom.theMainTranslator.getString("org.jtomtom.main.tab.settings.label"), new TabParametres());
		tabbedPane.addTab(JTomtom.theMainTranslator.getString("org.jtomtom.main.tab.about.label"), new TabAbout());
		tabbedPane.addChangeListener(this);
		
		panel.add(tabbedPane, BorderLayout.CENTER);
		panel.add(buildPageEndPanel(), BorderLayout.PAGE_END);
		
		return panel;
	}
	
	private JPanel buildPageEndPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton bouton = new JButton(new QuitterAction());
		panel.add(bouton);
		return panel;
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		// Permet de ne charger les infos qu'à l'affichage des onglets
		if (tabbedPane == event.getSource()) {
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
