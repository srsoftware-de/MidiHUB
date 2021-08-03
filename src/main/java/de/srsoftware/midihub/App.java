package de.srsoftware.midihub;

import de.srsoftware.midihub.ui.Gui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class App {
    private static Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        boolean autoConnect = false;
        for (String arg : args){
            switch (arg){
                case "--help":
                case "-h":
                    showHelp();
                    return;
                case "--auto-connect":
                case "-a":
                    autoConnect = true;
                    break;
            }
        }

        Gui gui = new Gui();
        if (autoConnect) gui.setAutoConnect(true);
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private static void showHelp(){
        System.out.println("Welcome to SRSoftware MidiHub!");
        System.out.println("==============================\n");
        System.out.println("Usage: java -jar Midihub.jar <options>");
        System.out.println("Options:");
        System.out.println("-a | --auto-connect : connect to the first device found");
        System.out.println("-h | --help         : show this help");
    }
}
