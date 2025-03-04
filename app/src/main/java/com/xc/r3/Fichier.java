package com.xc.r3;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Fichier implements Serializable {
    private String kind;
    private String id;
    private String name;
    private String mimeType;
    private String contenu;

    public Fichier() {
    }

    public Fichier(String kind, String id, String name, String mimeType) {
        this.kind = kind;
        this.id = id;
        this.name = name;
        this.mimeType = mimeType;
    }

    public static String getNoms(CommonActivity activity, List<Fichier> fichiers) {
        if (fichiers.isEmpty()) {
            return (activity.getString(R.string.liste_vide));
        }
        StringBuilder noms = new StringBuilder();
        for (Fichier f : fichiers) {
            noms.append(f.getName()).append("\n");
        }
        return noms.toString();
    }

    @Override
    public String toString() {
        return "Fichier{" +
                "kind='" + kind + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getKind() {

        return kind;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getContenu() {
        return contenu;
    }

    public String getFormatedName() {
        List<Character> separateurs = Arrays.asList('.', '-', ' ', '_');
        String nameTeste = name + "   ";
        StringBuilder newName = new StringBuilder("OA");
        int cptChiffres = 0;
        int i = 1;
        while (i < nameTeste.length() && cptChiffres < 6) {
            char car = nameTeste.charAt(i);
            if (Character.isDigit(car)) {
                if (separateurs.contains(nameTeste.charAt(i - 1)) && separateurs.contains(nameTeste.charAt(i + 1))) {
                    newName.append('0');
                    cptChiffres++;
                }
                newName.append(car);
                cptChiffres++;
            }
            i++;
        }
        if (cptChiffres == 6)
            return newName.toString();
        return "{ nom de fichier incorrect }"; // accolade pour qu'il soit le
        // dernier lors du tri
    }

    public boolean isDateFromFileNameToday() {
        try {
            String pattern = "yyMMdd";
            DateFormat df = new SimpleDateFormat(pattern);
            Date today = Calendar.getInstance().getTime();
            String sToday = df.format(today);
            String nomFichierModifie = this.getFormatedName().substring(2);
            return nomFichierModifie.equals(sToday);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTypeDanger() {
        List<String> dangers = Arrays.asList(".D", "-D", " D", "_D", ".d", "-d", " d", "_d");
        for (String danger : dangers) {
            if (this.name.contains(danger)) return true;
        }
        return false;
    }

}
