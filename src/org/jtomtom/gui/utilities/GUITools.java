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
package org.jtomtom.gui.utilities;

import java.awt.Color;

import javax.swing.UIManager;

public final class GUITools {
    /**
     * Permet de savoir si on utilise Nimbus comme thème
     * @return
     */
    public static final boolean usingNimbus() {
        return UIManager.getLookAndFeel().getName().equals("Nimbus");
    }

    /**
     * Génère une couleur en appliquant les décalages sur la teinte, la saturation et 
     * la luminosité de la couleur de base
     * @param base Couleur de base
     * @param dH Décalage de la Teinte (Hue)
     * @param dS Décalage de la Saturation (Saturation)
     * @param dB Décalage de la Luminosité (Brightness)
     * @return Couleur résultante
     */
    public static final Color deriveColorHSB(Color base, float dH, float dS, float dB) {
    	// Convertion de la couleur RVB en HSB
        float hsb[] = Color.RGBtoHSB(
                base.getRed(), base.getGreen(), base.getBlue(), null);

        hsb[0] += dH;
        hsb[1] += dS;
        hsb[2] += dB;
        
        return Color.getHSBColor(
                hsb[0] < 0? 0 : (hsb[0] > 1? 1 : hsb[0]),
                hsb[1] < 0? 0 : (hsb[1] > 1? 1 : hsb[1]),
                hsb[2] < 0? 0 : (hsb[2] > 1? 1 : hsb[2]));
                                               
    }
    
}
