/*
 Copyright (c) 2014 Rodolphe LOUIS-SIDNEY
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
package com.mentorgen.tools.profile.instrument.classfilter;

public class WebLogicClassLoaderFilter  implements ClassLoaderFilter {

	@Override
	public boolean canFilter() {
		return true;
	}
	
	private static final String[] CLASSLOADERS = new String[] {
		// niveau CLASSPATH 
		//   "sun.misc.Launcher$ExtClassLoader",
		//   "sun.misc.Launcher$AppClassLoader",
		// niveau EAR et EJB 
		   "java.net.URLClassLoader",
		   "weblogic.utils.classloaders.GenericClassLoader",
		   "weblogic.utils.classloaders.FilteringClassLoader",
		// "weblogic.utils.classloaders.GenericClassLoader",
		// niveau WAR 
		   "weblogic.utils.classloaders.ChangeAwareClassLoader",
		   "weblogic.servlet.jsp.TagFileClassLoader",
		   "weblogic.servlet.jsp.JspClassLoader"
	};

	@Override
	public boolean accept(ClassLoader loader) {
		for (String TheClassLoaderName : CLASSLOADERS) {
	        if (loader.getClass().getName().equals(TheClassLoaderName)) {
	          return true;
	        }
	    }
		return false;
	}
}
