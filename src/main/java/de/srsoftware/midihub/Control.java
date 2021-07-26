package de.srsoftware.midihub;

import javax.sound.midi.Receiver;

public interface Control extends Receiver {
    public void assign(Mixer mixer);
}
