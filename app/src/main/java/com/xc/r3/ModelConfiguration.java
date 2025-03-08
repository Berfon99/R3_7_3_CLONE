package com.xc.r3;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ModelConfiguration implements Serializable {
    private String reset;
    private List<ItemInterface> modes;

    public void setReset(String reset) {
        this.reset = reset;
    }

    public String getReset() {
        return reset;
    }

    public void setModes(List<ItemInterface> modes) {
        this.modes = modes;
    }

    public List<ItemInterface> getModes() {
        return modes;
    }

    public String[] getLibellesModesCourts() {
        String[] libelles = new String[modes.size()];
        for (int i = 0; i < modes.size(); i++) {
            String libelle = modes.get(i).getLibelle().split(" ")[0];
            libelles[i] = libelle;
        }
        return libelles;
    }

    public String[] getLibellesModesLongs() {
        String[] libelles = new String[modes.size()];
        for (int i = 0; i < modes.size(); i++) {
            String libelle = modes.get(i).getLibelle();
            libelles[i] = libelle;
        }
        return libelles;
    }

    public ItemInterface getMode(int indiceMode) {
        return this.modes.get(indiceMode);
    }
}