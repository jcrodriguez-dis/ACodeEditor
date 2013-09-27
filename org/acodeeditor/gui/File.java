/**
 * $Id: File.java,v 1.3 2013-06-07 15:07:20 juanca Exp $
 * gui.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.gui;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.acodeeditor.editors.CodeEditorFactory;
import org.acodeeditor.editors.GenericCodeEditor;
import org.acodeeditor.editors.Highlights;
import org.acodeeditor.editors.LineNumber;
import org.acodeeditor.editors.StyledEditorKitHS;


public class File{
	private String name;
	private JScrollPane scroll;
	private JEditorPane editor;
	private JSplitPane sp;
	private LineNumber lineNumber;
	private Highlights highlights;
	public File(String name, String code, Highlights highlights,LineNumber lineNumber,
				Main mainWindow, boolean showLineNumber,
				boolean restricted, boolean readOnly){
		this.highlights = highlights;
		this.name=name;
		this.lineNumber=lineNumber;
		editor = new JEditorPane();
		editor.setEditorKitForContentType("text/code", new StyledEditorKitHS());
		editor.setContentType("text/code");
		editor.setFont(highlights.getRegularFont());
		editor.setComponentPopupMenu(mainWindow.getPopupMenu());
		editor.getInputMap().put(KeyStroke.getKeyStroke("ctrl X"), mainWindow.getActionCut());
		editor.getInputMap().put(KeyStroke.getKeyStroke("ctrl C"), mainWindow.getActionCopy());
		editor.getInputMap().put(KeyStroke.getKeyStroke("ctrl V"), mainWindow.getActionPaste());
		editor.getInputMap().put(KeyStroke.getKeyStroke("shift DELETE"), mainWindow.getActionCut());
		editor.getInputMap().put(KeyStroke.getKeyStroke("ctrl INSERT"), mainWindow.getActionCopy());
		editor.getInputMap().put(KeyStroke.getKeyStroke("shift INSERT"), mainWindow.getActionPaste());
		GenericCodeEditor doc=CodeEditorFactory.getCodigo(name,highlights);
		doc.setPane(editor);
		if(restricted){
			editor.getDropTarget().setActive(false);
		}
		editor.setDocument(doc);
		try {
			doc.setText(code);
		} catch (BadLocationException e) {}
		doc.initUndo();
		doc.setReadOnly(readOnly);
		sp= new JSplitPane();
		sp.setBorder(null);
		if(showLineNumber)
		   sp.setLeftComponent(lineNumber);
		else
		   sp.setLeftComponent(null);
		sp.setRightComponent(editor);
		sp.setDividerSize(0);
		scroll = new JScrollPane(sp);
		scroll.getVerticalScrollBar().setUnitIncrement(highlights.getCharHeight());
	}
	public void changeFontSize(Highlights highlights){
		this.highlights = highlights;
		GenericCodeEditor code=(GenericCodeEditor)editor.getDocument();
		code.setParagraphAttributes(0, code.getLength(), highlights.getRegular(), true);
		code.changed(0);
		scroll.getVerticalScrollBar().setUnitIncrement(highlights.getCharHeight());			
	}
	public void setPreferedSize(Dimension d){
		scroll.setPreferredSize(d);
	}
	public String getText(){
		Document doc=editor.getDocument();
		try {
			return doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {}
		return "";
	}
	public String getFileName(){
		return name;
	}
	public void changeFileName(String newName){
		name=newName;
		GenericCodeEditor doc=CodeEditorFactory.getCodigo(name,highlights);
		doc.setPane(editor);
		GenericCodeEditor previous=(GenericCodeEditor)editor.getDocument();
		previous.dataTransfer(doc);
		int pos=editor.getCaretPosition();
		editor.setDocument(doc);
		editor.setCaretPosition(pos);
		previous.setStop();
	}
	public JComponent getPane(){
		return scroll;
	}
	public JEditorPane getEditor(){
		return editor;
	}
	public boolean isNumerated() {
		return sp.getLeftComponent() != null;
	}
	public void setNumeration() {
		sp.setLeftComponent(lineNumber);
	}
	public void removeNumeration() {
		sp.setLeftComponent(null);
	}
}

