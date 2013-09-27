/**
 * $Id: CasesCodeEditor.java,v 1.3 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import javax.swing.text.MutableAttributeSet;

public class CasesCodeEditor extends GenericCodeEditor {

	private static final long serialVersionUID = -9058212941202133241L;

	enum States {
		REGULAR, IN_INPUT, IN_OUTPUT
	};
	int lineStart;
	protected void highlight(int begin, int end, MutableAttributeSet type) {
		if(begin>end || lineStart+end>=getLength()) return;
		setCharacterAttributes(lineStart+begin, end-begin+1, type, true);
	}

	public CasesCodeEditor(Highlights fonts) {
		super(fonts);
		reserved.clear();
		reserved.add("case");
		reserved.add("input");
		reserved.add("output");
		reserved.add("gradereduction");
		reserved.add("inputend");
		reserved.add("outputend");
    }
	static void normalizeTag(StringBuilder text, StringBuilder tag, StringBuilder value){
		int i;
		int len=text.length();
		tag.setLength(0);
		for(i=0; i<len; i++){
			char c=text.charAt(i);
			if(c=='=')
				break;
			if(!Character.isWhitespace(c))
				tag.append(Character.toLowerCase(c));
		}
		value.setLength(0);
		for(i++; i<len; i++){
			value.append(text.charAt(i));
		}
	}

	public void codeHighlight() {
		/*
		 * [case=text]|
		 * [inputend=MARK]|
		 * [input=*line]|[input=rawMARK]
		 * [outputend=MARK]|
		 * [output=*line]|[output=rawMARK]
		 */
		States state = States.REGULAR;
		String s = getText();
		String tags="";
		StringBuilder line= new StringBuilder(100);
		StringBuilder tag = new StringBuilder(100);
		StringBuilder value = new StringBuilder(100);
		StringBuilder inputEnd=new StringBuilder();
		StringBuilder outputEnd=new StringBuilder();
		int l = s.length();
		for (int i = 0; i < l; i++) {//For every line
			lineStart=i;
			line.setLength(0);
			for(;i<l;i++){
				if ((i % 100 == 0) && getNeedUpdate()) {
					return;
				}
				char current = s.charAt(i);
				char next;
				if (i + 1 < l)
					next = s.charAt(i + 1);
				else
					next = '\0';
				if (current == CR){ // Check end of line
					if (next == LF){
						i++;
					}
					break;
				}
				if (current == LF ){ // Check end of line
					break;
				}
				line.append(current);
			}
			int poseq;
			if((poseq=line.indexOf("="))>=0){
				normalizeTag(line, tag,value);
				tags=tag.toString();
			}else{
				tag.setLength(0);
				tags="";
			}
			if(state == States.IN_INPUT || state == States.IN_OUTPUT){
				StringBuilder endTag;
				if(state == States.IN_INPUT)
					endTag=inputEnd;
				else
					endTag=outputEnd;
				if(endTag.length()>0){ //Check for end of input
					int pos=line.indexOf(endTag.toString());
					if(pos>=0){
						highlight(0,pos-1,font.getString());
						highlight(pos,pos+endTag.length()-1,font.getVariable());
						highlight(pos+endTag.length(),line.length()-1,font.getComment());
						state=States.REGULAR;
						continue; //Next line
					}
					else{
						highlight(0,line.length()-1,font.getString());
						continue;
					}
				}else if(state == States.IN_INPUT && tags.equals("input")){
					highlight(0,line.length()-1,font.getString());
					continue;
				}else if(tag.length()>0 && reserved.contains(tags)){//End of input/output, other valid tag
					state=States.REGULAR;
					//Go on to process the current tag
				}else{
					highlight(0,line.length()-1,font.getString());
					continue; //Next line
				}
			}
			if(state != States.REGULAR) continue;
			if(tag.length()>0){
				if(tags.equals("input") || tags.equals("output")){
					highlight(0, poseq-1, font.getReserved());
					highlight(poseq, poseq, font.getRegular());
					StringBuilder endTag;
					if(tags.equals("input"))
						endTag=inputEnd;
					else
						endTag=outputEnd;
					int pos;
					if(endTag.length()>0 && (pos=value.indexOf(endTag.toString()))>=0){
						highlight(poseq+1, poseq+1+pos-1, font.getString());
						highlight(poseq+1+pos, poseq+1+pos+endTag.length()-1, font.getVariable());
						highlight(poseq+1+pos+endTag.length(),line.length()-1, font.getComment());						
					}else{
						highlight(poseq+1, line.length()-1, font.getString());
						if(tags.equals("input"))
							state=States.IN_INPUT;
						else
							state=States.IN_OUTPUT;
					}
				}else if(tags.equals("output")){
					highlight(0, poseq-1, font.getReserved());
					highlight(poseq, poseq, font.getRegular());
					int pos;
					if(outputEnd.length()>0 && (pos=value.indexOf(outputEnd.toString()))>=0){
						highlight(poseq+1, poseq+1+pos-1, font.getString());
						highlight(poseq+1+pos, poseq+1+pos+outputEnd.length()-1, font.getVariable());
						highlight(poseq+1+pos+outputEnd.length(),line.length()-1, font.getComment());						
					}else{
						highlight(poseq+1, line.length()-1, font.getString());
						state=States.IN_OUTPUT;
					}
				}else if(tags.equals("gradereduction") || tags.equals("inputend") || tags.equals("outputend")){
					highlight(0, poseq-1, font.getReserved());
					highlight(poseq, poseq, font.getRegular());
					highlight(poseq+1, line.length()-1, font.getVariable());
				}else if(tags.equals("case")){
					highlight(0, poseq-1, font.getReserved());
					highlight(poseq, poseq, font.getRegular());
					highlight(poseq+1, line.length()-1, font.getString());
				}else{
					highlight(0, line.length()-1, font.getComment());
				}
				if(tags.equals("inputend")){
					inputEnd=new StringBuilder(value);
				} else if(tags.equals("outputend")){
					outputEnd=new StringBuilder(value);
				}
			}else{
				highlight(0, line.length()-1, font.getComment());
			}
		}
	}

	public String getType(){
		return "cases";
	}
	public String getTypeName(){
		return "VPL automatic evaluation cases";
	}

}
