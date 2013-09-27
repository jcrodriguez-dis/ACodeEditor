/**
 * $Id: ShCodeEditor.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import java.util.HashSet;
import java.util.Set;

import javax.swing.text.MutableAttributeSet;

public class ShCodeEditor extends GenericCodeEditor {
	private static final long serialVersionUID = -2733487736622479392L;

	enum States {
		REGULAR, IN_LINE_COMMENT, IN_STRING, IN_DSTRING, IN_CSTRING
	};

	protected Set<String> predefinedVars = new HashSet<String>();

	protected void initializeReserved() {
		{
			String list[] = { "exec", "eval", "cd", "exit", "export",
					"getopts", "hash", "pwd", "readonly", "return", "shift",
					"test", "times", "trap", "unset", "umask", "alias", "bind",
					"builtin", "command", "declare", "echo", "enable", "help",
					"let", "local", "logout", "printf", "read", "shopt",
					"source", "type", "typeset", "ulimit", "unalias", "set",
					"until", "do", "done", "while", "for", "break", "continue",
					"if", "then", "elif", "else", "fi", "case", "in", "esac",
					"select", "function" };
			reserved.clear();
			for (int i = 0; i < list.length; i++)
				reserved.add(list[i]);
		}
		{
			String list[] = { "CDPATH", "HOME", "IFS", "MAIL", "MAILPATH",
					"OPTARG", "OPTIND", "PATH", "PS1", "PS2", "BASH",
					"BASH_ENV", "BASH_VERSION", "BASH_VERSINFO", "COLUMNS",
					"COMP_CWORD", "COMP_LINE", "COMP_POINT", "COMP_WORDS",
					"COMPREPLY", "DIRSTACK", "EUID", "FCEDIT", "FIGNORE",
					"FUNCNAME", "GLOBIGNORE", "GROUPS", "histchars", "HISTCMD",
					"HISTCONTROL", "HISTFILE", "HISTFILESIZE", "HISTIGNORE",
					"HISTSIZE", "HOSTFILE", "HOSTNAME", "HOSTTYPE",
					"IGNOREEOF", "INPUTRC", "LANG", "LC_ALL", "LC_COLLATE",
					"LC_CTYPE", "LC_MESSAGES", "LC_NUMERIC", "LINENO", "LINES",
					"MACHTYPE", "MAILCHECK", "OLDPWD", "OPTERR", "OSTYPE",
					"PIPESTATUS", "POSIXLY_CORRECT", "PPID", "PROMPT_COMMAND",
					"PS3", "PS4", "PWD", "RANDOM", "REPLY", "SECONDS",
					"SHELLOPTS", "SHLVL", "TIMEFORMAT", "TMOUT", "UID", "1",
					"2", "3" };
			predefinedVars.clear();
			for (int i = 0; i < list.length; i++)
				predefinedVars.add(list[i]);
		}
	}

	public ShCodeEditor(Highlights fonts) {
		super(fonts);
		initializeReserved();
	}

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
			String pendiente = text.substring(begin, end + 1);
			if (reserved.contains(pendiente))
				type = font.getReserved();
			else if (predefinedVars.contains(pendiente))
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

	public String getType() {
		return "sh";
	}

	public String getTypeName() {
		return "sh script";
	}

}
