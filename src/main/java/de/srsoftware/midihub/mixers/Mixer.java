package de.srsoftware.midihub.mixers;

public interface Mixer {
    void disconnect();

    void close();

    void changeMarker(int delta);

    void changeTrack(int delta);

    boolean handleSolo(int num, boolean enabled);

    boolean handleRec(int num, boolean enabled);

    boolean handleMute(int num, boolean enabled);


    void handlePoti(int num, float percent);

    void handleFader(int num, float percent);

    void highlightChannel(int num);
    void unhighlightChannel(int num);

    void highlightFaderGroup(int count);
    void unhighlightFaderGroup(int count);

    void highlightBus(int bus);
    void unhighlightBus(int bus);
}
