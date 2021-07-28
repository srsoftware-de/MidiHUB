package de.srsoftware.midihub.ui;

import javax.swing.*;
import java.awt.*;

public class LogList extends JList<String> {
    private static LogList singleton;
    private static final int DEFAULT_MAX_LENGTH = 2048;
    private static int maxLength = DEFAULT_MAX_LENGTH;

    public LogList(){
        super(new DefaultListModel<String>());
        setSize(new Dimension(1200,600));
    }

    public static LogList get() {
        if (singleton == null) singleton = new LogList();
        return singleton;
    }

    public static void add(String msg, Object... fills){
        for (Object f:fills){
            int pos = msg.indexOf("{}");
            if (pos < 0) {
                break;
            }
            msg = msg.substring(0,pos)+f+msg.substring(pos+2);
        }
        DefaultListModel<String> model = ((DefaultListModel<String>) get().getModel());
        while (model.size() > maxLength) model.remove(0);
        model.addElement(msg);
        get().ensureIndexIsVisible(model.size()-1);
    }

    public static void setMaxLength(int maxLength) {
        LogList.maxLength = maxLength;
    }
}
