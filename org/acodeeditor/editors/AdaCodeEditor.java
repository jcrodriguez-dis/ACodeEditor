/**
 * $Id: AdaCodeEditor.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;

public class AdaCodeEditor extends GenericCodeEditor {
	private static final long serialVersionUID = 5648767031249921464L;
	enum States {REGULAR, IN_IDENTIFIER, IN_LINE_COMMENT, IN_STRING, IN_CHAR};

	protected void initializeReserved() {
		String list[] = {
				"abort", 
				"abs",
				"abstract", 
				"accept",
				"access",
				"aliased",
				"all",
				"and",
				"array", 
				"at",
				"begin", 
				"body",
				"case",
				"constant", 
				"declare",
				"delay",
				"delta",
				"digits",
				"do",
				"else",
				"elsif",
				"end",
			    "entry",
				"exception", 
				"exit",
				"for",
				"function", 
				"generic",
				"goto",
				"if",
				"in",
				"interface", 
				"is",
				"limited", 
				"loop",
				"mod",
				"new",
				"not",
				"null",
				"of",
				"or",
				"others", 
				"out",
				"overriding",
				"package",
				"pragma",
				"private",
				"procedure",
				"protected",
				"raise",
				"range",
				"record",
				"rem",
				"renames", 
				"requeue",
				"return",
				"reverse",
				"select",
				"separate",
				"subtype",
				"synchronized",
				"tagged",
				"task",
				"terminate", 
				"then",
				"type",
				"until",
				"use",
				"when",
				"while",
				"with",
				"xor"};
		reserved.clear();
		for (int i = 0; i < list.length; i++)
			reserved.add(list[i]);
	}
	public AdaCodeEditor(Highlights fonts) {
		super(fonts);
		initializeReserved();
	}
	public String getId(){
		return super.getId().toLowerCase();
	}
	protected boolean isIdentifierChar(char c) {
		return (c >= 'A' && c <= 'Z') ||
            (c >= 'a' && c <= 'z') ||
		    (c >= '0' && c <= '9') ||
		    (c == '_') ||
            (c >= 128);
	}
	protected boolean isIdentifier(String text, int begin, int end){
		return isIdentifierChar(text.charAt(begin));
	}
	
	protected void showPending(String text, int begin, int end) {
		if(begin>end || end>=text.length()) return;
		MutableAttributeSet type=font.getRegular();
		if (isIdentifier(text,begin,end)){
			String pendiente=text.substring(begin, end+1).toLowerCase();
			if(reserved.contains(pendiente))
			   type=font.getReserved();
		}
		setCharacterAttributes(begin, end-begin+1, type, true);
	}
	protected void codeHighlight() {
		int l = getLength();
		String s = getText();
		int blockIni = 0;
		States state=States.REGULAR;
		for (int i = 0; i < l; i++) {
			if((i%100 == 0) && getNeedUpdate()){
				return;
			}
			char next='\0', current = s.charAt(i);
			if (i < (l - 1))
				next = s.charAt(i + 1);
			//Normalize end of line
			if (current == CR)
				if (next == LF)
					continue;
				else
					current = LF;
			switch(state){
			case IN_LINE_COMMENT:  // CHeck end of line comment
				if (current == LF) {
					highlight(blockIni, i, font.getComment());
					state = States.REGULAR;
					blockIni=i+1;
				}
				break;
			case IN_STRING: // Check end of string
				if (current == '"'){
					if (next == '"'){ // Case "" into string
						i++;
					} else {
						highlight(blockIni, i, font.getString());
						state = States.REGULAR;
						blockIni=i+1;
					}
				}
				break;
			case REGULAR:
			case IN_IDENTIFIER:
				if (current == '-' && next == '-') {
					showPending(s, blockIni, i-1);
					blockIni = i;
					i++;
					state = States.IN_LINE_COMMENT;
				} else if (current == '"') {
					showPending(s, blockIni, i-1);
					blockIni = i;
					state = States.IN_STRING;
				} else if (current == '\'' && i + 2 < l) {
					showPending(s, blockIni, i-1);
					if(s.charAt(i+2)=='\'') {
						highlight(i, i+2, font.getString());
						blockIni=i+3;
						i+=2;
					}
					else blockIni=i;
				}
				else if (isIdentifierChar(current)) {
					if (state==States.REGULAR){
						if(blockIni < i)
							showPending(s,blockIni, i - 1);
                        blockIni=i;
                        state=States.IN_IDENTIFIER;
					}
				} else if(state==States.IN_IDENTIFIER){
					showPending(s,blockIni, i - 1);
                    blockIni=i;
                    state=States.REGULAR;
				}
			}
		}
		switch(state){
		case IN_LINE_COMMENT:
			highlight(blockIni, l-1, font.getComment());
			break;
		case IN_STRING:
			highlight(blockIni, l-1, font.getString());
			break;
		case REGULAR:
		case IN_IDENTIFIER:
			showPending(s, blockIni, l-1);
			break;
		}
		endUpdate();
	}
	public void insertString(int offset,
            String str,
            AttributeSet a)
            throws BadLocationException{
		if(str.length() == 1 && str.charAt(0) == '\n'){
			String s=getText();
			//Localizamos linea anterior
			int lineBegin=offset-1;
			for(; lineBegin>=0; lineBegin--)
				if(s.charAt(lineBegin)== '\n') break;
			lineBegin++;
			if(lineBegin<offset){
			   int added=0;
			   while(lineBegin+added<offset && s.charAt(lineBegin+added)==' ')
			      added++;
			   if(offset>0){
				   String line=s.substring(lineBegin,offset).toLowerCase();
				  if(line.endsWith("loop") || line.endsWith("then")
				     || line.endsWith("begin") || line.endsWith("is")
				     || line.endsWith("declare") || line.endsWith("else"))
					 str += "   ";
			   }
			   if(added >0){
				  str+=s.substring(lineBegin,lineBegin+added);
			   }
			}
		}
		super.insertString(offset, str,a);
	}
	public String getType(){
		return "ada";
	}
	public String getTypeName(){
		return "Ada";
	}

}
