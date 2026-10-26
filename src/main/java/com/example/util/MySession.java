package com.example.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

public class MySession {
    private HttpSession session;
    private Map<String, Object> attributes = new HashMap<>();

    public MySession(HttpSession session) {
        this.session = session;

        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            attributes.put(attributeName, session.getAttribute(attributeName));
        }
    }

    public Object getAttribut(String nom) {
        return attributes.get(nom);
    }

    public void setAttribute(String nom, Object valeur) {
        attributes.put(nom, valeur);
        if(session != null) {
            session.setAttribute(nom, valeur);
        }
    }

    public HttpSession getHttpSession() {
        return session;
    }

    public void removeAttribute(String nom) {
        attributes.remove(nom);
        if(session != null){
            session.removeAttribute(nom);
        }
    }

    public void invalidate() {
        attributes.clear();
        if(session != null){
            session.invalidate();
        }
    }

    // public void miseAJourHttpSession() {
    //     if(session != null) {
    //         for (Map.Entry<String, Object> entry : attributes.entrySet()) {
    //             session.setAttribute(entry.getKey(), entry.getValue());
    //         }
    //     }
    // }
}
