/**
 * $Id: Main.java,v 1.9 2013-07-09 14:18:30 juanca Exp $
 * gui.* is part of ACodeEditor
 * Copyright (C) 2011 Juan Carlos Rodríguez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.jnlp.ClipboardService;
import javax.jnlp.FileSaveService;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.acodeeditor.editors.Console;
import org.acodeeditor.editors.GenericCodeEditor;
import org.acodeeditor.editors.Highlights;
import org.acodeeditor.editors.LineNumber;
import org.acodeeditor.util.I18n;

public class Main extends JApplet {
	private static final long serialVersionUID = 2283624169932108343L;
	private LineNumber lineNumber=null;
	LineNumber getLineNumber(){
		if(lineNumber == null){
			lineNumber = new LineNumber(getHighlighting());
		}
		return lineNumber;
	}

    private String relURL(String rel){
    	return getCodeBase().toExternalForm()+rel;
    }

    private Main getMe(){
    	return this;
    }
	void updateFontSize(){
		int size=fontSizeSlider.getValue();
		setFont(getFont().deriveFont(size));
		//TODO Explore change full application font size
		getHighlighting().setSize(size);
		getLineNumber().setMaxLineNumber(9999);
		for(File file:files){
			file.changeFontSize(getHighlighting());
		}
		File f=activeFile();
		if(f!=null){
			if(getLineNumberMenuItem().getState())
				   f.setNumeration();
				else
				   f.removeNumeration();
		}
		validate();
	}

	private ArrayList<File> files= new ArrayList<File>();  //  @jve:decl-index=0:
	private int minNumberOfFiles = 0;
	private int maxNumberOfFiles = 100;

	private Highlights highlighting = null;
	private JTabbedPane tabs = null;
	private JMenuBar menuBar = null;
	private JMenu editSubmenu = null;
	private JMenu fileSubmenu = null;
	//TODO Remove MenuItem change to Action
	private JMenuItem redoMenuItem = null;
	private JMenuItem undoMenuItem = null;
	private JMenuItem newFileMenuItem = null;
	private JMenuItem changeNameMenuItem = null;
	private JMenuItem cutMenuItem = null;
	private JMenuItem copyMenuItem = null;
	private JMenuItem pasteMenuItem = null;
	private JMenuItem findReplaceMenuItem = null;
	private JMenuItem selectAllMenuItem = null;
	private JMenu optionsSubmenu = null;
	private JCheckBoxMenuItem lineNumberMenuItem = null;
	private JMenuItem fontSizeMenuItem = null;
	private JMenuItem deleteFileMenuItem = null;
	private JMenu helpSubmenu = null;
	private JMenuItem helpMenuItem = null;
	private JMenuItem contectualHelpMenuItem = null;
	private JMenuItem aboutMenuItem = null;
	/**
	 * Actions to manage
	 */
	private Action actionNewFile = null;  //  @jve:decl-index=0:
	private Action actionChangeName = null;
	private Action actionDeleteFile = null;  //  @jve:decl-index=0:
	private Action actionUndo = null;  //  @jve:decl-index=0:
	private Action actionRedo = null;  //  @jve:decl-index=0:
	private Action actionCut = null;
	private Action actionCopy = null;  //  @jve:decl-index=0:
	private Action actionPaste = null;
	private Action actionSelectAll = null;
	private Action actionFindReplace = null;  //  @jve:decl-index=0:
	private Action actionHelp = null;  //  @jve:decl-index=0:
	private Action actionContextualHelp = null;  //  @jve:decl-index=0:
	private Action actionAbout = null;  //  @jve:decl-index=0:
	private Action actionLineNumber= null;
	private Action actionNext = null;
	private Action actionReplace = null;
	private Action actionReplaceNext = null;
	private Action actionReplaceAll = null;
	/*
	 * FindReplace components
	 */
	private JDialog dialogFindReplace = null;  //  @jve:decl-index=0:visual-constraint="318,11"
	private JPanel dialagoFindReplaceContentPane = null;
	private JDialog dialogHelp = null;  //  @jve:decl-index=0:visual-constraint="8,209"
	private JPanel dialogFindReplaceFindPanel = null;
	private JComboBox<String> dialogFindReplaceComboFind = null;
	private JPanel dialogFindReplaceReplacePanel = null;
	private JComboBox<String> dialogFindReplaceComboReplace = null;
	private JPanel dialogFindReplaceOptionsPanel = null;
	private JCheckBox dialogFindReplaceCaseSensitiveOption = null;
	private JPanel dialogFindReplaceCaseSensitivePanel = null;
	private JPanel dialogFindReplaceAction1Panel = null;
	private JPanel dialogFindReplaceAction2Panel = null;
	private JButton dialogFindReplaceNextButton = null;
	private JButton dialogFindReplaceReplaceFindButton = null;
	private JButton dialogFindReplaceButton = null;
	private JButton dialogFindReplaceAllButton = null;
	private JSplitPane baseSplit = null;
	private JSplitPane getDivisionBase(){
		if(baseSplit==null){
			baseSplit = new JSplitPane();
			baseSplit.setBorder(null);
			baseSplit.setDividerSize(4);
			baseSplit.setContinuousLayout(true);
			baseSplit.setResizeWeight(0.5D);
			baseSplit.setLeftComponent(getTabs());
			baseSplit.setRightComponent(getPanelEntrega());
			Dimension minimumSize = new Dimension(150, 20);
			getTabs().setMinimumSize(minimumSize);
			getPanelEntrega().setMinimumSize(minimumSize);
		}
		return baseSplit;
	}

	static private ClipboardService clipboardService= null;
	public static ClipboardService getClipboardService(){
		if(clipboardService == null){
			 try {
                 Class.forName("javax.jnlp.ServiceManager");
                 clipboardService =
                     (ClipboardService)javax.jnlp.ServiceManager.
                         lookup("javax.jnlp.ClipboardService");
             } catch(Throwable e) {}
		}
		return clipboardService;
	}

	static private FileSaveService fileSaveService= null;
	public static FileSaveService getFileSaveService(){
		if(fileSaveService == null){
			 try {
                 Class.forName("javax.jnlp.ServiceManager");
                 fileSaveService =
                     (FileSaveService)javax.jnlp.ServiceManager.
                         lookup("javax.jnlp.FileSaveService");
             } catch(Throwable e) {}
		}
		return fileSaveService;
	}
	
	/**
	 * Default constructor
	 */
	public Main() {
		super();
	}

	Timer timerZeroFiles=null;
	/**
	 * This method initializes the applet
	 * 
	 * @return void
	 */
	public void init() {
		try {
			//Workaround to avoid error set in 6.0 update 22
			new javax.swing.text.html.parser.ParserDelegator();
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this);
			/*
			 * Code to fix an error in Explorer but in Firefox
			 */
			JEditorPane.registerEditorKitForContentType("text/html", "javax.swing.text.html.HTMLEditorKit");
			/*	Code to fix an unknown error in old Java version
			 */
			JTextPane noThing=new JTextPane(); 
			noThing.setContentType("text/html");
			setHTMLFontSize(noThing);
			noThing.setText("<html></html>");
		} catch (Exception e) {}
		jnlpServicesAvailable = getClipboardService()!=null;
		//Getting Applet Param tags
		try {
			String code="UTF-8";
			String i18n=getParameter("i18n");
			if(i18n!=null){
				setI18nStrings(URLDecoder.decode(i18n,code));
			}
			String lang=getParameter("lang");
			if(lang!=null){
				setLang(URLDecoder.decode(lang,code));
			}
			String maxFiles=getParameter("maxfiles");
			if(maxFiles!=null){
				setMaxFiles(Integer.parseInt(maxFiles));
			}
			String minFiles=getParameter("minfiles");
			if(minFiles!=null){
				setMinFiles(Integer.parseInt(minFiles));
			}
			String fontSize=getParameter("fontsize");
			if(fontSize!=null){
				getFontSizeSlider().setValue(Integer.parseInt(fontSize));
			}
			String re=getParameter("restrictededit");
			if(re!=null){
				setRestrictedEdit();
			}
			String ro=getParameter("readonly");
			if(ro!=null){
				setReadOnly();
			}
			for(int i=0; i<100; i++){
				String fileName=getParameter("filename"+i);
				String data=getParameter("filedata"+i);
				if(fileName == null || data == null) break;
				addFile(URLDecoder.decode(fileName,code),URLDecoder.decode(data,code));	
			}
			getContentPane().validate();
			String grade=getParameter("grade");
			String compilation=getParameter("compilation");
			String evaluation=getParameter("evaluation");
			if(grade!=null || compilation!= null || evaluation!=null){
				setResult(grade, compilation, evaluation);
				getContentPane().invalidate();
			}
			if(files.size()>0){
				getTabs().setSelectedIndex(0);
			}else if(maxNumberOfFiles>0){
				timerZeroFiles=new Timer(1000, new AbstractAction(){
					private static final long serialVersionUID = 1L;
					public void actionPerformed(ActionEvent e) {
						if(getContentPane().isShowing() && files.size() ==0  && maxNumberOfFiles>0){
							timerZeroFiles.stop();
							timerZeroFiles.setRepeats(false);
							timerZeroFiles=null;
							getActionNewFile().actionPerformed(null);
						}
					}
				});
				timerZeroFiles.start();
			}
		} catch (UnsupportedEncodingException e) {
			JOptionPane.showMessageDialog(getContentPane(), "Data decode error. Reload page");
		}
		catch (Throwable e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(getContentPane(), e);
		}
	}
	
	public String[][] getParameterInfo() {
	    String[][] info = {
	      // Parameter Name     Kind of Value   Description
	        {"i18n",				"String",	"Lines of (name=traslation)"},
	        {"lang",				"String",	"Language code"},
	        {"maxfiles",			"int",		"Maximum number of files"},
	        {"minfiles",			"int",		"Minimum number of files"},
	        {"restrictededit",		"bool",		"limit paste to local clipboard"},
	        {"readonly",			"bool",		"Disable edit"},
	        {"grade",				"String",	"Grade to show"},
	        {"compilation",			"String",	"Compilation result"},
	        {"evaluation",			"String",	"Evaluation"},
	        {"filename#",			"String",	"File name (# = 0,1,...)"},
	        {"filedata#",			"String",	"File data (# = 0,1,...)"},
	    };
	    return info;
	}    


	/**
	 * This method destroy the applet 
	 * @return void
	 */
	public void destroy() {
		for(File file:files){
			((GenericCodeEditor)file.getEditor().getDocument()).setStop();
		}
		files=null;
	}

	public String getAppletInfo(){
		return "Applet Code Editor\nAuthor JCRP et al.";
	}
    Highlights getHighlighting(){
    	if(highlighting==null){
    		highlighting=new Highlights();
    	}
    	return highlighting;
    }
    boolean fileAllreadyExists(String fileName, File file){
	    fileName=fileName.trim();
	    for(File f1:files){
	    	if(f1!=file && f1.getFileName().equals(fileName)){
	    		return true;
	    	}
	    }
	    return false;
	}
	public void setMinFiles(int min){
		minNumberOfFiles = min;
		getFileSubmenu().setVisible(minNumberOfFiles < maxNumberOfFiles);
	}
	public void setMaxFiles(int max){
		maxNumberOfFiles = max;
		getFileSubmenu().setVisible(minNumberOfFiles < maxNumberOfFiles);
	}
	public int getNFiles(){
		return files.size();
	}
	public void setBackgroudColor(String color){
		getHighlighting().setBackgroud(Color.decode(color));
	}
	int lastInnerDividerLocation=-1;
	int lastDividerLocation=-1;
	final protected static int HTMLMaxLen=32000;
	protected static String inputDecode(String input)throws UnsupportedEncodingException{
		if(input == null){
			return "";
		}
		String output = URLDecoder.decode(input,"UTF-8");
		if(output.length()> HTMLMaxLen){
			output = output.substring(0, HTMLMaxLen);
		}
		return output;
	}
	public void setResult(String grade, String compilation, String evaluation) throws UnsupportedEncodingException{
		//final String beginTags="<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; utf-8\"></head><body>";
		//final String endTags="</body></html>";
		//TODO review better divider auto location
		grade = inputDecode(grade);
		compilation = inputDecode(compilation);
		evaluation=inputDecode(evaluation);
		getProposedGrade().setFont(getHighlighting().getRegularFont());
		getProposedGrade().setVisible(true);
		getProposedGrade().setText(grade);
		getCompilationResult().setContentType("text/html; utf-8");
		getCompilationResult().setText(compilation);
		getEvaluationResult().setContentType("text/html; utf-8");
		getEvaluationResult().setText(evaluation);
		if(grade.length()+compilation.length()+evaluation.length()>0){
			if(!getPanelEntrega().isVisible()){
				getPanelEntrega().setVisible(true);
				if(lastDividerLocation==-1){
					getDivisionBase().setDividerLocation(0.7D);
				}else{
					getDivisionBase().setDividerLocation(lastDividerLocation);
				}
			}
			getContentPane().validate();
			JSplitPane innerSplit=(JSplitPane)submissionPanel.getBottomComponent();
			innerSplit.setVisible(true);
			//Show both
			if(compilation.length()>0 && evaluation.length()>0){
				if(!(getCompilationResult().isVisible() && getEvaluationResult().isVisible())){
					getCompilationResult().setVisible(true);
					getEvaluationResult().setVisible(true);
					innerSplit.setVisible(true);
					innerSplit.setResizeWeight(0.5D);
					if(lastDividerLocation==-1){
						innerSplit.setDividerLocation(0.5D);
					}else{
						innerSplit.setDividerLocation(lastDividerLocation);
					}
				}
			}
			//Show only compilation
			if(compilation.length()>0 && evaluation.length()==0){
				if(!(getCompilationResult().isVisible() && !getEvaluationResult().isVisible())){
					if(getCompilationResult().isVisible() && getEvaluationResult().isVisible()){
						lastInnerDividerLocation=innerSplit.getLastDividerLocation();
					}
					getCompilationResult().setVisible(true);
					getEvaluationResult().setVisible(false);
					innerSplit.setResizeWeight(1.0D);
					innerSplit.setDividerLocation(1.0D);
				}
			}
			//Show only evaluation
			if(compilation.length()==0 && evaluation.length()>0){
				if(!(!getCompilationResult().isVisible() && getEvaluationResult().isVisible())){
					if(getCompilationResult().isVisible() && getEvaluationResult().isVisible()){
						lastInnerDividerLocation=innerSplit.getLastDividerLocation();
					}
					getCompilationResult().setVisible(false);
					getEvaluationResult().setVisible(true);
					innerSplit.setResizeWeight(0.0D);
					innerSplit.setDividerLocation(0);
				}
			}
			if(compilation.length()==0 && evaluation.length()==0){
				if(getCompilationResult().isVisible() && getEvaluationResult().isVisible()){
					lastInnerDividerLocation=innerSplit.getLastDividerLocation();
				}
				getCompilationResult().setVisible(false);
				getEvaluationResult().setVisible(false);
				innerSplit.setResizeWeight(0.0D);
				innerSplit.setDividerLocation(0);
			}
		}else{
			if(getPanelEntrega().isVisible()){
				lastDividerLocation=getPanelEntrega().getLastDividerLocation();
				getPanelEntrega().setVisible(false);
			}
		}
	}

	private ProcessStatus processStatus=null;
	/**
	 * Add the statusBar to the menu
	 * @param JMenuBar jmb
	 */
	public void initProcessBar(JMenuBar jmb){
		processStatus = new ProcessStatus(jmb);
	}
	
	/**
	 * Start status bar process
	 * @param String text process text
	 */
	public void startStatusBarProcess(String text){
		processStatus.start(text);
	}

	/**
	 * Update the state bar state
	 */
	public void updateStatusBarProcess(){
		processStatus.update();
	}
	/**
	 * Stop status bar process
	 */
	public void endStatusBarProcess(){
		processStatus.end();
	}
	
	/**
	 * Stop status bar process and show a text for a period
	 */
	public void endStatusBarProcess(String text){
		processStatus.end(text);
	}

	private String normalizaLF(String entrada){
		final char CR='\r';
		final char LF='\n';
		StringBuffer res= new StringBuffer(entrada.length());
		for(int i=0; i<entrada.length();i++){
			char actual=entrada.charAt(i);
			if(actual==CR){
			   char anterior=' ';
			   if(i>0) anterior=entrada.charAt(i-1);
			   char siguiente=' ';
			   if(i+1<entrada.length()) siguiente=entrada.charAt(i+1);
			   if(!(anterior==LF || siguiente ==LF)){
				  res.append(LF); //MAC OS text
			   }
			}
			else res.append(actual);
		}
		return res.toString();
	}
	public void addFile(String fileName, String data){
		if(fileName==null)return;
		fileName =fileName.trim();
		if(fileName.length() ==0 || fileAllreadyExists(fileName, null))
			return;
		data = normalizaLF(data);
		File f=new File(fileName,data,getHighlighting(),getLineNumber()
				       ,this,getLineNumberMenuItem().getState(),
				       getRestrictedEdit(),getReadOnly());
		files.add(f);
		getTabs().addTab(fileName, f.getPane());
		getTabs().setSelectedComponent(f.getPane());
	}
	public String getFileName(int i){
		if(i<0 || i>= files.size()) return"";
		return files.get(i).getFileName();
	}

	public String getFileContent(int i){
		if(i<0 || i>= files.size()) return"";
		String code=((File)files.get(i)).getText();
		try {
			return URLEncoder.encode(code,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "ERROR";
		}
	}

	public String getFileContent(String fileName){
		for(Iterator<File> i=files.iterator();i.hasNext();){
			File f= i.next();
			if(f.getFileName().equals(fileName))
				return f.getText();
		}
		return "";
	}
	
	public void setI18nStrings(String i18nStrings){
		I18n.initialize(i18nStrings);
		this.setJMenuBar(getMenuBar());
		this.setContentPane(getDivisionBase());
		this.validate();
	}

	public void setLang(String lang){
		I18n.setLang(lang); 	    
	}
	
	private boolean restrictedEdit=false;
	public void setRestrictedEdit(){
		restrictedEdit = true;
	}
	public boolean getRestrictedEdit(){
		return restrictedEdit;
	}

	private boolean readOnly=false;
	public void setReadOnly(){
		readOnly = true;
	}
	public boolean getReadOnly(){
		return readOnly;
	}

	/**
	 * This method initializes tabs	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getTabs() {
		if (tabs == null) {
			tabs = new JTabbedPane();
			tabs.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					File f=activeFile();
					if(f!=null){
						if(getLineNumberMenuItem().getState())
							f.setNumeration();
						else
							f.removeNumeration();
					}
					int posfichero=getTabs().getSelectedIndex();
					getNewFileMenuItem().setEnabled(files.size() < maxNumberOfFiles);
					getChangeNameMenuItem().setEnabled(posfichero >= minNumberOfFiles);
					getDeleteFileMenuItem().setEnabled(posfichero >= minNumberOfFiles);
				}
			});
			tabs.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(java.awt.event.FocusEvent e) {
					File f=activeFile();
					if(f!=null) {
						if(getLineNumberMenuItem().getState())
							f.setNumeration();
						else
							f.removeNumeration();
						f.getEditor().requestFocusInWindow();
					}
				}
			});
			tabs.getActionMap().put(I18n.getString(I18n.help), getActionHelp());
			tabs.getActionMap().put(I18n.getString(I18n.contextualHelp), getActionContextualHelp());
		}
		return tabs;
	}

	/**
	 * This method initializes menuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getMenuBar() {
		if (menuBar == null) {
			menuBar = new JMenuBar();
			menuBar.add(getFileSubmenu());
			menuBar.add(getEditSubmenu());
			menuBar.add(getOptionsSubmenu());
			menuBar.add(Box.createHorizontalGlue());
			initProcessBar(menuBar);
			menuBar.add(getHelpSubmenu());
		}
		return menuBar;
	}

	/**
	 * This method initializes editSubmenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getEditSubmenu() {
		if (editSubmenu == null) {
			editSubmenu = new JMenu();
			editSubmenu.setText(I18n.getString(I18n.edit));
			editSubmenu.add(getUndoMenuItem());
			editSubmenu.add(getRedoMenuItem());
			editSubmenu.addSeparator();
			editSubmenu.add(getCutMenuItem());
			editSubmenu.add(getCopyMenuItem());
			editSubmenu.add(getPasteMenuItem());
			editSubmenu.addSeparator();
			editSubmenu.add(getSelectAllMenuItem());
			editSubmenu.addSeparator();
			editSubmenu.add(getFindReplaceMenuItem());
			editSubmenu.add(getJMenuItem4());
			}
		return editSubmenu;
	}

	/**
	 * This method initializes fileSubmenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getFileSubmenu() {
		if (fileSubmenu == null) {
			fileSubmenu = new JMenu();
			fileSubmenu.setText(I18n.getString(I18n.file));
			fileSubmenu.setMnemonic(KeyEvent.VK_F);
			fileSubmenu.add(getNewFileMenuItem());
			fileSubmenu.add(getChangeNameMenuItem());
			fileSubmenu.add(getDeleteFileMenuItem());
		}
		return fileSubmenu;
	}

	/**
	 * This method initializes redoMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getRedoMenuItem() {
		if (redoMenuItem == null) {
			redoMenuItem = new JMenuItem();
			redoMenuItem.setAction(getActionRedo());
		}
		return redoMenuItem;
	}

	private File activeFile(){
		int selected=getTabs().getSelectedIndex();
		if(selected == -1)
			return null;
		else
			return (File)files.get(selected);
	}
	/**
	 * This method initializes undoMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getUndoMenuItem() {
		if (undoMenuItem == null) {
			undoMenuItem = new JMenuItem();
			undoMenuItem.setAction(getActionUndo());
		}
		return undoMenuItem;
	}

	/**
	 * This method initializes newFileMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getNewFileMenuItem() {
		if (newFileMenuItem == null) {
			newFileMenuItem = new JMenuItem();
			newFileMenuItem.setAction(getActionNewFile());
		}
		return newFileMenuItem;
	}

	/**
	 * This method initializes changeNameMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getChangeNameMenuItem() {
		if (changeNameMenuItem == null) {
			changeNameMenuItem = new JMenuItem();
			changeNameMenuItem.setAction(getActionChangeName());
		}
		return changeNameMenuItem;
	}

	/**
	 * This method initializes cutMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getCutMenuItem() {
		if (cutMenuItem == null) {
			cutMenuItem = new JMenuItem();
			cutMenuItem.setAction(getActionCut());
		}
		return cutMenuItem;
	}

	/**
	 * This method initializes copyMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getCopyMenuItem() {
		if (copyMenuItem == null) {
			copyMenuItem = new JMenuItem();
			copyMenuItem.setAction(getActionCopy());
		}
		return copyMenuItem;
	}

	/**
	 * This method initializes pasteMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getPasteMenuItem() {
		if (pasteMenuItem == null) {
			pasteMenuItem = new JMenuItem();
			pasteMenuItem.setAction(getActionPaste());
		}
		return pasteMenuItem;
	}

	/**
	 * This method initializes findReplaceMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getFindReplaceMenuItem() {
		if (findReplaceMenuItem == null) {
			findReplaceMenuItem = new JMenuItem();
			findReplaceMenuItem.setAction(getActionFindReplace());
		}
		return findReplaceMenuItem;
	}

	/**
	 * This method initializes selectAllMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSelectAllMenuItem() {
		if (selectAllMenuItem == null) {
			selectAllMenuItem = new JMenuItem();
			selectAllMenuItem.setAction(getActionSelectAll());
		}
		return selectAllMenuItem;
	}

	/**
	 * This method initializes optionsSubmenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getOptionsSubmenu() {
		if (optionsSubmenu == null) {
			optionsSubmenu = new JMenu();
			optionsSubmenu.setText(I18n.getString(I18n.options));
			optionsSubmenu.add(getLineNumberMenuItem());
			optionsSubmenu.add(getFontSizeMenuItem());
		}
		return optionsSubmenu;
	}

	/**
	 * This method initializes lineNumberMenuItem	
	 * 	
	 * @return javax.swing.JCheckBoxMenuItem	
	 */
	private JCheckBoxMenuItem getLineNumberMenuItem() {
		if (lineNumberMenuItem == null) {
			lineNumberMenuItem = new JCheckBoxMenuItem();
			lineNumberMenuItem.setSelected(true);
			lineNumberMenuItem.setAction(getActionLineNumber());
		}
		return lineNumberMenuItem;
	}

	/**
	 * This method initializes fontSizeMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getFontSizeMenuItem() {
		if (fontSizeMenuItem == null) {
			fontSizeMenuItem = new JMenuItem();
			fontSizeMenuItem.setText(I18n.getString(I18n.fontSize));
			fontSizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getFontSizeDialog().setVisible(true);
					
				}
			});
		}
		return fontSizeMenuItem;
	}

	/**
	 * This method initializes deleteFileMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getDeleteFileMenuItem() {
		if (deleteFileMenuItem == null) {
			deleteFileMenuItem = new JMenuItem();
			deleteFileMenuItem.setAction(getActionDeleteFile());
		}
		return deleteFileMenuItem;
	}

	/**
	 * This method initializes helpSubmenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getHelpSubmenu() {
		if (helpSubmenu == null) {
			helpSubmenu = new JMenu();
			helpSubmenu.setText(I18n.getString(I18n.help));
			helpSubmenu.add(getHelpMenuItem());
			helpSubmenu.add(getContectualHelpMenuItem());
			helpSubmenu.add(new JMenuItem(getActionGeneralHelp()));
			helpSubmenu.add(getAboutMenuItem());
		}
		return helpSubmenu;
	}

private Action getActionNewFile(){
	if(actionNewFile == null){
		actionNewFile= new AbstractAction(){
			private static final long serialVersionUID = -2591739125931873566L;
			{
				putValue(NAME,I18n.getString(I18n.newFile));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.createNewFile));
	            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control N"));
	            putValue(MNEMONIC_KEY,KeyStroke.getKeyStroke("n"));
	        }
			public void actionPerformed(ActionEvent e) {
				if(files.size() < maxNumberOfFiles){
					  Object res=JOptionPane.showInputDialog(getTabs(),
							  I18n.getString(I18n.fileName));
					    if(res==null) return;
					    String nombre=res.toString().trim();
					    if(nombre.length()==0 || fileAllreadyExists(nombre, null)){
					    		JOptionPane.showMessageDialog(getTabs(),
					    				I18n.getString(I18n.incorrectFileName));
					    		return;
					    }
						addFile(nombre, "");
				}
			}
			
		};
	}
	return actionNewFile;
}
private Action getActionChangeName(){
	if(actionChangeName == null){
		actionChangeName= new AbstractAction(){
			private static final long serialVersionUID = 5893256040016140578L;
			{
				putValue(NAME,I18n.getString(I18n.rename));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.renameFile));
	            putValue(MNEMONIC_KEY,KeyStroke.getKeyStroke("c"));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f!=null){
					int pos=getTabs().getSelectedIndex();
					if(pos!=-1 && pos >= minNumberOfFiles){
						String pregunta=I18n.getString(I18n.newFileName);
					    Object res=JOptionPane.showInputDialog(getTabs(),
							   pregunta,f.getFileName());
					    if(res==null) return;
					    String nombre=res.toString().trim();
					    if(nombre.length()==0||fileAllreadyExists(nombre, f)){
				    		JOptionPane.showMessageDialog(getTabs(),
				    				I18n.getString(I18n.incorrectFileName));
				    		return;
				    	}
					    f.changeFileName(res.toString());
					    getTabs().setTitleAt(pos, f.getFileName());
					}
				}	
			}
			
		};
	}
	return actionChangeName;
}
private Action getActionDeleteFile(){
	if(actionDeleteFile == null){
		actionDeleteFile= new AbstractAction(){
			private static final long serialVersionUID = -2345067684594243554L;
			{
				putValue(NAME,I18n.getString(I18n.delete));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.deleteFile));
	            putValue(MNEMONIC_KEY,KeyStroke.getKeyStroke("e"));
	   	       }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f!=null){
					int pos=getTabs().getSelectedIndex();
					if(pos!=-1 && pos >= minNumberOfFiles){
						String pregunta=I18n.getString(I18n.deleteFileQ)+" \""+f.getFileName()+"\"";
					   if(JOptionPane.showConfirmDialog(getTabs(),
							   pregunta,pregunta,JOptionPane.YES_NO_OPTION)
							   == JOptionPane.YES_OPTION){
					       getTabs().remove(pos);
					       files.remove(pos);
					       ((GenericCodeEditor)f.getEditor().getDocument()).setStop();
					   }
					}
				}				
			}
			
		};
	}
	return actionDeleteFile;
}
private Action getActionUndo(){
	if(actionUndo == null){
		actionUndo= new AbstractAction(){
			private static final long serialVersionUID = 2025078104838062817L;
			{
				putValue(NAME,I18n.getString(I18n.undo));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.undoChange));
	            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control Z"));
	            putValue(MNEMONIC_KEY,KeyStroke.getKeyStroke("d"));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f != null){
					((GenericCodeEditor)f.getEditor().getDocument()).undo();
				}
			}};
	}
	return actionUndo;
}
private Action getActionRedo(){
	if(actionRedo == null){
		actionRedo= new AbstractAction(){
			private static final long serialVersionUID = 3223467476022496481L;
			{
				putValue(NAME,I18n.getString(I18n.redo));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.redoUndone));
	            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("shift control Z"));
	            putValue(MNEMONIC_KEY,KeyStroke.getKeyStroke("d"));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f != null){
					((GenericCodeEditor)f.getEditor().getDocument()).redo();
				}
			}};
	}
	return actionRedo;
}

private static boolean jnlpServicesAvailable=false;

public static boolean getJnlpServicesAvailable(){
	return jnlpServicesAvailable;
}

private String localClipboard = "";

void setLocalClipboard(String s){
	localClipboard = s;
}

String getLocalClipboard(){
	return localClipboard;
}

Action getActionCut(){
	if(actionCut == null){
		actionCut= new AbstractAction(){
			private static final long serialVersionUID = -8541286290535301269L;
			{
				putValue(NAME,I18n.getString(I18n.cut));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.cutText));
	            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control X"));
	            putValue(MNEMONIC_KEY,KeyStroke.getKeyStroke("t"));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f != null){
					String s=f.getEditor().getSelectedText();
					if(getRestrictedEdit() || !getJnlpServicesAvailable()){
						setLocalClipboard(s);
					}
					else{
						try{
							javax.jnlp.ClipboardService cs=getClipboardService();
							if(cs!=null){
								cs.setContents(new StringSelection(s));
							}
						}catch (Throwable e1) {
							setLocalClipboard(s);
						}
					}
					f.getEditor().replaceSelection("");
				}
			}};
		}
	return actionCut;
}
Action getActionCopy(){
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
				File f=activeFile();
				if(f != null){
					String s=f.getEditor().getSelectedText();
					if(getRestrictedEdit() || !getJnlpServicesAvailable()){
						setLocalClipboard(s);
					}
					else{
						try{
							javax.jnlp.ClipboardService cs=getClipboardService();
							if(cs!=null){
								cs.setContents(new StringSelection(s));
							}
						}catch (Throwable e1) {
							setLocalClipboard(s);
						}
					}
				}
			}};
	}
	return actionCopy;
}
Action getActionPaste(){
	if(actionPaste == null){
		actionPaste= new AbstractAction(){
			private static final long serialVersionUID = -1183210964688352085L;
			{ 
				putValue(NAME, I18n.getString(I18n.paste));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.pasteText));
	            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control V"));
	            putValue(MNEMONIC_KEY,new Integer(KeyEvent.VK_V));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f != null){
					String s="";
					if(getRestrictedEdit() || !getJnlpServicesAvailable()){
						s=getLocalClipboard();
					}
					else{
						try{
							javax.jnlp.ClipboardService cs=getClipboardService();
							if(cs!=null){
								Transferable t=cs.getContents();
								s = t.getTransferData(DataFlavor.stringFlavor).toString();
							}
						}catch (Throwable e1) {
							s=getLocalClipboard();
						}
					}
					f.getEditor().replaceSelection(s);
				}
			}};
	}
	return actionPaste;
}

private Action getActionSelectAll(){
	if(actionSelectAll == null){
		actionSelectAll= new AbstractAction(){
			private static final long serialVersionUID = 1935572878538135274L;
			{ 
				putValue(NAME,I18n.getString(I18n.selectAll));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.selectAllText));
	            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control A"));
	            putValue(MNEMONIC_KEY,KeyStroke.getKeyStroke("p"));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f != null)
					f.getEditor().selectAll();
			}};
	}
	return actionSelectAll;
}

private Action getActionFindReplace(){
	if(actionFindReplace == null){
		actionFindReplace= new AbstractAction(){
			private static final long serialVersionUID = 1887113812120032561L;
			{ 
				putValue(NAME,I18n.getString(I18n.findReplace));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.findFindReplace));
	            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control F"));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f != null){
					getDialogFindReplace().setVisible(true);
				}
			}};
	}
	return actionFindReplace;
}

/**
 * This method initializes helpMenuItem	
 * 	
 * @return javax.swing.JMenuItem	
 */
private JMenuItem getHelpMenuItem() {
	if (helpMenuItem == null) {
		helpMenuItem = new JMenuItem();
		helpMenuItem.setAction(getActionHelp());
	}
	return helpMenuItem;
}

/**
 * This method initializes contectualHelpMenuItem	
 * 	
 * @return javax.swing.JMenuItem	
 */
private JMenuItem getContectualHelpMenuItem() {
	if (contectualHelpMenuItem == null) {
		contectualHelpMenuItem = new JMenuItem();
		contectualHelpMenuItem.setAction(getActionContextualHelp());
	}
	return contectualHelpMenuItem;
}

/**
 * This method initializes aboutMenuItem	
 * 	
 * @return javax.swing.JMenuItem	
 */
private JMenuItem getAboutMenuItem() {
	if (aboutMenuItem == null) {
		aboutMenuItem = new JMenuItem();
		aboutMenuItem.setAction(getActionAbout());
	}
	return aboutMenuItem;
}
private Action getActionHelp(){
	if(actionHelp == null){
		actionHelp= new AbstractAction(){
			private static final long serialVersionUID = -593338544731486650L;
			{ 
				putValue(NAME,I18n.getString(I18n.help));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.programHelp));
	            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("F1"));
	            putValue(MNEMONIC_KEY,KeyStroke.getKeyStroke("a"));
	        }
			public void actionPerformed(ActionEvent e) {
				JDialog d=getDialogHelp();
				if(d==null){
					JOptionPane.showMessageDialog(getTabs(), I18n.getString(I18n.pageUnaccessible)+"\n");
				}
				else d.setVisible(true);
			}};
	}
	return actionHelp;
}

