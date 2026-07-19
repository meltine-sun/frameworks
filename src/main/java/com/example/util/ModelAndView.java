package com.example.util;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String vue;
    private Map<String, Object> donnees;

    public ModelAndView(String vue) {
        this.vue = vue;
        this.donnees = new HashMap<>();
    }

    public ModelAndView(String vue, Map<String, Object> donnees) {
        this.vue = vue;
        this.donnees = donnees;
    }

    public String getVue() {
        return vue;
    }

    public void setVue(String vue) {
        this.vue = vue;
    }

    public Map<String, Object> getDonnees() {
        return donnees;
    }

    public void setDonnees(Map<String, Object> donnees) {
        this.donnees = donnees;
    }

    public void ajouterDonnee(String cle, Object valeur) {
        this.donnees.put(cle, valeur);
    }

    @Override
    public String toString() {
        return "ModelAndView{" +
                "vue='" + vue + '\'' +
                ", donnees=" + donnees +
                '}';
    }
}