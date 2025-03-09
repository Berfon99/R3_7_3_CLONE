package com.xc.r3;

import java.io.Serializable;
import java.util.List;

public class ModelConfiguration implements Serializable {
    private List<ItemInterface> modes;
    private String folder;

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
    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}