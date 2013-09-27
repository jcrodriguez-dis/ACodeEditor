/**
 * $Id: CCodeEditor.java,v 1.3 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

public class CCodeEditor extends GenericCodeEditor {
	private static final long serialVersionUID = -4939895978988116479L;
	enum States {REGULAR, IN_IDENTIFIER, IN_COMMENT, IN_LINE_COMMENT, IN_PREPROCESOR, IN_STRING, IN_CHAR};

	protected char firstNoSpace;

	protected char lastNoSpace;
	protected States state;

	protected void initialize() {
		state=States.REGULAR;
		firstNoSpace = '\0';
		lastNoSpace = '\0';
	}
    protected void setReservedWords(){
		String list[] = { "auto", "break", "case", "char", "const",
				"continue", "default", "do", "double", "else", "enum",
				"extern", "float", "for", "goto", "if", "inline", "int",
				"long", "register", "restrict", "return", "short", "signed",
				"sizeof", "static", "struct", "switch", "typedef", "union",
				"unsigned", "void", "volatile", "while", "_Bool", "_Complex",
				"_Imaginary", "NULL" };
		reserved.clear();
		for (int i = 0; i < list.length; i++)
			reserved.add(list[i]);    	
    }
	public CCodeEditor(Highlights fonts) {
		super(fonts);
		initialize();
		setReservedWords();
	}

	protected boolean isIdentifierChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
				|| (c >= '0' && c <= '9') || (c == '_') || (c >= 128);
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
		String preprocessor="";
		while(act>=0){
			char c=s.charAt(act);
			if(c==' ' || c=='\t'){
				act--;
				continue;
			}
			if(c=='#'){
				preprocessor="preprocessor_";
			}
			break;
		}
		return preprocessor+left+rigth;
	}

	void lineAdvance() {
		firstNoSpace = '\0';
		lastNoSpace = '\0';
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
				}else if (current == '#' && firstNoSpace == current) {
					showPending(s,blockStart, i - 1);
					state=States.IN_PREPROCESOR;
					blockStart = i;
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
			case IN_PREPROCESOR:
				if (current == LF && lastNoSpace != '\\') {
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
	public void insertString(int offset,
            String str,
            AttributeSet a)
            throws BadLocationException{
		if(str.length() == 1 && str.charAt(0) == '\n' && offset>0){//New line
			String s=getText();
			//Locating previous line
			int lineStart=offset-1;
			for(; lineStart>=0; lineStart--)
				if(s.charAt(lineStart)== '\n') break;
			lineStart++;
			int added;
			for(added=0;lineStart+added<offset;added++){
				char c= s.charAt(lineStart+added);
				if(c != ' ' && c != '\t'){
					break;
				}
			}
			if(added >0){
				str += s.substring(lineStart,lineStart+added);
			}
			if(s.charAt(offset-1)=='{'){ //previos char == {
				str += "    ";
			}
		}
		super.insertString(offset, str,a);
	}
	public void remove(int offs,
            int len)
            throws BadLocationException{
		if(len == 1 && getPane().getCaret().getDot() == offs+1){
			String s=getText();
			if(s.charAt(offs)==' '){
				int i;
				boolean remove=false;
				for(i=offs; i>=0; i--){
					if(s.charAt(i)!=' '){
						if(s.charAt(i) == '\n') remove=true;
						break;
					}
				}
				if(i<0) remove=true;
				if(remove)
  				   while(offs>0 && len<4 && s.charAt(offs-1)==' '){
					  offs--;
					  len++;
				   }
			}
		}

		super.remove(offs, len);
	}
	public String getType(){
		return "c";
	}
	public String getTypeName(){
		return "C";
	}
}
