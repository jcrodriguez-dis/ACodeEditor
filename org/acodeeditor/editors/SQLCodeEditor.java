/**
 * $Id: SQLCodeEditor.java,v 1.3 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import java.util.HashSet;
import java.util.Set;

import javax.swing.text.MutableAttributeSet;

public class SQLCodeEditor extends GenericCodeEditor {
	private static final long serialVersionUID = 7397583418429961177L;
	enum States {
		REGULAR, IN_COMMENT, IN_LINE_COMMENT, IN_STRING, IN_DSTRING
	};
	protected Set<String> keywords=new HashSet<String>();
	protected void initializeReserved() {
		String lista[] = { "ABSOLUTE", "ACTION", "ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE",
							"AS", "ASC", "ASSERTION", "AT", "AUTHORIZATION", "AVG", 
							"BEGIN", "BETWEEN", "BIT", "BIT_LENGTH", "BOTH", "BY", 
							"CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", 
							"CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", 
							"CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", 
							"COMMIT", "CONNECT", "CONNECTION", "CONSTRAINT", 
							"CONSTRAINTS", "CONTINUE", "CONVERT", "CORRESPONDING", 
							"CREATE", "CROSS", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", 
							"CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATE", "DAY", "DEALLOCATE", 
							"DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", 
							"DESC", "DESCRIBE", "DESCRIPTOR", "DIAGNOSTICS", "DISCONNECT", "DISTINCT", 
							"DOMAIN", "DOUBLE", "DROP", "ELSE", "END", "END -EXEC", "ESCAPE", "EXCEPT", 
							"EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXTERNAL", "EXTRACT", "FALSE", 
							"FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FROM", "FULL", "GET", 
							"GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "HAVING", "HOUR", "IDENTITY", 
							"IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INPUT", "INSENSITIVE", 
							"INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION", 
							"JOIN", "KEY", "LANGUAGE", "LAST", "LEADING", "LEFT", "LEVEL", "LIKE", "LOCAL", 
							"LOWER", "MATCH", "MAX", "MIN", "MINUTE", "MODULE", "MONTH", "NAMES", "NATIONAL", 
							"NATURAL", "NCHAR", "NEXT", "NO", "NOT", "NULL", "NULLIF", "NUMERIC", 
							"OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "OUTER", 
							"OUTPUT", "OVERLAPS", "PAD", "PARTIAL", "POSITION", "PRECISION", "PREPARE", 
							"PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", 
							"REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", "ROLLBACK", 
							"ROWS", "SCHEMA", "SCROLL", "SECOND", "SECTION", "SELECT", "SESSION", 
							"SESSION_USER", "SET", "SIZE", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", 
							"SQLERROR", "SQLSTATE", "SUBSTRING", "SUM", "SYSTEM_USER", "TABLE", "TEMPORARY", 
							"THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", 
							"TRANSACTION", "TRANSLATE", "TRANSLATION", "TRIM", "TRUE", "UNION", "UNIQUE", 
							"UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE", "VALUES", 
							"VARCHAR", "VARYING", "VIEW", "WHEN", "WHENEVER", "WHERE", "WITH", "WORK", 
							"WRITE", "YEAR", "ZONE"};
		reserved.clear();
		for (int i = 0; i < lista.length; i++)
			reserved.add(lista[i]);
		String listb[] = { "ADA", "C", "CATALOG_NAME", "CHARACTER_SET_CATALOG", "CHARACTER_SET_NAME",
				"CHARACTER_SET_SCHEMA", "CLASS_ORIGIN", "COBOL", "COLLATION_CATALOG", "COLLATION_NAME",
				"COLLATION_SCHEMA", "COLUMN_NAME", "COMMAND_FUNCTION", "COMMITTED", "CONDITION_NUMBER",
				"CONNECTION_NAME", "CONSTRAINT_CATALOG", "CONSTRAINT_NAME", "CONSTRAINT_SCHEMA",
				"CURSOR_NAME", "DATA", "DATETIME_INTERVAL_CODE", "DATETIME_INTERVAL_PRECISION",
				"DYNAMIC_FUNCTION", "FORTRAN", "LENGTH", "MESSAGE_LENGTH", "MESSAGE_OCTET_LENGTH",
				"MESSAGE_TEXT", "MORE", "MUMPS", "NAME", "NULLABLE", "NUMBER", "PASCAL", "PLI", "REPEATABLE",
				"RETURNED_LENGTH", "RETURNED_OCTET_LENGTH", "RETURNED_SQLSTATE", "ROW_COUNT", "SCALE",
				"SCHEMA_NAME", "SERIALIZABLE", "SERVER_NAME", "SUBCLASS_ORIGIN", "TABLE_NAME", "TYPE",
				"UNCOMMITTED", "UNNAMED" };
		keywords.clear();
		for (int i = 0; i < listb.length; i++)
			keywords.add(listb[i]);
	}

	public SQLCodeEditor(Highlights fonts) {
		super(fonts);
		initializeReserved();
	}

	protected boolean isIdentifierChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
				|| (c >= '0' && c <= '9') || (c == '$') || (c == '_');
	}

	protected void showPending(String text, int begin, int end) {
		if (begin > end || end >= text.length())
			return;
		MutableAttributeSet type = font.getRegular();
		String pending = text.substring(begin, end + 1).toUpperCase();
		if (reserved.contains(pending))
			type = font.getReserved();
		else if(keywords.contains(pending))
			type = font.getVariable();
		highlight(begin, end, type);
	}

	public void codeHighlight() {
		States state= States.REGULAR;
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
			case REGULAR: {
				if ((current >= 'A' && current <= 'Z') || (current >= 'a' && current <= 'z')) {
					continue;
				} else {
					if (beginPending < i) {
						showPending(s, beginPending, i - 1);
						beginPending = i;
					}
					if (current == '/' && next == '*') {
						state = States.IN_COMMENT;
						continue;
					}
					if (current == '-' && next == '-') {
						state = States.IN_LINE_COMMENT;
						continue;
					}
					if (current == '"') {
						state = States.IN_DSTRING;
						continue;
					}
					if (current == '\'') {
						state = States.IN_STRING;
					}
					showPending(s, beginPending, i);
					beginPending = i + 1;
				}
				break;
			}
			case IN_LINE_COMMENT: {
				if (current == LF) {
					highlight(beginPending, i, font.getComment());
					beginPending = i + 1;
					state = States.REGULAR;
				}
				break;
			}
			case IN_COMMENT: {
				if (current == '*' && next == '/') {
					highlight(beginPending, i + 1, font.getComment());
					i++;
					beginPending = i + 1;
					state = States.REGULAR;
				}
				break;
			}
			case IN_DSTRING: {
				if (current == '"') {
					if (next == '"') {
						i++;
					} else {
						highlight(beginPending, i, font.getString());
						beginPending = i + 1;
						state = States.REGULAR;
					}
				}
				break;
			}
			case IN_STRING: {
				if (current == '\'') {
					highlight(beginPending, i, font.getString());
					beginPending = i + 1;
					state = States.REGULAR;
				}
				break;
			}
			}
		}
		switch (state) {
		case REGULAR:
			showPending(s, beginPending, l - 1);
			break;
		case IN_DSTRING:
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
		return "sql";
	}

	public String getTypeName() {
		return "SQL";
	}

}