private JFrame contextualHelpDialog=null;
private JPopupMenu popupMenu = null;
private JDialog fontSizeDialog = null;  //  @jve:decl-index=0:visual-constraint="661,95"
private JPanel fontSizePanel = null;
private JSlider fontSizeSlider = null;
private JMenuItem jMenuItem4 = null;
private JSplitPane submissionPanel=null;
private JTextPane compilationResult = null;
private JTextPane evaluationResult = null;
private JTextField proposedGrade = null;

private JSplitPane getPanelEntrega(){
	if(submissionPanel==null){
		submissionPanel = new JSplitPane();
		submissionPanel.setVisible(false);
		submissionPanel.setMinimumSize(new Dimension(200,20));
		submissionPanel.setPreferredSize(new Dimension(200,200));
		JSplitPane inner= new JSplitPane();
		inner.setOrientation(JSplitPane.VERTICAL_SPLIT);
		inner.setDividerLocation(0.5D);
		inner.setContinuousLayout(true);
		inner.setTopComponent(new JScrollPane(getCompilationResult()));
		inner.setResizeWeight(0.5D);
		inner.setBottomComponent(new JScrollPane(getEvaluationResult()));
		inner.invalidate();
		submissionPanel.setDividerSize(0);
		submissionPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		submissionPanel.setTopComponent(getProposedGrade());
		submissionPanel.setBottomComponent(inner);
	}
	return submissionPanel;
}
private StyleSheet cssBase=null;  //  @jve:decl-index=0:
private StyleSheet getCssBase(){
	if(cssBase==null){
		cssBase = new StyleSheet();
		String url=getCodeBase().toExternalForm()+"help/estilos.css";
		try {
			cssBase.importStyleSheet(new URL(url));
			Thread.sleep(100);
		} catch (Exception e) {}
	}
	return cssBase;
}
/*
 * Set style sheet font size for default HTML
 */
