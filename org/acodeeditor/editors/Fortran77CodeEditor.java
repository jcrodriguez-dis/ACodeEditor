/**
 * $Id: Fortran77CodeEditor.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import java.util.HashSet;
import java.util.Set;

import javax.swing.text.MutableAttributeSet;

public class Fortran77CodeEditor extends GenericCodeEditor {
	private static final long serialVersionUID = -2404070789317333160L;
	enum States {
		REGULAR, IN_LINE_COMMENT, IN_STRING, IN_DSTRING, IN_CSTRING
	};

	protected Set<String> declarators = new HashSet<String>();

	protected void initializeReserved() {
		{
			String list[] = {
					"accept", "assign",	"backspace",
					"call",	"close", "continue",
					"decode", "do",	"dowhile",
					"else", "elseif", "encode",
					"enddo", "endfile",	"endif",
					"goto",	"if", "include",
					"inquire", "open", "pause",
					"print", "return", "rewind",
					"save", "static", "stop",
					"write"};
			reserved.clear();
			for (int i = 0; i < list.length; i++)
				reserved.add(list[i]);
		}
		{
			String list[] = {"automatic", "blockdata", "byte", "character",
					"common", "complex", "data", "dimension", "doublecomplex",
					"doubleprecision", "end", "endmap", "endstructure",	"endunion",
					"equivalence", "external", "format", "function",
					"implicit",	"integer", "intrinsic", "logical",
					"map", "namelist", "options", "parameter",
					"pointer", "pragma", "program", "real",
					"record", "static", "structure", "subroutine",
					"type", "union", "virtual", "volatile"
};
			declarators.clear();
			for (int i = 0; i < list.length; i++)
				declarators.add(list[i]);
		}
	}

	public Fortran77CodeEditor(Highlights fonts) {
		super(fonts);
		initializeReserved();
	}
//TODO Redo FORTRAN HightLighter
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
				|| (c >= '0' && c <= '9') || (c == '$') || (c == '_');
	}

	protected void showPending(String text, int begin, int end) {
		if (begin > end || end >= text.length())
			return;
		MutableAttributeSet type = font.getRegular();
		if (text.charAt(begin) == '$') { // Variable
			type = font.getVariable();
		} else if (isIdentifier(text, begin, end)) {
			String word = text.substring(begin, end + 1).toLowerCase();
			if (reserved.contains(word))
				type = font.getReserved();
			else if (declarators.contains(word))
				type = font.getVariable();
		}
		highlight(begin, end, type);
	}

	public void codeHighlight() {
		States state = States.REGULAR;
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
			case REGULAR: { // Situaci�n normal
				if (current == '#') {// Comienza un comentario
					showPending(s, beginPending, i - 1);
					beginPending = i;
					state = States.IN_LINE_COMMENT;
				} else if (current == '"') {
					showPending(s, beginPending, i - 1);
					beginPending = i;
					state = States.IN_DSTRING;
				} else if (current == '\\') {
					i++;
				} else if (current == '\'') {
					showPending(s, beginPending, i - 1);
					beginPending = i;
					state = States.IN_STRING;
				} else if (current == '$') {
					if (next == '\'') {
						showPending(s, beginPending, i - 1);
						beginPending = i;
						i++;
						state = States.IN_CSTRING;
					} else if ((next >= '0' && next <= '9') || next == '*'
							|| next == '@' || next == '#' || next == '?'
							|| next == '-' || next == '$' || next == '!'
							|| next == '_') { // Parametros
						showPending(s, beginPending, i - 1);
						beginPending = i;
						showPending(s, i, i + 1);
						beginPending = i + 2;
						i++;
					} else {
						showPending(s, beginPending, i - 1);
						beginPending = i;
					}
				} else if (isIdentifierChar(current)) {
					if (beginPending < i
							&& !isIdentifierChar(s.charAt(beginPending))) {
						showPending(s, beginPending, i - 1);
						beginPending = i;
					}
				} else {
					if (beginPending < i
							&& isIdentifierChar(s.charAt(beginPending))) {
						showPending(s, beginPending, i - 1);
						beginPending = i;
					}
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
			case IN_CSTRING:
				if (current == '\'') {
					highlight(beginPending, i, font.getString());
					beginPending = i + 1;
					state = States.REGULAR;
				} else if (current == '\\') {
					i++;
				}
				break;
			case IN_DSTRING: {
				if (current == '"') {
					if (s.charAt(beginPending) == '$') {
						highlight(beginPending, i - 1, font.getVariable());
						beginPending = i;
					}
					highlight(beginPending, i, font.getString());
					beginPending = i + 1;
					state = States.REGULAR;
				} else if (current == '\\') {
					i++;
				} else if (current == '$') {
					if (beginPending < i) {
						if (s.charAt(beginPending) == '$')
							highlight(beginPending, i - 1, font.getVariable());
						else
							highlight(beginPending, i - 1, font.getString());
						beginPending = i;
					}
				} else {
					if (beginPending < i && !isIdentifierChar(current)) {
						if (s.charAt(beginPending) == '$')
							highlight(beginPending, i - 1, font.getVariable());
						else
							highlight(beginPending, i - 1, font.getString());
						beginPending = i;
					}
				}
			}
				break;
			case IN_STRING: {
				if (current == '\'') {
					highlight(beginPending, i, font.getString());
					beginPending = i + 1;
					state = States.REGULAR;
				}
			}
				break;
			}
		}
		switch (state) {
		case REGULAR:
			showPending(s, beginPending, l - 1);
			break;
		case IN_DSTRING:
		case IN_CSTRING:
		case IN_STRING:
			highlight(beginPending, l - 1, font.getString());
			break;

		case IN_LINE_COMMENT:
			highlight(beginPending, l - 1, font.getComment());
			break;
		}
		endUpdate();
	}

	public String getType(){
		return "fortran77";
	}
	public String getTypeName(){
		return "Fortran 77";
	}

}
