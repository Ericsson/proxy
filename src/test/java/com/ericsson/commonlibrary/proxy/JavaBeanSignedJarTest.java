/*
Copyright (c) 2018 Ericsson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.ericsson.commonlibrary.proxy;

import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.testng.annotations.Test;

public class JavaBeanSignedJarTest {
	private static final String TCSH = "/bin/tcsh";
    private static final String BASH = "/bin/bash";
    
	private String shellPath;

    @Test
    public void javaBeanSingedJar() {
    	assertTrue(runCmd("external-sign.jar").contains("Setter for proxy bean works."));
    }
    
    @Test
    public void javaBeanNotSingedJar() {
    	assertTrue(runCmd("external.jar").contains("Setter for proxy bean works."));
    }
    
    private String getFilePath(String resource) {
    	URL url = getClass().getClassLoader().getResource(resource);
    	return url.getFile();
    }
    
    private String runCmd(String externalJar) {
    	String delimeter = ":";
    	if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
    		delimeter = ";";
    	}
    	String classPath = System.getProperty("java.class.path");
    	String javaHome = System.getProperty("java.home");
    	String startCmd = javaHome + File.separator + "bin" + File.separator + "java -cp "
    						+ classPath + delimeter + getFilePath(externalJar)
    						+ " com.ericsson.commonlibrary.proxy.external.ExternalEntry";
        System.out.println("Java command is: " + startCmd);  
        String[] osCmd;
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            osCmd = new String[] { "cmd.exe", "/c", startCmd };
        } else {
        	osCmd = new String[] { getShellPath(), "-c", startCmd };            
        }
        return startProcess(new ProcessBuilder(osCmd));
    }
    
    private String getShellPath() {
    	if (shellPath == null) {    		
    		if (new File(TCSH).exists()) {
                shellPath = TCSH;
            } else if (new File(BASH).exists()) {
                shellPath = BASH;
            } else {
                throw new RuntimeException("Didn't find any usable shell program");
            }
    	}
        return shellPath;
    }
    
    private String startProcess(ProcessBuilder builder) {
        try {
            Process process = builder.start();
            process.waitFor();
            return internalSend(process);
        } catch (IOException | InterruptedException ioe) {
            System.out.println("VisualVM cmd failed" + ioe.toString());
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return "";
    }

    private String internalSend(Process process) {
        StringBuilder out = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                System.out.println(line);
            }

        } catch (IOException ioe) {
        	System.out.println("Could not read from process" + ioe.toString());
        }

        return out.toString();
    }

    
}
