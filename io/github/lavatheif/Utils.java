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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cf32047
 */
public class Utils {
    public static int screen = 0;
    private static boolean blockServer = false;//Will stop people spamming the submit button
    private static String USERTOKEN = null;
    public static String ID = null;
    public static String tripID = "";
    private static int mode = -1;
    public static boolean isAdmin = false;
 
    public static String[] teachers = {};

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
        System.out.println(data);
        blockServer = false;
        
        if(!valid && errMsg.equalsIgnoreCase("Invalid token.  Please log in.")){
            mode = -1;
            CollegeTripPlanner.loginScreen.show();
            CollegeTripPlanner.loginScreen.dataInvalid("");
            return;
        }
                
        if(login){
            if(valid){
                USERTOKEN = data.get("token");
                ID = data.get("id");
                isAdmin = data.get("admin").equalsIgnoreCase("true");
                try {
                    Thread.sleep(1000);//ensure that token is saved to the db
                } catch (InterruptedException ex) {}
                CollegeTripPlanner.loginScreen.dataValid();
            }else{
                CollegeTripPlanner.loginScreen.dataInvalid(errMsg);
            }
            return;
        }
        
        if(mode==0){
            //getting trips
            data.remove("valid");
            int[] keys = getOrder(data);
            
            //add elements to return array, in descending order.
            for(int i = keys.length-1; i >= 0; i--){
                String trip = keys[i]+"";
                CollegeTripPlanner.mainMenu.addTrip(Integer.parseInt(trip), new Gson().fromJson(data.get(trip), HashMap.class));
            }
            mode = -1;
            return;
        }else if(mode==1){
            teachers = data.get("teachersString").split("-");
            CollegeTripPlanner.viewDetails.addData(data);
            mode = -1;
           return;
        }else if(mode == 2){
            if(valid){
                CollegeTripPlanner.viewDetails.tripAccepted();
            }else{
                CollegeTripPlanner.viewDetails.dataInvalid(errMsg);
            }
            mode = -1;
            return;
        }else if(mode == 3){
            if(valid){
                CollegeTripPlanner.viewDetails.tripDenied();
            }else{
                CollegeTripPlanner.viewDetails.dataInvalid(errMsg);
            }
            mode = -1;
            return;
        }else if(mode == 4){
            if(valid){
                CollegeTripPlanner.viewDetails.downloadFiles(data);
            }else{
                CollegeTripPlanner.viewDetails.dataInvalid(errMsg);
            }
            mode = -1;
            return;
        }

        if(screen == 0){
            if(valid){
                CollegeTripPlanner.start.dataValid();
                tripID = data.get("trip id");
                teachers = data.get("teachersString").split("-");
//                System.out.println("Trip: "+tripID);
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

    private static int[] getOrder(HashMap<String, String> data) {
        //sorts a hashmap from low to high values
        
        int[] keys = new int[data.keySet().size()];
        
        //convert the keys to integers
        for(int i = 0; i < data.keySet().size(); i++){
            int key = Integer.parseInt(""+data.keySet().toArray()[i]);
            keys[i] = key;
        }
        
        //sort it
        Arrays.sort(keys);
        
        return keys;
    }
}
