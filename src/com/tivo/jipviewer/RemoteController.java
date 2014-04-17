/*/////////////////////////////////////////////////////////////////////

Copyright (C) 2006 TiVo Inc.  All rights reserved.

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions are met:

+ Redistributions of source code must retain the above copyright notice, 
  this list of conditions and the following disclaimer.
+ Redistributions in binary form must reproduce the above copyright notice, 
  this list of conditions and the following disclaimer in the documentation 
  and/or other materials provided with the distribution.
+ Neither the name of TiVo Inc nor the names of its contributors may be 
  used to endorse or promote products derived from this software without 
  specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  POSSIBILITY OF SUCH DAMAGE.

/////////////////////////////////////////////////////////////////////*/

package com.tivo.jipviewer;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import com.mentorgen.tools.profile.remote.Commands;
import com.mentorgen.tools.profile.remote.DataSocket;

class RemoteController extends Container implements Commands, ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4301398484251463634L;
	
	JTextField mHostname;
    SpinnerNumberModel mPortModel;
    JTextField mState;
    JButton    mButtonExecCommand;
    JComboBox  mComboCommand;
    JTextField mDataToSend;
    JTextArea  mResultCommand;
    JButton    mButton;
    JButton    mButtonGetDump;
    JButton    mButtonStatus;
    
    private static String listOfcommand [] =
    	{START,STOP,FILE,CLEAR,FINISH,STATUS, GETXMLDUMP,DEBUGON,DEBUGOFF,
    	GETEXCLUDELIST,REPLACEEXCLUDELIST,GETINCLUDELIST,REPLACEINCLUDELIST,
    	GETCLASSLOADERSBYNAME,REPLACECLASSLOADERSBYNAME};

    RemoteController() {
        mPortModel = new SpinnerNumberModel(15599, 0, 65355, 1);
        JSpinner portSpinner = new JSpinner(mPortModel);
        
        mHostname = makeTextInput("localhost");
        mState    = makeTextOutput("state");
        mButtonStatus = makeButton("state");
        mButton   = makeButton("start/stop");
        mButtonGetDump = makeButton("data");
        mButtonExecCommand = makeButton("exec");
       
        Container pair = makePair(makeLabel("server hostname"), mHostname);
        add(pair);
        
        pair = makePair(makeLabel("server port"), portSpinner);
        add(pair);

        pair = makePair(makeLabel("state"), mState);
        add(pair);
 
        Container three = makeThree(mButtonStatus,mButton, mButtonGetDump);
        add(three);
        
        mComboCommand = makeComboBox(listOfcommand,2);
        pair = makePair(mComboCommand,mButtonExecCommand);
        add(pair);
        
        mDataToSend = makeTextInput("");
        pair = makePair(makeLabel("param "), mDataToSend);
        add(pair);
        
        mResultCommand = makeTextArea("",50,150);
        pair = makePair(makeLabel("result"), new JScrollPane(mResultCommand));
        add(pair);
            	
        updateState();
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    private void updateState() {
    	
        String state = "unknown";
        String buttonLabel = "start";
        String hostname = mHostname.getText();
        int port = mPortModel.getNumber().intValue();
        DataSocket rd = sendReceive(STATUS,hostname, port);
      
        if (rd!=null) {  
          String remoteState=new String(rd.getData());
          if (remoteState.equals("start")) {
        	   state = "running";
        	   buttonLabel = "stop";
        	   
          }
          if (remoteState.equals("stop")) {
        	state = "stopped";
            buttonLabel = "start";
          }
        }
        
        mState.setText(state);
        mButton.setText(buttonLabel);

        String btnCmd = buttonLabel;
        mButton.setActionCommand(btnCmd);
    }
    
    public void newJipViewer(){
    	    String hostname = mHostname.getText();
            int port = mPortModel.getNumber().intValue();
    	    DataSocket rd = sendReceive(GETXMLDUMP,hostname, port);
    	    if (rd==null) {
    	    	  String msg = ("Trouble sending '" + GETXMLDUMP + "' to " + hostname +":" + port + "\n");
    	    	  printError(msg);
        		 return;
        	}
    	    if (rd.getData().length == 0) {
   	    	 String msg = ("No data from command '" + GETXMLDUMP 
   	    			       + "' to " + hostname +":" + port + "\n"
   	    			       +" "+rd.getCommand());
             printError(msg);
       		 return;
       	    } 
    	    
        	try {
        		String title = hostname+":"+rd.getCommand();
				JipViewer.createNewViewer(title, rd.getData());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("start")) {
            startProfiler();
            return;
        }
        if (cmd.equals("stop")) {
            stopProfiler();
            return;
        }
        if (cmd.equals("state")) {
        	updateState();
        	return;
        } 
        if (cmd.equals("data") ){
        	 newJipViewer();
        	return;
        }
        
        if (cmd.equals("exec")) {
            execRemoteCommand();
        	return;
        }        
        throw new RuntimeException("unexpected button cmd ("+cmd+")");
    }
    
    void execRemoteCommand() {
    	 String hostname = mHostname.getText();
         int port = mPortModel.getNumber().intValue();
    	 String command = (String) mComboCommand.getSelectedItem();
    	 String param = mDataToSend.getText();
    	 if (param!=null) {
    		 param = param.trim();
    		 if (param.length()==0) {
    			 param=null;
    		 }
    	 }
    	
    	 try {    	  	 
    		   DataSocket dataSocket = new DataSocket();	   
 		       if (START.equals(command) || FINISH.equals(command) 
 			       || DEBUGON.equals(command) || DEBUGOFF.equals(command)
 			       || CLEAR.equals(command)) {
 			       dataSocket.send(command,null, hostname, port);
 			       return;
 		       } 
 		       if (GETXMLDUMP.equals(command)) {
 	               dataSocket.sendReceive(GETXMLDUMP,null, hostname, port);
 	              if (dataSocket.getData()!=null) {
 	            	  mResultCommand.setText(new String(dataSocket.getData(),"UTF-8"));
 	              }
 	              return;
 		       }
 		       if (STATUS.equals(command)) {
 	               dataSocket.sendReceive(STATUS,null, hostname, port);
 	               if (dataSocket.getData()!=null) {
 	                  mResultCommand.setText(new String(dataSocket.getData()));
 	                  updateState();
 	               }
 	               return;
 		       } 
 		       if ((GETCLASSLOADERSBYNAME.equals(command) ||
 		         GETINCLUDELIST.equals(command) ||
 		         GETEXCLUDELIST.equals(command))) {
 	             dataSocket.sendReceive(command,null, hostname,port);
 	             if (dataSocket.getData()!=null) {
 	               mResultCommand.setText(new String(dataSocket.getData()));
 	             }
 	             return;
 		       } 
 		       if ((REPLACEEXCLUDELIST.equals(command) || 
 			       REPLACEINCLUDELIST.equals(command) || 
 			       REPLACECLASSLOADERSBYNAME.equals(command) ||
 			       FILE.equals(command) ) && param!=null) {
 			       dataSocket.send(command, param.getBytes(), hostname, port);
 		           return;
 		       }	
    	 } catch (IOException e) {
    		 printConnexionError(command, hostname, port, e);
    	 }		 
    }

    void startProfiler() {
    	stProfiler(START);
    }
    
    void stopProfiler() {
        stProfiler(FINISH);
    }
    
    void stProfiler (String command) {
    	String hostname = mHostname.getText();
        int port = mPortModel.getNumber().intValue();
        if (send(command, hostname, port)) {
        	sleepTimeMs(1000);
            updateState();
        }
    }
    
    void sleepTimeMs(long ms) {
    	try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// do Nothing;
		}
    }

    /**
     * sends the given message to the given server:port.
     * returns true if there were no *detectable* errors.
     * (note that this doesn't mean there weren't errors!)
     */
    static boolean send(String command, String server, int port) {
        DataSocket dataSocket = new DataSocket();
    	try {
            dataSocket.send(command, null, server, port);
            return true;
        } catch (IOException e) {
            printConnexionError(command, server, port, e);
            return false;
        }
    }
    
    static DataSocket sendReceive(String command, String server, int port){
    	  DataSocket dataSocket = new DataSocket();
    	  try {
    		   dataSocket.sendReceive(command, null, server, port);
               return dataSocket;
          } catch (IOException e) {
              return null;
          } 
    }
    
    private static void printConnexionError(String command, String server, int port, Exception e) {
    	String msg = ("Trouble sending '" + command + "' to " + server +
                ":" + port + ":\n" + e +"\n");
        printError(msg);
    }
    
    private static void printError(String msg) {
    	String title = "error";
        JOptionPane.showMessageDialog(null, msg, title,JOptionPane.ERROR_MESSAGE);
    }
    
    private Container makePair(Component left, Component right) {
        Container pair = new Container();
        pair.add(left);
        pair.add(makeLabel("   "));  // inelegant spacer!
        pair.add(right);
        pair.setLayout(new BoxLayout(pair, BoxLayout.X_AXIS));
        return pair;
    }
    
    private Container makeThree(Component left, Component center,Component right) {
        Container three = new Container();
        three.add(left);
        three.add(makeLabel("   "));  // inelegant spacer!
        three.add(center);
        three.add(makeLabel("   "));  // inelegant spacer!
        three.add(right);
        three.setLayout(new BoxLayout(three, BoxLayout.X_AXIS));
        return three;
    }

    private JButton makeButton(String text) {
        JButton button = new JButton(text);
        button.addActionListener(this);
        return button;
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        return label;
    }
    
    private JComboBox makeComboBox (String listOfcommand[], int pos) {
    	JComboBox comboBox = new JComboBox(listOfcommand);
        comboBox.setSelectedIndex(pos);
        return comboBox;
    }

    private JTextField makeTextInput(String text) {
        JTextField field = new JTextField(text);
        return field;
    }

    private JTextField makeTextOutput(String text) {
        JTextField field = new JTextField(text);
        field.setEditable(false);
        return field;
    }
    
    private JTextArea makeTextArea(String text, int rows, int columns) {
        JTextArea field = new JTextArea(text,rows,columns);
        field.setEditable(false);   
        return field;
    }
}
