package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class VotingStructureTest {
    @AfterAll
    static void creanFile(){
        new File("src/main/resources/junitTestSave.json").delete();
//
    }
    @Test
    void createTopic(){
        VotingStructure.getInstance().load("junitTest");
        VotingStructure.getInstance().createTopic("junitTest");
        Assertions.assertEquals("[junitTest, testTopic]", VotingStructure.getInstance().textTopics());
    }

    @Test
    void load(){
        VotingStructure.getInstance().load("junitTest");
        Assertions.assertEquals("[testTopic]",VotingStructure.getInstance().textTopics());
    }

    @Test
    void save() throws JsonProcessingException {
        VotingStructure.getInstance().load("junitTest");
        VotingStructure.getInstance().save("junitTestSave");
        String line;
        try(BufferedReader br = new BufferedReader(new FileReader("src/main/resources/junitTestSave.json"))) {
            StringBuilder sb = new StringBuilder();
            line = br.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue(line.startsWith("{\"topics\":{\"testTopic\":{\"name\":\"testTopic\",\"votin"));
    }

    @Test
    void executeCommandCreateTopic(){
        VotingStructure.executeCommand("create topic -n=testTopic");
        Assertions.assertEquals(
                "[testTopic]",VotingStructure.getInstance().textTopics());
    }

    @Test
    void executeCommandCreateVote(){
        VotingStructure.executeCommand("create topic -n=testTopic");
        VotingStructure.executeCommand("createVote testTopic voName description opt1 opt2 testCreator");
        Assertions.assertEquals(
                "testCreator", VotingStructure.getInstance().getTopics().get("testTopic")
                        .getVoting("voName").getCreator());
    }

    @Test
    void executeCommandView1(){
        VotingStructure.executeCommand("create topic -n=testTopic");
        VotingStructure.executeCommand("createVote testTopic voName description opt1 opt2 testCreator");
        Assertions.assertEquals(
                "[testTopic]",
                VotingStructure.executeCommand("view bleb"));
    }

    @Test
    void executeCommandView2(){
        VotingStructure.executeCommand("create topic -n=testTopic");
        VotingStructure.executeCommand("createVote testTopic voName description opt1 opt2 testCreator");
        Assertions.assertEquals(
                "[voName]",
                VotingStructure.executeCommand("view -t=testTopic bleb"));
    }

    @Test
    void executeCommandView3(){
        VotingStructure.getInstance().load("junitTest");
        Assertions.assertEquals("Name=voname\nOptions[ option2: 0 option1: 1 ]",
                VotingStructure.executeCommand("view -t=testTopic -v=voname bleb"));
    }

    @Test
    void executeCommandVoteView(){
        VotingStructure.getInstance().load("junitTest");
        Assertions.assertEquals("[option2, option1]",
                VotingStructure.executeCommand("vote -t=testTopic -v=voname bleb"));
    }

    @Test
    void executeCommandVote(){
        VotingStructure.getInstance().load("junitTest");
        VotingStructure.executeCommand("vote testTopic voname option2 bleb");
        Assertions.assertEquals("Name=voname\nOptions[ option2: 1 option1: 1 ]",
                VotingStructure.executeCommand("view -t=testTopic -v=voname bleb"));
    }

    @Test
    void executeCommandDelete(){
        VotingStructure.getInstance().load("junitTest");
        VotingStructure.executeCommand("delete -t=testTopic -v=voname bleb");
        Assertions.assertEquals("[]",
                VotingStructure.executeCommand("view -t=testTopic bleb"));
    }
}
