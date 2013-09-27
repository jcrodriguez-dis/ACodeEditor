/**
 * $Id: JHTMLNavegable.java,v 1.3 2013-06-07 15:07:20 juanca Exp $
 * gui.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.gui;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.acodeeditor.util.I18n;


class JHTMLNavegable extends JHTMLPane implements HyperlinkListener{
	private static final long serialVersionUID = -5283717388367688285L;
	protected LinkedList<URL> previous=new LinkedList<URL>();
	protected LinkedList<URL> next=new LinkedList<URL>();
	private Action getPreviousPageAction(){
		return new AbstractAction(){
			private static final long serialVersionUID = 7046186166123065672L;
			{ 
				putValue(NAME,I18n.getString(I18n.previousPage));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.returnToPreviousPage));
	        }
			public void actionPerformed(ActionEvent e) {
				previousPage();
			}};		
	}
	private Action getNextPageAction(){
		return new AbstractAction(){
			private static final long serialVersionUID = -3205079366058712601L;
			{ 
				putValue(NAME, I18n.getString(I18n.nextPage));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.goNextPage));
	        }
			public void actionPerformed(ActionEvent e) {
				nextPage();
			}};
	}	
	protected JPopupMenu getPopupMenu(){
		if(popupMenu==null){
			super.getPopupMenu();
			popupMenu.add(getPreviousPageAction());
			popupMenu.add(getNextPageAction());
		}
		return popupMenu;		
	}

	protected Container getParent(Object o){ //Return container to change cursor
		if(!(o instanceof Container)) return null;
		Container res=(Container)o;
		while(res != null &&
			 !(res instanceof JDialog) && 
			 !(res instanceof JWindow) && 
			 !(res instanceof JFrame))
			res = res.getParent();
		return res;
	}
	public JHTMLNavegable(Main m){
		super(m);
		addHyperlinkListener(this);
		setComponentPopupMenu(getPopupMenu());
	}
	public void setPage(URL url) throws IOException{
		super.setPage(url);
		previous.addFirst(url);
	}
	public void hyperlinkUpdate(HyperlinkEvent he) {
		Container dad=getParent(he.getSource());
		if(he.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
			URL url=he.getURL();
			if(url==null) return;
			try{
				super.setPage(url);
				previous.addFirst(url);
			} catch (Exception e) {
				/*
				if(!url.toExternalForm().startsWith(getCodeBase().toExternalForm())){
					getAppletContext().showDocument(url, "_blank");
				}*/
			}
			if(dad!=null)
			   dad.setCursor(Cursor.getDefaultCursor());
		}
		else if(he.getEventType() == HyperlinkEvent.EventType.ENTERED){
			if(dad!=null)
				   dad.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		else{
			if(dad!=null)
			   dad.setCursor(Cursor.getDefaultCursor());
		}
	}
	public void nextPage(){
		if(next.isEmpty()) return;
		try{
			super.setPage(next.getFirst());
			previous.addFirst(next.removeFirst());
		} catch (Exception e) {
		}
	}
	public void previousPage(){
		if(previous.size()<2) return;
		try{
			next.addFirst(previous.removeFirst());
			super.setPage(previous.getFirst());
		} catch (Exception e) {}			
	}
}
