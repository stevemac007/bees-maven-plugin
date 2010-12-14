package com.staxnet.mojo.tomcat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class RemoteDeployer extends Deployer{    
    protected void invokeDeploy(String server, String appId, String domain, String environments,
            File deployBinPackage, File deploySrcPackage, String username, String password,
            String description,
            WriteListener writeListener) throws Exception {        
        String uploadURL = "http://" + server + "/api/applications/" + domain + "/" + appId;
        PostMethod filePost = new PostMethod(uploadURL);

        String[] serverParts = server.split(":");
        String authScopeServer = serverParts[0];
        int authScopePort = Integer
                .parseInt(serverParts.length == 2 ? serverParts[1] : "80");
        try {
            ArrayList<Part> parts = new ArrayList<Part>();

            int fileUploadSize = 0;
            FilePart binFile = new FilePart("app_package", deployBinPackage);            
            parts.add(binFile);
            fileUploadSize += binFile.length();
            FilePart srcFile = null;
            if(deploySrcPackage != null)
            {
                srcFile = new FilePart("app_src", deploySrcPackage);
                parts.add(srcFile);
                fileUploadSize += srcFile.length();
            }            
            
            parts.add(new StringPart("appid", appId));
            parts.add(new StringPart("domain", domain));
            if (environments != null && !environments.equals(""))
                parts.add(new StringPart("environment", environments));
                        
            if(description != null)
                parts.add(new StringPart("description", description));

            ProgressUploadEntity uploadEntity = new ProgressUploadEntity(parts
                    .toArray(new Part[parts.size()]), filePost.getParams(),
                    writeListener, fileUploadSize);
            filePost.setRequestEntity(uploadEntity);
            HttpClient client = HttpClientHelper.createClient();
            client.getParams().setAuthenticationPreemptive(true);

            Credentials defaultcreds = new UsernamePasswordCredentials(
                    username, password);
            client.getState().setCredentials(
                    new AuthScope(authScopeServer, authScopePort,
                            AuthScope.ANY_REALM), defaultcreds);
            client.getHttpConnectionManager().getParams().setConnectionTimeout(
                    500);

            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                logger.info("Deploy complete, response="
                        + filePost.getResponseBodyAsString());
            } else {
                logger.info("Deploy failed, response="
                        + HttpStatus.getStatusText(status));
                throw new Exception(filePost.getResponseBodyAsString());
            }
        } finally {
            filePost.releaseConnection();
        }
    }

    class ProgressUploadEntity extends MultipartRequestEntity {
        private WriteListener listener;
        long length;

        public ProgressUploadEntity(Part[] parts, HttpMethodParams params,
                WriteListener listener, long length) {
            super(parts, params);
            this.listener = listener;
            this.length = length;
        }

        @Override
        public void writeRequest(OutputStream out) throws IOException {
            WriteListenerOutputStream listenStream = new WriteListenerOutputStream(
                    out, listener, length);
            super.writeRequest(listenStream);
        }
    }

    class WriteListenerOutputStream extends OutputStream {
        private OutputStream targetStream;
        private long bytesWritten;
        private boolean isClosed;
        private long bytesToSend;
        private WriteListener writeListener;

        public WriteListenerOutputStream(OutputStream targetStream,
                WriteListener writeListener, long length) {
            super();
            this.targetStream = targetStream;
            this.writeListener = writeListener;
            this.bytesToSend = length;
        }

        public void close() throws IOException {
            isClosed = true;
            targetStream.close();
        }

        public void flush() throws IOException {
            targetStream.flush();
        }

        public void write(byte[] b, int off, int len) throws IOException {
            targetStream.write(b, off, len);
            trackBytesWritten(len);
        }

        public void write(byte[] b) throws IOException {
            targetStream.write(b);
            trackBytesWritten(b.length);
        }

        public void write(int b) throws IOException {
            targetStream.write(b);
            trackBytesWritten(1);
        }

        public boolean isClosed() {
            return isClosed;
        }

        public long getBytesWritten() {
            return bytesWritten;
        }

        private void trackBytesWritten(long count) {
            bytesWritten += count;
            if (writeListener != null) {
                writeListener.handleBytesWritten(count, bytesWritten,
                        bytesToSend);
            }
        }
    }
}
