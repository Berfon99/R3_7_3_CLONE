package com.xc.r3;

import androidx.annotation.NonNull;

public class ItemInterface {
    private final String id;
    private final String libelle;
    private final String xcbs;

    public ItemInterface(String id, String libelle, String xcbs) {
        this.id = id;
        this.libelle = libelle;
        this.xcbs = xcbs;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getXcbs() {
        return xcbs;
    }

    public String getId() {

        return id;
    }

    @NonNull
    @Override
    public String toString() {
        return "ItemInterface{" +
                "id='" + id + '\'' +
                ", libelle='" + libelle + '\'' +
                ", xcbs='" + xcbs + '\'' +
                '}';
    }
}
