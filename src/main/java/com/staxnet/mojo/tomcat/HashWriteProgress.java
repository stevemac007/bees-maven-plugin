package com.staxnet.mojo.tomcat;

public class HashWriteProgress implements WriteListener{
    boolean uploadComplete = false;
    long hashMarkCount = 0;
    public void handleBytesWritten(long deltaCount,
            long totalWritten, long totalToSend) {                    
        if(uploadComplete)
            return;
        
        int totalMarks = (int)(totalWritten/(totalToSend/100f));
        while(hashMarkCount < totalMarks)
        {
            hashMarkCount++;
            if(hashMarkCount % 25 == 0)
            {
                if(hashMarkCount < 100)
                    System.out.println(String.format("uploaded %d%%", hashMarkCount));
                else
                {
                    //upload completed (or will very soon)
                    uploadComplete = true;
                    System.out.println("upload completed");
                    System.out.println("deploying application to server(s)...");
                }
            }
            else
            {
                System.out.print(".");
            }
        }
    }
}
