package com.staxnet.mojo.tomcat;

public abstract class DeployerFactory {
    private static DeployerFactory instance;

    public static DeployerFactory getInstance() {
        if(instance == null)
        {
            instance = new DeployerFactory()
            {
                public Deployer createDeployer() {
                    return new RemoteDeployer();
                }                
            };
        }
        return instance;
    }

    public static void setInstance(DeployerFactory instance) {
        DeployerFactory.instance = instance;
    }
    
    public abstract Deployer createDeployer();    
}
