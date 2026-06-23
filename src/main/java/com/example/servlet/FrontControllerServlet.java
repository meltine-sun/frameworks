package com.example.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.example.annotation.Controller;
import com.example.util.Utilitaire;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontControllerServlet extends HttpServlet{

    private List<String> listClass = new ArrayList<>();
    
    @Override
    public void init() throws ServletException{
        try {
            String packageName = getServletContext().getInitParameter("package");

            List<Class<?>> classes = Utilitaire.getClassesAnnote(packageName, Controller.class);

            for(Class<?> c : classes) {
                listClass.add(c.getName());
            }

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
        String url = req.getRequestURL().toString();
        PrintWriter out = resp.getWriter();
        out.println(url); 
        
        out.println("Classes with @Controller annotation:");
        if(listClass.isEmpty()) {
            out.println("No classes found with @Controller annotation.");
        } else {
            for(String className : listClass) {
                out.println(className);
            }
        }
    }
}
