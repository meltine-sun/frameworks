package com.example.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.example.annotation.Controller;
import com.example.annotation.RestAPI;
import com.example.annotation.UrlMapping;
import com.example.util.MappingInfo;
import com.example.util.ModelAndView;
import com.example.util.UrlMethod;
import com.example.util.Utilitaire;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontControllerServlet extends HttpServlet{
  
    private Map<UrlMethod, MappingInfo> mapping = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException{
        Object mappingContext = getServletContext().getAttribute("mapping");

        if(mappingContext != null) {
            mapping = (Map<UrlMethod, MappingInfo>) mappingContext;
        } else {
            try {
                String packageName = getServletContext().getInitParameter("package");

                mapping = Utilitaire.getMapping(packageName, Controller.class, UrlMapping.class);

            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        processRequest(req,resp);
    } 

     @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        processRequest(req,resp);
    } 

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        String url = req.getRequestURI().substring(req.getContextPath().length());
        PrintWriter out = resp.getWriter();
        
        String httpMethod = req.getMethod();

        UrlMethod cle = new UrlMethod(url, httpMethod);

        if(mapping.containsKey(cle)) {
            MappingInfo info = mapping.get(cle);

            try {
                Class<?> classe = Class.forName(info.getNomClasse());
                Object instance = classe.getDeclaredConstructor().newInstance();
                Method methode = classe.getMethod(info.getNomMethod());

                Object resultat = methode.invoke(instance);

                boolean estJson = methode.isAnnotationPresent(RestAPI.class);

                if(estJson)
                {
                    Gson gson = new Gson();
                    String json = gson.toJson(resultat);
                    resp.setContentType("application/json");
                    resp.getWriter().write(json);
                    return;
                } else if(resultat instanceof ModelAndView) {
                    ModelAndView modelAndView = (ModelAndView) resultat;

                    for(Map.Entry<String, Object> entry : modelAndView.getDonnees().entrySet())
                    {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }
                    req.getRequestDispatcher(modelAndView.getVue()).forward(req, resp); 

                } else {
                    resp.setContentType("text/plain;charset=UTF-8");
                    out.println(resultat.toString());
                }

            } catch (Exception e) {
                throw new ServletException("Erreur lors de l'exécution de " + info.getNomClasse() + "." + info.getNomMethod(), e);
            }

        } else {
            out.println("Url inconnue : " + url + " (" + httpMethod + ")");
            out.println("Voici toutes les méthodes :");
            if(mapping.isEmpty()) {
                out.println("Aucune route trouvée.");
            } else {
                for(Map.Entry<UrlMethod, MappingInfo> map : mapping.entrySet()) {
                    out.println("Url : " + map.getKey().getUrl());
                    out.println("Method : " + map.getKey().getMethod());
                    out.println("Classe : " + map.getValue().getNomClasse());
                    out.println("Méthodes : " + map.getValue().getNomMethod());
                }
            }
        }
    }
}