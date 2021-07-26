package de.srsoftware.midihub.controllers;

import de.srsoftware.midihub.mixers.Mixer;

import javax.sound.midi.Receiver;

public interface Control extends Receiver {
    public void assign(Mixer mixer);
}