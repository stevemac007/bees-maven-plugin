package com.staxnet.mojo.tomcat;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

public class MojoUtils {
	/**
     * The plugin messages.
     */
    private static RB rb = new RB(MojoUtils.class);
    
	static File findDeploymentDescriptor(File deployDescriptor, String baseDir, String warSourceDirectory) throws MojoExecutionException {
		if (deployDescriptor != null) {
			if (deployDescriptor.exists()) {
				return deployDescriptor;
			} else {
			    return null;
			}
		} else {
			File f = new File(baseDir, "src" + File.separator + "main"
					+ File.separator + "config" + File.separator + "stax-application.xml");			
			if (f.exists()) {
				return f;
			} else {
				return null;
			}
		}
	}
}
