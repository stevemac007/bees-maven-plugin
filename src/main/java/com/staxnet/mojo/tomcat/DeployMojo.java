package com.staxnet.mojo.tomcat;

//Based on org.codehaus.mojo.tomcat.RunMojo (Copyright 2006 Mark Hobson), which was licensed
//under the Apache License, Version 2.0. You may obtain a copy of the License at
//       http://www.apache.org/licenses/LICENSE-2.0 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.stax.api.HashWriteProgress;
import net.stax.api.StaxClient;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.staxnet.appserver.config.AppConfig;
import com.staxnet.appserver.config.AppConfigHelper;
import com.staxnet.appserver.utils.ZipHelper;
import com.staxnet.appserver.utils.ZipHelper.ZipEntryHandler;

/**
 * Deploys the current project package to the Stax service.
 * 
 * @goal deploy
 * @execute phase = "package"
 * @requiresDependencyResolution runtime
 * 
 */
public class DeployMojo extends AbstractI18NMojo
{
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
     * @parameter expression="${bees.appid}"
     */
    private String appid;

    /**
     * Bees username.
     * 
     * @parameter expression="${bees.username}"
     */
    private String username;

    /**
     * Bees password.
     * 
     * @parameter expression="${bees.password}"
     */
    private String password;

    /**
     * Configuration environments to use.
     * 
     * @parameter expression="${bees.environment}"
     */
    private String environment;

    /**
     * Message associated with the deployment.
     * 
     * @parameter expression="${bees.message}"
     */
    private String message;

    /**
     * Bees deployment server.
     * 
     * @parameter expression="${bees.api.server}" default-value = "api.stax.net"
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
     * The path to the Bees deployment descriptor.
     * 
     * @parameter expression="${bees.appxml}" default-value =
     *            "${basedir}/src/main/config/stax-application.xml"
     */
    private File appConfig;

    /**
     * The path to the J2EE appplication deployment descriptor.
     * 
     * @parameter expression="${bees.j2ee.appxml}" default-value = "${basedir}/src/main/config/application.xml"
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
    protected boolean isWar()
    {
        return "war".equals(packaging);
    }

    /*
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // Initialize the parameter values (to allow system property overrides
        // from the command line)
        initParameters();

        // ensure project is a web application
        if (!isWar()) {
            getLog().info(getMessage("RunMojo.nonWar"));
            return;
        }
        
        // create the deployment package
        if(appConfig.exists() && appxml.exists())
        {
            FileOutputStream fstream = null;
            try {
                fstream = new FileOutputStream(deployFile);
                ZipOutputStream zos = new ZipOutputStream(fstream);
                ZipHelper.addFileToZip(warFile, "webapp.war", zos);
                ZipHelper.addFileToZip(appConfig,
                                       "META-INF/stax-application.xml", zos);
                ZipHelper.addFileToZip(appxml, "META-INF/application.xml", zos);
                zos.close();
            } catch (Exception e) {
                throw new MojoFailureException(
                                               this,
                                               getMessage("StaxMojo.packageFailed"),
                                               e.getMessage());
            }
        }
        else
        {
            deployFile = warFile;
        }

        // deploy the application to the server
        try {
            String apiUrl = String.format("http://%s/api", server);
            initCredentials();
            
            AppConfig appConfig =
                getAppConfig(
                    deployFile,
                             ApplicationHelper.getEnvironmentList(environment),
                             new String[] { "deploy" });
            initAppId(appConfig);

            String defaultAppDomain = username;
            String[] appIdParts = appid.split("/");
            String domain = null;
            if (appIdParts.length > 1) {
                domain = appIdParts[0];
            } else if (defaultAppDomain != null &&
                       !defaultAppDomain.equals(""))
            {
                domain = defaultAppDomain;
                appid = domain + "/" + appid;
            } else {
                throw new MojoExecutionException(
                                         "default app domain could not be determined, appid needs to be fully-qualified ");
            }
            
            environment = StringHelper.join(appConfig.getAppliedEnvironments()
                                            .toArray(new String[0]), ",");
            
            System.out.println(String.format(
                                             "Deploying application %s (environment: %s)",
                                             appid, environment));
            StaxClient client =
                new StaxClient(apiUrl, username, password, "xml", "0.1");

            if(deployFile.getName().endsWith(".war"))
            {
                client.applicationDeployWar(appid, environment, message,
                                            deployFile.getAbsolutePath(), null,
                                            new HashWriteProgress());
            }
            else
            {
                client.applicationDeployEar(appid, environment, message,
                    deployFile.getAbsolutePath(), null,
                    new HashWriteProgress());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoFailureException(
                                           this,
                                           getMessage("StaxMojo.deployFailed"),
                                           e.getMessage());
        }
    }

    private void initAppId(AppConfig appConfig) throws IOException
    {
        if (appid == null || appid.equals("")) {
            appid = appConfig.getApplicationId();

            if (appid == null || appid.equals(""))
                appid = promptForAppId();
            
            if (appid == null || appid.equals(""))
                throw new IllegalArgumentException(
                                                   "No application id specified");
        }
    }

    private String getSysProperty(String parameterName, String defaultValue)
    {
        String value = System.getProperty(parameterName);
        if (value != null)
            return value;
        else
            return defaultValue;
    }

    /**
     * Initialize the parameter values (to allow system property overrides)
     */
    private void initParameters()
    {
        appid = getSysProperty("bees.appid", appid);
        username = getSysProperty("bees.username", username);
        password = getSysProperty("bees.password", password);
        server = getSysProperty("bees.server", server);
        environment = getSysProperty("bees.environment", environment);
        message = getSysProperty("bees.message", message);
    }

    private void initCredentials() throws IOException
    {
        boolean promptForUsername = username == null;
        boolean promptForPassword = username == null || password == null;
        BufferedReader inputReader =
            new BufferedReader(new InputStreamReader(System.in));
        if (promptForUsername) {
            System.out.print("Enter your CloudBees Username: ");
            username = inputReader.readLine();
        }

        if (promptForPassword) {
            System.out.print("Enter your CloudBees Password: ");
            password = inputReader.readLine();
        }
    }
    
    private String promptForAppId() throws IOException
    {
        BufferedReader inputReader =
            new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter application ID (ex: account/appname): ");
        String appId = inputReader.readLine();
        return appId;
    }

    private AppConfig getAppConfig(File deployZip,
                                   final String[] environments,
                                   final String[] implicitEnvironments) throws IOException
    {
        final AppConfig appConfig = new AppConfig();

        FileInputStream fin = new FileInputStream(deployZip);
        try {
            ZipHelper.unzipFile(fin, new ZipEntryHandler()
            {
                public void unzip(ZipEntry entry, InputStream zis) throws IOException
                {
                    if (entry.getName().equals(
                                               "META-INF/stax-application.xml") ||
                        entry.getName().equals("WEB-INF/stax-web.xml"))
                    {
                        AppConfigHelper.load(appConfig, zis, null,
                                             environments,
                                             implicitEnvironments);
                    }
                }
            }, false);
        } finally {
            fin.close();
        }

        return appConfig;
    }

}
