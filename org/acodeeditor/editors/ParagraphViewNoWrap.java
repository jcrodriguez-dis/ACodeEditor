/**
 * $Id: ParagraphViewNoWrap.java,v 1.2 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import javax.swing.text.Element;
import javax.swing.text.ParagraphView;

public class ParagraphViewNoWrap extends ParagraphView {
	public ParagraphViewNoWrap(Element e){
		super(e);
	}
    public void layout(int width, int height) {
        super.layout(Short.MAX_VALUE, height);
    }
    public float getMinimumSpan(int axis) {
        return super.getPreferredSpan(axis);
    }
}
