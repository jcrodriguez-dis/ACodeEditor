/**
 * $Id: Highlights.java,v 1.3 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public class Highlights {
	private MutableAttributeSet lineNumber, regular, comment, string,
			reserved, preprocesor, function, variable, regularBackground, highlightedBackground;
	private Font regularFont;

	private int size = 14;
	private int charSize;
	private int charHeight;
	private Color backgroud = Color.white;

	private void initialize() {
		final int espacesByTab=3;
		final int maxTabsNumber=28;
		//Calculate size and height
		BufferedImage dummy= new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics g= dummy.getGraphics();
		regularFont=new Font("Monospaced", Font.PLAIN,size);
		charSize = g.getFontMetrics(regularFont).charWidth('0');
		charHeight = g.getFontMetrics(regularFont).getHeight();
		int tabSize = charSize * espacesByTab;
		TabStop[] tabs = new TabStop[maxTabsNumber];
		for (int j = 0; j < tabs.length; j++){
			tabs[j] = new TabStop( (j+1) * tabSize , TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);
		}
		TabSet tabSet = new TabSet(tabs);
		regular = new SimpleAttributeSet();
		StyleConstants.setFontFamily(regular, "Monospaced");
		StyleConstants.setFontSize(regular, size);
		StyleConstants.ColorConstants.setForeground(regular, Color.black);
		StyleConstants.ColorConstants.setBackground(regular, backgroud);
		StyleConstants.setTabSet(regular, tabSet);
		regularBackground = new SimpleAttributeSet();
		StyleConstants.ColorConstants.setBackground(regularBackground, backgroud);
		comment = new SimpleAttributeSet();
		comment.addAttributes(regular);
		StyleConstants.ColorConstants.setForeground(comment, new Color(0x78B078)); //green
		string = new SimpleAttributeSet();
		string.addAttributes(regular);
		StyleConstants.ColorConstants.setForeground(string, Color.blue);
		reserved = new SimpleAttributeSet();
		reserved.addAttributes(regular);
		StyleConstants.ColorConstants.setForeground(reserved, new Color(0x000080)); //Dark blue
		StyleConstants.setBold(reserved, true);
		preprocesor = new SimpleAttributeSet();
		preprocesor.addAttributes(regular);
		StyleConstants.ColorConstants.setForeground(preprocesor, Color.red); //red
		StyleConstants.setBold(preprocesor, true);
		variable = new SimpleAttributeSet();
		variable.addAttributes(regular);
		StyleConstants.ColorConstants.setItalic(variable, true);
		function = new SimpleAttributeSet();
		function.addAttributes(regular);
		StyleConstants.ColorConstants.setItalic(function, true);
		StyleConstants.ColorConstants.setForeground(function, Color.BLUE.darker()); // blue
		lineNumber = new SimpleAttributeSet();
		lineNumber.addAttributes(regular);
		StyleConstants.ColorConstants.setBackground(lineNumber, new Color(220, 220, 255)); //light blue 
		highlightedBackground= new SimpleAttributeSet();
		StyleConstants.ColorConstants.setBackground(highlightedBackground, new Color(0xff,0xff,0x99)); //Yellow
	}

	public Highlights() {
		initialize();
	}

	public MutableAttributeSet getComment() {
		return comment;
	}

	public MutableAttributeSet getRegular() {
		return regular;
	}

	public MutableAttributeSet getPreprocesor() {
		return preprocesor;
	}

	public MutableAttributeSet getReserved() {
		return reserved;
	}

	public MutableAttributeSet getString() {
		return string;
	}

	public MutableAttributeSet getVariable() {
		return variable;
	}
	public MutableAttributeSet getFunction() {
		return function;
	}

	public void setString(MutableAttributeSet ristra) {
		this.string = ristra;
	}

	public MutableAttributeSet getLineNumber() {
		return lineNumber;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
		initialize();
	}

	public void setBackgroud(Color b){
		backgroud=b;
		initialize();
	}
	public Color getBackgroud(){
		return backgroud;
	}
	public int getCharHeight() {
		return charHeight;
	}

	public int getCharSize() {
		return charSize;
	}

	public Font getRegularFont() {
		return regularFont;
	}

	public MutableAttributeSet getHighlightedBackground() {
		return highlightedBackground;
	}

	public void setVariable(MutableAttributeSet variable) {
		this.variable = variable;
	}

	public MutableAttributeSet getRegularBackground() {
		return regularBackground;
	}
}