private void setHTMLFontSize(JTextPane tp){
	HTMLEditorKit ek;
	try{
		ek=(HTMLEditorKit)tp.getEditorKitForContentType("text/html");
	}
	catch (Exception e) {
		return;
	}
	if(ek==null) return; //En las VMS antiguas no funciona bien
	ek.setLinkCursor(new Cursor(Cursor.HAND_CURSOR));
	StyleSheet css= getCssBase();
	StyleSheet cssOriginal= ek.getStyleSheet();
	String styleSize="body{font-size : "+
	                    (int)(100+((getFontSizeSlider().getValue()-14)/48.)*100)+"%;}";
	cssOriginal.addStyleSheet(css);
    cssOriginal.addStyle(styleSize, cssOriginal.getRule("body"));

}

private void ajustContextualHelpSize(){
	if(contextualHelpDialog==null) return;
	contextualHelpDialog.setVisible(true);
	contextualHelpDialog.pack();
	File f=activeFile();
	if(f == null) return;
	Point absCaretPos=f.getEditor().getCaret().getMagicCaretPosition(); //Caret pos document absolute
	Rectangle visibleRec= f.getPane().getVisibleRect();
	Point caretPos = new Point(absCaretPos.x-visibleRec.x, absCaretPos.y-visibleRec.y); //Change caret pos to split relative
	//Check window position and size
	Dimension idealSize=contextualHelpDialog.getSize(); //Ideal size after pack()
	int height=idealSize.height;
	int width=idealSize.width;
	int xOffset=0;
	int yOffset=0;
	int minimum=200;
	//Calculate yOffset an cut height to an adequate value
	yOffset=caretPos.y+getHighlighting().getCharHeight();					
	int maxHeight=200;
	if(height> maxHeight){
		height=maxHeight;
	}
	if(height<minimum){
		height=minimum;
	}
	//Calculate xOffset an cut width to apropiate value
	int maxWidth=500;
	if(width>maxWidth)
		width=maxWidth;
	if(width<minimum) width=minimum;
	xOffset =caretPos.x;
	Point windowPos=f.getEditor().getLocationOnScreen();
	//Set location based on window location on screen
	contextualHelpDialog.setLocation(windowPos.x+xOffset, windowPos.y+yOffset);
	//If need resize and scroll
	if(width!= idealSize.width || height != idealSize.height){
		JScrollPane sp = (JScrollPane) contextualHelpDialog.getContentPane();
		sp.setSize(width,height);
		contextualHelpDialog.setSize(width,height);
		sp.invalidate();
		contextualHelpDialog.invalidate();
		//Set location again
		contextualHelpDialog.setLocation(windowPos.x+xOffset, windowPos.y+yOffset);
	}
}

