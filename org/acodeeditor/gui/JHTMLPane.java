/**
 * $Id: JHTMLPane.java,v 1.4 2013-06-07 15:07:20 juanca Exp $
 * gui.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.gui;

import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import org.acodeeditor.util.I18n;

class JHTMLPane extends JTextPane{
	private static final long serialVersionUID = 4073971130258003128L;
	protected Main main;
    protected Action actionCopy;
	protected Action getActionCopy(){
		if(actionCopy == null){
			actionCopy= new AbstractAction(){
				private static final long serialVersionUID = 1885767447085086761L;
				{
					putValue(NAME,I18n.getString(I18n.copy));
		            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.copyText));
		            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control C"));
		            putValue(MNEMONIC_KEY,KeyStroke.getKeyStroke("c"));
		        }
				public void actionPerformed(ActionEvent e) {
					String s=getSelectedText();
					if(main.getRestrictedEdit() || !Main.getJnlpServicesAvailable()){
						main.setLocalClipboard(s);
					}else{
						try{
							javax.jnlp.ClipboardService cs=Main.getClipboardService();
							if(cs!=null){
								cs.setContents(new StringSelection(s));
							}
							requestFocusInWindow();
							getCaret().setVisible(true);
						}catch (Throwable e1) {
							main.setLocalClipboard(s);
						}
					}
				}};
		}
		return actionCopy;
	}
	protected JPopupMenu popupMenu=null;
	protected JPopupMenu getPopupMenu(){
		if(popupMenu==null){
			popupMenu = new JPopupMenu();
			popupMenu.add(getActionCopy());
			getInputMap().put(KeyStroke.getKeyStroke("ctrl C"), getActionCopy());
		}
		return popupMenu;		
	}

	public JHTMLPane(Main m){
		super();
		main=m;
		setEnabled(true);
		setEditable(false);
		setComponentPopupMenu(getPopupMenu());
	}
	public void setPage(String url) throws MalformedURLException, IOException{
		setPage(new URL(url));
	}
	public void setPage(URL url) throws IOException{
		super.setText("");
		super.setPage(url);
		try{
			int lastSize=0;
		   for(int i=0;i<50; i++){ //Wait until the page was load
		      Thread.sleep(100);
		      String text=getText();
			  if(lastSize == text.length() && text.lastIndexOf("</html>") != -1){
				Thread.sleep(100);
				break;
			  }
			  lastSize = text.length();
		   }
		}
		catch (Exception e) {}
	}
}
