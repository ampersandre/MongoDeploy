package com.amp.mongodeploy;

import com.mongodb.DBObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MongoDeployTest extends MongoTest {

    MongoDeploy mongoDeploy;
    MongoDeployScript mockScript;


    @Before
    public void setup() {
        mockScript = mock(MongoDeployScript.class);
        mongoDeploy = new MongoDeploy(mongoDb, Collections.singletonList(mockScript));
    }

    @After
    public void teardown() {
        mongoDb.getCollection(MongoDeploy.MONGO_DEPLOY_COLLECTION).drop();
    }

    @Test
    public void runScripts_shouldRunTheGivenScript_whenItHasNotBeenRun() {
        //execute
        mongoDeploy.runScripts();

        //assert
        verify(mockScript).run();
    }

    @Test
    public void runScripts_shouldNotRunTheGivenScript_whenItHasAlreadyBeenRun() {
        when(mockScript.getScriptName()).thenReturn("ClassName");

        //execute
        mongoDeploy.runScripts();
        mongoDeploy.runScripts();

        //assert
        verify(mockScript, times(1)).run();
    }

    @Test
    public void runScripts_shouldRegisterTheIdInTheCollection_whenTheScriptIsRun() {
        String expectedScriptId = "ClassName";
        when(mockScript.getScriptName()).thenReturn(expectedScriptId);

        //execute
        mongoDeploy.runScripts();

        //assert
        DBObject result = mongoDb.getCollection(MongoDeploy.MONGO_DEPLOY_COLLECTION).findOne();
        assertEquals(expectedScriptId, result.get("_id"));
    }

    @Test
    public void runScripts_shouldRegisterTheRunDateInTheCollection_whenTheScriptIsRun() {
        when(mockScript.getScriptName()).thenReturn("ClassName");

        //execute
        mongoDeploy.runScripts();

        //assert
        DBObject result = mongoDb.getCollection(MongoDeploy.MONGO_DEPLOY_COLLECTION).findOne();
        assertNotNull(result.get("date"));
    }

    @Test
    public void hasError_shouldReturnTrue_whenAnErrorOccursWhenAScriptThrowsAnError() {
        doThrow(new RuntimeException()).when(mockScript).run();

        //execute
        mongoDeploy.runScripts();

        //assert
        assertTrue(mongoDeploy.hasError());
    }

    @Test
    public void hasError_shouldReturnFalse_whenTheScriptRunsNormally() {
        //execute
        mongoDeploy.runScripts();

        //assert
        assertFalse(mongoDeploy.hasError());
    }

    @Test
    public void runScripts_shouldRunAllScripts_whenMultipleScriptsAreProvided() {
        MongoDeployScript mockScript2 = mock(MongoDeployScript.class);
        mongoDeploy = new MongoDeploy(mongoDb, Arrays.asList(mockScript, mockScript2));

        //execute
        mongoDeploy.runScripts();

        //assert
        InOrder inOrder = inOrder(mockScript, mockScript2);
        inOrder.verify(mockScript).run();
        inOrder.verify(mockScript2).run();
    }
}