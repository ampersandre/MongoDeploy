package com.amp.mongodeploy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class MongoDeployTest extends MongoTest {

    MongoDeploy mongoDeploy;


    @Before
    public void setup() {
        
    }

    @After
    public void teardown() {
        mongoDb.getCollection(MongoDeploy.MONGO_DEPLOY_COLLECTION).drop();
    }

    @Test
    public void runScripts_shouldRunTheGivenScript_whenItHasNotBeenRun() {
        MongoDeployScript mockScript = mock(MongoDeployScript.class);

        //execute
        mongoDeploy = new MongoDeploy(mongoDb, Collections.singletonList(mockScript));
        mongoDeploy.runScripts();

        //assert
        verify(mockScript).run();
    }

    @Test
    public void runScripts_shouldNotRunTheGivenScript_whenItHasAlreadyBeenRun() {
        MongoDeployScript mockScript = mock(MongoDeployScript.class);
        when(mockScript.getScriptName()).thenReturn("ClassName");

        //execute
        mongoDeploy = new MongoDeploy(mongoDb, Collections.singletonList(mockScript));
        mongoDeploy.runScripts();
        mongoDeploy.runScripts();

        //assert
        verify(mockScript, times(1)).run();
    }
}