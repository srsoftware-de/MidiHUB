package de.srsoftware.midihub;

import de.srsoftware.midihub.ui.Gui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class App {
    private static Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        boolean autoConnect = false;
        boolean minimize = false;
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
                case "--minimize":
                case "-m":
                    minimize = true;
                    break;
            }
        }

        Gui gui = new Gui();
        if (autoConnect) gui.setAutoConnect(true);
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        if (minimize) gui.setState(Frame.ICONIFIED);
    }

    private static void showHelp(){
        System.out.println("Welcome to SRSoftware MidiHub!");
        System.out.println("==============================\n");
        System.out.println("Usage: java -jar Midihub.jar <options>");
        System.out.println("Options:");
        System.out.println("-a | --auto-connect : connect to the first device found");
        System.out.println("-h | --help         : show this help");
        System.out.println("-m | --minimize     : minimize window on startup");
    }
}
