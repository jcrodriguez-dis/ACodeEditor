/**
 * $Id: StyledEditorKitHS.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;
import javax.swing.text.*;

public class StyledEditorKitHS extends StyledEditorKit {
	private static final long serialVersionUID = -7390655015531668175L;
	public ViewFactory getViewFactory() {
		return new StyledViewFactoryNoWrap(); 
	   }
	   class StyledViewFactoryNoWrap implements ViewFactory {
		public View create(Element elem) {
		   String kind = elem.getName();
		   if (kind != null) {
			if (kind.equals(AbstractDocument.ContentElementName)) {
			   return new LabelView(elem) /*{
				public View breakView(int axis, int p0, float pos, float len) {
				   return super.breakView(axis,p0,pos,999999f);
				}
			   }*/;
			} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
			   return new ParagraphViewNoWrap(elem);
			} else if (kind.equals(AbstractDocument.SectionElementName)) {
			   return new BoxView(elem, View.Y_AXIS);
			} else if (kind.equals(StyleConstants.ComponentElementName)) {
			   return new ComponentView(elem);
			} else if (kind.equals(StyleConstants.IconElementName)) {
			   return new IconView(elem);
			}
		   }
		   return new LabelView(elem);
		}
	   }
	}
