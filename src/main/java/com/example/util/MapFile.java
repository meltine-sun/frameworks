package com.example.util;

public class MapFile {
    private String nom;
    private String extension;
    private byte[] contenu;

    public MapFile(String nom, String extension, byte[] contenu){
        this.nom = nom;
        this.extension = extension;
        this.contenu = contenu;
    }

    public MapFile(){}

    public String getNom(){
        return nom;
    }

    public void setNom(String nom){
        this.nom = nom;
    }
    
    public String getExtension(){
        return extension;
    }

    public void setExtension(String extension){
        this.extension = extension;
    }

    public byte[] getContenu(){
        return contenu;
    }

    public byte[] getContenuCopy(){
        return contenu.clone();
    }

    public void setContenu(byte[] contenu){
        this.contenu = contenu;
    }

    @Override 
    public String toString(){
        return "MapFile{" +
                "nom = '" + nom + '\'' +
                ", extension = '" + extension + '\'' +
                ", contenu = " + (contenu != null ? "[...]" : "null") +
                '}';
    }
}
