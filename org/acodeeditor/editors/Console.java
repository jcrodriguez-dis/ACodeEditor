/**
 * $Id: Console.java,v 1.5 2013-07-09 14:17:42 juanca Exp $
 * editors.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodríguez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.editors;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.acodeeditor.gui.Main;
import org.acodeeditor.util.I18n;

class ConsoleDoc extends DefaultStyledDocument implements Runnable {
	private static final long serialVersionUID = -852166104864394680L;
	public static final char CR = '\r';
	public static final char LF = '\n';
	public static final char TAB = '\t';
	public static final char BELL = (char) 7;
	public static final char BS = (char) 8;
	public static final char ESC = (char) 27;
	public static final char B64 = (char) 17;
	private final int capacity = 10000;
	private MutableAttributeSet regularText, inputText;
	private Color background, foreground;
	private Socket connection = null;
	private OutputStream out = null;
	private InputStream in = null;
	private Highlights fonts = null;
	private JTextPane pane = null;
	private java.awt.Frame rootFrame = null;
	private String initialTitle = null;
	private StringBuffer textTypewrited = null;
	private Charset charset = null;
	private static Integer globalIdentification = 0; // Console identification
	// number
	private int identification = 0; // Console identification number

	private void clear() {
		try {
			super.remove(0, getLength());
		} catch (BadLocationException e) {
			e.printStackTrace(System.err);
		}
	}

	private void append(String s, javax.swing.text.AttributeSet a) {
		try {
			if (getLength() > capacity)
				super.remove(0, getLength() - capacity);
			super.insertString(getLength(), s, a);
		} catch (BadLocationException e) {
			e.printStackTrace(System.err);
		}
		if (pane != null) {
			pane.setCaretPosition(getLength());
		}
	}

	private void setTitle(String t) {
		if (rootFrame != null) {
			if (initialTitle == null)
				initialTitle = rootFrame.getTitle() + " " + identification;
			rootFrame.setTitle(initialTitle + " - " + t);
		}
	}

	public ConsoleDoc(Highlights highlights) {
		this(highlights, "UTF-8");
	}

	public ConsoleDoc(Highlights highlights, String charsetName) {
		this.charset = Charset.forName(charsetName);
		synchronized (globalIdentification) {
			globalIdentification++;
			identification = globalIdentification;// Get new identification;
		}
		textTypewrited = new StringBuffer();
		fonts = highlights;
		background = Color.black;
		foreground = Color.white;
		regularText = new SimpleAttributeSet();
		regularText.addAttributes(fonts.getRegular());
		StyleConstants.ColorConstants.setForeground(regularText, Color.white);
		StyleConstants.ColorConstants.setBackground(regularText, Color.black);
		inputText = new SimpleAttributeSet();
		inputText.addAttributes(regularText);
		StyleConstants.ColorConstants.setForeground(inputText, Color.yellow);
		setParagraphAttributes(0, 1000, inputText, true);
	}

	public void connect(String server, int port, String key) {
		final int ntry = 30;
		if (pane != null)
			pane.setVisible(true);
		String message = I18n.getString(I18n.connecting) + " (" + server + ":"
				+ port + ")";
		InetAddress address;
		try {
			address = InetAddress.getByName(server);
		} catch (UnknownHostException e2) {
			setTitle(message + " " + e2.getMessage());
			return;
		}
		setTitle(message);
		for (int i = 0; i < ntry; i++) {
			try {
				connection = new Socket(address, port);
				out = connection.getOutputStream();
				in = connection.getInputStream();
				out.write(key.getBytes());
				out.flush();
				connection.setReceiveBufferSize(64 * 1024);
				new Thread(this).start();
				return;
			} 
			catch (SecurityException e) {
				 	JOptionPane.showMessageDialog(pane, e.getMessage()+"\nPosible reason:\n" +
				 			"1) Applets can't access servers if both are\n"+
				 			"   running in the same machine.\n" +
				 			"   Try to access from another machine.\n"+
				 			"2) Firewall configuration (server or client)\n",e.getMessage(),
				 			JOptionPane.ERROR_MESSAGE);
					connection = null;
					setTitle(I18n.getString(I18n.connectionFail));
					return;
			}
			catch (Exception e) {
				try {
					message += ".";
					setTitle(message + " " + e.getMessage());
					Thread.sleep(300);
				} catch (Exception e1) {
					e.printStackTrace(System.err);
				}
				connection = null;
			}
		}
		setTitle(I18n.getString(I18n.connectionFail));
	}

	public void disconnect() {
		if (connection != null) {
			try {
				connection.close();
				connection = null;
			} catch (IOException e) {
				//No error
			}
		}
	}

	public void sendControl(char c) {
		try {
			out.write(c);
		} catch (Exception e) {
			//No error
		}
	}

	public void send(String s) {
		try {
			out.write(s.getBytes());
		} catch (Exception e) {
			//No error
		}
	}

	public void insertString(int off, String s, javax.swing.text.AttributeSet a)
			throws BadLocationException {
		if (connection != null && connection.isConnected()) {
			try {
				out.write(s.getBytes(charset));
				out.flush();
				synchronized (textTypewrited) {
					textTypewrited.append(s);
				}
			} catch (IOException e) {
				//No error
			}
		}
	}

	public void remove(int off, int len) throws BadLocationException {
		if (connection != null && connection.isConnected()
				&& off == getLength() - 1 && len == 1) {
			try {
				out.write(BS);
				out.flush();
			} catch (IOException e) {
				//No error
			}
			if (pane != null)
				pane.setCaretPosition(getLength());
		}
	}

	static int getESCValue(String s) {
		if (s.length() == 0)
			return 1; // default value
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return 0;
		}
	}

	static boolean isEndLine(char c) {
		return c == LF || c == CR;
	}

	void moveBackward() {
		// append("(MB)",inputText);
		int pos = pane.getCaretPosition();
		if (pos > 0) {
			try {
				char d = super.getText(pos - 1, 1).charAt(0);
				if (!isEndLine(d)) // Not to new line
					pane.setCaretPosition(pos - 1);
			} catch (BadLocationException e) {
				e.printStackTrace(System.err);
			}
		}
	}

	void moveTo(int x, int y) {
		// append("(MB)",inputText);
		// use view to model
		int pos = pane.getCaretPosition();
		if (pos > 0) {
			try {
				char d = super.getText(pos - 1, 1).charAt(0);
				if (!isEndLine(d)) // Not to new line
					pane.setCaretPosition(pos - 1);
			} catch (BadLocationException e) {
				e.printStackTrace(System.err);
			}
		}
	}

	void moveForward() {
		// append("(MF)",inputText);
		int pos = pane.getCaretPosition();
		try {
			if (pos < getLength()) {
				char d = super.getText(pos, 1).charAt(0);
				if (!isEndLine(d)) // Not to new line
					pane.setCaretPosition(pos + 1);
			} else
				pane.setCaretPosition(getLength());
		} catch (BadLocationException e) {
			e.printStackTrace(System.err);
		}
	}

	void eraseEndOfLine() {
		// append("(EEOL)",inputText);
		int pos = pane.getCaretPosition();
		int len;
		int l = getLength();
		// Locate end of line
		for (len = 0; true; len++) {
			char d;
			try {
				d = getText(pos + len, 1).charAt(0);
				if (isEndLine(d) || pos + len + 1 >= l) {
					super.remove(pos, len);
					return;
				}
			} catch (BadLocationException e) {
				e.printStackTrace(System.err);
				return;
			}
		}
	}

	void backspace() {
		try { // always remove last char
			int l = getLength();
			char d = getText(l - 1, 1).charAt(0);
			if (!isEndLine(d)) {
				super.remove(l - 1, 1);
			}
		} catch (BadLocationException e) {
			e.printStackTrace(System.err);
		}
	}

	public void run() {
		int nfigures = 0;
		int nsounds = 0;
		boolean in_ESC_sequence = false;
		boolean in_BASE64_sequence = false;
		String escCodes = "";
		if (connection == null)
			return;
		try {
			clear();
			setTitle(I18n.getString(I18n.connected));
			InputStreamReader ins = new InputStreamReader(in, charset);
			int read;
			char buf[] = new char[10000];
			StringBuilder text = new StringBuilder(10000);
			while ((read = ins.read(buf)) != -1) {
				String s = new String(buf, 0, read);
				for (int i = 0; i < read; i++) {
					char c = s.charAt(i);
					if (in_ESC_sequence) { // Not all ANSI codes implemented
						// Ansi codes taken from
						// http://ascii-table.com/ansi-escape-sequences-vt-100.php
						// input += c;
						in_ESC_sequence = false;
						switch (c) {
						// Char after ESC
						case '[':
							in_ESC_sequence = true;
							break;
						// Esc[Line;Columnf Cursor Position
						case 'H':
						case 'f':
							// Esc[ValueA Cursor Up
						case 'A':

							// Esc[ValueB Cursor Down: Moves the cursor down by
							// the specified number of lines without changing
							// columns
						case 'B':
							// "ESC[" + escCodes + c, regularText);
							break;

						// Esc[ValueC Cursor Forward
						case 'C': {
							int l = getESCValue(escCodes);
							for (int k = 0; k < l; k++)
								moveForward();
							break;
						}
							// Esc[ValueD Cursor Backward
						case 'D': {
							int l = getESCValue(escCodes);
							for (int k = 0; k < l; k++)
								moveBackward();
							break;
						}
							// Save Cursor Position.
						case 's':
							// Restore Cursor Position:
						case 'u':
							// Esc[2J Erase Display
							//append("ESC[" + escCodes + c, regularText);
							clear();
							break;
						case 'J':
							int l = getESCValue(escCodes);
							if (l == 2) {
								clear();
							}
							break;
						// Esc[K Erase Line: Clears all characters from the
						// cursor position to the end of the line (including the
						// character at the cursor position).
						case 'K': {
							eraseEndOfLine();
							break;
						}
							// Esc[Value;...;Valuem Set Graphics Mode:
						case 'm':
							// sc[=Valueh Set Mode
						case 'h':
							// Esc[=Valuel Reset Mode
						case 'l':
							// Esc[Code;String;...p Set Keyboard Strings
						case 'p':
							//append("ESC[" + escCodes + c, regularText);
							break;
						default:
							if (Character.isDigit(c) || c == ';') {
								in_ESC_sequence = true;
								escCodes += c;
							}
						}

					} else if (in_BASE64_sequence) {
						escCodes += c;
						if (escCodes.length() == 3) {
							if (escCodes.equals("IMG")) {
								// append("Loading IMG",regularText);
								nfigures++;
								s = loadMedia(ins, s.substring(i + 1), new DisplayImage(), nfigures);
								i = -1;
								read = s.length();
							} else if (escCodes.equals("SND")) {
								nsounds++;
								s = loadMedia(ins, s.substring(i + 1), new PlaySound(), nsounds);
								i = -1;
								read = s.length();
							} else{
								text.append(escCodes);
							}
							escCodes = "";
							in_BASE64_sequence = false;
							continue;
						}
						if (escCodes.length() >= 3) {
							text.append(escCodes);
							escCodes = "";
							in_BASE64_sequence = false;
							continue;
						}
					} else {
						if (c == ESC) { // Start of esc secuence
							// input += "←";
							in_ESC_sequence = true;
							append(text.toString(), regularText);
							text.setLength(0);
							escCodes = "";
						} else if (c == BS) { // Backspace
							// input += "◘";
							backspace();
						} else if (c == BELL) {
							// input += "•";
							java.awt.Toolkit.getDefaultToolkit().beep();
						} else if (c == B64) {
							in_BASE64_sequence = true;
							append(text.toString(), regularText);
							text.setLength(0);
							escCodes = "";
						} else if (!Character.isISOControl(c)
								|| Character.isWhitespace(c)) { // is visible
							text.append(c);
							// if(!isEndLine(c))
							// input +=c;
						} else {
							// input +="("+(int)c+")";
							append(text.toString(), regularText);
							text.setLength(0);
							append("(" + (int) c + ")", regularText);
						}
					}
				}
				append(text.toString(), regularText);
				text.setLength(0);
			}
		}
		catch (SocketException e){
			//Soket closed end of conection
		}
		catch (Exception e) {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			PrintStream ps= new PrintStream(baos);
			e.printStackTrace(ps);ps.flush();
			JOptionPane.showMessageDialog(null, e.getMessage()+"\n"+baos, e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
		}
		disconnect();
		pane.getCaret().setBlinkRate(0);
		setTitle(I18n.getString(I18n.connectionClosed));
		//TODO is needed?
		// Wait for the user to close the window
		while (true) {
			if (!rootFrame.isVisible()) {
				rootFrame = null;
				pane = null;
				connection = null;
				out = null;
				in = null;
				return;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}

	public void setPanel(JTextPane panel) {
		this.pane = panel;
		panel.setBackground(background);
		panel.setCaretColor(foreground);
	}

	public void setRoot(java.awt.Frame raíz) {
		this.rootFrame = raíz;
		raíz.setBackground(background);
	}

	interface Media{
		void init(byte[] data, int id);
	}

	class DisplayImage implements Media{
		public void init(byte[] data, final int id) {
			try {
				// append("DRAWING",regularText);
				final BufferedImage image = ImageIO.read(new ByteArrayInputStream(
						data));
				// append("IMG Width "+image.getWidth()+"  Height"+image.getHeight(),regularText);
				class CopyAction extends AbstractAction {
					private static final long serialVersionUID = -7704944116080608492L;

					{
						putValue(NAME, I18n.getString(I18n.copy));
						putValue(SHORT_DESCRIPTION, I18n.getString(I18n.copyText));
						putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
						putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
					}

					public void actionPerformed(ActionEvent e) {
						if (org.acodeeditor.gui.Main.getJnlpServicesAvailable()) {
							try {
								javax.jnlp.ClipboardService cs = org.acodeeditor.gui.Main
										.getClipboardService();
								if (cs != null) {
									cs.setContents(new ClipboardImage(image));
								}
							} catch (Throwable e1) {
							}
						} else {
							try {
								Clipboard clipboard = Toolkit.getDefaultToolkit()
										.getSystemClipboard();
								if (clipboard != null)
									clipboard.setContents(
											new ClipboardImage(image), null);
							} catch (Throwable e1) {
							}
						}
					}
				}

				class SaveAsAction extends AbstractAction {
					private static final long serialVersionUID = -1106836587795149793L;

					{
						putValue(NAME, I18n.getString(I18n.save));
						putValue(SHORT_DESCRIPTION, I18n.getString(I18n.save));
						putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl S"));
						putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
					}

					public void actionPerformed(ActionEvent e) {
						if (org.acodeeditor.gui.Main.getFileSaveService()!=null) {
							try {
								javax.jnlp.FileSaveService fss = org.acodeeditor.gui.Main
										.getFileSaveService();
								if (fss != null) {
									final String fn=I18n.getString(I18n.figure)+"_" + identification
									+ "_" + id;
									final String[] exts={"png"};
									ByteArrayOutputStream os= new ByteArrayOutputStream();
									ImageIO.write(image,"png",os);
									byte[] data=os.toByteArray();
									ByteArrayInputStream is= new ByteArrayInputStream(data);
									fss.saveFileDialog("",exts,is,fn);
								}
							} catch (Throwable e1) {}
						}
					}
				}

				final JFrame frame = new JFrame();
				final JPanel panel = new JPanel() {
					private static final long serialVersionUID = 7980651572817731500L;
					private BufferedImage originalImage = image;
					private Image scaled = image;

					public void paint(Graphics g) {
						Insets insets = frame.getInsets();
						int width = (frame.getWidth() - insets.left) - insets.right;
						int height = (int) ((float) width
								/ originalImage.getWidth() * originalImage
								.getHeight());
						if (scaled.getWidth(null) != width) {
							scaled = originalImage.getScaledInstance(width, height,
									Image.SCALE_SMOOTH);
							frame.setSize(width + insets.left + insets.right,
									height + insets.top + insets.bottom);
							setSize(width, height);
						} else if (height + insets.top + insets.bottom != frame
								.getHeight()) {
							frame.setSize(width + insets.left + insets.right,
									height + insets.top + insets.bottom);
						}
						g.drawImage(scaled, 0, 0, null);
						if (Runtime.getRuntime().freeMemory() < 2000000) {// 2Mb
							System.gc();
							try {
								wait(100);
							} catch (InterruptedException e) {
							}
							if (Runtime.getRuntime().freeMemory() < 2000000) {
								JOptionPane
										.showMessageDialog(
												this,
												"Memory low",
												"Memory low: Try closing some applet window",
												JOptionPane.WARNING_MESSAGE);
							}
						}
					}
				};
				frame.setContentPane(panel);
				final CopyAction copyAction = new CopyAction();
				final SaveAsAction saveAction = new SaveAsAction();
				
				panel.setFocusable(true);
				panel.getInputMap().put(KeyStroke.getKeyStroke("ctrl C"),I18n.copy);
				panel.getInputMap().put(KeyStroke.getKeyStroke("ctrl INSERT"),I18n.copy);
				panel.getActionMap().put(I18n.copy, copyAction);
				final JPopupMenu popup = new JPopupMenu();
				popup.add(copyAction);
				if(Main.getFileSaveService()!=null){
					popup.add(saveAction);
					panel.getInputMap().put(KeyStroke.getKeyStroke("ctrl S"),I18n.save);
					panel.getActionMap().put(I18n.save, saveAction);
				}
				panel.setComponentPopupMenu(popup);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setTitle(I18n.getString(I18n.figure) + " " + identification
						+ " - " + id);
				int width = rootFrame.getWidth();
				int height = (int) ((float) width / image.getWidth() * image
						.getHeight());
				frame.setSize(width, height);
				frame.setLocationRelativeTo(rootFrame);
				int offset=(id%10)*20;
				frame.setLocation(rootFrame.getX()+offset,rootFrame.getY()+offset);
				frame.setVisible(true);
			} catch (Throwable e) {
				JOptionPane
				.showMessageDialog(
						null,e,
						"Exception",
						JOptionPane.WARNING_MESSAGE);
			}
		}
		
	}

	class PlaySound implements Media, Runnable{
		Clip clip;
		JFrame frame;
		JButton start,stop;
		JSlider slider;
		private synchronized void updateState(){
			if(clip.getMicrosecondPosition()>=clip.getMicrosecondLength()){
				clip.stop();
				clip.setMicrosecondPosition(0);
				slider.setValue(slider.getMinimum());
			}else{
				if(stop.isEnabled()){
					int srange=slider.getMaximum()-slider.getMinimum();
					int sposition=slider.getMinimum()+(int)((clip.getMicrosecondPosition()*srange)/clip.getMicrosecondLength());
					if(slider.getValue() != sposition){
						slider.setValue(sposition);
					}
				}
			}
			boolean active=clip.isActive();
			if(start.isEnabled() == active){
				start.setEnabled(!active);
			}
			if(stop.isEnabled() != active){
				stop.setEnabled(active);
			}
		}
		public void run(){
			while(slider.isEnabled()){
				try{
					Thread.sleep(50);
					updateState();
				}catch (InterruptedException e) {
					e.printStackTrace(System.err);
					break;
				}
			}
		}
		public void init(byte[] data, final int id) {
			final int sliderMax=1000;
			final long second=1000000;
			final long mminute=60*second;
			final long mhour=60*second;
			try {
				InputStream inputStream=new ByteArrayInputStream(data);
				AudioInputStream audioInputStream=AudioSystem.getAudioInputStream(inputStream);
				clip=AudioSystem.getClip();
				clip.open(audioInputStream);						
				frame = new JFrame();
				JPanel panel = new JPanel();
				BufferedImage playImage=new BufferedImage(30, 30,BufferedImage.TYPE_INT_ARGB);
				BufferedImage stopImage = new BufferedImage(30, 30,BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d= playImage.createGraphics();
				g2d.setBackground(new Color(0,0,0,0));
				g2d.clearRect(0, 0, 30, 30);
				g2d.setPaint(Color.black);
				int[] xtriangle={5,25,5};
				int[] ytriangle={5,15,25};
				g2d.fillPolygon(xtriangle, ytriangle, 3);
				start= new JButton("",new ImageIcon(playImage));
				start.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						clip.start();						
					}
				});
				g2d= stopImage.createGraphics();
				g2d.setBackground(new Color(0,0,0,0));
				g2d.clearRect(0, 0, 30, 30);
				g2d.setPaint(Color.black);
				int[] xrec1={5,13,13, 5};
				int[] yrec1={5, 5,25,25};
				g2d.fillPolygon(xrec1, yrec1, 4);
				int[] xrec2={17,25,25, 17};
				int[] yrec2={5, 5,25,25};
				g2d.fillPolygon(xrec2, yrec2, 4);
				stop= new JButton("",new ImageIcon(stopImage));
				stop.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						clip.stop();						
					}
				});
				slider = new JSlider();
				slider.addChangeListener(new ChangeListener(){
					public void stateChanged(ChangeEvent e) {
						if(start.isEnabled()){
							int position=slider.getValue();
							int sliderRange=slider.getMaximum()-slider.getMinimum();
							float offset=(float)(position-slider.getMinimum())/sliderRange;
							long microseconds=(long)(offset*clip.getMicrosecondLength());
							clip.setMicrosecondPosition(microseconds);
						}
					}
				});
				slider.setValue(0);
				slider.setMinimum(0);
				slider.setMaximum(sliderMax);
				String tt="";
				final int seconds=(int)(clip.getMicrosecondLength()/second);
				int multi=1,unit=0;
				int majorTiks=0;
				if(clip.getMicrosecondLength() < second){ //< second
					unit=(int)((clip.getMicrosecondLength()*10)/second); // deciseconds
					multi=1;
					tt="ds";
				}else if(clip.getMicrosecondLength() < mminute){ //< minute
					unit=seconds;
					tt="s";
				}else if(clip.getMicrosecondLength()<mhour){//< hour
					unit=seconds/60;
					tt="m";
				}else{ //> hour
					unit=(seconds/60)/60;
					tt="m";
				}
				if(unit>0){
					while(unit/multi > 10) multi*=2;										
					majorTiks=sliderMax/(unit/multi);					
					Dictionary<Integer, JLabel> labels= new Hashtable<Integer,JLabel>();
					int key=0;
					for(int i=0; i<=sliderMax; i+=majorTiks, key+=multi){
						labels.put(i, new JLabel(key+tt));
					}
					slider.setMajorTickSpacing(majorTiks);
					slider.setPaintTicks(true);
					slider.setLabelTable(labels);
					slider.setPaintLabels(true);
				}
				panel.add(start);
				panel.add(stop);
				panel.add(slider);
				//Stop the clip if we close the window
				frame.addWindowListener(new WindowListener(){
					public void windowActivated(WindowEvent e) {}
					public void windowClosed(WindowEvent e) {clip.stop();slider.setEnabled(false);}
					public void windowClosing(WindowEvent e) {}
					public void windowDeactivated(WindowEvent e) {}
					public void windowDeiconified(WindowEvent e) {}
					public void windowIconified(WindowEvent e) {}
					public void windowOpened(WindowEvent e) {}				
				});
				frame.setContentPane(panel);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setTitle(I18n.getString(I18n.sound) + " " + identification
						+ " - " + id);
				frame.pack();
				frame.setLocationRelativeTo(rootFrame);
				int offset=(id%10)*20;
				frame.setLocation(rootFrame.getX()+offset,rootFrame.getY()+offset);
				frame.setResizable(false);
				frame.setVisible(true);
				clip.start();
				new Thread(this).start();
			} catch (Throwable e) {
				JOptionPane.showMessageDialog(rootFrame, e.getMessage());
			}
		}
		
	}

	
	String loadMedia(InputStreamReader in, String rest, Media media, int id) {
		// append("\nSTART LOAD\n", regularText);
		int read = -1;
		char[] readbuf = new char[100000];
		char[] buf;
		StringBuilder data = new StringBuilder(100000);
		String text = "";
		try {
			reading: do {
				int len;
				if (read == -1) {
					buf = rest.toCharArray();
					len = buf.length;
				} else {
					buf = readbuf;
					len = read;
				}
				for (int i = 0; i < len; i++) {
					char c = buf[i];
					if (c == B64) {
						text = new String(buf, i + 1, len - (i + 1));
						data.append(buf, 0, i);
						break reading;
					}
				}
				data.append(buf, 0, len);
			} while ((read = in.read(readbuf)) != -1);
		} catch (Exception e) {
			append(e.getMessage(), regularText);
		}
		// append("LOADED "+data.length()+"\n", regularText);
		byte[] decodedData = null;
		try {
			decodedData = org.acodeeditor.util.Base64.decode(data.toString());
		} catch (Exception e) {
			append("Decode error " + e.getMessage(), regularText);
			return text;
		}
		if (decodedData != null) {
			// append("DECODED "+decodedData.length+"\n", regularText);
			media.init(decodedData, id);
		} else
			append(data + "\nError decoding image\n", regularText);
		return text;
	}
}

public class Console {
	private static int offset = 0; // Offset to place console at diferent
	// location
	final ConsoleDoc inner;

	public Console(final String host, final int port, final String key,
			Highlights highlights, JApplet parent) {
		inner = new ConsoleDoc(highlights);
		JTextPane tp = new JTextPane();
		tp.setDocument(inner);
		inner.setPanel(tp);
		JFrame console = new JFrame();
		inner.setRoot(console);
		console.setTitle(I18n.getString(I18n.console));
		int width = highlights.getCharSize() * 80 + 20; // 80 cols + border size
		int height = highlights.getCharHeight() * 24 + 20; // 24 lines + border
		// size
		try {
			Point start = parent.getLocationOnScreen();
			console.setLocation(offset + start.x
					+ (parent.getSize().width - width) / 2, offset + start.y
					+ (parent.getSize().height - height) / 2);
			offset = (offset + 16) % 160; // Update offset
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		console.setSize(new Dimension(width, height));
		console.setContentPane(new JScrollPane(tp));
		console.setVisible(true);
		console.toFront();
		new Thread(new Runnable() {
			public void run() {
				inner.connect(host, port, key);
			}
		}).start();
		tp.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg) {
				String toSend = null;
				switch (arg.getKeyCode()) { // We send vt100 arrowkey codes
				case KeyEvent.VK_UP:
					toSend = "\u001bA";
					break;
				case KeyEvent.VK_DOWN:
					toSend = "\u001bB";
					break;
				case KeyEvent.VK_RIGHT:
					toSend = "\u001bC";
					break;
				case KeyEvent.VK_LEFT:
					toSend = "\u001bD";
					break;
				default:
				}
				if (toSend != null) {
					inner.send(toSend);
				}
			}

			public void keyReleased(KeyEvent arg) {
			}

			public void keyTyped(KeyEvent arg) {
				if (arg.isControlDown()) {
					inner.sendControl(arg.getKeyChar());
				}
			}
		});
		console.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		console.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				if (e.getComponent() != null
						&& e.getComponent().getClass() == JFrame.class) {
					JFrame console = (JFrame) e.getComponent();
					console.setVisible(false);
				}
				inner.disconnect();
			}
		});
	}
}

class ClipboardImage implements Transferable {
	private Image image;

	public ClipboardImage(Image image) {
		this.image = image;
	}

	// Returns supported flavors
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	// Returns true if flavor is supported
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return DataFlavor.imageFlavor.equals(flavor);
	}

	// Returns image
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (!DataFlavor.imageFlavor.equals(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return image;
	}
}
