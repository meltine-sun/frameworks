package com.example.listener;

import java.util.Map;

import com.example.annotation.Controller;
import com.example.annotation.UrlMapping;
import com.example.util.MappingInfo;
import com.example.util.UrlMethod;
import com.example.util.Utilitaire;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();

            String packageName = context.getInitParameter("package");

            Map<UrlMethod, MappingInfo> mapping = Utilitaire.getMapping(packageName, Controller.class, UrlMapping.class);

            context.setAttribute("mapping", mapping);

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