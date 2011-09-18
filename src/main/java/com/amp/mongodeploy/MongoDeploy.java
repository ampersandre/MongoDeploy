package com.amp.mongodeploy;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.List;

public class MongoDeploy {

    public static final String ID_FIELD = "_id";
    private DB mongoDb;
    private List<MongoDeployScript> mongoDeployScripts;
    public static final String MONGO_DEPLOY_COLLECTION = "mongoDeploy";

    public MongoDeploy(DB mongoDb, List<MongoDeployScript> mongoDeployScripts) {
        this.mongoDb = mongoDb;
        this.mongoDeployScripts = mongoDeployScripts;
    }

    public void runScripts() {
        for(MongoDeployScript script : mongoDeployScripts) {
            if (scriptHasNotBeenRun(script)) {
                script.run();
                trackScript(script);
            }
        }
    }

    private void trackScript(MongoDeployScript script) {
        DBObject scriptRecord = dbObject(ID_FIELD, script.getScriptName());
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