private java.awt.event.FocusAdapter contextualHelpRemover=null;
private java.awt.event.FocusAdapter getContextualHelpRemover(){
	if(contextualHelpRemover==null){
		contextualHelpRemover=new java.awt.event.FocusAdapter() {
	       public void focusLost(java.awt.event.FocusEvent e) {
				if(contextualHelpDialog==null) return;
				contextualHelpDialog.setVisible(false);
				contextualHelpDialog.dispose();
				contextualHelpDialog=null;
			}
		};
	}
	return contextualHelpRemover;
};

private Action getActionContextualHelp(){
	if(actionContextualHelp == null){
		actionContextualHelp= new AbstractAction(){
			private static final long serialVersionUID = 5352893317293511985L;
			{ 
				putValue(NAME,I18n.getString(I18n.contextualHelp));
				//TODO need rethink
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.contextualHelp));
	            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control F1"));
	        }
			public void actionPerformed(ActionEvent e) {
				String directories[]= {"reserved","keywords","functions","libraries"};
				File f=activeFile();
				if(f == null) return;
				GenericCodeEditor doc=(GenericCodeEditor)f.getEditor().getDocument();
				String type=doc.getType();
				if(type.equals("")) return;
				String identifier=doc.getId();
				if(identifier==null || identifier.equals("")) return;
				Point posCaret=f.getEditor().getCaret().getMagicCaretPosition();
				Point posDoc=f.getEditor().getLocationOnScreen();
				if(posCaret == null || posDoc == null) return;
				JHTMLPane tp=new JHTMLPane(getMe());
				boolean found=false;
				String urlBase="help/"+I18n.getLang()+'/'+type+"/";
				String url="";
				for(String directory: directories){
					try {
						url=relURL(urlBase+directory+"/"+identifier+".html");
						tp.setPage(url);
						found=true;
						break; //end of loop
					} catch (IOException e1) {
						//System.err.println(e1.getMessage());
					}
				}
				if(!found) return;
				tp.addHyperlinkListener(new HyperlinkListener(){
					public void hyperlinkUpdate(HyperlinkEvent he) {
						if(he.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
							JHTMLPane tp=(JHTMLPane) he.getSource();
							try {
								tp.removeFocusListener(getContextualHelpRemover());
								tp.getParent().removeFocusListener(getContextualHelpRemover());
								JHTMLPane newPage= new JHTMLPane(getMe());
								newPage.setPage(he.getURL());
								newPage.addHyperlinkListener(this);
								newPage.addFocusListener(getContextualHelpRemover());
								JScrollPane sp= new JScrollPane(newPage);
								sp.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
								sp.setVisible(true);
								sp.addFocusListener(getContextualHelpRemover());
							    contextualHelpDialog.setContentPane(sp);
							    contextualHelpDialog.setCursor(Cursor.getDefaultCursor());
								ajustContextualHelpSize();
								newPage.requestFocus();
							} catch (Exception e) {}
						} else if(he.getEventType() == HyperlinkEvent.EventType.ENTERED)
							contextualHelpDialog.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						else contextualHelpDialog.setCursor(Cursor.getDefaultCursor());
					}
				});
				tp.setEditable(false);
				tp.setVisible(true);
				if(contextualHelpDialog!=null){
					contextualHelpDialog.setVisible(false);
					contextualHelpDialog.dispose();
					contextualHelpDialog=null;					
				};
		        contextualHelpDialog= new JFrame();
				contextualHelpDialog.setMinimumSize(new Dimension(200,200));
				contextualHelpDialog.setVisible(false);
				contextualHelpDialog.setUndecorated(true);
				java.awt.event.FocusAdapter remover=new java.awt.event.FocusAdapter() {
					public void focusLost(java.awt.event.FocusEvent e) {
						if(contextualHelpDialog==null) return;
						contextualHelpDialog.setVisible(false);
						contextualHelpDialog.dispose();
						contextualHelpDialog=null;
					}
				};
				tp.addFocusListener(getContextualHelpRemover());
				JScrollPane sp= new JScrollPane(tp);
				sp.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
				sp.setVisible(true);
				//sp.addFocusListener(getEliminadorAyudaContextual());
				contextualHelpDialog.setVisible(true);
				contextualHelpDialog.addFocusListener(remover);
				contextualHelpDialog.setContentPane(sp);
				//Inicialmente situamos la ventana debajo
				contextualHelpDialog.setLocation(posDoc.x+posCaret.x,posDoc.y+posCaret.y+getFontSizeSlider().getValue());
				ajustContextualHelpSize();
				tp.requestFocus();
			}};
	}
	return actionContextualHelp;
}
private Action getActionAbout(){
	if(actionAbout == null){
		actionAbout= new AbstractAction(){
			private static final long serialVersionUID = 7689815074170913698L;
			{ 
				putValue(NAME,I18n.getString(I18n.about));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.appletCodeEditorAbout));
	        }
			public void actionPerformed(ActionEvent e) {
				String message= "ACodeEditor - Applet Code Editor\n"+
					            "Version 1.3.1 25/06/2013\n"+
					            "Authors:\n"+
					            "Programming\n"+
					            "  Juan Carlos Rodríguez-del-Pino\n"+
					            "Contextual Language Help\n"+
					            "  Margarita Díaz-Roca\n"+
					            "  José Daniel González-Domínguez\n"+
					            "  Zenón Hernádez-Figueroa\n"+
					            "Scala editor\n"+
					            "  Lang Michael\n"+
					            "  Lückl Bernd\n"+
					            "  Lang Johannes\n"+
					            "Licensed under GNU/GPL v3, see LICENSE.txt or\n"+
					            " http://www.gnu.org/licenses/gpl-3.0.html"
					            ;
				JOptionPane.showMessageDialog(getContentPane(), message);
			}};
	}
	return actionAbout;
}

