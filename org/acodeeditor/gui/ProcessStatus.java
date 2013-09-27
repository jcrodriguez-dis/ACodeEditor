/**
 * $Id: ProcessStatus.java,v 1.3 2013-06-07 15:07:20 juanca Exp $
 * gui.* is part of ACodeEditor
 * Copyright (C) 2009 Juan Carlos Rodr�guez-del-Pino. All rights reserved.
 * license GNU/GPL, see LICENSE.txt or http://www.gnu.org/licenses/gpl-3.0.html
 * @author Juan Carlos Rodriguez-del-Pino
 **/
package org.acodeeditor.gui;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JProgressBar;

public class ProcessStatus {
	private JLabel processLabel=null;
	private JProgressBar  processBar=null;
	private TimeoutChecker processTimer= null;
	private boolean processAlive=false;
	class TimeoutChecker extends Thread{
		protected int every;
		protected volatile boolean stop;
		public TimeoutChecker(int every){
			this.every=every;
			this.stop=false;
		}
		public void cancel(){
			stop=true;
		}
		@Override
		public void run() {
			while(true){
				try {
					Thread.sleep(every*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					stop=true;
				}
				synchronized (processBar) {
					if(stop){
						break;
					}
					if(!processAlive){
						processLabel.setText(processLabel.getText()+" (Timeout)");
						processBar.setIndeterminate(false);
						processBar.setVisible(false);
						break;
					}
					processAlive=false;
				}
			}
		}
		
	}
	class RemoveChecker extends TimeoutChecker{
		public RemoveChecker(int every){
			super(every);
		}
		@Override
		public void run() {
			try {
				Thread.sleep(every*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (processBar) {
				stop=true;
				processLabel.setText("");
				processBar.setVisible(false);
				processBar.setIndeterminate(false);
				processAlive=false;
			}
		}
		
	}

	/**
	 * Add the statusBar to the menu
	 * @param JMenuBar jmb
	 */
	public ProcessStatus(JMenuBar menuBar){
		processLabel  = new JLabel();
		processLabel.setVisible(false);
		//TODO use constants 
		processBar = new JProgressBar(1,10);
		Dimension size=new Dimension(50,15);
		processBar.setMaximumSize(size);
		processBar.setSize(size);
		processBar.setVisible(false);
		menuBar.add(processLabel);
		menuBar.add(processBar);
	}
	
	/**
	 * Start status bar process
	 * @param String text process text
	 */
	public void start(String text){
		synchronized (processBar) {
			if(processTimer!=null){
				processTimer.cancel();
				processTimer=null;
			}
			processLabel.setText(" "+text+" ");
			processLabel.setVisible(true);
			processBar.setValue(1);
			processBar.setIndeterminate(true);
			processBar.setVisible(true);
			processAlive= false;
			//Start timer to check timeout
			processTimer= new TimeoutChecker(15);
			processTimer.start();
		}
	}

	/**
	 * Keep alive state bar
	 */
	public void update(){
		synchronized (processBar) {
			processAlive = true;
		}
	}
	
	/**
	 * Clear the state bar info (deprecated)
	 */
	public void end(){
		synchronized (processBar) {
			if(processTimer!=null){
				processTimer.cancel();
				processTimer=null;
			}			
			processAlive=false;
			processLabel.setVisible(false);
			processBar.setVisible(false);
		}
	}

	/**
	 * Set the state bar text, stop processBar, set time out
	 * @param text String text to set
	 */
	public void end(String text){
		synchronized (processBar) {
			if(processTimer!=null){
				processTimer.cancel();
				processTimer=null;
			}
			processTimer = new RemoveChecker(30); //TODO 30?
			processLabel.setText(" "+text+" ");
			processLabel.setVisible(true);
			processBar.setIndeterminate(false);
			processBar.setVisible(false);
			processAlive=false;
			processTimer.start();
		}
	}

}
