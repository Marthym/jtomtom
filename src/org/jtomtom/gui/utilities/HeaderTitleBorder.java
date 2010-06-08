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
    private String 			m_title;
    private Font			m_titleFont;
    
    /**
     * Teinte de la couleur du fond d'entête
     */
	public static final float 	TEINTE = (float) 0.63369966;
	
	/**
	 * Couleur de gauche (partie foncé du dégradé)
	 */
    public static final Color	LEFT_COLOR = Color.getHSBColor(TEINTE-.013f, .15f, .85f);
    
    /**
     * Couleur de droite (partie transparente du dégradé)
     */
    public static final Color	RIGHT_COLOR = Color.getHSBColor(TEINTE-.005f, .24f, .80f);
    
    private static final Color   OPAQUE_COLOR = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    private static final Color   TRANSPARENT_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    
    /**
     * Police par défault de l'entête
     */
    public static final Font	DEFAULT_FONT = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 20);
	
    /**
     * Constructeur initialisant le titre de mon bloc
     * @param title	Titre du bloc (en général un panel
     */
    public HeaderTitleBorder(String pTitle) {
    	this(pTitle, DEFAULT_FONT);
    } 
    
    /**
     * Constructeur initialisant le titre de mon bloc
     * @param title	Titre du bloc (en général un panel
     */
    public HeaderTitleBorder(String pTitle, Font pFont) {
        m_title = pTitle;
        m_titleFont = pFont;
    } 
    
    /**
     * Retourne la hauteur de l'entête en fonction du titre
     * @param c
     * @return
     */
    private final int getTitleHeight(Component c) {
        FontMetrics metrics = c.getFontMetrics(m_titleFont);
        return (int)(metrics.getHeight() * 1.4);
    }
 
    /* (non-Javadoc)
     * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int titleHeight = getTitleHeight(c);
        
        // - Allez, on commence par créer un image transparente
        BufferedImage titleImage = GraphicsEnvironment.getLocalGraphicsEnvironment().
        	getDefaultScreenDevice().getDefaultConfiguration().
        	createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        
        // - On crée le dégradé
        GradientPaint gradient = new GradientPaint(0, 0, 
                LEFT_COLOR, 0, titleHeight, 
                RIGHT_COLOR, false);
        
        // - On dessine l'entête de cadre avec le dégradé
        Graphics2D theGraph2D = (Graphics2D)titleImage.getGraphics();
        theGraph2D.setPaint(gradient);
        theGraph2D.fillRect(x, y, width, titleHeight);
        theGraph2D.setPaint(
        		new GradientPaint(0, 0, OPAQUE_COLOR, width, 0, TRANSPARENT_COLOR));        
        theGraph2D.setComposite(AlphaComposite.DstIn);
        theGraph2D.fillRect(x, y, width, titleHeight);
        theGraph2D.dispose();
        
        g.drawImage(titleImage, x, y, c);
        
        
        // Maintenant on dessine le titre
        theGraph2D = (Graphics2D)g.create();
        theGraph2D.setRenderingHint(
        		RenderingHints.KEY_TEXT_ANTIALIASING, 
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        theGraph2D.setColor(c.getForeground());
        theGraph2D.setFont(m_titleFont);
        FontMetrics metrics = c.getFontMetrics(theGraph2D.getFont());
        theGraph2D.drawString(m_title, x + 8, 
                y + (titleHeight - metrics.getHeight())/2 + metrics.getAscent()); 
        theGraph2D.dispose();
        
    }

	/* (non-Javadoc)
	 * @see javax.swing.border.Border#getBorderInsets(java.awt.Component)
	 */
	@Override
	public Insets getBorderInsets(Component c) {
        Insets borderInsets = new Insets(0,10,0,5);        
        borderInsets.top = getTitleHeight(c);
        return borderInsets;
	}

	/* (non-Javadoc)
	 * @see javax.swing.border.Border#isBorderOpaque()
	 */
	@Override
	public boolean isBorderOpaque() {
		return false;
	}
}
