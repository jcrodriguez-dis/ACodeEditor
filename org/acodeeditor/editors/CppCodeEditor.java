/**
 * $Id: CppCodeEditor.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;


public class CppCodeEditor extends CCodeEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7915254647726347868L;
    protected void setReservedWords(){
			String list[] = {
					//From C
					 "auto", "break", "case", "char", "const",
						"continue", "default", "do", "double", "else", "enum",
						"extern", "float", "for", "goto", "if", "inline", "int",
						"long", "register", "return", "short", "signed",
						"sizeof", "static", "struct", "switch", "typedef", "union",
						"unsigned", "void", "volatile", "while", "NULL",
					//C++ and not C
			  "and", "and_eq", "bitand", "bitor",
		      "bool", "catch", "class", "compl", "const_cast", "delete",
		      "dynamic_cast", "explicit", "export", "false", "friend",
		      "inline", "namespace","new","not","not_eq","operator", "or",
		      "or_eq", "private", "protected", "public", "reinterpret_cast",
		      "static_cast", "template", "this","throw", "true", "try",
		      "typeid", "typename", "using", "virtual", "xor", "xor_eq",};
			reserved.clear();
			for (int i = 0; i < list.length; i++)
				reserved.add(list[i]);   
    }
    public CppCodeEditor(Highlights fonts) {
		super(fonts);
    }
	public String getType(){
		return "cpp";
	}
	public String getTypeName(){
		return "C++";
	}

}
