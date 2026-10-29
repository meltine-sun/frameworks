package com.example.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.annotation.Controller;
import com.example.annotation.UrlMapping;
import com.example.util.MappingInfo;
import com.example.util.MySession;
import com.example.util.UrlMethod;
import com.example.util.Utilitaire;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpSession;

public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();

            String packageName = context.getInitParameter("package");

            Map<UrlMethod, MappingInfo> mapping = Utilitaire.getMapping(packageName, Controller.class, UrlMapping.class);
            context.setAttribute("mapping", mapping);

            Map<String, MySession> mapSession = new ConcurrentHashMap<>();
            context.setAttribute("session", mapSession);

            String sessionKey = context.getInitParameter("auth.sessionKey");
            context.setAttribute("auth.sessionKey", sessionKey);

            System.out.println("Chargement des routes terminé : " + mapping.size() + " route(s) trouvée(s).");

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement des routes au démarrage", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Arrêt de l'application.");
    }
}