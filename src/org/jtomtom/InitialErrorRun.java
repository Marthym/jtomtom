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
package org.jtomtom;

import javax.swing.JOptionPane;

/**
 * @author marthym
 * 
 * Va permettre le lancement d'un message d'erreur avant tout démarrage de l'interface
 * Ca permet de respecter la règle de lancement à l'intérieur de l'EDT
 *
 */
public class InitialErrorRun implements Runnable {

	private Exception m_error;
	
	public InitialErrorRun(Exception e) {
		m_error = e;
	}
	
	@Override
	public void run() {
		JOptionPane.showMessageDialog(null, 
				m_error.getLocalizedMessage(), 
				"Erreur ! ", JOptionPane.ERROR_MESSAGE);
	}

}
