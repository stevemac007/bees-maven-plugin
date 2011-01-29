package com.staxnet.mojo.tomcat;

public class ApplicationHelper {
    public static String[] getEnvironmentList(String environments, String...prependEnvs)
    {
        if(environments == null && prependEnvs.length == 0)
            return new String[0];
        
        if(environments == null)
            environments = "";
        
        //split the environments string and prepend the run environment 
        String[] envSplit = environments.split(",");
        String[] envList = new String[envSplit.length+prependEnvs.length];
        for(int i=0; i<prependEnvs.length; i++)
            envList[i] = prependEnvs[i];
        
        for(int i=0; i<envSplit.length; i++)
            envList[prependEnvs.length + i] = envSplit[i].trim();
        return envList;
    }
}
