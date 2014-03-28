/*
 Copyright (c) 2014, Rodolphe LOUIS-SIDNEY
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

package com.mentorgen.tools.profile.remote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
/**
 * Implementation of a simple data exchange protocol
 * A network frame is : <command> \r <data length> \r <data>
 * @author Rodolphe Louis-Sidney
 *
 */
public class DataSocket {
	private String _command = null;
	private byte [] _data = null;
	
	public DataSocket() {
	}
	/**
	 * get the last command received 
	 * @return String
	 */
    public String getCommand() {
    	return  _command;
    }
    /**
     * get the last data received
     * @return byte []
     */
    public byte [] getData() {
    	return _data;
    }
    
	 /**
	    * Send a frame <name> \r <data length> \r <data>
	    * @param socket
	    * @param data
	    * @throws IOException
	    */
	public void send (Socket socket, String command, byte[] data) throws IOException {
	   	   BufferedOutputStream out = new BufferedOutputStream (socket.getOutputStream());
	   	   out.write(command.getBytes());
	   	   out.write('\r');
	   	   if (data!=null && data.length>0) {
	   		   out.write(String.valueOf(data.length).getBytes());
	   		   out.write('\r');
	   		   out.write(data, 0, data.length);
	   	   } else {
	   		  out.write('0');
	   		  out.write('\r');
	   	   } 
		   out.flush();
	}
   /**
    * Receive a frame
    * The data should be retrieve by getCommand and getData 
    * @param socket
    * @throws IOException
    */
   public void receive(Socket socket) throws IOException {
	   _data=null;
	   _command=null;
       socket.setSoTimeout(5000); 
   	   BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
   	   String command = readString(in);
   	   Integer rLength = Integer.valueOf(readString(in));
       byte [] data = new byte[rLength];
       int nRead=in.read(data,0,rLength);
       int current = nRead;
       while (nRead!=-1 && current<rLength) {
                  nRead = in.read(data, current, (rLength-current));
                  if(nRead >= 0) current += nRead;
       } 
       _command = command;
       _data = data;
    }
    
    private String readString(BufferedInputStream in) throws IOException {
      StringBuffer b = new StringBuffer();
  	  while (true) {
  		int r = in.read();
  		char c = (char) r;		
  		if (c == '\r' || r==-1) {
  					 break;
  		} else {
  					 b.append(c);
  		}
  	  }
  	  return b.toString();
    }
    /**
     * Send a command and data
     * @param command
     * @param data
     * @param server
     * @param port
     * @throws IOException
     */
    public void send(String command, byte[] data, String server, int port) throws IOException {
        Socket socket = new Socket(server, port);
        send(socket, command, data);
        socket.close();
   }
   
    /**
     *  Send a command and read the response
     * @param command
     * @param data
     * @param server
     * @param port
     * @throws IOException
     */
   public void sendReceive(String command, byte[] data, String server, int port) throws IOException {
        Socket socket = new Socket(server, port);
        send(socket, command, data);
        receive(socket);
        socket.close();
   }
    
}
