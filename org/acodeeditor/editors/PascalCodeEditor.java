/**
 * $Id: PascalCodeEditor.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;

public class PascalCodeEditor extends GenericCodeEditor {
	private static final long serialVersionUID = -4939895978988116479L;
	enum States {REGULAR, IN_IDENTIFIER, IN_COMMENT_P, IN_COMMENT_C, IN_STRING};
	protected States state;

	protected void initialize() {
		state=States.REGULAR;
	}
    protected void setReservedWords(){
		String list[] = {"and", "end", "label", "repeat", "while",
				"asm", "exports", "library", "set", "with",
				"array", "file", "mod", "shl", "xor",
				"begin", "for", "nil", "shr",
				"case", "function", "not", "string",
				"const", "goto", "object", "then",
				"constructor", "if", "of", "to",
				"destructor", "implementation", "or", "type",
				"div", "in", "packed", "unit",
				"do", "inherited", "procedure", "until",
				"downto", "inline", "program", "uses",
				"else", "interface", "record", "var" };
		reserved.clear();
		for (int i = 0; i < list.length; i++)
			reserved.add(list[i]);    	
    }
	public PascalCodeEditor(Highlights fonts) {
		super(fonts);
		initialize();
		setReservedWords();
	}

	protected boolean isIdentifierChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
				|| (c >= '0' && c <= '9') || (c == '_');
	}
	
	protected void showPending(String text, int begin, int end) {
		if(begin>end || end>=text.length()) return;
		MutableAttributeSet type=font.getRegular();
		if (isIdentifier(text,begin,end)){
			String word=text.substring(begin, end+1).toLowerCase();
			if(reserved.contains(word))
			   type=font.getReserved();
		}
		setCharacterAttributes(begin, end-begin+1, type, true);
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

	protected void codeHighlight() {
		String s=getText();
		int l = s.length();
		int blockStart = 0;
		initialize();
		char current = '\0';
		for (int i = 0; i < l; i++) {
			if((i%100 == 0) && getNeedUpdate()){
				return;
			}
			char next;
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
			switch(state){
			case REGULAR:
			case IN_IDENTIFIER:
				if (current == '(') {
					if (next == '*') { // Block comment begin
						showPending(s,blockStart, i - 1);
						state=States.IN_COMMENT_P;
						blockStart = i;
						i++;
						continue;
					}
				} else if (current == '{') {
					showPending(s,blockStart, i - 1);
					state=States.IN_COMMENT_C;
					blockStart = i;
				}else if (current == '\'') {
					showPending(s,blockStart, i - 1);
					state=States.IN_STRING;
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
				break;
			case IN_COMMENT_P:
				if (current == '*') {
					if (next == ')') {
						state= States.REGULAR;
						highlight(blockStart, i+1,font.getComment());
						i++;
						blockStart = i + 1;
						continue;
					}
				}
				break;
			case IN_COMMENT_C:
				if (current == '}') {
					highlight(blockStart, i, font.getComment());
					blockStart = i + 1;
					state= States.REGULAR;
				}
				break;
			case IN_STRING:				
				if (current == '\''){
					if (next != '\'') {
						highlight(blockStart, i, font.getString());
						blockStart = i + 1;
						state=States.REGULAR;
					}else{
						i++; //Remove next ' from scan 
					}
				}
				break;
			}
		}
		switch(state){
		case REGULAR:
		case IN_IDENTIFIER:
			showPending(s,blockStart, l-1);
			break;
		case IN_COMMENT_C:
		case IN_COMMENT_P:
			highlight(blockStart, l-1,font.getComment());
			break;
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
			//begin indent
			if(offset>5){
				if(s.substring(offset-5, offset).equalsIgnoreCase("begin")){
					str += "\t";
				}
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
				boolean remove=false;
				for(int i=offs; i>=0; i--){
					if(s.charAt(i)!=' '){
						if(s.charAt(i) == '\n') remove=true;
						break;
					}
				}
				if(remove)
  				   while(offs>0 && len<3 && s.charAt(offs-1)==' '){
					  offs--;
					  len++;
				   }
			}
		}

		super.remove(offs, len);
	}
	public String getType(){
		return "pas";
	}
	public String getTypeName(){
		return "Pascal";
	}
}
