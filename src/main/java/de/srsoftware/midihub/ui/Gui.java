package de.srsoftware.midihub.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class Gui extends JFrame {
    private static Logger LOG = LoggerFactory.getLogger(Gui.class);

    public Gui() {
        super("MidiHub");

        setLayout(new BorderLayout());

        LogList.setMaxLength(128);
        JScrollPane logScroll = new JScrollPane(LogList.get());
        logScroll.setPreferredSize(new Dimension(600,300));
        add(logScroll,BorderLayout.NORTH);

        JTable table = new AssignmentTable();
        add(table,BorderLayout.CENTER);

        pack();
        //setLocationRelativeTo(null);
        setLocation(4000,0);
        setVisible(true);
    }
}
