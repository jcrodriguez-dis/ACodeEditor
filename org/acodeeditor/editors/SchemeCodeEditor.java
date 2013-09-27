/**
 * $Id: SchemeCodeEditor.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

public class SchemeCodeEditor extends GenericCodeEditor {
	private static final long serialVersionUID = -6875053599645017641L;

	enum States {
		REGULAR, IN_IDENTIFIER, IN_COMMENT, IN_LINE_COMMENT, IN_STRING, IN_CHAR
	};
	protected boolean isIdentifierChar(char c) {
		//TODO need improve from http://www.cs.indiana.edu/scheme-repository/R4RS/r4rs_9.html#SEC67
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
				|| (c >= '0' && c <= '9') || (c == '_') || (c == '-') || (c == '?') || (c == '!');
	}

	boolean previousIsOpenParenthesis(String s, int pos) {
		for (; pos >= 0; pos--) {
			if (s.charAt(pos) == '(')
				return true;
			if (s.charAt(pos) != ' ')
				return false;
		}
		return false;
	}

	void initialize() {
		//These list is taken from GeSHi
		String list[] = {  "abs", "acos", "and", "angle", "append", "appply", "approximate",
	            "asin", "assoc", "assq", "assv", "atan",
	            "begin", "boolean?", "bound-identifier=?",
	            "caar", "caddr", "cadr", "call-with-current-continuation",
	            "call-with-input-file", "call-with-output-file", "call/cc", "car",
	            "case", "catch", "cdddar", "cddddr", "cdr", "ceiling", "char->integer",
	            "char-alphabetic?", "char-ci<=?", "char-ci<?", "char-ci?", "char-ci>=?",
	            "char-ci>?", "char-ci=?", "char-downcase", "char-lower-case?",
	            "char-numeric", "char-ready", "char-ready?", "char-upcase",
	            "char-upper-case?", "char-whitespace?", "char<=?", "char<?", "char=?",
	            "char>=?", "char>?", "char?", "close-input-port", "close-output-port",
	            "complex?", "cond", "cons", "construct-identifier", "cos",
	            "current-input-port", "current-output-port",
	            "d", "define", "define-syntax", "delay", "denominator", "display", "do",
	            "e", "eof-object?", "eq?", "equal?", "eqv?", "even?", "exact->inexact",
	            "exact?", "exp", "expt", "else",
	            "f", "floor", "for-each", "force", "free-identifer=?",
	            "gcd", "gen-counter", "gen-loser", "generate-identifier",
	            "identifier->symbol", "identifier", "if", "imag-part", "inexact->exact",
	            "inexact?", "input-port?", "integer->char", "integer?", "integrate-system",
	            "l", "lambda", "last-pair", "lcm", "length", "let", "let*", "letrec",
	            "list", "list->string", "list->vector", "list-ref", "list-tail", "list?",
	            "load", "log",
	            "magnitude", "make-polar", "make-promise", "make-rectangular",
	            "make-string", "make-vector", "map", "map-streams", "max", "member",
	            "memq", "memv", "min", "modulo",
	            "negative", "newline", "nil", "not", "null?", "number->string", "number?",
	            "numerator",
	            "odd?", "open-input-file", "open-output-file", "or", "output-port",
	            "pair?", "peek-char", "positive?", "procedure?",
	            "quasiquote", "quote", "quotient",
	            "rational", "rationalize", "read", "read-char", "real-part", "real?",
	            "remainder", "return", "reverse",
	            "s", "sequence", "set!", "set-char!", "set-cdr!", "sin", "sqrt", "string",
	            "string->list", "string->number", "string->symbol", "string-append",
	            "string-ci<=?", "string-ci<?", "string-ci=?", "string-ci>=?",
	            "string-ci>?", "string-copy", "string-fill!", "string-length",
	            "string-ref", "string-set!", "string<=?", "string<?", "string=?",
	            "string>=?", "string>?", "string?", "substring", "symbol->string",
	            "symbol?", "syntax", "syntax-rules",
	            "t", "tan", "template", "transcript-off", "transcript-on", "truncate",
	            "unquote", "unquote-splicing", "unwrap-syntax",
	            "vector", "vector->list", "vector-fill!", "vector-length", "vector-ref",
	            "vector-set!", "vector?",
	            "with-input-from-file", "with-output-to-file", "write", "write-char",
	            "zero?" };
		reserved.clear();
		for (int i = 0; i < list.length; i++)
			reserved.add(list[i]);

	}

	public SchemeCodeEditor(Highlights fonts) {
		super(fonts);
		initialize();
	}

	public void codeHighlight() {
		States state=States.REGULAR;
		int commentLevel = 0;
		String s = getText();
		int l = s.length();
		int beginPending = 0;
		for (int i = 0; i < l; i++) {
			char current = s.charAt(i);
			char next;
			if ((i % 100 == 0) && getNeedUpdate()) {
				return;
			}
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
			case REGULAR: {
				if (current == '/' && next == '*') {
					showPending(s, beginPending, i - 1);
					commentLevel = 1;
					i++;
					state = States.IN_COMMENT;
				} else if (current == ';') { // Comienza un comentario de linea
					showPending(s, beginPending, i - 1);
					beginPending=i;
					state = States.IN_LINE_COMMENT;
				} else if (current == '"') {
					showPending(s, beginPending, i - 1);
					beginPending=i;
					state = States.IN_STRING;
				} else if (current == '#' && next == '\\') {
					showPending(s, beginPending, i - 1);
					beginPending=i;
					state = States.IN_CHAR;
				} else if (isIdentifierChar(current)) {
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
				if (!Character.isLetter(current) && current != '-') {
					highlight(beginPending, i, font.getString());
					beginPending = i + 1;
					state = States.REGULAR;
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
		return "scheme";
	}

	public String getTypeName() {
		return "Scheme";
	}

}
