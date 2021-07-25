package de.srsoftware.midihub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import javax.swing.*;
import java.util.List;
import java.util.Scanner;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class App {
    private static Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws InvalidMidiDataException, InterruptedException {

        Gui gui = new Gui();
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}
