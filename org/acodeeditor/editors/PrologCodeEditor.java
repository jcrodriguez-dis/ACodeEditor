/**
 * $Id: PrologCodeEditor.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodríguez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import javax.swing.text.MutableAttributeSet;

public class PrologCodeEditor extends GenericCodeEditor {
	private static final long serialVersionUID = 740010150682764123L;

	enum States {
		REGULAR, IN_IDENTIFIER, IN_COMMENT, IN_LINE_COMMENT, IN_STRING, IN_CHAR
	};

	protected boolean nextIsOpenParenthesis(String s, int begin) {
		for (int i = begin; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '(')
				return true;
			if (c != ' ' && c != CR && c != LF && c != '\t')
				return false;
		}
		return false;
	}

	protected boolean isIdentifierChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
				|| (c >= '0' && c <= '9') || (c == '_');
	}

	protected void showPending(String text, int begin, int end) {
		if (begin > end || end >= text.length())
			return;
		MutableAttributeSet type = font.getRegular();
		char beginc = text.charAt(begin);
		if (isIdentifierChar(beginc)) {
			if ((beginc >= 'A' && beginc <= 'Z') || beginc == '_')
				type = font.getVariable(); // Fuente para variable
			else if (beginc >= 'a' && beginc <= 'z') {
				if (nextIsOpenParenthesis(text, end + 1)
						|| text.substring(begin, end).equals("is"))
					type = font.getReserved(); // Fuente para predicado
				else
					type = font.getPreprocesor(); // Fuente para atomo
			}
		}
		highlight(begin, end, type);
	}

	public PrologCodeEditor(Highlights fonts) {
		super(fonts);
	}

	public void codeHighlight() {
		States state = States.REGULAR;
		int commentLevel = 0;
		String s = getText();
		int l = s.length();
		int beginPending = 0;
		for (int i = 0; i < l; i++) {
			if ((i % 100 == 0) && getNeedUpdate()) {
				return;
			}
			char current = s.charAt(i);
			char next;
			if (i + 1 < l)
				next = s.charAt(i + 1);
			else
				next = '\0';
			if (current == CR) // Normalizamos los finales de linea
				if (next == LF)
					continue;
				else
					current = LF;
			switch (state) {
			case IN_IDENTIFIER:
			case REGULAR: { // Situaci�n normal
				if (current == '/' && next == '*') {// Comienza un comentario
					showPending(s, beginPending, i - 1);
					commentLevel = 1;
					beginPending = i;
					i++;
					state = States.IN_COMMENT;
				} else if (current == '%') { // Comienza un comentario de
												// linea
					showPending(s, beginPending, i - 1);
					beginPending = i;
					state = States.IN_LINE_COMMENT;
				} else if (current == '"') {
					showPending(s, beginPending, i - 1);
					beginPending = i;
					state = States.IN_STRING;
				} else if (current == '\'') {
					showPending(s, beginPending, i - 1);
					beginPending = i;
					state = States.IN_CHAR;
				} else  if (isIdentifierChar(current)) {
					if (state==States.REGULAR){
						if(beginPending < i)
							showPending(s,beginPending, i - 1);
						beginPending=i;
                        state=States.IN_IDENTIFIER;
					}
				} else if(state==States.IN_IDENTIFIER){
					showPending(s,beginPending, i - 1);
					beginPending=i;
                    state=States.REGULAR;
				}
			}
				break;
			case IN_STRING: {
				if (current == '"') {
					highlight(beginPending, i, font.getString());
					beginPending = i + 1;
					state = States.REGULAR;
				} else if (current == '\\') {
					i++;
				}
			}
				break;
			case IN_CHAR: {
				if (current == '\'') {
					highlight(beginPending, i, font.getString());
					beginPending = i + 1;
					state = States.REGULAR;
				} else if (current == '\\') {
					i++;
				}
			}
				break;
			case IN_LINE_COMMENT: {
				if (current == LF) {
					highlight(beginPending, i, font.getComment());
					beginPending = i + 1;
					state = States.REGULAR;
				}
			}
				break;
			case IN_COMMENT: {
				if (current == '*' && next == '/') {
					commentLevel--;
					i++;
					if (commentLevel == 0) {
						highlight(beginPending, i, font.getComment());
						beginPending = i + 1;
						state = States.REGULAR;
					}
				} else if (current == '/' && next == '*') {
					commentLevel++;
				}

			}
				break;
			}
		}
		switch (state) {
		case IN_IDENTIFIER:
		case REGULAR:
			showPending(s, beginPending, l - 1);
			break;
		case IN_STRING:
		case IN_CHAR:
			highlight(beginPending, l - 1, font.getString());
			break;
		case IN_COMMENT:
		case IN_LINE_COMMENT:
			highlight(beginPending, l - 1, font.getComment());
			break;
		}
		endUpdate();
	}

	public String getType() {
		return "prolog";
	}

	public String getTypeName() {
		return "Prolog";
	}

}
