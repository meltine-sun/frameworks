package com.example.util;

import java.lang.reflect.Method;
import java.util.Map;

import com.example.annotation.Auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class AuthUtil {
    private static String getAuthKey(HttpServletRequest req){
        String key = (String) req.getServletContext().getAttribute("auth.sessionKey");
        
        if(key == null) {
            throw new IllegalStateException( "La clé 'auth.sessionKey' n'est pas configuré" );
        }

        return key;
    }

    public static boolean isAuthRequired(Class<?> controller, Method methode){
        return methode.isAnnotationPresent(Auth.class) || controller.isAnnotationPresent(Auth.class);
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

    public static void login(HttpServletRequest req, Object utilisateur){
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
