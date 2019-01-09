/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lavatheif;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author cf32047
 */
public class Utils {
    private static int screen = 0;
    private static boolean blockServer = false;//Will stop people spamming the submit button
    private static String USERTOKEN = null;
    private static String ID = null;
    public static String tripID = "";
    public static String[] teachers = {};
    private static int mode = -1;
    
    private static boolean login = false;
    
    public static void contactServer(HashMap<String, String> jsonData, int id){
        contactServer(jsonData);
        mode = id;
    }
    
    public static void contactServer(HashMap<String, String> jsonData){
        if(blockServer)
            return;
        blockServer = true;
        
        login = jsonData.get("request").equals("login");
        
        if(!login && mode==-1){
            //editing the trip
            jsonData.put("tripID", tripID);
        }
        jsonData.put("token", USERTOKEN);
        jsonData.put("id", ID);
        String data = new Gson().toJson(jsonData);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {                    
                    onServerReply(send(data));
                    return;
                } catch (Exception e) {
                e.printStackTrace();
                }
                onServerReply("{\"valid\":\"false\", \"errMsg\":\"Unknown Error Occoured\"}");
            }
        }).start();
    }
    
    private static String send(String data) throws IOException{
        String reply = "{}";

        Socket s = new Socket("localhost", 25000);

        PrintWriter pw = new PrintWriter(s.getOutputStream());
        Scanner scan = new Scanner(s.getInputStream());
        pw.println(data);
        pw.flush();
        while(!scan.hasNext()){}//Wait for reply
        reply = scan.nextLine();

        s.close();
        pw.close();
        scan.close();
        
        return reply;
    }
    
    private static void onServerReply(String message) {
        HashMap<String, String> data = new Gson().fromJson(message, HashMap.class);
        boolean valid = data.get("valid").equals("true");//TODO Get from message data.
        String errMsg = data.get("errMsg");
        blockServer = false;
        
        if(!valid && errMsg.equalsIgnoreCase("Invalid token.  Please log in.")){
            mode = -1;
            CollegeTripPlanner.loginScreen.show();
            CollegeTripPlanner.loginScreen.dataInvalid("");
            return;
        }
                
        if(login){
            System.out.println(data);
            if(valid){
                USERTOKEN = data.get("token");
                ID = data.get("id");
                CollegeTripPlanner.loginScreen.dataValid();
            }else{
                CollegeTripPlanner.loginScreen.dataInvalid(errMsg);
            }
            return;
        }
        
        if(mode==0){
            //getting trips
            data.remove("valid");
            for(String trip : data.keySet()){
                CollegeTripPlanner.mainMenu.addTrip(Integer.parseInt(trip), new Gson().fromJson(data.get(trip), HashMap.class));
            }
            mode = -1;
            return;
        }

        if(screen == 0){
            if(valid){
                CollegeTripPlanner.start.dataValid();
                tripID = data.get("trip id");
                teachers = data.get("teachersString").split("-");
                System.out.println("Trip: "+tripID);
                System.out.println("Teachers: ");
                for(int i = 0;i<teachers.length; i++){
                    System.out.println(teachers[i].replace("@woking.ac.uk}", "").replace("{email=", ""));
                }
            }else{
                CollegeTripPlanner.start.dataInvalid(errMsg);
            }
        }else if(screen == 1){
            if(valid)
                CollegeTripPlanner.details.dataValid();
            else
                CollegeTripPlanner.details.dataInvalid(errMsg);
        }else if(screen == 2){
            if(valid)
                CollegeTripPlanner.uploads.dataValid();
            else
                CollegeTripPlanner.uploads.dataInvalid(errMsg);
        }
        if(valid)
            screen++;
    }

    static void newTrip() {
        screen = 0;
    }
}
