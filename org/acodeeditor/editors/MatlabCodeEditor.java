/**
 * $Id: MatlabCodeEditor.java,v 1.3 2013-06-07 15:07:20 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import java.util.HashSet;
import java.util.Set;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;

public class MatlabCodeEditor extends GenericCodeEditor {
	private static final long serialVersionUID = -4939895978988116479L;
	enum States {REGULAR, IN_IDENTIFIER, IN_COMMENT, IN_LINE_COMMENT, IN_PREPROCESOR, IN_STRING, IN_CHAR};

	protected char lastNoSpace;
	protected char stringDelimiter;
	protected States state;
	protected Set<String> functions;

	protected void initialize() {
		state=States.REGULAR;
		lastNoSpace = LF;
	}
    protected void setReservedWords(){
		String list[] = {
				//Source MATLAB Quick Reference Author: Jialong He
				//﻿Managing Commands and Functions 	 
				"addpath", "doc", "docopt", "genpath", "help", "helpbrowser",
				"helpdesk", "helpwin", "lasterr", "lastwarn", "license",
				"lookfor", "partialpath", "path", "pathtool", "profile",
				"profreport", "rehash", "rmpath", "support", "type", "ver",
				"version", "web", "what", "whatsnew", "which",
				//Managing Variables and the Workspace
				"clear", "disp", "length", "load", "memory", "mlock",
				"munlock", "openvar", "Open", "pack", "save", "saveas",
				"size", "who", "whos", "workspace",
				//﻿Starting and Quitting MATLAB 	 
				"finish", "exit", "matlab", "matlabrc", "quit", "startup",
				//  as a Programming Language
				"builtin", "eval", "evalc", "evalin", "feval",
				"function", "global", "nargchk", "persistent", "script",
				//Control Flow
				"break", "case", "catch", "continue", "else", "elseif",
				"end", "error", "for", "if", "otherwise", "return",
				"switch", "try", "warning", "while",
				//Interactive Input
				"input", "keyboard", "menu", "pause",
				//﻿Object-Oriented Programming 	 
				"class", "double", "inferiorto", "inline",
				"int8", "int16", "int32", "isa", "loadobj",
				"saveobj", "single", "superiorto", "uint8",
				"uint16", "uint32",
				//Operators
				"kron", "xor", "and"
		};
		reserved.clear();
		for (int i = 0; i < list.length; i++)
			reserved.add(list[i]);
		String listf[] = {
				"kron", "xor", "all", "any", "exist", "find",
				"is*", "isa", "logical", "mislocked", "builtin",
				"eval", "evalc", "evalin", "feval", "function",
				"global", "nargchk", "persistent", "script", "break",
				"case", "catch", "else", "elsei", "end", "error", "for",
				"if", "otherwise", "return", "switch", "try", "warning",
				"while", "input", "keyboard", "menu", "pause", "class",
				"double", "inferiorto", "inline", "int8", "int16",
				"int32", "isa", "loadobj", "saveobj", "single",
				"superiorto", "uint8", "int16", "uint32", "dbclear",
				"dbcont", "dbdown", "dbmex", "dbquit", "dbstack",
				"dbstatus", "dbstep", "dbstop", "dbtype", "dbup",
				"blkdiag", "eye", "linspace", "logspace", "ones",
				"rand", "randn", "zeros", "ans", "computer", "eps",
				"flops", "Inf", "inputname", "NaN", "nargin",
				"nargout", "pi", "realmax", "realmin", "varargin",
				"varargout", "calendar", "clock", "cputime", "date",
				"datenum", "datestr", "datevec", "eomday", "etime", "now",
				"tic", "toc", "weekday", "cat", "diag", "fliplr",
				"flipud", "repmat", "reshape", "rot90", "tril", "triu",
				"compan", "gallery", "hadamard", "hankel", "hilb",
				"invhilb", "magic", "pascal", "toeplitz", "wilkinson",
				"abs", "acos", "acosh", "acot", "acoth", "acsc", "acsch",
				"angle", "asec", "asech", "asin", "asinh", "atan",
				"atanh", "atan2", "ceil", "complex", "conj", "cos",
				"cosh", "cot", "coth", "csc", "csch", "exp", "fix",
				"floor", "gcd", "imag", "lcm", "log", "log2", "log10",
				"mod", "nchoosek", "real", "rem", "round", "sec", "sech",
				"sign", "sin", "sinh", "sqrt", "tan", "tanh", "airy", "besselh",
				"besseli", "besselk", "besselj", "Bessely", "beta", "betainc",
				"betaln", "ellipj", "ellipke", "erf", "erfc", "erfcx", "erfiny",
				"expint", "factorial", "gamma", "gammainc", "gammaln",
				"legendre", "pow2", "rat", "rats", "cart2pol", "cart2sph",
				"pol2cart", "sph2cart", "abs", "eval", "real", "strings",
				"deblank", "findstr", "lower", "strcat", "strcmp", "strcmpi",
				"strjust", "strmatch", "strncmp", "strrep", "strtok", "strvcat",
				"symvar", "texlabel", "upper", "char", "int2str", "mat2str",
				"num2str", "sprintf", "sscanf", "str2double", "str2num", "bin2dec",
				"dec2bin", "dec2hex", "hex2dec", "hex2num", "fclose", "fopen",
				"fread", "fwrite", "fgetl", "fgets", "fprintf", "fscanf",
				"feof", "ferror", "frewind", "fseek", "ftell", "sprintf",
				"sscanf", "dlmread", "dlmwrite", "hdf", "imfinfo", "imread", "imwrite",
				"textread", "wk1read", "wk1write", "bitand", "bitcmp", "bitor", "bitmax",
				"bitset", "bitshift", "bitget", "bitxor", "fieldnames", "getfield", "rmfield",
				"setfield", "struct Create", "struct2cell", "class", "isa", "cell",
				"cellfun", "cellstr", "cell2struct", "celldisp", "cellplot", "num2cell",
				"cat", "flipdim", "ind2sub", "ipermute", "ndgrid", "ndims", "permute",
				"reshape", "shiftdim", "squeeze", "sub2ind", "cond", "condeig", "det",
				"norm", "null", "orth", "rank", "rcond", "rref", "rrefmovie", "subspace",
				"trace", "chol", "inv", "lscov", "lu", "nnls", "pinv", "qr", "balance",
				"cdf2rdf", "eig", "gsvd", "hess", "poly", "qz", "rsf2csf", "schur",
				"svd", "expm", "funm", "logm", "sqrtm", "qrdelete", "qrinsert", "bar",
				"barh", "hist", "hold", "loglog", "pie", "plot", "polar", "semilogx",
				"semilogy", "subplot", "bar3", "bar3h", "comet3", "cylinder", "fill3",
				"plot3", "quiver3", "slice", "sphere", "stem3", "waterfall", "clabel",
				"datetick", "grid", "gtext", "legend", "plotyy", "title", "xlabel",
				"ylabel", "zlabel", "contour", "contourc", "contourf", "hidden", "meshc",
				"mesh", "peaks", "surf", "surface", "surfc", "surfl", "trimesh", "trisurf",
				"coneplot", "contourslice", "isocaps", "isonormals", "isosurface",
				"reducepatch", "reducevolume", "shrinkfaces", "smooth3", "stream2",
				"stream3", "streamline", "surf2patch", "subvolume", "griddata", "meshgrid",
				"area", "box", "comet", "compass", "errorbar", "ezcontour", "ezcontourf",
				"ezmesh", "ezmeshc", "ezplot", "ezplot3", "ezpolar", "ezsurf", "ezsurfc",
				"feather", "fill", "fplot", "pareto", "pie3", "plotmatrix", "pcolor", "rose",
				"quiver", "ribbon", "stairs", "scatter", "scatter3", "stem", "convhull",
				"delaunay", "dsearch", "inpolygon", "polyarea", "tsearch", "voronoi",
				"camdolly", "camlookat", "camorbit", "campan", "campos", "camproj", "camroll",
				"camtarget", "camup", "camva", "camzoom", "daspect", "pbaspect", "view",
				"viewmtx", "xlim", "ylim", "zlim", "camlight", "diffuse", "lighting",
				"lightingangle", "material", "specular", "brighten", "bwcontr", "caxis",				
				"colorbar", "colorcube", "colordef", "colormap", "graymon", "hsv2rgb",
				"rgb2hsv", "rgbplot", "shading", "spinmap", "surfnorm", "whitebg", "autumn",
				"bone", "contrast", "cool", "copper", "flag", "gray", "hot", "hsv", "jet",
				"lines", "prism", "spring", "summer", "winter", "orient", "print", "printopt",
				"saveas", "copyobj", "findobj", "gcbo", "gco", "get", "rotate", "ishandle", "set",
				"axes", "figure", "image", "light", "line", "patch", "rectangle", "surface",
				"text Create", "uicontext Create", "capture", "clc", "clf", "clg", "close",
				"gcf", "newplot", "refresh", "saveas", "axis", "cla", "gca", "propedit",
				"reset", "rotate3d", "selectmoveresize", "shg", "ginput", "zoom", "dragrect",
				"drawnow", "rbbox", "dialog", "errordlg", "helpdlg", "inputdlg", "listdlg",
				"msgbox", "pagedlg", "printdlg", "questdlg", "uigetfile", "uiputfile",
				"uisetcolor", "uisetfont", "warndlg", "menu", "menuedit", "uicontextmenu",
				"uicontrol", "uimenu", "dragrect", "findfigs", "gcbo", "rbbox",
				"selectmoveresize", "textwrap", "uiresume", "uiwait Used", "waitbar",
				"waitforbuttonpress", "convhull", "cumprod", "cumsum", "cumtrapz", "delaunay",
				"dsearch", "factor", "inpolygon", "max", "mean", "median", "min", "perms",
				"polyarea", "primes", "prod", "sort", "sortrows", "std", "sum", "trapz",
				"tsearch", "var", "voronoi", "del2", "diff", "gradient", "corrcoef", "cov",
				"conv", "conv2", "deconv", "filter", "filter2", "abs", "angle", "cplxpair",
				"fft", "fft2", "fftshift", "ifft", "ifft2", "ifftn", "ifftshift",
				"nextpow2", "unwrap", "cross", "intersect", "ismember", "setdiff",
				"setxor", "union", "unique", "conv", "deconv", "poly", "polyder",
				"polyeig", "polyfit", "polyval", "polyvalm", "residue", "roots",
				"griddata", "interp1", "interp2", "interp3", "interpft", "interpn",
				"meshgrid", "ndgrid", "spline", "dblquad", "fmin", "fmin", "fzero",
				"ode45,", "ode113,", "ode15s,", "ode23s", "ode23t,", "ode23tb", "odefile",
				"odeget", "odeset", "quad,", "vectorize", "spdiags", "speye", "sprand",
				"sprandn", "sprandsym", "find", "full", "sparse", "spconvert", "nnz",
				"nonzeros", "nzmax", "spalloc", "spfun", "spones", "colmmd", "colperm",
				"dmperm", "randperm", "symmmd", "symrcm", "condest", "normest", "bicg",
				"bicgstab", "cgs", "cholinc", "cholupdate", "gmres", "luinc", "pcg", "qmr",
				"qr", "qrdelete", "qrinsert", "qrupdate", "eigs", "svds", "spparms",
				"lin2mu", "mu2lin", "sound", "soundsc", "auread", "auwrite", "wavread",
				"wavwrite", "addpath", "do", "docopt", "help", "helpdesk", "helpwin",
				"lasterr", "lastwarn", "lookfor", "partialpath", "path", "pathtool",
				"profile", "profreport", "rmpath", "type", "ver", "version", "web",
				"what", "whatsnew", "which", "clear", "disp", "length", "load", "mlock",
				"munlock", "openvar", "pack", "save", "saveas", "size", "who", "whos",
				"workspace", "clc", "echo", "format", "home", "more", "cd", "copyfile",
				"delete", "diary", "dir", "edit", "fileparts", "fullfile", "inmem", "ls",
				"matlabroot", "mkdir", "open", "pwd", "tempdir", "tempname", "matlabrc",
				"quit", "startup"
		};
		functions = new HashSet<String>();
		for (int i = 0; i < listf.length; i++)
			if(!reserved.contains(listf[i])) //Add not reserved
				functions.add(listf[i]);
    }
	public MatlabCodeEditor(Highlights fonts) {
		super(fonts);
		initialize();
		setReservedWords();
	}

	protected boolean isIdentifierChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
				|| (c >= '0' && c <= '9') || (c == '_') || (c >= 128);
	}
	
	void lineAdvance() {
		lastNoSpace = LF;
	}

	protected void showPending(String text, int begin, int end) {
		if(begin>end || end>=text.length()) return;
		MutableAttributeSet type=font.getRegular();
		if (isIdentifier(text,begin,end)){
			String pendiente=text.substring(begin, end+1);
			if(reserved.contains(pendiente))
			   type=font.getReserved();
			else if(functions.contains(pendiente))
				   type=font.getFunction();
		}
		highlight(begin, end, type);
	}

	protected void codeHighlight() {
		String s=getText();
		int l = s.length();
		int blockStart = 0;
		initialize();
		char current = '\0';
		for (int i = 0; i < l; i++) {
			if((i%100 == 0) && getNeedUpdate()){
				return;
			}
			char next;
			char previous = current;
			current = s.charAt(i);
			if (i < (l - 1))
				next = s.charAt(i + 1);
			else
				next = '\0';
			if (current == CR)
				if (next == LF)
					continue;
				else
					current = LF;
			if (previous != ' ' && previous != '\t') { // Keep first and last no space char of line
				lastNoSpace = previous;
			}
			switch(state){
			case REGULAR:
			case IN_IDENTIFIER:
				if (current == '%') {
					if(next == '{'){
						showPending(s,blockStart, i - 1);
						state=States.IN_COMMENT;
						blockStart = i;
						i++;
						continue;
					}else{
						showPending(s,blockStart, i - 1);
						state=States.IN_LINE_COMMENT;
						blockStart = i;
						continue;
					}
				} else if (current == '"') {
					showPending(s,blockStart, i - 1);
					state=States.IN_STRING;
					stringDelimiter=current;
					blockStart = i;
				} else if (current == '\'' && 
						(lastNoSpace == LF || "[,;'=(".indexOf(lastNoSpace) >-1)) {
					showPending(s,blockStart, i - 1);
					state=States.IN_STRING;
					stringDelimiter=current;
					blockStart = i;
				} else if(state==States.IN_IDENTIFIER){
					showPending(s,blockStart, i - 1);
                    blockStart=i;
                    state=States.REGULAR;
				}
				if (current == LF)
					lineAdvance();
				break;
			case IN_LINE_COMMENT:
				if (current == LF) {
					highlight(blockStart, i, font.getComment());
					blockStart = i + 1;
					state= States.REGULAR;
				}
				break;
			case IN_COMMENT:
				if(current == LF){
					highlight(blockStart, i+1, font.getComment());
					blockStart = i + 1;
				}else if (current == '%' && next == '}') {
					highlight(blockStart, i+1, font.getComment());
					blockStart = i + 2;
					i++;
					state= States.REGULAR;
					continue;
				}
				break;
			case IN_STRING:
				if(current==stringDelimiter && next==stringDelimiter){
					i++;
				}else if(stringDelimiter == '"' && current =='\\'){
					i++;
				}else if(stringDelimiter == current){
					highlight(blockStart, i, font.getString());
					blockStart = i + 1;
					state=States.REGULAR;
				}
				break;
			}
		}
		switch(state){
		case REGULAR:
		case IN_IDENTIFIER:
			showPending(s,blockStart, l-1);
			break;
		case IN_LINE_COMMENT:
			highlight(blockStart, l-1,font.getComment());
			break;
		case IN_PREPROCESOR:
				highlight(blockStart, l-1, font.getPreprocesor());
			break;
		case IN_CHAR:				
		case IN_STRING:				
			highlight(blockStart, l-1, font.getString());
			break;
		}
		endUpdate();
	}
	public void insertString(int offset,
            String str,
            AttributeSet a)
            throws BadLocationException{
		if(str.length() == 1 && str.charAt(0) == '\n' && offset>0){//New line
			String s=getText();
			//Locating previous line
			int lineStart=offset-1;
			for(; lineStart>=0; lineStart--)
				if(s.charAt(lineStart)== '\n') break;
			lineStart++;
			int added;
			for(added=0;lineStart+added<offset;added++){
				char c= s.charAt(lineStart+added);
				if(c != ' ' && c != '\t'){
					break;
				}
			}
			if(added >0){
				str += s.substring(lineStart,lineStart+added);
			}
			if(s.charAt(offset-1)=='{'){ //previos char == {
				str += "\t";
			}
		}
		super.insertString(offset, str,a);
	}
	public void remove(int offs,
            int len)
            throws BadLocationException{
		if(len == 1 && getPane().getCaret().getDot() == offs+1){
			String s=getText();
			if(s.charAt(offs)==' '){
				boolean remove=false;
				for(int i=offs; i>=0; i--){
					if(s.charAt(i)!=' '){
						if(s.charAt(i) == '\n') remove=true;
						break;
					}
				}
				if(remove)
  				   while(offs>0 && len<3 && s.charAt(offs-1)==' '){
					  offs--;
					  len++;
				   }
			}
		}

		super.remove(offs, len);
	}
	public String getType(){
		return "m";
	}
	public String getTypeName(){
		return "Matlab";
	}
}
