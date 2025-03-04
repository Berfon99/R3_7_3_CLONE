package com.xc.r3;

import java.io.Serializable;
import java.util.List;

public class Configuration implements Serializable {
    private String version;
    private String reset;
    private List<ItemInterface> modes;
    private List<ItemInterface> themes;

    public void setVersion(String version) {
        this.version = version;
    }

    public void setReset(String reset) {
        this.reset = reset;
    }

    public String getReset() {

        return reset;
    }

    public void setModes(List<ItemInterface> modes) {
        this.modes = modes;
    }

    public void setThemes(List<ItemInterface> themes) {
        this.themes = themes;
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

    public String[] getLibellesThemes() {
        String[] libelles = new String[themes.size()];
        for (int i = 0; i < themes.size(); i++) {
            String libelle = themes.get(i).getLibelle();
            libelles[i] = libelle;
        }
        return libelles;
    }

    public ItemInterface getMode(int indiceMode) {
        return this.modes.get(indiceMode);
    }

    public ItemInterface getTheme(int indiceTheme) {
        return this.themes.get(indiceTheme);
    }

    public String getVersion() {
        return version;
    }
}
