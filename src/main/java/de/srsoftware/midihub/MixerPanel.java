package de.srsoftware.midihub;

import com.illposed.osc.transport.udp.OSCPortOut;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

public class MixerPanel extends JPanel {
    private final JTextField port;
    private final JTextField address;
    private Mixer mixer;
    private Listener listener;
    private Logger logger;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public interface Listener{
        public void onConnect(Mixer mixer);
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
    }

    private void tryConnect() {
        try {
            String host = address.getText();
            int port = Integer.parseInt(this.port.getText());
            if (mixer != null) mixer.disconnect();
            mixer = null;
            mixer = new M32C(host,port,logger);
            if (listener != null) listener.onConnect(mixer);
        } catch (Exception e){

        }
    }

    public void onConnect(Listener listener){
        this.listener = listener;
    }
}
