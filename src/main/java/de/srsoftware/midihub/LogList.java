package de.srsoftware.midihub;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;

import javax.swing.*;
import java.util.List;
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
