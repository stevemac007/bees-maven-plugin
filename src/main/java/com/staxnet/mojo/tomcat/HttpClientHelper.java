package com.staxnet.mojo.tomcat;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public class HttpClientHelper {
    public static HttpClient createClient()
    {
        HttpClient client = new HttpClient();
        String proxyHost = System.getProperty("http.proxyHost");
        if(proxyHost != null)
        {
            int proxyPort = 80;
            String proxyPortStr = System.getProperty("http.proxyPort");
            if(proxyPortStr != null)
            {
                proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
            }
            
            client.getHostConfiguration().setProxy(proxyHost,proxyPort);
            
            //if there are proxy credentials available, set those too
            Credentials proxyCredentials = null;
            String proxyUser = System.getProperty("http.proxyUser");
            String proxyPassword = System.getProperty("http.proxyPassword");
            if(proxyUser != null || proxyPassword != null)
                proxyCredentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);
            if(proxyCredentials != null)
                client.getState().setProxyCredentials(AuthScope.ANY, proxyCredentials);
        }

        return client;
    }
}
