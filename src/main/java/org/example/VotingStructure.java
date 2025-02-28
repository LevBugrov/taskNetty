package org.example;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class VotingStructure{
    private static VotingStructure instance;
    private final Hashtable<String, Topic> topics;
    @JsonIgnore
    protected static Logger logger = Logger.getLogger(VotingStructure.class.getName());

    private VotingStructure(){
        topics = new Hashtable<>();
        logger.setUseParentHandlers(false);
        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler("src/main/resources/statusVoSt.log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.addHandler(fileHandler);
        logger.info("Start log from "+VotingStructure.class.getName());

    }
    public Hashtable<String, Topic> getTopics() {return topics;}
    public String textTopics() {return topics.keySet().toString();}

    public static synchronized VotingStructure getInstance(){
        if(instance == null){
            instance = new VotingStructure();
        }
        return instance;
    }

    public void createTopic(String topic){
        if(!topics.contains(topic)) {
            topics.put(topic, new Topic(topic));
            logger.info(topic+" topic was created");
        }
    }

    public static String executeCommand(String command){
        String[] splitCM = command.split(" ");
        switch (splitCM[0]){
            case "help":
                logger.info("help");
                return """
                        create topic -n=<topic>     создает новый раздел c именем -n
                        create vote -t=<topic>      запускает создание нового голосования в разделе -t
                        delete -t=topic -v=<vote>   удалить голосование с именем <vote> из <topic>
                        view                        показывает список разделов
                        view -t=<topic>             показывает список голосований в разделе
                        view -t=<topic> -v=<vote>   отображает информацию по голосованию
                        vote -t=<topic> -v=<vote>   запускает выбор ответа в голосовании
                        exit                        завершение работы программы""";
            case "create":
                logger.info("Creating topic " + splitCM[2].substring(3));
                VotingStructure.getInstance().createTopic(splitCM[2].substring(3));
                return splitCM[2].substring(3)+" was created successfully";
            case "createVote":
                logger.info("Creating vote " + splitCM[2].substring(3));
                getInstance().topics.get(splitCM[1]).
                        createVote(splitCM[splitCM.length-1], splitCM[2], splitCM[3],
                                Arrays.copyOfRange(splitCM, 2, splitCM.length-1));
                return "vote was created successfully";
            case "view":
                if(splitCM.length==2){
                    logger.info("Get topics");
                    return getInstance().textTopics();
                }
                if(splitCM.length==3) {
                    logger.info("Get vote");
                    return getInstance().topics.get(splitCM[1].substring(3)).votingText();
                }
                if(splitCM.length==4) {
                    logger.info("Get vote info");
                    return getInstance().topics.get(splitCM[1].substring(3)).
                            getVoting(splitCM[2].substring(3)).voteInfo();
                }
                return "invalid view";
            case "vote":
                if(splitCM.length == 4){
                    logger.info("Get vote options");
                    return getInstance().topics.get(splitCM[1].substring(3)).
                            getVoting(splitCM[2].substring(3)).optionsInfo();
                }
                if(splitCM.length == 5){
                    logger.info(splitCM[4]+" voting");
                    getInstance().topics.get(splitCM[1])
                            .getVoting(splitCM[2])
                            .vote(splitCM[3], splitCM[4]);
                    return "sucsesful vote";
                }
            case "delete":
                logger.info("Creating vote " + splitCM[2].substring(3));
                return getInstance().topics.get(splitCM[1].substring(3))
                        .deleteVote(splitCM[splitCM.length-1], splitCM[2].substring(3));


        }
        return "invalid command";
    }

    public void save(String fileName) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String text = mapper.writeValueAsString(getInstance());
        try (PrintWriter out = new PrintWriter("src/main/resources/" + fileName + ".json")) {
            logger.info("Saving VotingStructure");
            out.println(text);
        } catch (FileNotFoundException e) {
            logger.severe("Save failed "+e.toString());
            throw new RuntimeException(e);
        }
    }

    public void load(String fileName){
        try(BufferedReader br = new BufferedReader(new FileReader("src/main/resources/" + fileName + ".json"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            instance = new ObjectMapper().readValue(sb.toString(), VotingStructure.class);
            logger.info("Load "+fileName);
        } catch (IOException e) {
            logger.severe("Load failed "+e.toString());
            throw new RuntimeException(e);
        }

    }

}

class Topic{
    private String name;
    private Hashtable<String, Voting> votings;

    public Topic(String name){
        this.name = name;
        votings = new Hashtable<>();
    }

    public Topic(){}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public Hashtable<String, Voting> getVotings() {return votings;}
    public void setVotings(Hashtable<String, Voting> votings) { this.votings = votings;}

    Voting getVoting(String votingName){return votings.get(votingName);}
    public String votingText(){return votings.keySet().toString();}

    public void createVote(String creator, String vote, String description, String[] vo){
        votings.put(vote, new Voting(creator, vote, description, vo));
        //return vote + " was created";
    }
    public String deleteVote(String creator, String vote){
        if(votings.get(vote).getCreator().equals(creator)){
            votings.remove(vote);
            return vote+" was removed";
        }
        return vote+" wasn't removed";
    }

}

class Voting{
    private String name;
    private String creator;
    private String description;
    private Hashtable<String, VotingOption> options;

    public Voting(String creator, String vote, String description, String[] vo){
        this.creator = creator;
        this.name = vote;
        this.description = description;
        options = new Hashtable<>();
        for(String voOption: vo){
            options.put(voOption, new VotingOption(voOption));
        }
    }
    public Voting(){}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getCreator() {return creator;}
    public void setCreator(String creator) {this.creator = creator;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public Hashtable<String, VotingOption> getOptions() {return options;}
    public void setOptions(Hashtable<String, VotingOption> options) {this.options = options;}


    public String optionsInfo(){return String.join(" ",options.keySet().toString());}
    public String voteInfo(){
        StringBuilder out = new StringBuilder();
        out.append("Name=").append(name).append("\nOptions[ ");
        for(String key:options.keySet()) {
            VotingOption option = options.get(key);
            out.append(option.getName()).append(": ").append(option.countVoters()).append(" ");
        }
        out.append("]");
        return out.toString();
    }

    public String vote(String votingOption, String name){
        for (Map.Entry<String, VotingOption> entry : options.entrySet()) {
            String option = entry.getKey();
            VotingOption voOpt = entry.getValue();
            if(voOpt.containsVoter(name) && option.equals(votingOption))
                return "already vote";
            if(voOpt.containsVoter(name))
                voOpt.deleteVoter(name);
        }
        options.get(votingOption).addVoter(name);
        return "vote success";
    }

}
class VotingOption{
    private String name;
    private ArrayList<String> voters;

    public VotingOption(String name){
        this.name = name;
        voters = new ArrayList<>();
    }
    public VotingOption(){}
    public String getName() {return name;}
    public void setName(String name) {this.name=name;}
    public ArrayList<String> getVoters() {return voters;}
    public void setVoters(ArrayList<String> voters) {this.voters = voters;}

    boolean containsVoter(String voter){return voters.contains(voter);}
    void addVoter(String voter){voters.add(voter);}
    void deleteVoter(String voter){voters.remove(voter);}
    int countVoters(){return voters.size();}

}
