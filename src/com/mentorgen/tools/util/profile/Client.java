/*
Copyright (c) 2005, MentorGen, LLC
All rights reserved.

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions are met:

+ Redistributions of source code must retain the above copyright notice, 
  this list of conditions and the following disclaimer.
+ Redistributions in binary form must reproduce the above copyright notice, 
  this list of conditions and the following disclaimer in the documentation 
  and/or other materials provided with the distribution.
+ Neither the name of MentorGen LLC nor the names of its contributors may be 
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
 */

package com.mentorgen.tools.util.profile;

import com.mentorgen.tools.profile.remote.Commands;
import com.mentorgen.tools.profile.remote.DataSocket;

public class Client implements Commands {
	
	static void help() {
		System.out.println("use: java -jar client.jar <cmd> <server> <port> [file-name]  [data-list]");
		System.out.println("where <cmd> is:");
		System.out.println("file\tstart\tfinish\tdebugon\tstatus\tgetxmldump\tdebugoff\tgetexcludelist\treplaceexcludelist");
		System.out.println("getincludelist\treplaceincludelist\tgetclassloadersbyname\treplaceclassloadersbyname");
		System.out.println("[file-name] is only used by the file command");
        System.out.println("[data-list] is only used by the replace include/exclude/classloadersbyname list commands");
        System.out.println(" ");
	}

	public static void main(String[] args) throws Exception {		
		if (args.length == 0) {
			help();
			return;
		} 
		DataSocket dataSocket = new DataSocket();
		
		if (FILE.equals(args[0]) && args.length == 4) {
			dataSocket.send(FILE,args[3].getBytes(),args[1],  Integer.parseInt(args[2]));
			return;
		} 
		
		if (START.equals(args[0]) ||
			FINISH.equals(args[0]) ||
			DEBUGON.equals(args[0]) || 
			DEBUGOFF.equals(args[0]) && args.length == 3) {
			dataSocket.send(args[0],null, args[1], Integer.parseInt(args[2]));
			return;
		} 

		if (GETXMLDUMP.equals(args[0]) && args.length == 3) {
	        dataSocket.sendReceive(GETXMLDUMP,null, args[1], Integer.parseInt(args[2]));
	        System.out.println(new String(dataSocket.getData(),"UTF-8"));
	        return;
		}
		
		if (STATUS.equals(args[0]) && args.length == 3) {
	        dataSocket.sendReceive(STATUS,null, args[1], Integer.parseInt(args[2]));
	        System.out.println(new String(dataSocket.getData()));
	        return;
		} 
		
		if ((GETCLASSLOADERSBYNAME.equals(args[0]) ||
		     GETINCLUDELIST.equals(args[0]) ||
		     GETEXCLUDELIST.equals(args[0])) && args.length == 3) {
	        dataSocket.sendReceive(args[0],null, args[1], Integer.parseInt(args[2]));
	        System.out.println(new String(dataSocket.getData()));
	        return;
		} 
		
		if ((REPLACEEXCLUDELIST.equals(args[0]) || 
			 REPLACEINCLUDELIST.equals(args[0]) || 
			 REPLACECLASSLOADERSBYNAME.equals(args[0])) && args.length == 4) {
			 dataSocket.send(args[0],args[3].getBytes(), args[1], Integer.parseInt(args[2]));
		 return;
		}
		
	    help();
	}
}
