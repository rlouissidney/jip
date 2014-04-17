/*
 Copyright (c) 2014 Lon F. Binder
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
	/**
	 * A filter that accepts classes that are loaded by Tomcat's 
	 * web app class loader (this has only been tested with Tomcat 5.5.x)
	 * 
	 * @author <a href="lbinder@folica.com">Lon F. Binder</a>
	 * @see com.mentorgen.tools.profile.instrument.classfilter.ClassLoaderFilter
	 */
	public class OFBizClassLoaderFilter implements ClassLoaderFilter {
	    private static final String CLASSLOADER_TOMCAT = "org.apache.catalina.loader.WebappClassLoader";
	    private static final String CLASSLOADER_URLBASED = "java.net.URLClassLoader";
	    public boolean canFilter() {
	        return true;
	    }

	    public boolean accept(ClassLoader loader) {
	        String loaderName = loader.getClass().getName();
	        return CLASSLOADER_TOMCAT.equals(loaderName) || CLASSLOADER_URLBASED.equals(loaderName);
	    }

	} // Everyone likes the end of class