private Action getActionLineNumber(){
	if(actionLineNumber == null){
		actionLineNumber= new AbstractAction(){
			private static final long serialVersionUID = -5003765197887678974L;
			{ 
				putValue(NAME,I18n.getString(I18n.lineNumber));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.toggleShowLineNumber));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f!=null){
					if(getLineNumberMenuItem().getState())
						   f.setNumeration();
						else
						   f.removeNumeration();
				}
			}
		};
	}
	return actionLineNumber;
}
private String getDialogSearchText(){
	JComboBox<String> comboFind=getDilagoFindReplaceComboFind();
	if(comboFind.getSelectedItem() == null) return null;
	String text=(String)comboFind.getSelectedItem();
	if(comboFind.getSelectedIndex()==-1)
		comboFind.addItem(text);
	return text;
}

private String getDialogReplaceText(){
	JComboBox<String> comboRemplace=getDilagoFindReplaceComboReplace();
	if(comboRemplace.getSelectedItem() == null) return null;
	String text=(String)comboRemplace.getSelectedItem();
	if(comboRemplace.getSelectedIndex()==-1)
		comboRemplace.addItem(text);
	return text;
}

private Action getActionNext(){
	if(actionNext == null){
		actionNext= new AbstractAction(){
			private static final long serialVersionUID = 1L;
			{ 
				putValue(NAME,I18n.getString(I18n.next));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.findNextSearchString));
	            putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control K"));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f != null){
				    String search=getDialogSearchText();
				    if(search==null || search.length()==0) return;
				    GenericCodeEditor doc=(GenericCodeEditor)f.getEditor().getDocument();
					String text=doc.getText();
				    int posCaret=f.getEditor().getCaretPosition();
				    if(!getDialogFindReplaceCaseSensitiveOption().isSelected()){
				    	search=search.toLowerCase();
				    	text=text.toLowerCase();
				    }
					int posFound=text.indexOf(search, posCaret);
					if(posFound!=-1){
						f.getEditor().setSelectionStart(posFound);
						f.getEditor().setSelectionEnd(posFound+search.length());
					}
				}
			}};
	}
	return actionNext;
}

