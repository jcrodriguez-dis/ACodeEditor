/**
 * $Id: JavaCodeEditor.java,v 1.4 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import javax.swing.text.BadLocationException;

public class JavaCodeEditor extends CCodeEditor {
	private static final long serialVersionUID = 2047698565895041713L;

	protected void setReservedWords() {
		String list[] = {
				"abstract",	"continue", "for", "new", "switch",
				"assert", "default", "goto", "package", "synchronized",
				"boolean", "do", "if", "private", "this",
				"break", "double", "implements", "protected", "throw",
				"byte", "else", "import", "public", "throws",
				"case", "enum", "instanceof", "return", "transient",
				"catch", "extends", "int", "short", "try",
				"char", "final", "interface", "static", "void",
				"class", "finally", "long", "strictfp", "volatile",
				"const", "float", "native", "super", "while",
				"true", "false", "null" };
		reserved.clear();
		for (int i = 0; i < list.length; i++)
			reserved.add(list[i]);
	}
	public String getId(){
		int pos=getPane().getCaretPosition();
		String s;
		try {
			s = getText(0, getLength());
		} catch (BadLocationException e) {
			return "";
		}
		int l=s.length();
		int act;
		String rigth="";
		for(act=pos; act<l;act++ ){
			char c=s.charAt(act);
			if(isIdentifierChar(c)) rigth+=c;
			else break;
		}
		String left="";
		for(act=pos-1; act>=0;act-- ){
			char c=s.charAt(act);
			if(isIdentifierChar(c)) left=c+left;
			else break;
		}
		return left+rigth;
	}
	public JavaCodeEditor(Highlights fonts){
		super(fonts);
		setReservedWords();
	}
	protected void codeHighlight() {
		String s=getText();
		int l = s.length();
		int blockStart = 0;
		initialize();
		char current = '\0', previous = '\0';
		for (int i = 0; i < l; i++) {
			if((i%100 == 0) && getNeedUpdate()){
				return;
			}
			char next;
			previous = current;
			current = s.charAt(i);
			if (i < (l - 1))
				next = s.charAt(i + 1);
			else
				next = '\0';
			if (current == CR)
				if (next == LF)
					continue;
				else
					current = LF;
			if (current != ' ' && current != '\t') { // Keep first and last no space char of line
				if (current != LF){
					lastNoSpace = current;
					if (firstNoSpace == '\0')
						firstNoSpace = current;
				}
				else{
					lastNoSpace = '\0';
					firstNoSpace = '\0';
				}
			}
			switch(state){
			case REGULAR:
			case IN_IDENTIFIER:
				if (current == '/') {
					if (next == '*') { // Block comment begin
						showPending(s,blockStart, i - 1);
						state=States.IN_COMMENT;
						blockStart = i;
						i++;
						continue;
					}
					if (next == '/') { // Line comment begin
						showPending(s,blockStart, i - 1);
						state=States.IN_LINE_COMMENT;
						blockStart = i;
						i++;
						continue;
					}
				} else if (current == '"') {
					showPending(s,blockStart, i - 1);
					state=States.IN_STRING;
					blockStart = i;
				} else if (current == '\'') {
					showPending(s,blockStart, i - 1);
					state=States.IN_CHAR;
					blockStart = i;
				}else if (current == '@' && firstNoSpace == current) {
					showPending(s,blockStart, i - 1);
					state=States.IN_PREPROCESOR;
					blockStart = i;
					continue;
				} else if (isIdentifierChar(current)) {
					if (state==States.REGULAR){
						if(blockStart < i)
							showPending(s,blockStart, i - 1);
                        blockStart=i;
                        state=States.IN_IDENTIFIER;
					}
				} else if(state==States.IN_IDENTIFIER){
					showPending(s,blockStart, i - 1);
                    blockStart=i;
                    state=States.REGULAR;
				}
				if (current == LF)
					lineAdvance();
				break;
			case IN_COMMENT:
				if (current == '*') {
					if (next == '/') {
						state= States.REGULAR;
						highlight(blockStart, i+1,font.getComment());
						i++;
						blockStart = i + 1;
						continue;
					}
				}
				break;
			case IN_LINE_COMMENT:
				if (current == LF) {
					highlight(blockStart, i, font.getComment());
					blockStart = i + 1;
					state= States.REGULAR;
				}
				break;
			case IN_PREPROCESOR: //Annotations
				if (!isIdentifierChar(current) && current != ' ' && current != '.') {
					highlight(blockStart, i, font.getPreprocesor());
					blockStart = i + 1;
					state= States.REGULAR;
				}
				break;
			case IN_CHAR:				
				if (current == '\'')
					if (previous != '\\') {
						highlight(blockStart, i , font.getString());
						blockStart = i + 1;
						state=States.REGULAR;
					}
				if (current == '\\' && previous == '\\')
					current = ' '; //In next loop previous==' '
				break;
			case IN_STRING:				
				if (current == '"')
					if (previous != '\\') {
						highlight(blockStart, i, font.getString());
						blockStart = i + 1;
						state=States.REGULAR;
					}
				if (current == '\\' && previous == '\\')
					current = ' '; //In next loop previous==' '
				break;
			}
		}
		switch(state){
		case REGULAR:
		case IN_IDENTIFIER:
			showPending(s,blockStart, l-1);
			break;
		case IN_COMMENT:
		case IN_LINE_COMMENT:
			highlight(blockStart, l-1,font.getComment());
			break;
		case IN_PREPROCESOR:
				highlight(blockStart, l-1, font.getPreprocesor());
			break;
		case IN_CHAR:				
		case IN_STRING:				
			highlight(blockStart, l-1, font.getString());
			break;
		}
		endUpdate();
	}

	public String getType(){
		return "java";
	}
	public String getTypeName(){
		return "Java";
	}

}
