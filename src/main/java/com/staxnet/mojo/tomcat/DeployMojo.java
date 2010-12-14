package com.staxnet.mojo.tomcat;

//Based on org.codehaus.mojo.tomcat.RunMojo (Copyright 2006 Mark Hobson), which was licensed
//under the Apache License, Version 2.0. You may obtain a copy of the License at
//       http://www.apache.org/licenses/LICENSE-2.0 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


/**
 * Deploys the current project package to the Stax service.
 * 
 * @goal deploy
 * @execute phase = "package"
 * @requiresDependencyResolution runtime
 * 
 */
public class DeployMojo extends AbstractI18NMojo {
	// ----------------------------------------------------------------------
	// Mojo Parameters
	// ----------------------------------------------------------------------
	/**
	 * The packaging of the Maven project that this goal operates upon.
	 * 
	 * @parameter expression = "${project.packaging}"
	 * @required
	 * @readonly
	 */
	private String packaging;

	/**
	 * The id of the stax application.
	 * 
	 * @parameter
	 */
	private String appid;

	/**
	 * Stax username.
	 * 
	 * @parameter
	 */
	private String username;

	/**
	 * Stax password.
	 * 
	 * @parameter
	 */
	private String password;
	
	/**
	 * Configuration environments to use.
	 * 
	 * @parameter
	 */
	private String environment;
	
	/**
	 * Message associated with the deployment.
	 * 
	 * @parameter
	 */
	private String message;

	/**
	 * Stax deployment server.
	 * 
	 * @parameter default-value = "www.stax.net"
	 * @required
	 */
	private String server;

	/**
	 * The web resources directory for the web application being run.
	 * 
	 * @parameter expression="${basedir}/src/main/webapp"
	 */
	private String warSourceDirectory;

	/**
	 * The path to the Stax deployment descriptor.
	 * 
	 * @parameter default-value = "${basedir}/src/main/config/stax-application.xml"
	 */
	private File appConfig;
	
	/**
	 * The path to the J2EE appplication deployment descriptor.
	 * 
	 * @parameter default-value = "${basedir}/src/main/config/application.xml"
	 */
	private File appxml;

	/**
	 * The set of dependencies for the web application being run.
	 * 
	 * @parameter default-value = "${basedir}"
	 * @required
	 * @readonly
	 */
	private String baseDir;

	/**
	 * The package output file.
	 * 
	 * @parameter default-value =
	 *            "${project.build.directory}/${project.build.finalName}.${project.packaging}"
	 * @required
	 * @readonly
	 */
	private File warFile;

	/**
	 * The packaged deployment file.
	 * 
	 * @parameter default-value = "${project.build.directory}/stax-deploy.zip"
	 * @required
	 * @readonly
	 */
	private File deployFile;
	
	/**
	 * Gets whether this project uses WAR packaging.
	 * 
	 * @return whether this project uses WAR packaging
	 */
	protected boolean isWar() {
		return "war".equals(packaging);
	}

	/*
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		// Initialize the parameter values (to allow system property overrides
		// from the command line)
		initParameters();

		// ensure project is a web application
		if (!isWar()) {
			getLog().info(getMessage("RunMojo.nonWar"));
			return;
		}

		// create the deployment package
		FileOutputStream fstream = null;
		try {
			fstream = new FileOutputStream(deployFile);
			ZipOutputStream zos = new ZipOutputStream(fstream);
			ZipHelper.addFileToZip(warFile, "webapp.war", zos);
			ZipHelper.addFileToZip(appConfig, "META-INF/stax-application.xml",
					zos);
			ZipHelper.addFileToZip(appxml, "META-INF/application.xml",
					zos);
			zos.close();
		} catch (Exception e) {
			throw new MojoFailureException(this,
					getMessage("StaxMojo.packageFailed"), e.getMessage());
		} finally {
			try {
				if(fstream != null)
					fstream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//deploy the application to the server
		try {
			initCredentials();			
			
			DeployerFactory.getInstance().createDeployer().deploy(server, appid, username, environment, deployFile.getAbsolutePath(), null, username,
					password, message, new HashWriteProgress());
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoFailureException(this,
					getMessage("StaxMojo.deployFailed"), e.getMessage());
		}
	}

	private String getSysProperty(String parameterName, String defaultValue) {
		String value = System.getProperty(parameterName);
		if (value != null)
			return value;
		else
			return defaultValue;
	}

	/**
	 * Initialize the parameter values (to allow system property overrides)
	 */
	private void initParameters() {
		appid = getSysProperty("appid", appid);
		username = getSysProperty("username", username);
		password = getSysProperty("password", password);
		server = getSysProperty("server", server);
		environment = getSysProperty("environment", environment);
		message = getSysProperty("message", message);
	}

	private void initCredentials() throws IOException {
		boolean promptForUsername = username == null;
		boolean promptForPassword = username == null || password == null;
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(
				System.in));
		if (promptForUsername) {
			System.out.print("Enter your Stax Username: ");
			username = inputReader.readLine();
		}

		if (promptForPassword) {
			System.out.print("Enter your Stax Password: ");
			password = inputReader.readLine();
		}
	}

}
