package com.staxnet.mojo.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import com.staxnet.appserver.config.AppConfig;
import com.staxnet.appserver.config.AppConfigHelper;
import com.staxnet.appserver.config.AppConfigParser;
import com.staxnet.appserver.utils.ZipHelper;
import com.staxnet.appserver.utils.ZipHelper.ZipEntryHandler;

public abstract class Deployer {
    static Logger logger = Logger.getLogger(Deployer.class.getSimpleName());

    public void deploy(String server, String appId, String domain, String environments,
            String deployBinFile, String deploySrcFile, String username, String password, String description,
            WriteListener writeListener) throws Exception {
        AppConfig appConfig = getAppConfig(new File(deployBinFile),
                ApplicationHelper.getEnvironmentList(environments), new String[]{"deploy"});
        if (appId == null || appId.equals("")) {
            appId = appConfig.getApplicationId();

            if (appId == null || appId.equals(""))
                throw new IllegalArgumentException(
                        "No application id specified");
        }
        
        String[] appIdParts = appId.split("/");
        if(appIdParts.length > 1)
        {
            domain = appIdParts[0];
            appId = appIdParts[1];
        }
        
        environments = StringHelper
        .join(appConfig.getAppliedEnvironments().toArray(
                new String[0]), ",");
        
        logger.info(String.format(
                "Deploying application: %s (environment: %s)", appId, environments));
        
        File binFile = deployBinFile != null ? new File(deployBinFile) : null;
        File srcFile = deploySrcFile != null ? new File(deploySrcFile) : null;
        invokeDeploy(server, appId, domain, environments, binFile, srcFile,
                username, password, description, writeListener);
    }

    protected abstract void invokeDeploy(String server, String appId, String domain, String environments,
            File deployBinPackage, File deploySrcPackage, String username, String password,
            String description,
            WriteListener writeListener) throws Exception;

    private AppConfig getAppConfig(File deployZip, final String[] environments, final String[] implicitEnvironments)
            throws IOException {
        final AppConfig appConfig = new AppConfig();
        final AppConfigParser appParser = new AppConfigParser();

        FileInputStream fin = new FileInputStream(deployZip);
        try {
            ZipHelper.unzipFile(fin, new ZipEntryHandler() {
                public void unzip(ZipEntry entry, InputStream zis)
                        throws IOException {
                    if (entry.getName().equals("META-INF/stax-application.xml") || entry.getName().equals("WEB-INF/stax-web.xml")) {
                        AppConfigHelper.load(appConfig, zis, null, environments, implicitEnvironments);
                    }
                }
            }, false);
        } finally {
            fin.close();
        }

        return appConfig;
    }
}
