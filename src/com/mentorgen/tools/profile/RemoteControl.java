/*
 Copyright (c) 2014
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 + Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.

 + Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

 + Neither the name of the project's author nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.mentorgen.tools.profile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.mentorgen.tools.profile.output.ProfileDump;
import com.mentorgen.tools.profile.remote.Commands;
import com.mentorgen.tools.profile.remote.DataSocket;
import com.mentorgen.tools.profile.runtime.Profile;

/**
 * A network remote control
 * @author Rodolphe Louis-Sidney
 *
 */
public class RemoteControl extends Thread implements Commands {
    private Controller _controller=null;
    private int _port;
    private ServerSocket _socket=null;
	
    @SuppressWarnings("unused")
	private RemoteControl() {
	}
	
    public RemoteControl(Controller controller, int port) {
		_controller = controller;
		_port = port;
		start();
	}
    
    //
	// Thread to open a server socket and listen for commands
	//
    
	public void run() {
		try {
			_socket = new ServerSocket(_port);		
			System.out.println("Profiler remote starting, listen  port : " + String.valueOf(_port));
			System.out.println("------------------");		
			while (true) {
				// Go !
				new HandleSocket(_socket.accept()).start();
			}
		} catch (SocketException e) {
			// eat this type of exception ...
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	//
	// Thread support multi server connections
	//
	
	public class HandleSocket extends Thread {
		private Socket childSocket = null;
	    public HandleSocket(Socket childSocket) {
	    	super("HandleSocket");
	        this.childSocket = childSocket;
	    }
		public void run() {
		      try {
		    	   execCommands(childSocket);
				   childSocket.close();
		         } catch (SocketException e) {
					// eat this type of exception ...
				 } catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				 }
	   }
	}
	
    public void close() throws IOException {
		System.out.println("Remote control -- shuttingdown");
		if (_socket!=null && !_socket.isClosed()) {
			_socket.close();
		}
			interrupt();
	}
    
    public void execCommands(Socket childSocket) throws IOException {
 	     
    	 DataSocket dataSocket = new DataSocket();
    	 
    	 dataSocket.receive(childSocket);
    	 
    	 String command = dataSocket.getCommand();
    	 
    	 if (Controller._debug) {
 	                System.out.println("Profiler remote command : "+command);
 	      }
 	      if (command.startsWith(START)) {
 			  _controller.start();
 			  return;
 	      } 
 	      
 	      if (command.startsWith(STOP)) {
 			  _controller.stop();
 			  return;
 	      } 
 	      
 	      if (command.startsWith(DUMP)) {
 		     ProfileDump.dump();
 			 return;		
 		  }
 	      
 	      if (command.startsWith(CLEAR)) {
 			  Profile.init();
 			  return;
 		  } 
 	      
 	      if (command.startsWith(FILE)) {
 				 Controller._fileName = new String(dataSocket.getData());
 				 return;
           }
 	      
 	      if (command.startsWith(GETCLASSLOADERSBYNAME)) {
               byte [] result = _controller.convertArrayToString(Controller._acceptClassLoadersByName).getBytes();
 	    	   dataSocket.send(childSocket, GETCLASSLOADERSBYNAME,result);	 
               return;
           }
 	      
 	      if (command.startsWith(REPLACECLASSLOADERSBYNAME)) {
             if (Controller._profile == true) {
               _controller.stop();
               ProfileDump.dump();
             }
             String NewClassesByName = new String(dataSocket.getData());
             Controller._acceptClassLoadersByName = Controller.parseList(NewClassesByName,false);
             if (Controller._debug){
                System.out.print("Remote new class loaders by name: ");
                System.out.println(_controller.convertArrayToString(Controller._acceptClassLoadersByName));
             }
             return;
          }
 	      
 	      if (command.startsWith(GETEXCLUDELIST)) {
 	    	  byte [] result = _controller.convertArrayToString(Controller._excludeList).getBytes();
              dataSocket.send(childSocket, GETEXCLUDELIST,result);
              return;
          }
 	      
 	      if (command.startsWith(REPLACEEXCLUDELIST)) {
             if (Controller._profile == true) {
               _controller.stop();
               ProfileDump.dump();
             }
             String TheExcludes =  new String(dataSocket.getData());
             Controller._excludeList = Controller.parseList(TheExcludes, true);
             if (Controller._debug){
                System.out.print("Remote new exclude list: ");
                System.out.println(Controller._excludeList);
             }
             Profile.init();
          } 
 	      
 	      if (command.startsWith(GETINCLUDELIST)) { 
              byte [] result = _controller.convertArrayToString(Controller._includeList).getBytes();
              dataSocket.send(childSocket, GETINCLUDELIST,result);
              return;
 	      }
 	      
 	     if (command.startsWith(REPLACEINCLUDELIST)) {
             if (Controller._profile == true) {
               _controller.stop();
               ProfileDump.dump();
             }
             String TheIncludes = new String(dataSocket.getData());
             Controller._includeList = Controller.parseList(TheIncludes, true);
             if (Controller._debug){
                System.out.print("Remote new include list: ");
                System.out.println(Controller._includeList);
             }
             Profile.init();
             return;
 	     }
 	       	      
 	     if (command.startsWith(DEBUGON)) {
 	    	   Controller._debug = true;
               return;
          }
 	     
 	     if (command.startsWith(DEBUGOFF)) {
 	    	Controller._debug = false;
               return;
 		 }
 	     
 	     if (command.startsWith(FINISH)) {
 		       _controller.stop();              // stop
 			   ProfileDump.dump();	// dump
 			   Profile.init();		// clear
 			   return;
 		  }
 	     
 	      if (command.startsWith(STATUS)) {
 		        dataSocket.send(childSocket, STATUS,(Controller._profile ? START.getBytes():STOP.getBytes()));
 		        return;
 		  }	
 	      
 	      if (command.startsWith(GETXMLDUMP)) {
 	    	    if (ProfileDump.isDumping){
 	    		 dataSocket.send(childSocket,GETXMLDUMP + " : still dumping data", new String().getBytes());  
 	    		 return;
 	    	    }
 	    	    String xmlFileName = ProfileDump.getLastXMLFileName();  
 	    	    File myFile = null;
 			    if (xmlFileName!=null) {
 			    	myFile=new File(xmlFileName);
 			    } 
 			    if (myFile!=null && myFile.exists() && myFile.isFile()) {
 			    	  byte[] mybytearray = new byte[(int) myFile.length()];
 			          BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
 			          bis.read(mybytearray, 0, mybytearray.length);
 			          dataSocket.send(childSocket,xmlFileName, mybytearray);
 			          bis.close();
 			    } else {
 			    	  dataSocket.send(childSocket,GETXMLDUMP + " : file does not exist", new String().getBytes());  
 			    }
 	      }
     }
}
