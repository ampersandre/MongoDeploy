package com.amp.mongodeploy;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.Date;
import java.util.List;

public class MongoDeploy {
    public static final String MONGO_DEPLOY_COLLECTION = "mongoDeploy";
    protected static final String ID_FIELD = "_id";
    protected static final String DATE_FIELD = "date";

    private DB mongoDb;
    private List<MongoDeployScript> mongoDeployScripts;
    private boolean hasError = false;

    public MongoDeploy(DB mongoDb, List<MongoDeployScript> mongoDeployScripts) {
        this.mongoDb = mongoDb;
        this.mongoDeployScripts = mongoDeployScripts;
    }

    public boolean hasError() {
        return hasError;
    }

    public void runScripts() {
        for(MongoDeployScript script : mongoDeployScripts) {
            try {
                runScript(script);
            } catch (Exception e) {
                hasError = true;
            }
        }
    }

    private void runScript(MongoDeployScript script) {
        if (scriptHasNotBeenRun(script)) {
            script.run();
            trackScript(script);
        }
    }

    private void trackScript(MongoDeployScript script) {
        DBObject scriptRecord = dbObject(ID_FIELD, script.getScriptName());
        scriptRecord.put(DATE_FIELD, new Date());
        getCollection().save(scriptRecord);
    }

    private boolean scriptHasNotBeenRun(MongoDeployScript script) {
        DBObject scriptQuery = dbObject(ID_FIELD, script.getScriptName());
        DBObject scriptInDatabase = getCollection().findOne(scriptQuery);

        return scriptInDatabase == null;
    }
    private DBCollection getCollection() { return mongoDb.getCollection(MONGO_DEPLOY_COLLECTION); }

    private DBObject dbObject(String key, Object value) { return new BasicDBObject(key, value); }
}
