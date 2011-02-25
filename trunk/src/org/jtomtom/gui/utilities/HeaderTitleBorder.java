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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import javax.swing.UIManager;
import javax.swing.border.Border;

public class HeaderTitleBorder implements Border {
    private String 			borderTitle;
    private Font			borderTitleFont;
    
    /**
     * Hue of header background color
     */
	public static final float 	HUE = (float) 0.63369966;
	
	/**
	 * Color on the left (dark side)
	 */
    public static final Color	LEFT_COLOR = Color.getHSBColor(HUE-.013f, .15f, .85f);
    
    /**
     * Color on the left (transparent side)
     */
    public static final Color	RIGHT_COLOR = Color.getHSBColor(HUE-.005f, .24f, .80f);
    
    private static final Color   OPAQUE_COLOR = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    private static final Color   TRANSPARENT_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    
    /**
     * Default header font
     */
    public static final Font	DEFAULT_FONT = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 20);
	
    public HeaderTitleBorder(String pTitle) {
    	this(pTitle, DEFAULT_FONT);
    } 
    
    public HeaderTitleBorder(String pTitle, Font pFont) {
        borderTitle = pTitle;
        borderTitleFont = pFont;
    } 
    
    /**
     * Return the header height based on the title
     * @param c
     * @return
     */
    private final int getTitleHeight(Component c) {
        FontMetrics metrics = c.getFontMetrics(borderTitleFont);
        return (int)(metrics.getHeight() * 1.4);
    }
 
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int titleHeight = getTitleHeight(c);
        
        // - We begin by create transparent image
        BufferedImage titleImage = GraphicsEnvironment.getLocalGraphicsEnvironment().
        	getDefaultScreenDevice().getDefaultConfiguration().
        	createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        
        // - Create gradient
        GradientPaint gradient = new GradientPaint(0, 0, 
                LEFT_COLOR, 0, titleHeight, 
                RIGHT_COLOR, false);
        
        // - Draw the header with the gradient
        Graphics2D theGraph2D = (Graphics2D)titleImage.getGraphics();
        theGraph2D.setPaint(gradient);
        theGraph2D.fillRect(x, y, width, titleHeight);
        theGraph2D.setPaint(
        		new GradientPaint(0, 0, OPAQUE_COLOR, width, 0, TRANSPARENT_COLOR));        
        theGraph2D.setComposite(AlphaComposite.DstIn);
        theGraph2D.fillRect(x, y, width, titleHeight);
        theGraph2D.dispose();
        
        g.drawImage(titleImage, x, y, c);
        
        
        // - Now drawing title
        theGraph2D = (Graphics2D)g.create();
        theGraph2D.setRenderingHint(
        		RenderingHints.KEY_TEXT_ANTIALIASING, 
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        theGraph2D.setColor(c.getForeground());
        theGraph2D.setFont(borderTitleFont);
        FontMetrics metrics = c.getFontMetrics(theGraph2D.getFont());
        theGraph2D.drawString(borderTitle, x + 8, 
                y + (titleHeight - metrics.getHeight())/2 + metrics.getAscent()); 
        theGraph2D.dispose();
        
    }

	@Override
	public Insets getBorderInsets(Component c) {
        Insets borderInsets = new Insets(0,10,0,5);        
        borderInsets.top = getTitleHeight(c);
        return borderInsets;
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}
}
