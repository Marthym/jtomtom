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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * @author Frédéric Combes
 *
 * Exception spécial JTomtom
 */
//TODO : Create an checked JTomtomUserException for user return message
//TODO : Verify all function for remove throws JTomtomException if not needed
public class JTomtomException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(JTomtomException.class);
	
	private static final ResourceBundle m_rbErrors = 
		ResourceBundle.getBundle("org.jtomtom.gui.resources.lang.jTomtom-errors", Locale.getDefault());

	
	public JTomtomException(String message) {
		super(translateMessage(message, null));
		LOGGER.error(getMessage());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("STACKTRACE", this);
		}
	}
	
	public JTomtomException(String message, Throwable exception) {
		super(translateMessage(message, null), exception);
		LOGGER.error(getMessage());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("STACKTRACE", this);
		}
	}
	
	public JTomtomException(String message, String[] args) {
		super(translateMessage(message, args));
		LOGGER.error(getMessage());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("STACKTRACE", this);
		}
	}
	
	public JTomtomException(String message, String[] args, Throwable exception) {
		super(translateMessage(message, args), exception);
		LOGGER.error(getMessage());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("STACKTRACE", this);
		}
	}
	
	public JTomtomException(Throwable exception) {
		super(exception);
		LOGGER.error(exception.getLocalizedMessage());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("STACKTRACE", this);
		}

	}
	
	/**
	 * Fonction chargé de faire la traduction des messages reçu si c'est possible
	 * sinon on retourne le message tel quel.
	 * @param message	Message ou clé de traduction
	 * @param args		Chaines à remplacer dans le le message
	 * @return			Message traduit et complété
	 */
	private static final String translateMessage(String message, String[] args) {
		String endMessage = null;
		
		// On commence par traduire
		try {
			endMessage = m_rbErrors.getString(message);
		} catch (MissingResourceException e) {
			endMessage = message;
		}
		
		// On remplace les arguments
		if (args != null && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				endMessage = endMessage.replace("%"+Integer.toString(i+1), args[i]);
			}
		}
		return endMessage;
	}
}
