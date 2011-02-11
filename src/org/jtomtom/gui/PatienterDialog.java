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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.jtomtom.Application;

/**
 * @author Frédéric Combes
 *
 */
public class PatienterDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private final ResourceBundle theTranslator = Application.getInstance().getMainTranslator();

	private JButton annuler;
	private SwingWorker<?, ?> worker;
	private JProgressBar progressBar;
	
	public PatienterDialog(SwingWorker<?, ?> p_worker) {
		super();
		
		build();
		
		worker = p_worker;
		worker.execute();
	}
	
	private void build() {
		// - Define properties
		setTitle(theTranslator.getString("org.jtomtom.main.dialog.wait.title"));
		setSize(300, 110);
		setModal(true);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		// - Define content
		setContentPane(buildContentPane());
	}
	
	private JPanel buildContentPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JLabel label = new JLabel(theTranslator.getString("org.jtomtom.main.dialog.wait.message"));
		panel.add(label, BorderLayout.PAGE_START);
		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setPreferredSize(
				new Dimension(this.getSize().width-10,20));
		
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new FlowLayout());
		progressPanel.add(progressBar);
		panel.add(progressPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		annuler = new JButton(theTranslator.getString("org.jtomtom.main.dialog.wait.button.cancel.label"));
		annuler.addActionListener(this);
		buttonPanel.add(annuler);
		panel.add(buttonPanel, BorderLayout.PAGE_END);
		
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent p_event) {
		if (p_event.getSource() == annuler) {
			worker.cancel(true);
			this.dispose();
		}
	}
	
	/**
	 * Update progress bar in the window
	 * @param current	Current value
	 * @param total		Max value
	 */
	public void refreshProgressBar(int current, int total) {
		if (total <= 0) {
			progressBar.setIndeterminate(true);
			return;
		} else {
			progressBar.setMinimum(0);
			progressBar.setMaximum(total);
			progressBar.setValue(current);
			progressBar.setIndeterminate(false);
		}
	}
}
