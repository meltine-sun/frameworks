package com.example.util;

public class MappingInfo {
    private String nomClasse;
    private String nomMethod;
    private String url;

    public MappingInfo(String nomClasse, String nomMethod, String url) {
        this.nomClasse = nomClasse;
        this.nomMethod = nomMethod;
        this.url = url;
    }

    public String getNomClasse() {
        return nomClasse;
    }

    public void setNomClasse(String nomClasse) {
        this.nomClasse = nomClasse;
    }

    public String getNomMethod() {
        return nomMethod;
    }

    public void setNomMethod(String nomMethod) {
        this.nomMethod = nomMethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "MappingInfo{" +
                "nomClasse='" + nomClasse + '\'' +
                ", nomMethod=" + nomMethod +
                ", url='" + url + '\'' +
                '}';
    }
}
