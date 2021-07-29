package de.srsoftware.midihub.mixers;

public interface Mixer {
    void changeMarker(int delta);
    void changeTrack(int delta);
    void close();
    void disconnect();
    boolean getMute(int num);
    boolean getSolo(int i);
    void handleFader(int num, float normalized);
    boolean handleMute(int num, boolean enabled);
    void handlePoti(int num, float percent);
    boolean handleRec(int num, boolean enabled);
    void handleSet();
    boolean handleSolo(int num, boolean enabled);
    void highlightBus(int bus);
    void highlightChannel(int num);
    void highlightFaderGroup(int count);
    void stop(int val);
    void unhighlightBus(int bus);
    void unhighlightChannel(int num);
    void unhighlightFaderGroup(int count);
    void handleCycle();
}
