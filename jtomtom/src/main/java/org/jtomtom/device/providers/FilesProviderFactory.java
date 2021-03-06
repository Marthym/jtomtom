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
package org.jtomtom.device.providers;

import java.io.File;
import java.io.FileNotFoundException;


/**
 * @author Frédéric Combes
 *
 */
public class FilesProviderFactory {
	
	public final static TomtomFilesProvider getFilesProvider(File rootDirectory) throws FileNotFoundException {
		File loopdir = new File(rootDirectory, CarminatFilesProvider.DIR_CARMINAT_LOOPBACK);
		if (loopdir.exists()) {
			return new CarminatFilesProvider(rootDirectory);
		} else {
			return new TomtomFilesProvider(rootDirectory);
		}
	}
	
	private FilesProviderFactory() {}
}
