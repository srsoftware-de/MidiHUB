package de.srsoftware.midihub;

import javax.sound.midi.Receiver;

public interface Mixer {
    void disconnect();

    void close();

    void changeMarker(int delta);

    void changeTrack(int delta);

    void handleSolo(int num, boolean enabled);

    void handleRec(int num, boolean enabled);

    void handleMute(int num, boolean enabled);


    void handlePoti(int num, float percent);

    void handleFader(int num, float percent);

    void highlightChannel(int num);
    void unhighlightChannel(int num);

    void highlightFaderGroup(int count);
    void unhighlightFaderGroup(int count);

    void highlightBus(int bus);
    void unhighlightBus(int bus);
}
