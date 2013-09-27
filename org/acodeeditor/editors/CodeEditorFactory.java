/**
 * $Id: CodeEditorFactory.java,v 1.3 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

public class CodeEditorFactory {
	public static GenericCodeEditor getCodigo(String fileName, Highlights fonts) {
		fileName = fileName.toLowerCase();
		if (fileName.endsWith(".c")) { // C language
			return new CCodeEditor(fonts);
		} else if (fileName.endsWith(".cpp")
				|| fileName.endsWith(".h")
				|| fileName.endsWith(".cc")
				|| fileName.endsWith(".C")
				|| fileName.endsWith(".hxx")) { // C++ language
			return new CppCodeEditor(fonts);
		} else if (fileName.endsWith(".adb")
				|| fileName.endsWith(".ads")
				|| fileName.endsWith(".ada")) { // Ada language
			return new AdaCodeEditor(fonts);
		} else if (fileName.endsWith(".java")) { // Java language
			return new JavaCodeEditor(fonts);
		} else if (fileName.endsWith(".pl")) { // Prolog language
			return new PrologCodeEditor(fonts);
		} else if (fileName.endsWith(".ss")
				||fileName.endsWith(".scm")) { // Scheme language
			return new SchemeCodeEditor(fonts);
		} else if (fileName.endsWith(".pas")) { // Pascal
			return new PascalCodeEditor(fonts);
		} else if (fileName.endsWith(".f")
				||fileName.endsWith(".f77")) { // Fortran & Fortran 77 language
			return new Fortran77CodeEditor(fonts);
		}else if (fileName.endsWith(".sh")) { // Shell script bash  language
			return new ShCodeEditor(fonts);
		}else if (fileName.endsWith(".sql")) { // SQL language
			return new SQLCodeEditor(fonts);
		}else if (fileName.endsWith(".m")) { // Matlab language
			return new MatlabCodeEditor(fonts);
		}else if (fileName.endsWith(".py")) { // Python language
			return new PythonCodeEditor(fonts);
		}else if (fileName.endsWith(".cases")) { // Evaluation cases
			return new CasesCodeEditor(fonts);
		}else if (fileName.endsWith(".scala")) { // Scala language by 
			return new ScalaCodeEditor(fonts);
		} else { //unknow
			return new GenericCodeEditor(fonts);
		}
	}
}
