/**
 * $Id: LineNumber.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.text.StyleConstants;

public class LineNumber extends JTextArea {
	private static final long serialVersionUID = 1890088323554851141L;
	private Highlights fonts=null;
	public LineNumber(Highlights fonts){
		super();
		this.fonts= fonts;
		setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		setEditable(false);
		setFocusable(false);
		setMaxLineNumber(9999);
	}

    public void setMaxLineNumber(int nl){
		String snl=""+nl;
		setPreferredSize(new Dimension(fonts.getCharSize()*snl.length()+8,100));
    	StringBuilder sb= new StringBuilder();
    	for(int i=1; i<= nl;i++){ //Four chars
    		if(i<10)       sb.append("   ");
    		else if(i<100) sb.append("  ");
    		else if(i<1000)sb.append(" ");
    		sb.append(i);
    		sb.append('\n');
    	}
    	setBackground(StyleConstants.getBackground(fonts.getLineNumber()));
		Font fuente=new Font(StyleConstants.getFontFamily(fonts.getLineNumber()),
	             Font.PLAIN,StyleConstants.getFontSize(fonts.getLineNumber()));
    	setFont(fuente);
    	setText(sb.toString());
    }
}
