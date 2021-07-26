package de.srsoftware.midihub.ui;

import javax.swing.*;
import java.util.Vector;

public class LogList extends JList<String> implements Logger {

    private final Vector<String> data = new Vector<>();

    public LogList(){
        data.add("Test");
        data.add("Test2");
        setListData(data);
        ensureIndexIsVisible(50);
    }

    public void log(String msg, Object... fills) {
        for (Object f:fills){
            int pos = msg.indexOf("{}");
            if (pos < 0) {
                break;
            }
            msg = msg.substring(0,pos)+f+msg.substring(pos+2);
        }
        data.add(msg);
        while (data.size()>30) data.remove(0);
        setListData(data);
    }

}
