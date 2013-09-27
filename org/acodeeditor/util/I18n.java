/**
 * $Id: I18n.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * util.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.util;

import java.util.HashMap;
import java.util.Map;

public class I18n{
	static public final String previousPage="previous_page";
	static public final String returnToPreviousPage= "return_to_previous_page";
	static public final String nextPage= "next_page";
	static public final String goNextPage= "go_next_page";
	static public final String help= "help";
	static public final String contextualHelp= "contextual_help";
	static public final String generalHelp= "general_help";
	static public final String edit= "edit";
	static public final String file= "file";
	static public final String options= "options";
	static public final String newFile= "new";
	static public final String createNewFile= "create_new_file";
	static public final String fileName= "file_name";
	static public final String incorrectFileName= "incorrect_file_name";
	static public final String rename= "rename";
	static public final String renameFile= "renameFile";
	static public final String newFileName= "new_file_name";
	static public final String delete= "delete";
	static public final String deleteFile= "delete_file";
	static public final String deleteFileQ= "delete_file_q";
	static public final String save= "save";
	static public final String undo= "undo";
	static public final String undoChange= "undo_change";
	static public final String redo= "redo";
	static public final String redoUndone= "redo_undone";
	static public final String cut= "cut";
	static public final String cutText= "cut_text";
	static public final String copy= "copy";
	static public final String copyText= "copy_text";
	static public final String paste= "paste";
	static public final String pasteText= "paste_text";
	static public final String pasteSystem= "paste_system";
	static public final String pasteSystemText= "paste_system_text";
	static public final String selectAll= "select_all";
	static public final String selectAllText= "select_all_text";
	static public final String findReplace= "find_replace";
	static public final String findFindReplace= "find_find_replace";
	static public final String programHelp= "program_help";
	static public final String pageUnaccessible= "page_unaccessible";
	static public final String about= "about";
	static public final String helpAbout= "help_about";
	static public final String appletCodeEditorAbout= "applet_code_editor_about";
	static public final String lineNumber= "line_number";
	static public final String toggleShowLineNumber= "toggle_show_line_number";
	static public final String next= "next";
	static public final String findNextSearchString= "find_next_search_string";
	static public final String replace= "replace";
	static public final String replaceSelectionIfMatch= "replace_selection_if_match";
	static public final String replaceFind= "replace_find";
	static public final String replaceFindNext= "replace_find_next";
	static public final String replaceAll= "replace_all";
	static public final String replaceAllNext= "replace_all_next";
	static public final String languageHelp= "language_help";
	static public final String console= "console";
	static public final String find= "find";
	static public final String figure= "figure";
	static public final String caseSensitive= "case_sensitive";
	static public final String fontSize= "font_size";
	static public final String connecting= "connecting";
	static public final String connectionFail= "connection_fail";
	static public final String connected= "connected";
	static public final String connectionClosed= "connection_closed";
	static public final String sound="sound";

	static private Map<String,String> strings= new HashMap<String,String>();
	static private String lang="en_utf8";
	static public void initialize(String data){
		strings.clear();
		String[] lines = data.split("\n");
		for(String line:lines){
			int pos=line.indexOf("=");
			if(pos != -1){
				String key=line.substring(0, pos).trim();
				String value=line.substring(pos+1, line.length()).trim();
				strings.put(key, value);
			}
		}
	}
	static public String getString(String key){
		String res=strings.get(key);
		if(res==null) return key;
		return res;
	}
	public static String getLang() {
		return lang;
	}
	public static void setLang(String lang) {
		I18n.lang = lang;
	}
}