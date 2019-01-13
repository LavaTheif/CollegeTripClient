/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lavatheif;

/**
 *
 * @author Charlie
 */
public class CollegeTripPlanner {

    public static TripDetailsScreen details;
    public static ViewTripDetails viewDetails;
    public static StartScreen start;
    public static FileUploads uploads;
    public static Login loginScreen;
    public static MainMenu mainMenu;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //TODO: allow log in
        //create new screen to list all trip.  Add a refresh button to it
        mainMenu = new MainMenu();
//        loginScreen.hide();
    }
    
}
