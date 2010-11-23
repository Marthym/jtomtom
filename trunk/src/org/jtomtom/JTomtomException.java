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
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * @author Frédéric Combes
 *
 * Unchecked jTomtom Exception
 */
//TODO: Create an checked JTomtomUserException for user return message
public class JTomtomException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(JTomtomException.class);
	
	private static final ResourceBundle localizedErrorsMessage = 
		ResourceBundle.getBundle("org.jtomtom.gui.resources.lang.jTomtom-errors");
	
	private static final ResourceBundle errorsMessage = 
		ResourceBundle.getBundle("org.jtomtom.gui.resources.lang.jTomtom-errors", Locale.ROOT);
	
	private String[] messageArguments;
	
	public JTomtomException(String message) {
		super(message);
		logException();
	}
	
	public JTomtomException(String message, Throwable exception) {
		super(message, exception);
		logException();
	}
	
	//TODO: Make variable argument number
	public JTomtomException(String message, String[] args) {
		super(message);
		messageArguments = args;
		
		logException();
	}
	
	//TODO: Make variable argument number
	public JTomtomException(String message, String[] args, Throwable exception) {
		super(message, exception);
		messageArguments = args;
		
		logException();
	}
	
	public JTomtomException(Throwable exception) {
		super(exception);
		logException();
	}
	
	private final void logException() {
		LOGGER.error(getMessage());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("DEBUG STACKTRACE", this);
		}		
	}

	@Override
	public String getMessage() {
		String theMessage = super.getMessage();
		if (errorsMessage.containsKey(theMessage)) {
			theMessage = errorsMessage.getString(theMessage);
		} 
		
		theMessage = replaceTemplateParameters(theMessage);
		return theMessage;
	}
	
	@Override
	public String getLocalizedMessage() {
		String theMessage = super.getMessage();
		if (localizedErrorsMessage.containsKey(theMessage)) {
			theMessage = localizedErrorsMessage.getString(theMessage);
		} 
		
		theMessage = replaceTemplateParameters(theMessage);
		return theMessage;
	}
	
	private final String replaceTemplateParameters(String message) {
		String endMessage = message;
		
		if (messageArguments != null && messageArguments.length > 0) {
			for (int i = 0; i < messageArguments.length; i++) {
				endMessage = endMessage.replace("%"+Integer.toString(i+1), messageArguments[i]);
			}
		}
		return endMessage;
	}
}
