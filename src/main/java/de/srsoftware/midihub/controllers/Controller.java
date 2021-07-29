package de.srsoftware.midihub.controllers;

import de.srsoftware.midihub.mixers.Mixer;

import javax.sound.midi.Receiver;

public interface Controller extends Receiver {
    public boolean assign(Mixer mixer);
    public Controller disconnect();
}
