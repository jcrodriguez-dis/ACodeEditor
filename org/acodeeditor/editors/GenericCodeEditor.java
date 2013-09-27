/**
 * $Id: GenericCodeEditor.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.text.*;

public class GenericCodeEditor extends DefaultStyledDocument implements Runnable {
	class Changes{
		boolean insertion; //The change was a insertion, not deletion
		String text; //Inserted, deleted text
		int pos; //Action position
		boolean changeable;
		public Changes(boolean insertion, String text, int pos ){
			this.insertion=insertion;
			this.text=text;
			this.pos=pos;
			changeable = text.length() == 1;
		}
		public boolean isInsertion() {
			return insertion;
		}
		public int getPosition() {
			return pos;
		}
		public void decrementPosition() {
			pos--;
		}
		public String getText() {
			return text;
		}
		public void appendText(String t){
			text += t;
		}
		public void addTextAtBeginning(String t){
			text = t+text;
		}
		public boolean isModificable(){
			return changeable;
		}
	}
	private static final long serialVersionUID = -4617382484045865076L;
	public static final char CR='\r';
	public static final char LF='\n';
	protected LinkedList<Changes> done=new LinkedList<Changes>();
	protected LinkedList<Changes> undone=new LinkedList<Changes>();
    protected JEditorPane view;
	protected Highlights font;
	protected Set<String> reserved=new HashSet<String>();
	protected boolean needUpdate=false; //True if the document need update
	protected boolean updating=false;   //True if document is been updating
	protected int     changeFrom=0;     //Change from
	protected Thread  updatingThread=null;
	protected volatile boolean stop=false;       //True if updating must stop
	public synchronized void changed(int from) {  //The document is changed
		if(from < changeFrom)
			changeFrom=from;
		needUpdate=true;
		updating=false;
	}
	protected synchronized void startUpdate() { //Updating start
		if(needUpdate){
		   updating=true;
		}
	}
	protected synchronized void endUpdate() { //Updating end
		if(needUpdate && updating){
            changeFrom=getLength();
			needUpdate=false;
			updating=false;
		}
	}
	protected synchronized boolean getNeedUpdate(){ //Need start or restart an update
		return needUpdate && !updating;
	}
	protected synchronized int getChangedFrom() {
		return changeFrom;		
	}
	protected boolean isIdentifierChar(char c){ //True if c is an identifier char
		return false;
	}
	public String getId(){
		int pos=getPane().getCaretPosition();
		int l=getLength();
		String s;
		try {
			s = getText(0, getLength());
		} catch (BadLocationException e) {
			return "";
		}
		String right="";
		for(int i=pos; i<l;i++ ){
			char c=s.charAt(i);
			if(isIdentifierChar(c)) right+=c;
			else break;
		}
		String left="";
		for(int i=pos-1; i>=0;i-- ){
			char c=s.charAt(i);
			if(isIdentifierChar(c)) left=c+left;
			else break;
		}
		return left+right;
	}
	protected void codeHighlight(){
		highlight(0, getLength()-1, font.getRegular());
		endUpdate();
	}
	public void setStop(){
		stop=true;
	}
	public void run(){
    	while(!stop){
      	  try {
  			  Thread.sleep(100); //Main loop wait
  	    	  if(getNeedUpdate()){
  	    	     startUpdate(); //Start updating
  	    	     Thread.sleep(100); //Wait for other change
  			     if(!getNeedUpdate()) //If don't need update, reset code Highligh 
  				     codeHighlight();
  	    	  }
  		  } catch (Exception e1) {};
      	}		
	}
    public void dataTransfer(GenericCodeEditor gce){
    	int l=getLength();
    	String content;
		try {
			content = getText(0, l);
	    	gce.setText(content);
		} catch (BadLocationException e) {}
    	gce.done=done;
    	gce.undone=undone;
    	gce.view=view;
    	changed(0);
    }

	protected boolean isIdentifier(String text, int begin, int end){
		for(int i=begin; i<=end;i++)
			if(!isIdentifierChar(text.charAt(i))) return false;
		return true;
	}
	protected void highlight(int begin, int end, MutableAttributeSet type) {
		if(updating && changeFrom-100 >end) return;
		if(begin>end || end>=getLength()) return;
		setCharacterAttributes(begin, end-begin+1, type, true);
	}
	protected void showPending(String text, int begin, int end) {
		if(begin>end || end>=text.length()) return;
		MutableAttributeSet type=font.getRegular();
		if (isIdentifier(text,begin,end)){
			String pendiente=text.substring(begin, end+1);
			if(reserved.contains(pendiente))
			   type=font.getReserved();
		}
		highlight(begin, end, type);
	}
	public GenericCodeEditor(Highlights fonts){
		super();
		this.font = fonts;
		setParagraphAttributes(0,getLength(),fonts.getRegular(),true);
		updatingThread = new Thread(this);
		updatingThread.setPriority(Thread.MIN_PRIORITY);
		updatingThread.start();
	}
	
	protected boolean readonly=false;
	public void setReadOnly(boolean ro){
		readonly=ro;
	}
	public boolean getReadOnly(){
		return readonly;
	}

	public void setPane(JEditorPane view){
		this.view=view;
		view.setBackground(font.getBackgroud());
		view.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
	}
	public JEditorPane getPane(){
		return view;
	}
	public boolean undoable(){
		return done.size()>0;
	}
	public boolean redoable(){
		return undone.size()>0;
	}
	public void initUndo(){
		done.clear();
		undone.clear();
	}
	public void undo(){
		if(done.size()==0) return;
		Changes mod=(Changes)done.getLast();
		if(mod.isInsertion()){
			try {
				super.remove(mod.getPosition(), mod.getText().length());
				getPane().setCaretPosition(mod.getPosition());
			} catch (BadLocationException e) {}
			changed(mod.getPosition());
		}
		else{
			try {
				int offset=mod.getPosition();
				AttributeSet attribute;
				if(getLength()>0){
					if(offset == 0) attribute=getCharacterElement(0).getAttributes();
					else attribute=getCharacterElement(offset-1).getAttributes();
				}
				else attribute=font.getRegular();
				super.insertString(mod.getPosition(), mod.getText(),attribute);
				getPane().setCaretPosition(mod.getPosition()+mod.getText().length());
			} catch (BadLocationException e) {}
			changed(mod.getPosition());			
		}
		undone.add(done.removeLast());
	}
	
	public void redo(){
		if(undone.size()==0) return;
		Changes mod=(Changes)undone.getLast();
		if(mod.isInsertion()){
			try {
				int offset=mod.getPosition();
				AttributeSet attribute;
				if(getLength()>0){
					if(offset == 0) attribute=getCharacterElement(0).getAttributes();
					else attribute=getCharacterElement(offset-1).getAttributes();
				}
				else attribute=font.getRegular();
				super.insertString(mod.getPosition(), mod.getText(),attribute);
				getPane().setCaretPosition(mod.getPosition()+mod.getText().length());
			} catch (BadLocationException e) {}
			changed(mod.getPosition());
		}
		else{
			try {
				super.remove(mod.getPosition(), mod.getText().length());
				getPane().setCaretPosition(mod.getPosition());
			} catch (BadLocationException e) {}
			changed(mod.getPosition());			
		}
		done.add(undone.removeLast());
	}
	public void insertString(int offset,
            String str,
            AttributeSet a)
            throws BadLocationException{
		if(getReadOnly()) return; //Reject insert
		removeLineHighlight();
		AttributeSet attribute;
		if(getLength()>0){
			if(offset == 0) attribute=getCharacterElement(0).getAttributes();
			else attribute=getCharacterElement(offset-1).getAttributes();
		}
		else attribute=font.getRegular();
		super.insertString(offset, str,attribute);
		//Save undo info
		//Try to compact action
		boolean compacted=false;
		if(done.size()>0 && str.length() == 1){
			Changes last=(Changes)done.getLast();
			if(last.isInsertion() && last.isModificable()
			   && last.getPosition()+last.getText().length() == offset){
				last.appendText(str);
				compacted=true;
			}
		}
		if(!compacted)
		  done.add(new Changes(true,str,offset));
		changed(offset);
	}
	
	public void setText(String str)
            throws BadLocationException{
		if(getReadOnly()) return; //Reject insert
		removeLineHighlight();
		super.remove(0, getLength());
		super.insertString(0, str,font.getRegular());
		changed(0);
	}

	public void remove(int offs,
            int len)
            throws BadLocationException{
		if(getReadOnly()) return; //Reject remove
		removeLineHighlight();
		String deleted=getText(offs, len);
		//Try to joint with last change
		boolean compacted=false;
		if(deleted.length() == 1 && done.size()>0){
			Changes ultima=(Changes)done.getLast();
			if(!ultima.isInsertion() && ultima.isModificable()){
				if(ultima.getPosition() == offs){ //DEL
					ultima.appendText(deleted);
					compacted=true;
				}
				else if(ultima.getPosition() == offs-1){ //BACKSPACE
					ultima.addTextAtBeginning(deleted);
					ultima.decrementPosition();
					compacted=true;
				}
			}
		}
		if(!compacted)
			done.add(new Changes(false,deleted,offs));
		super.remove(offs, len);
		changed(offs);
	}
	private boolean lineHighlighted=false;
	private int  lineHighlightOffset=0;
	private int  lineHighlightLength=0;
	
	public void lineHighlight(int n){
		//Search line
		removeLineHighlight();
		String s=getText();
		int line=1;
		int pos=0;
		while(pos<s.length()){
			if(line==n) break;
			if(s.charAt(pos)=='\n')
				line++;
			pos++;
		}
		if(line==n) {//Line found
			lineHighlightOffset=pos;
			lineHighlightLength=0;
			while(lineHighlightOffset+lineHighlightLength<s.length()){
				if(s.charAt(lineHighlightOffset+lineHighlightLength)=='\n')
					break;
				lineHighlightLength++;
			}
			setCharacterAttributes(lineHighlightOffset, lineHighlightLength, font.getHighlightedBackground(), false);
			lineHighlighted=true;
		}
	}
	public void removeLineHighlight(){
		if(lineHighlighted){
			setCharacterAttributes(lineHighlightOffset, lineHighlightLength, font.getRegularBackground(), false);
			lineHighlighted=false;
		}
		
	}
    public void goToLineHighlighted(){
    	if(lineHighlighted){
    		getPane().setCaretPosition(lineHighlightOffset);
    	}
    }
	public String getType(){
		return "";
	}
	public String getTypeName(){
		return "";
	}
	public String getText(){
		try {
			return getText(0, getLength());
		} catch (BadLocationException e) {
			return ""; //Code not reachable
		}
	}
	public void copy(){
	
	}
}
