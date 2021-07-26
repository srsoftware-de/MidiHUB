package de.srsoftware.midihub;

import javax.sound.midi.Receiver;

public interface Mixer {
    public void disconnect();

    void close();

    void changeMarker(int delta);

    void changeTrack(int delta);

    void handleSolo(int num, boolean enabled);

    void handleRec(int num, boolean enabled);

    void handleMute(int num, boolean enabled);


    void handlePoti(int num, float percent);

    void handleFader(int num, float percent);

    public void highlight(int count);
    public void unhighlight(int count);
}
