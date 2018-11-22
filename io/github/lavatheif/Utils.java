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
    private static String USERTOKEN = "Secret-Token";
    public static String tripID = "";
    
    public static void contactServer(HashMap<String, String> jsonData){
        if(blockServer)
            return;
        blockServer = true;
        
        jsonData.put("token", USERTOKEN);
        String data = new Gson().toJson(jsonData);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {                    
                    onServerReply(send(data));
                    return;
                } catch (Exception e) {}
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
        
        if(screen == 0){
            if(valid){
                CollegeTripPlanner.start.dataValid();
                tripID = data.get("trip id");
                System.out.println("Trip: "+tripID);
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
        blockServer = false;
    }
}
