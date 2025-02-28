package org.example;

import java.util.Scanner;

public class ClientForm {
    private static String state = "standart";



    public static String getRequest() {
        Scanner sc = new Scanner(System.in);

        if(!state.equals("standard")){
            state = "standard";
            String out = state;
            return "vote " + out + " " + sc.nextLine();
        }

        String msg2server;
        msg2server = sc.nextLine();
        while (msg2server.isEmpty()) {
            System.out.println("Please enter a non-empty message");
            msg2server = sc.nextLine();
        }
        if (msg2server.startsWith("create vote -t="))
            return ClientForm.createVote(msg2server.substring(15));
        if (msg2server.startsWith("vote -t=")){
            state = msg2server.substring(msg2server.indexOf("-t=")+3, msg2server.indexOf(" ")) +" "
                    +msg2server.substring(msg2server.indexOf("-v=")+3);

            return msg2server;
        }
        return msg2server;
    }

    public static String createVote(String topic){
        StringBuilder out = new StringBuilder("createVote "+topic+" ");
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter voting name");
        out.append(sc.nextLine()+" ");
        System.out.println("Enter description");
        out.append(sc.nextLine()+" ");
        System.out.println("Enter number of options");
        int numberOptions = Integer.parseInt(sc.nextLine());
        for (int i = 0; i < numberOptions; i++) {
            System.out.println("Enter the name of the " + i + " voting option");
            out.append(sc.nextLine()+" ");
        }
        return out.toString();
    }

    public static String vote(String topicAndVote){
        StringBuilder out = new StringBuilder("vote ");
        return out.toString();
    }

    public static String getVote(){
        System.out.println("Enter vote options");
        String s = new Scanner(System.in).nextLine();
        return "";
    }
}