private Action getActionReplace(){
	if(actionReplace == null){
		actionReplace= new AbstractAction(){
			private static final long serialVersionUID = -7832085968935768046L;
			{ 
				putValue(NAME,I18n.getString(I18n.replace));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.replaceSelectionIfMatch));
	            //putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control K"));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f != null){
				    String search=getDialogSearchText();
				    if(search==null || search.length()==0) return;
				    String replace=getDialogReplaceText();
				    if(replace==null) replace="";
					String selected=f.getEditor().getSelectedText();
				    if(!getDialogFindReplaceCaseSensitiveOption().isSelected()){
				    	search=search.toLowerCase();
				    	selected=selected.toLowerCase();
				    }
					if(!search.equals(selected)) return;
					f.getEditor().replaceSelection(replace);
					f.getEditor().getCaret().setVisible(true);
				}
			}};
	}
	return actionReplace;
}
private Action getActionReplaceNext(){
	if(actionReplaceNext == null){
		actionReplaceNext= new AbstractAction(){
			private static final long serialVersionUID = 5588884090492530411L;
			{ 
				putValue(NAME,I18n.getString(I18n.replaceFind));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.replaceFindNext));
	            //putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control K"));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f != null){
				    String search=getDialogSearchText();
				    if(search==null || search.length()==0) return;
				    String replace=getDialogReplaceText();
				    if(replace==null) replace="";
					String selected=f.getEditor().getSelectedText();
					if(selected==null) return;
				    if(!getDialogFindReplaceCaseSensitiveOption().isSelected()){
				    	search=search.toLowerCase();
				    	selected=selected.toLowerCase();
				    }
					if(!search.equals(selected)) return;
				    GenericCodeEditor doc=(GenericCodeEditor)f.getEditor().getDocument();
					f.getEditor().replaceSelection(replace);
					String text=doc.getText();
				    if(!getDialogFindReplaceCaseSensitiveOption().isSelected()){
				    	text=text.toLowerCase();
				    }
				    int posCaret=f.getEditor().getCaretPosition();
					int posFound=text.indexOf(search, posCaret);
					if(posFound!=-1){
						f.getEditor().setSelectionStart(posFound);
						f.getEditor().setSelectionEnd(posFound+search.length());
					}
				}
			}};
	}
	return actionReplaceNext;
}
private Action getActionReplaceAll(){
	if(actionReplaceAll == null){
		actionReplaceAll= new AbstractAction(){
			private static final long serialVersionUID = -1438109863546380520L;
			{ 
				putValue(NAME,I18n.getString(I18n.replaceAll));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.replaceAllNext));
	            //putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("control K"));
	        }
			public void actionPerformed(ActionEvent e) {
				File f=activeFile();
				if(f != null){
				    String search=getDialogSearchText();
				    if(search==null || search.length()==0) return;
				    String replace=getDialogReplaceText();
				    if(replace==null) replace="";
				    if(!getDialogFindReplaceCaseSensitiveOption().isSelected()){
				    	search=search.toLowerCase();
				    }
					while(true){
  					   String selected=f.getEditor().getSelectedText();
  					   if(selected==null) return;
  					    if(!getDialogFindReplaceCaseSensitiveOption().isSelected()){
  					    	selected=selected.toLowerCase();
  					    }
					   if(!search.equals(selected)) return;
				       GenericCodeEditor doc=(GenericCodeEditor)f.getEditor().getDocument();
					   f.getEditor().replaceSelection(replace);
					   String text=doc.getText();
					    if(!getDialogFindReplaceCaseSensitiveOption().isSelected()){
					    	text=text.toLowerCase();
					    }
				       int posCaret=f.getEditor().getCaretPosition();
					   int posFound=text.indexOf(search, posCaret);
					   if(posFound!=-1){
						  f.getEditor().setSelectionStart(posFound);
						  f.getEditor().setSelectionEnd(posFound+search.length());
					   }
					}
				}
			}};
	}
	return actionReplaceAll;
}

private Action actionGeneralHelp=null;  //  @jve:decl-index=0:
private Action getActionGeneralHelp(){
	if(actionGeneralHelp == null){
		actionGeneralHelp= new AbstractAction(){
			private static final long serialVersionUID = 1290150785722046923L;
			{ 
				putValue(NAME,I18n.getString(I18n.languageHelp));
	            putValue(SHORT_DESCRIPTION, I18n.getString(I18n.languageHelp));
	        }
			public void actionPerformed(ActionEvent e) {
				showHelpDialog();
			}};
	}
	return actionGeneralHelp;
}

/**
 * This method initializes DialogoBuscarReemplazar	
 * 	
 * @return javax.swing.JDialog	
 */
private JDialog getDialogFindReplace() {
	if (dialogFindReplace == null) {
		dialogFindReplace = new JDialog();
		dialogFindReplace.setSize(new Dimension(267, 192));
		dialogFindReplace.setTitle(I18n.getString(I18n.findReplace));
		dialogFindReplace.setResizable(false);
		dialogFindReplace.setContentPane(getDialagoFindReplaceContentPane());
		dialogFindReplace.pack();
		Point pos=getTabs().getLocationOnScreen();
		int x=pos.x+(getTabs().getSize().width-dialogFindReplace.getSize().width)/2;
		int y=pos.y+(getTabs().getSize().height-dialogFindReplace.getSize().height)/2;
		dialogFindReplace.setLocation(x,y);
	}
	return dialogFindReplace;
}

/**
 * This method initializes dialagoFindReplaceContentPane	
 * 	
 * @return javax.swing.JPanel	
 */
private JPanel getDialagoFindReplaceContentPane() {
	if (dialagoFindReplaceContentPane == null) {
		dialagoFindReplaceContentPane = new JPanel();
		dialagoFindReplaceContentPane.setLayout(new BorderLayout());
		dialagoFindReplaceContentPane.add(getDilagoFindReplaceFindPanel(), BorderLayout.NORTH);
		dialagoFindReplaceContentPane.add(getDilagoFindReplaceReplacePanel(), BorderLayout.CENTER);
		dialagoFindReplaceContentPane.add(getDialogFindReplaceOptionsPanel(), BorderLayout.SOUTH);
	}
	return dialagoFindReplaceContentPane;
}

