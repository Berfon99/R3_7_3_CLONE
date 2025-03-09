package com.xc.r3;

import androidx.annotation.NonNull;

public class ItemInterface {
    private final String id;
    private final String libelle;

    public ItemInterface(String id, String libelle) {
        this.id = id;
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
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
                '}';
    }
}