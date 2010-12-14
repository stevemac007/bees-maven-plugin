package com.staxnet.mojo.tomcat;

//Based on org.codehaus.mojo.tomcat.RunMojo (Copyright 2006 Mark Hobson), which was licensed
//under the Apache License, Version 2.0. You may obtain a copy of the License at
//       http://www.apache.org/licenses/LICENSE-2.0 

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.staxnet.appserver.SnazAppServer;
import com.staxnet.appserver.config.AppConfig;
import com.staxnet.appserver.config.AppConfigHelper;

/**
 * Runs the current project as a dynamic web application using an embedded
 * Tomcat server.
 * 
 * @goal run
 * @execute phase = "compile"
 * @requiresDependencyResolution runtime
 * 
 */
public class RunMojo extends AbstractI18NMojo {
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
	 * The additional environment settings to load.
	 * 
	 * @parameter
	 */
	private String environment;

	/**
	 * The directory to create the Tomcat server configuration under.
	 * 
	 * @parameter expression = "${project.build.directory}/tomcat"
	 */
	private String configurationDir;

	/**
	 * The port to run the Tomcat server on.
	 * 
	 * @parameter default-value = "8080"
	 */
	private int port;

	/**
	 * The classes directory for the web application being run.
	 * 
	 * @parameter expression = "${project.build.outputDirectory}"
	 */
	private String classesDir;

	/**
	 * The set of dependencies for the web application being run.
	 * 
	 * @parameter default-value = "${project.artifacts}"
	 * @required
	 * @readonly
	 */
	private Set<Artifact> dependencies;

	/**
	 * The web resources directory for the web application being run.
	 * 
	 * @parameter expression="${basedir}/src/main/webapp"
	 */
	private String warSourceDirectory;

	/**
	 * The path to the Stax deployment descriptor.
	 * 
	 * @parameter default-value =
	 *            "${basedir}/src/main/config/stax-application.xml"
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
		// ensure project is a web application
		if (!isWar()) {
			getLog().info(getMessage("RunMojo.nonWar"));
			return;
		}

		appConfig = MojoUtils.findDeploymentDescriptor(appConfig, baseDir,
				warSourceDirectory);

		try {
			// add classes directory to loader
			ArrayList<String> classdirs = new ArrayList<String>();
			if (classesDir != null)
				classdirs.add(new File(classesDir).toURL().toString());

			// add artifacts to loader
			if (dependencies != null) {
				for (Iterator<Artifact> iterator = dependencies.iterator(); iterator
						.hasNext();) {
					Artifact artifact = iterator.next();
					classdirs.add(artifact.getFile().toURL().toString());
				}
			}			

			SnazAppServer appServer = new SnazAppServer(baseDir,
					configurationDir, getClass().getClassLoader(), classdirs
							.toArray(new String[0]), null, port, appConfig,
					appxml, getEnvironments(), null);
			appServer.start();
		} catch (ServletException exception) {
			throw new MojoExecutionException(exception.getMessage(), exception);
		} catch (IOException exception) {
			throw new MojoExecutionException(
					getMessage("RunMojo.cannotCreateConfiguration"), exception);
		} catch (Exception exception) {
			throw new MojoExecutionException(
					getMessage("RunMojo.cannotCreateConfiguration"), exception);
		}
	}

	private String[] getEnvironments() {
		String envString = this.environment != null ? this.environment : System
				.getProperty("run.environment");
		File appConfigFile = appConfig;
		if (envString == null || envString.equals("") && appConfigFile != null
				&& appConfigFile.exists()) {
			// load the default environment, and append the run environment
			AppConfig appConfig = new AppConfig();
			AppConfigHelper.load(appConfig, appConfigFile.getAbsolutePath(), new String[0], new String[0]);

			envString = appConfig.getDefaultEnvironment();
		}

		String[] environment = ApplicationHelper.getEnvironmentList(envString,
				"run");
		return environment;
	}
}
