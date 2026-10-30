package com.example.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.example.annotation.Auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthUtil {
    private static String getAuthKey(HttpServletRequest req){
        String key = (String) req.getServletContext().getAttribute("auth.sessionKey");
        
        if(key == null) {
            throw new IllegalStateException( "La clé 'auth.sessionKey' n'est pas configurée" );
        }

        return key;
    }

    private static String getRoleKey(HttpServletRequest req) {
        String key = (String) req.getServletContext().getAttribute("auth.roleKey");

        if(key == null) {
            throw new IllegalStateException( "La clé 'auth.roleKey' n'est pas configurée" );
        }

        return key;
    }

    public static Auth getAuth(Class<?> controller, Method methode) {
        Auth auth = methode.getAnnotation(Auth.class);
        return auth != null ? auth : controller.getAnnotation(Auth.class);
    }

    private static MySession getMySession(HttpServletRequest req){
        HttpSession session = req.getSession();

        @SuppressWarnings("unchecked")
        Map<String, MySession> mapSession = (Map<String, MySession>) req.getServletContext().getAttribute("session");

        if(mapSession == null) {
            throw new IllegalStateException( "La map de session n'est pas initialisée" );
        }

        return mapSession.computeIfAbsent(session.getId(), id -> new MySession(session));
    }

    public static boolean isAuthenticated(HttpServletRequest req){
        if(req.getSession(false) == null){
            return false;
        }

        return getMySession(req).getAttribut(getAuthKey(req)) != null;
    }

    private static boolean hasUnRole(HttpServletRequest req, String[] rolesRequis) {
        if(rolesRequis.length == 0) {
            return true;
        }

        Object valeur = getMySession(req).getAttribut(getRoleKey(req));

        if(!(valeur instanceof Collection<?>)) {
            return false;
        }

        Collection<?> rolesUtilisateur = (Collection<?>) valeur;

        return !Collections.disjoint(rolesUtilisateur, Arrays.asList(rolesRequis));
    }

    private static void rejectUnauthenticated(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Vous devez vous connecter pour accéder à cette page");
    }

    public static boolean verifier(HttpServletRequest req, HttpServletResponse resp, Class<?> controller, Method methode) throws IOException {
        Auth auth = getAuth(controller, methode);

        if( auth == null) {
            return true;
        }

        if(!isAuthenticated(req)) {
            rejectUnauthenticated(req, resp);
            return false;
        }

        if(!hasUnRole(req, auth.roles())){
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès refusé");
            return false;
        }

        return true;
    }

    // public static boolean isAuthRequired(Class<?> controller, Method methode){
    //     return methode.isAnnotationPresent(Auth.class) || controller.isAnnotationPresent(Auth.class);
    // }

    public static void login(HttpServletRequest req, Object utilisateur, String ...roles){
        HttpSession httpSession = req.getSession();

        @SuppressWarnings("unchecked")
        Map<String, MySession> mapSession = (Map<String, MySession>) req.getServletContext().getAttribute("session");

        if(mapSession == null) {
            throw new IllegalStateException( "La map de session n'est pas initialisée" );
        }

        MySession mySession = getMySession(req);

        String ancienId = httpSession.getId();
        req.changeSessionId();

        mapSession.remove(ancienId);
        mapSession.put(httpSession.getId(), mySession);

        mySession.setAttribute(getAuthKey(req), utilisateur);

        mySession.setAttribute(getRoleKey(req), new ArrayList<>(Arrays.asList(roles)));
    }

    public static void logout(HttpServletRequest req){
        HttpSession httpSession = req.getSession(false);

        if(httpSession == null){
            return;
        }

        String id = httpSession.getId();

        @SuppressWarnings("unchecked")
        Map<String, MySession> mapSession = (Map<String, MySession>) req.getServletContext().getAttribute("session");

        if(mapSession != null){
            mapSession.remove(id);
        }

        httpSession.invalidate();
    }


}