/**
 * This method initializes DialogoAyuda	
 * 	
 * @return javax.swing.JPanel	
 */
private JDialog getDialogHelp() {
	if (dialogHelp == null) {
		String url=getCodeBase().toExternalForm()+"help/"+I18n.getLang()+"/index.html";
		JTextPane tp=new JHTMLPane(getMe());
		try {
			tp.setPage(url);
		} catch (Exception e1) {
			url=getCodeBase().toExternalForm()+"help/en_utf8/index.html";
			try {
				tp.setPage(url);
			} catch (Exception e2) {
				tp.setContentType("text/html");
				tp.setText("<html><body><h1>"+I18n.getString(I18n.pageUnaccessible)+"</h2></body></html>");
			}
		}
		dialogHelp = new JDialog();
		dialogHelp.setTitle(I18n.getString(I18n.helpAbout));
		Point start=getLocationOnScreen();
		int width=getSize().width-100;
		if(width<400) width=400;
		if(width>800) width=800;
		int height=getSize().height-50;
		if(height<400) height=400;
		if(height>800) height=800;
		dialogHelp.setLocation(start.x+(getSize().width-width)/2,start.y+25);
		dialogHelp.setSize(new Dimension(width,height));
		dialogHelp.setContentPane(new JScrollPane(tp));
		dialogHelp.setModal(false);
		dialogHelp.setVisible(true);
		dialogHelp.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				dialogHelp.dispose();
				dialogHelp=null;
			}
		});		
	}
	return dialogHelp;
}

private void showHelpDialog() {
	JDialog helpDialog=null;
	File f=activeFile();
	if(f == null) return;
	GenericCodeEditor doc=(GenericCodeEditor)f.getEditor().getDocument();
	String type=doc.getType();
	String url=getCodeBase().toExternalForm()+"help/"+I18n.getLang()+"/"+type+"/index.html";
	JHTMLNavegable tp=new JHTMLNavegable(getMe());
	try {
		tp.setPage(url);
	} catch (Exception e1) {
		return;
	}
	helpDialog = new JDialog();
	helpDialog.setTitle(I18n.getString(I18n.generalHelp));
	Point start=getLocationOnScreen();
	int width=getSize().width-100;
	if(width<400) width=400;
	if(width>800) width=800;
	int height=getSize().height-50;
	if(height<400) height=400;
	if(height>800) height=800;
	helpDialog.setLocation(start.x+(getSize().width-width)/2,start.y+25);
	helpDialog.setSize(new Dimension(width,height));
	helpDialog.setContentPane(new JScrollPane(tp));
	helpDialog.setModal(false);
	helpDialog.setVisible(true);
	helpDialog.addWindowListener(new java.awt.event.WindowAdapter() {
		public void windowClosing(java.awt.event.WindowEvent e) {
			JDialog d=(JDialog ) e.getComponent();
			d.setVisible(false);
			d.dispose();
		}
	});		
}

public void initConsole(final int port, final String key) {
	new Console(getDocumentBase().getHost(),port,key,getHighlighting(),this);
}

/**
 * This method initializes dialogFindReplaceFindPanel	
 * 	
 * @return javax.swing.JPanel	
 */
private JPanel getDilagoFindReplaceFindPanel() {
	if (dialogFindReplaceFindPanel == null) {
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
		flowLayout.setVgap(5);
		flowLayout.setHgap(5);
		JLabel label = new JLabel();
		label.setText(I18n.getString(I18n.find)+": ");
		dialogFindReplaceFindPanel = new JPanel();
		dialogFindReplaceFindPanel.setLayout(flowLayout);
		dialogFindReplaceFindPanel.add(label, null);
		dialogFindReplaceFindPanel.add(getDilagoFindReplaceComboFind(), null);
	}
	return dialogFindReplaceFindPanel;
}

/**
 * This method initializes comboBuscar	
 * 	
 * @return javax.swing.JComboBox	
 */
private JComboBox<String> getDilagoFindReplaceComboFind() {
	if (dialogFindReplaceComboFind == null) {
		dialogFindReplaceComboFind = new JComboBox<String>();
		dialogFindReplaceComboFind.setPreferredSize(new Dimension(150, 20));
		dialogFindReplaceComboFind.setEditable(true);
		//dialogFindReplaceComboFind.addActionListener(getActionNext());
	}
	return dialogFindReplaceComboFind;
}

/**
 * This method initializes dialogFindReplaceReplacePanel	
 * 	
 * @return javax.swing.JPanel	
 */
private JPanel getDilagoFindReplaceReplacePanel() {
	if (dialogFindReplaceReplacePanel == null) {
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
		flowLayout.setVgap(0);
		flowLayout.setHgap(5);
		JLabel label = new JLabel();
		label.setText(I18n.getString(I18n.replace)+": ");
		dialogFindReplaceReplacePanel = new JPanel();
		dialogFindReplaceReplacePanel.setLayout(flowLayout);
		dialogFindReplaceReplacePanel.add(label, null);
		dialogFindReplaceReplacePanel.add(getDilagoFindReplaceComboReplace(), null);
	}
	return dialogFindReplaceReplacePanel;
}

/**
 * This method initializes comboReemplazar	
 * 	
 * @return javax.swing.JComboBox	
 */
private JComboBox<String> getDilagoFindReplaceComboReplace() {
	if (dialogFindReplaceComboReplace == null) {
		dialogFindReplaceComboReplace = new JComboBox<String>();
		dialogFindReplaceComboReplace.setPreferredSize(new Dimension(150, 20));
		dialogFindReplaceComboReplace.setEditable(true);
		dialogFindReplaceComboReplace.addActionListener(getActionReplaceNext());
	}
	return dialogFindReplaceComboReplace;
}

/**
 * This method initializes dialogFindReplaceOptionsPanel	
 * 	
 * @return javax.swing.JPanel	
 */
private JPanel getDialogFindReplaceOptionsPanel() {
	if (dialogFindReplaceOptionsPanel == null) {
		dialogFindReplaceOptionsPanel = new JPanel();
		dialogFindReplaceOptionsPanel.setLayout(new BorderLayout());
		dialogFindReplaceOptionsPanel.add(getDialogFindReplaceCaseSensitivePanel(), BorderLayout.NORTH);
		dialogFindReplaceOptionsPanel.add(getDialogFindReplaceAction1Panel(), BorderLayout.CENTER);
		dialogFindReplaceOptionsPanel.add(getDialogFindReplaceAction2Panel(), BorderLayout.SOUTH);
	}
	return dialogFindReplaceOptionsPanel;
}

/**
 * This method initializes dialogFindReplaceCaseSensitiveOption	
 * 	
 * @return javax.swing.JCheckBox	
 */
private JCheckBox getDialogFindReplaceCaseSensitiveOption() {
	if (dialogFindReplaceCaseSensitiveOption == null) {
		dialogFindReplaceCaseSensitiveOption = new JCheckBox();
		dialogFindReplaceCaseSensitiveOption.setText(I18n.getString(I18n.caseSensitive));
	}
	return dialogFindReplaceCaseSensitiveOption;
}

/**
 * This method initializes dialogFindReplaceCaseSensitivePanel	
 * 	
 * @return javax.swing.JPanel	
 */
private JPanel getDialogFindReplaceCaseSensitivePanel() {
	if (dialogFindReplaceCaseSensitivePanel == null) {
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
		flowLayout.setVgap(5);
		flowLayout.setHgap(5);
		dialogFindReplaceCaseSensitivePanel = new JPanel();
		dialogFindReplaceCaseSensitivePanel.setLayout(flowLayout);
		dialogFindReplaceCaseSensitivePanel.add(getDialogFindReplaceCaseSensitiveOption(), null);
	}
	return dialogFindReplaceCaseSensitivePanel;
}

/**
 * This method initializes dialogFindReplaceAction1Panel	
 * 	
 * @return javax.swing.JPanel	
 */
private JPanel getDialogFindReplaceAction1Panel() {
	if (dialogFindReplaceAction1Panel == null) {
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
		flowLayout.setVgap(0);
		flowLayout.setHgap(5);
		dialogFindReplaceAction1Panel = new JPanel();
		dialogFindReplaceAction1Panel.setLayout(flowLayout);
		dialogFindReplaceAction1Panel.add(getDialogFindReplaceNextButton(), null);
		dialogFindReplaceAction1Panel.add(getDialogFindReplaceReplaceFindButton(), null);
	}
	return dialogFindReplaceAction1Panel;
}

/**
 * This method initializes dialogFindReplaceAction2Panel	
 * 	
 * @return javax.swing.JPanel	
 */
