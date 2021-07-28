package de.srsoftware.midihub.ui;

import de.srsoftware.midihub.mixers.M32C;
import de.srsoftware.midihub.mixers.Mixer;

import javax.swing.*;
import java.awt.*;

public class MixerPanel extends JPanel {
    private final JTextField port;
    private final JTextField address;
    private Mixer mixer;
    private Listener listener;

    public interface Listener{
        void onConnect(Mixer mixer);
    }

    MixerPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("IP address/hostname | port"),BorderLayout.NORTH);
        address = new JTextField("192.168.1.149");
        add(address,BorderLayout.WEST);

        port = new JTextField("10023");
        add(port,BorderLayout.CENTER);
        JButton btn = new JButton("Connect");
        add(btn,BorderLayout.EAST);

        btn.addActionListener(action -> tryConnect());

        // TODO: Suche nach Mixern implementieren.
        // Dazu UDP-Message /xinfo an Broadcast-Adresse 255.255.255.255 : port (10023) senden
        // Mixer sollte mit UDP-Message /xinfo + ip + name + ? + version antworten
    }

    public void onConnect(Listener listener){
        this.listener = listener;
    }

    private void tryConnect() {
        try {
            String host = address.getText();
            int port = Integer.parseInt(this.port.getText());
            if (mixer != null) mixer.disconnect();
            mixer = null;
            mixer = new M32C(host,port);
            if (listener != null) listener.onConnect(mixer);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
