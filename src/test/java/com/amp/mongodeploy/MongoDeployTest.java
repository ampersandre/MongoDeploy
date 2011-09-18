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
    public void runScripts_shouldContinueRunningAllScripts_whenOneScriptHasAlreadyBeenRun() {
        when(mockScript.getScriptName()).thenReturn("Script1");
        mongoDeploy.runScripts();
        MongoDeployScript mockScript2 = mock(MongoDeployScript.class);
        when(mockScript2.getScriptName()).thenReturn("Script2");
        mongoDeploy = new MongoDeploy(mongoDb, Arrays.asList(mockScript, mockScript2));

        //execute
        mongoDeploy.runScripts();

        //assert
        verify(mockScript2, times(1)).run();
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
    public void runScripts_shouldNotRunSubsequentScripts_whenAScriptFails() {
        doThrow(new RuntimeException()).when(mockScript).run();
        MongoDeployScript mockScript2 = mock(MongoDeployScript.class);
        mongoDeploy = new MongoDeploy(mongoDb, Arrays.asList(mockScript, mockScript2));

        //execute
        mongoDeploy.runScripts();

        //assert
        verifyZeroInteractions(mockScript2);
    }

    @Test
    public void hasException_shouldReturnTrue_whenAnErrorOccursWhenAScriptThrowsAnException() {
        doThrow(new RuntimeException()).when(mockScript).run();

        //execute
        mongoDeploy.runScripts();

        //assert
        assertTrue(mongoDeploy.hasException());
    }

    @Test
    public void hasException_shouldReturnFalse_whenTheScriptRunsNormally() {
        //execute
        mongoDeploy.runScripts();

        //assert
        assertFalse(mongoDeploy.hasException());
    }

    @Test
    public void getException_shouldReturnMongoDeployException_whenTheScriptThrowsAnException() {
        String expectedMessage = "Exception happened";
        RuntimeException expectedException = new RuntimeException(expectedMessage);
        doThrow(expectedException).when(mockScript).run();

        //execute
        mongoDeploy.runScripts();

        //assert
        MongoDeployException actualException = mongoDeploy.getException();
        assertNotNull(actualException);
        assertEquals(expectedException, actualException.getCause());
        String expectedMongoDeployExceptionMessage = String.format("Exception encountered while running deploy scripts: [%s - %s]",
                expectedException.getClass().getName(), expectedMessage);
        assertEquals(expectedMongoDeployExceptionMessage, actualException.getMessage());
    }
}