private JPanel getDialogFindReplaceAction2Panel() {
	if (dialogFindReplaceAction2Panel == null) {
		FlowLayout flowLayout4 = new FlowLayout();
		flowLayout4.setAlignment(java.awt.FlowLayout.LEFT);
		flowLayout4.setVgap(5);
		flowLayout4.setHgap(5);
		dialogFindReplaceAction2Panel = new JPanel();
		dialogFindReplaceAction2Panel.setLayout(flowLayout4);
		dialogFindReplaceAction2Panel.add(getDialogFindReplaceButton(), null);
		dialogFindReplaceAction2Panel.add(getDialogFindReplaceAllButton(), null);
	}
	return dialogFindReplaceAction2Panel;
}

/**
 * This method initializes dialogFindReplaceNextButton	
 * 	
 * @return javax.swing.JButton	
 */
private JButton getDialogFindReplaceNextButton() {
	if (dialogFindReplaceNextButton == null) {
		dialogFindReplaceNextButton = new JButton();
		dialogFindReplaceNextButton.setText(I18n.getString(I18n.next));
		dialogFindReplaceNextButton.setAction(getActionNext());
	}
	return dialogFindReplaceNextButton;
}

/**
 * This method initializes dialogFindReplaceReplaceFindButton	
 * 	
 * @return javax.swing.JButton	
 */
private JButton getDialogFindReplaceReplaceFindButton() {
	if (dialogFindReplaceReplaceFindButton == null) {
		dialogFindReplaceReplaceFindButton = new JButton();
		dialogFindReplaceReplaceFindButton.setText(I18n.getString(I18n.replaceFind));
		dialogFindReplaceReplaceFindButton.setAction(getActionReplaceNext());
	}
	return dialogFindReplaceReplaceFindButton;
}

/**
 * This method initializes dialogFindReplaceButton	
 * 	
 * @return javax.swing.JButton	
 */
private JButton getDialogFindReplaceButton() {
	if (dialogFindReplaceButton == null) {
		dialogFindReplaceButton = new JButton();
		dialogFindReplaceButton.setText(I18n.getString(I18n.replace));
		dialogFindReplaceButton.setAction(getActionReplace());
	}
	return dialogFindReplaceButton;
}

/**
 * This method initializes dialogFindReplaceAllButton	
 * 	
 * @return javax.swing.JButton	
 */
private JButton getDialogFindReplaceAllButton() {
	if (dialogFindReplaceAllButton == null) {
		dialogFindReplaceAllButton = new JButton();
		dialogFindReplaceAllButton.setText(I18n.getString(I18n.replaceAll));
		dialogFindReplaceAllButton.setAction(getActionReplaceAll());
	}
	return dialogFindReplaceAllButton;
}

/**
 * This method initializes popupMenu	
 * 	
 * @return javax.swing.JPopupMenu	
 */
JPopupMenu getPopupMenu() {
	if (popupMenu == null) {
		popupMenu = new JPopupMenu();
		popupMenu.add(getActionContextualHelp());
		popupMenu.addSeparator();
		popupMenu.add(getActionUndo());
		popupMenu.add(getActionRedo());
		popupMenu.addSeparator();
		popupMenu.add(getActionCut());
		popupMenu.add(getActionCopy());
		popupMenu.add(getActionPaste());
		popupMenu.addSeparator();
		popupMenu.add(getActionSelectAll());		
		popupMenu.addSeparator();
		popupMenu.add(getActionFindReplace());
		popupMenu.add(getActionNext());
	}
	return popupMenu;
}

/**
 * This method initializes jDialog	
 * 	
 * @return javax.swing.JDialog	
 */
private JDialog getFontSizeDialog() {
	if (fontSizeDialog == null) {
		fontSizeDialog = new JDialog();
		fontSizeDialog.setTitle(I18n.getString(I18n.fontSize));
		fontSizeDialog.setSize(new Dimension(223, 85));
		fontSizeDialog.setModal(true);
		fontSizeDialog.setResizable(false);
		fontSizeDialog.setContentPane(getFontSizePanel());
		fontSizeDialog.pack();
	}
	Point pos=getLocationOnScreen();
	Dimension d=getSize();
	Dimension t=fontSizeDialog.getSize();
	fontSizeDialog.setLocation(pos.x+d.width-t.width, pos.y);
	return fontSizeDialog;
}

/**
 * This method initializes dialogoTama�oFuente	
 * 	
 * @return javax.swing.JPanel	
 */
private JPanel getFontSizePanel() {
	if (fontSizePanel == null) {
		fontSizePanel = new JPanel();
		fontSizePanel.setLayout(new BorderLayout());
		fontSizePanel.add(getFontSizeSlider(), BorderLayout.NORTH);
	}
	return fontSizePanel;
}

/**
 * This method initializes fontSizeSlider	
 * 	
 * @return javax.swing.JSlider	
 */
private JSlider getFontSizeSlider() {
	if (fontSizeSlider == null) {
		fontSizeSlider = new JSlider();
		fontSizeSlider.setMinimum(10);
		fontSizeSlider.setMinorTickSpacing(1);
		fontSizeSlider.setPaintLabels(true);
		fontSizeSlider.setPaintTicks(true);
		fontSizeSlider.setValue(14);
		fontSizeSlider.setPaintTrack(true);
		fontSizeSlider.setMajorTickSpacing(4);
		fontSizeSlider.setExtent(1);
		fontSizeSlider.setSnapToTicks(true);
		fontSizeSlider.setValueIsAdjusting(true);
		fontSizeSlider.setMaximum(42);
		fontSizeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				updateFontSize();
			}
		});
	}
	return fontSizeSlider;
}

/**
 * This method return fontSize	
 * 	
 * @return javax.swing.JSlider	
 */
public int getFontSize() {
	return getFontSizeSlider().getValue();
}

/**
 * This method set fontSize		
 */
public void setFontSize(int v) {
	if(v!=getFontSize()){
		getFontSizeSlider().setValue(v);
		updateFontSize();
	}
}

/**
 * This method initializes jMenuItem4	
 * 	
 * @return javax.swing.JMenuItem	
 */
private JMenuItem getJMenuItem4() {
	if (jMenuItem4 == null) {
		jMenuItem4 = new JMenuItem();
		jMenuItem4.setAction(getActionNext());
	}
	return jMenuItem4;
}
class RedirectorToFile implements javax.swing.event.HyperlinkListener {
	public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent he) {
		JTextPane tp=(JTextPane)he.getSource();
		if(he.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
			URL url=he.getURL();
			String urlPath;
			if(url!=null) urlPath= url.getPath();
			else urlPath=he.getDescription();
			int posNumeric=urlPath.indexOf("#");
			if(posNumeric>=0){//Posible enlace interno
				String fileAndLineNumber=urlPath.substring(posNumeric+1);
				int index=0;
				for (File file : files) {
					if(fileAndLineNumber.startsWith(file.getFileName()+".")){
						String snline=fileAndLineNumber.substring(file.getFileName().length()+1);
						try{
						   int nline= Integer.parseInt(snline);
						   GenericCodeEditor doc=(GenericCodeEditor)file.getEditor().getDocument();
						   getTabs().setSelectedIndex(index);
						   file.getEditor().requestFocusInWindow();
						   doc.lineHighlight(nline);
						   doc.goToLineHighlighted();
						   getTabs().revalidate();
						   file.getEditor().requestFocusInWindow();
						   doc.lineHighlight(nline);
						   doc.goToLineHighlighted();
						   getTabs().revalidate();
						   return;
						}
						catch (Exception e) {}
					}
					index++;
				}
			}
			if(url!=null)
			   getAppletContext().showDocument(url, "_blank");
		} else if(he.getEventType() == HyperlinkEvent.EventType.ENTERED)
			tp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		else tp.setCursor(Cursor.getDefaultCursor());
	}
}
/**
 * This method initializes compilationResult	
 * 	
 * @return javax.swing.JTextPane	
 */
private JTextPane getCompilationResult() {
	if (compilationResult == null) {
		compilationResult = new JHTMLPane(getMe());
		compilationResult.setMinimumSize(new Dimension(50,50));
		compilationResult.setEditable(false);
		compilationResult.setPreferredSize(new Dimension(500,500));
		compilationResult.addHyperlinkListener(new RedirectorToFile());
	}
	return compilationResult;
}
/**
 * This method initializes evaluationResult	
 * 	
 * @return javax.swing.JTextPane	
 */
private JTextPane getEvaluationResult() {
	if (evaluationResult == null) {
		evaluationResult = new JHTMLPane(getMe());
		//TODO rethink
		evaluationResult.setName("evaluationResult");
		evaluationResult.setEditable(false);
		evaluationResult.setVisible(false);
		evaluationResult.setPreferredSize(new Dimension(500,500));
		evaluationResult.addHyperlinkListener(new RedirectorToFile());
	}
	return evaluationResult;
}
/**
 * This method initializes resultadoEntrega	
 * 	
 * @return javax.swing.JTextField	
 */
private JTextField getProposedGrade() {
	if (proposedGrade == null) {
		proposedGrade = new JTextField();
		proposedGrade.setEditable(false);
		proposedGrade.setVisible(false);
		//TODO rethink
		proposedGrade.setName("proposedGrade");
	}
	return proposedGrade;
}
}
