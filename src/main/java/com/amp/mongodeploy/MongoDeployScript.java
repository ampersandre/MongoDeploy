package com.amp.mongodeploy;

public abstract class MongoDeployScript {

    public String getScriptName() {
        return this.getClass().getSimpleName();
    }

    public abstract void run();
}
