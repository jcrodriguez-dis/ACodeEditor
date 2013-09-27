/**
 * $Id: PythonCodeEditor.java,v 1.3 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2011 Juan Carlos Rodríguez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;

public class PythonCodeEditor extends GenericCodeEditor {
	private static final long serialVersionUID = -4939898438988116479L;
	enum States {REGULAR, IN_IDENTIFIER, IN_COMMENT, IN_LINE_COMMENT, IN_DECORATOR, IN_STRING};

	protected char firstNoSpace;

	protected char lastNoSpace;
	protected States state;

	protected void initialize() {
		state=States.REGULAR;
		firstNoSpace = '\0';
		lastNoSpace = '\0';
	}
    protected void setReservedWords(){
		String list[] = {"False", "class", "finally", "is", "return",
					"None", "continue", "for", "lambda", "try",
					"True", "def", "from", "nonlocal", "while",
					"and", "del", "global", "not", "with",
					"as", "elif", "if", "or", "yield",
					"assert", "else", "import", "pass",
					"break", "except", "in", "raise" };
		reserved.clear();
		for (int i = 0; i < list.length; i++)
			reserved.add(list[i]);    	
    }

    protected void showPending(String text, int begin, int end) {
		if(begin>end || end>=text.length()) return;
		MutableAttributeSet type=font.getRegular();
		if (isIdentifier(text,begin,end)){
			String pendiente=text.substring(begin, end+1);
			if(reserved.contains(pendiente))
				type=font.getReserved();
			else if(pendiente.charAt(0)=='_'){
				type=font.getVariable();
			}
		}
		highlight(begin, end, type);
	}

	public PythonCodeEditor(Highlights fonts) {
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
		return left+rigth;
	}

	void lineAdvance() {
		firstNoSpace = '\0';
		lastNoSpace = '\0';
	}
	protected void codeHighlight() {
		String s=getText();
		String stringLimit="";
		boolean rawString=false;
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
				if (current == '#') {
					showPending(s,blockStart, i - 1);
					state=States.IN_LINE_COMMENT;
					blockStart = i;
					i++;
					continue;
				} else if (current == '"') {
					showPending(s,blockStart, i - 1);
					blockStart = i;
					if(s.startsWith("\"\"\"",i)){
						if(firstNoSpace=='"'){
							state=States.IN_COMMENT;
							continue;
						}
						stringLimit = "\"\"\"";
					}else{
						stringLimit = "\"";
					}
					state=States.IN_STRING;
					rawString = Character.toLowerCase(previous)=='r';
				} else if (current == '\'') {
					showPending(s,blockStart, i - 1);
					blockStart = i;
					state=States.IN_STRING;
					rawString = Character.toLowerCase(previous)=='r';
					if(s.startsWith("'''",i)){
						stringLimit = "'''";
					}else{
						stringLimit = "'";
					}
				}else if (current == '@') {
					showPending(s,blockStart, i - 1);
					state=States.IN_DECORATOR;
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
				if (s.startsWith("\"\"\"", i)) {
					state= States.REGULAR;
					highlight(blockStart, i+3,font.getComment());
					i+=3;
					blockStart = i + 1;
					continue;
				}
				break;
			case IN_LINE_COMMENT:
				if (current == LF) {
					highlight(blockStart, i, font.getComment());
					blockStart = i + 1;
					state= States.REGULAR;
				}
				break;
			case IN_STRING:				
				if (s.startsWith(stringLimit, i)){
					if( rawString || previous != '\\') {
						state= States.REGULAR;
						highlight(blockStart, i+stringLimit.length(),font.getString());
						i+=stringLimit.length();
						blockStart = i + 1;
						continue;
					}
				}
				if(previous=='\\'){
					current ='\0';
				}
				break;
			case IN_DECORATOR:
				if (! isIdentifierChar(next) && next != '.' && next != ' '){
					state= States.REGULAR;
					highlight(blockStart, i,font.getPreprocesor());
					blockStart = i+1;
					continue;
				}
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
		case IN_DECORATOR:
				highlight(blockStart, l-1, font.getPreprocesor());
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
		return "py";
	}
	public String getTypeName(){
		return "Python";
	}
}
