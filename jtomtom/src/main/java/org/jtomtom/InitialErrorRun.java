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
package org.jtomtom;

import javax.swing.JOptionPane;

/**
 * @author Frédéric Combes
 * 
 * Used for show error or information message before initialise jTomtom UI
 * Lets respect the rule of launch within the EDT
 */
public class InitialErrorRun implements Runnable {

	private Exception m_error;
	
	public InitialErrorRun(Exception e) {
		m_error = e;
	}
	
	@Override
	public void run() {
		Application theApp = Application.getInstance();
		
		JOptionPane.showMessageDialog(null, 
				m_error.getLocalizedMessage(), 
				theApp.getMainTranslator().getString("org.jtomtom.main.dialog.default.error.title"), JOptionPane.ERROR_MESSAGE);
	}

}
