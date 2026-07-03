package com.example.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.example.annotation.Controller;
import com.example.annotation.UrlMapping;
import com.example.util.MappingInfo;
import com.example.util.UrlMethod;
import com.example.util.Utilitaire;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontControllerServlet extends HttpServlet{

    // private List<String> listClass = new ArrayList<>();
    private Map<UrlMethod, MappingInfo> mapping = new HashMap<>();
    
    @Override
    public void init() throws ServletException{
        try {
            String packageName = getServletContext().getInitParameter("package");

            mapping = Utilitaire.getMapping(packageName, Controller.class, UrlMapping.class);

        } catch (Exception e) {
            throw new ServletException(e);
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
        resp.setContentType("text/plain");
        String url = req.getRequestURI().substring(req.getContextPath().length());
        PrintWriter out = resp.getWriter();
        out.println(url); 
        
        String httpMethod = req.getMethod();

        UrlMethod cle = new UrlMethod(url, httpMethod);

        if(mapping.containsKey(cle)) {
            MappingInfo info = mapping.get(cle);
            out.println("Url trouvée : " + url + " (" + httpMethod + ")");
            out.println("Classe : " + info.getNomClasse());
            out.println("Méthode : " + info.getNomMethod());
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
