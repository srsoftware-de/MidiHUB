package de.srsoftware.midihub;
import de.srsoftware.midihub.ui.Gui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import javax.swing.*;
import java.io.IOException;

public class App {
    private static Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws InvalidMidiDataException, InterruptedException, IOException {
        Gui gui = new Gui();
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
