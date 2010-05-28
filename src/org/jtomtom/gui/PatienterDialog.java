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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 * @author marthym
 *
 */
public class PatienterDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	private JButton m_annuler;
	private SwingWorker<?, ?> m_worker;
	private JProgressBar m_progressBar;
	
	public PatienterDialog(SwingWorker<?, ?> p_worker) {
		super();
		
		build();
		
		m_worker = p_worker;
		m_worker.execute();
	}
	
	private void build() {
		// - Définition des propriétés
		setTitle("Patienter...");
		setSize(300, 110);
		setModal(true);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		// - Définition du contenu
		setContentPane(buildContentPane());
	}
	
	private JPanel buildContentPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JLabel label = new JLabel("Mise à jour en cours ...");
		panel.add(label, BorderLayout.PAGE_START);
		
		m_progressBar = new JProgressBar();
		m_progressBar.setIndeterminate(true);
		m_progressBar.setPreferredSize(
				new Dimension(this.getSize().width-10,20));
		
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new FlowLayout());
		progressPanel.add(m_progressBar);
		panel.add(progressPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		m_annuler = new JButton("Annuler");
		m_annuler.addActionListener(this);
		buttonPanel.add(m_annuler);
		panel.add(buttonPanel, BorderLayout.PAGE_END);
		
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent p_event) {
		if (p_event.getSource() == m_annuler) {
			m_worker.cancel(true);
			this.dispose();
		}
	}
	
	/**
	 * Met à jour la barre de progression de la fenètre
	 * @param current	Valeur courrent
	 * @param total		Valeur max
	 */
	public void refreshProgressBar(int current, int total) {
		if (total <= 0) {
			m_progressBar.setIndeterminate(true);
			return;
		} else {
			m_progressBar.setMinimum(0);
			m_progressBar.setMaximum(total);
			m_progressBar.setValue(current);
			m_progressBar.setIndeterminate(false);
		}
	}
}
