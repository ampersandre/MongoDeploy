package com.amp.mongodeploy;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.sun.xml.internal.ws.wsdl.writer.UsingAddressing;
import org.bson.types.ObjectId;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.UnknownHostException;

public class MongoTest {

    public static String dbName = new ObjectId().toString();
    public static DB mongoDb;

    @BeforeClass
    public static void beforeClass() throws UnknownHostException {
        String mongoHost = System.getProperty("mongo.test.host", "localhost");
        mongoDb = new Mongo(mongoHost).getDB(dbName);
    }

    @AfterClass
    public static void afterClass() {
        mongoDb.dropDatabase();
    }
}
