package de.srsoftware.midihub;

import javax.swing.*;
import java.awt.*;

public class MixerPanel extends JPanel {
    MixerPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("IP address or hostname"),BorderLayout.NORTH);
        JTextField input = new JTextField("192.168.1.1");
        add(input,BorderLayout.CENTER);

        JButton btn = new JButton("Connect");
        add(btn,BorderLayout.EAST);
    }
}
