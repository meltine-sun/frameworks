package com.example.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.util.MapFile;
import com.example.util.UploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.example.annotation.UrlMapping;

public class Utilitaire {

    public static List<Class<?>> getClasses(String packageName)
        throws Exception {

        List<Class<?>> classes = new ArrayList<>();

        String path = packageName.replace('.', '/');

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        URL resource = loader.getResource(path);

        if(resource == null) {
            return classes;
        }

        File directory = new File(resource.toURI());

        explorerRepertoire(directory, packageName, classes);

        return classes;
    }

    private static void explorerRepertoire(File directory, String packageCourant, List<Class<?>> classes) throws Exception {

        if(!directory.exists()) {
            return;
        }

        for(File file : directory.listFiles()) {

            if(file.isDirectory()) {

                String sousPackage = packageCourant + "." + file.getName();

                explorerRepertoire(file, sousPackage, classes);

            } else if(file.getName().endsWith(".class")) {

                String className = packageCourant + "." + file.getName().replace(".class", "");

                classes.add(Class.forName(className));
            }
        }
    }

    public static List<Class<?>> getClassesAnnote(String packageName, Class<? extends Annotation> annotation) throws Exception {
        List<Class<?>> resultat = new ArrayList<>();

        List<Class<?>> classes = getClasses(packageName);

        for(Class<?> c : classes) {
            if(c.isAnnotationPresent(annotation)){
                resultat.add(c);
            }
        }
        return resultat;
    }

    public static List<Method> getMethodesAnnote(Class<?> classe, Class<? extends Annotation> annotation){
        List<Method> resultat = new ArrayList<>();

        for(Method m : classe.getDeclaredMethods()){
            if(m.isAnnotationPresent(annotation)){
                resultat.add(m);
            }
        }
        return resultat;
    }

    public static Map<UrlMethod, MappingInfo> getMapping(String nomPackage, Class<? extends Annotation> annotationClass, Class<? extends Annotation> annotationMethod) throws Exception{
        Map<UrlMethod, MappingInfo> resuMap = new HashMap<>();

        List<Class<?>> classes = getClassesAnnote(nomPackage, annotationClass);

        for(Class<?> c : classes){
            List<Method> methodes = getMethodesAnnote(c, annotationMethod);

            for(Method m : methodes){
                UrlMapping urlMapping = (UrlMapping) m.getAnnotation(annotationMethod);

                String url = urlMapping.value();
                String urlmethod = urlMapping.method();

                UrlMethod cle = new UrlMethod(url, urlmethod);

                // on vérifie qu'aucune autre méthode n'utilise déjà cette url + ce verbe
                if(resuMap.containsKey(cle)) {
                    MappingInfo existant = resuMap.get(cle);
                    throw new IllegalStateException(
                        "L'url '" + url + "' avec la méthode '" + urlmethod +
                        "' est déjà associée à " + existant.getNomClasse() + "." + existant.getNomMethod());
                }

                String nomClasse = c.getName();
                String nomMethod = m.getName();

                MappingInfo mappingInfo = new MappingInfo(nomClasse, nomMethod, url);
                resuMap.put(cle, mappingInfo);
            }

        }

        return resuMap;
    }

    public static Method trouverMethode(Class<?> classe, String nomMethode){
        Method methode = null;
        for(Method m : classe.getMethods()){
            if(m.getName().equals(nomMethode)){
                methode = m;
                break;
            }
        }
        return methode;
    }

    public static Object[] getArguments(Method methode, Map<String, String[]> parametres, HttpServletRequest req) throws Exception {

        Parameter[] parametresMethode = methode.getParameters();

        Object[] arguments = new Object[parametresMethode.length];

        for (int i = 0; i < parametresMethode.length; i++) {

            Parameter parametre = parametresMethode[i];

            String nomParametre = parametre.getName();

            if (!parametre.isNamePresent()) {
                throw new IllegalStateException(
                        "Le nom du paramètre '"
                        + nomParametre
                        + "' n'est pas disponible. "
                        + "Active la compilation avec l'option -parameters."
                );
            }

            Class<?> type = parametre.getType();

            if (type == HttpServletRequest.class) {
                arguments[i] = req;
            }

            else if (type == MapFile.class) {

                arguments[i] = UploadUtil.construireMapFile(
                        req,
                        nomParametre
                );
            }

            else if (type == MySession.class) {
                HttpSession sess = req.getSession();

                @SuppressWarnings("unchecked")
                Map<String, MySession> mapSession = (Map<String, MySession>) req.getServletContext().getAttribute("session");

                String sessionId = sess.getId();

                MySession mysession = mapSession.computeIfAbsent(sessionId, id -> new MySession(sess));

                arguments[i] = mysession;
            }

            else if (estTypeSimple(type)) {

                String valeur = obtenirValeur(
                        parametres,
                        nomParametre
                );

                arguments[i] = convertir(valeur, type);
            }

            else if (List.class.isAssignableFrom(type)) {

                Type typeGenerique = parametre.getParameterizedType();

                if (!(typeGenerique instanceof ParameterizedType)) {
                    throw new IllegalArgumentException(
                            "La liste '" + nomParametre
                            + "' doit déclarer son type générique."
                    );
                }

                Type typeElement = ((ParameterizedType) typeGenerique).getActualTypeArguments()[0];

                if (!(typeElement instanceof Class<?>)) {
                    throw new IllegalArgumentException(
                            "Type d'élément non pris en charge pour "
                            + nomParametre
                    );
                }

                Class<?> classeElement = (Class<?>) typeElement;

                arguments[i] = construireListe(
                        nomParametre,
                        classeElement,
                        parametres
                );
            }

            // CAS 3 : OBJET
            else {
                arguments[i] = construireObjet(
                        type,
                        nomParametre,
                        parametres
                );
            }
        }

        return arguments;
    }

    private static boolean estTypeSimple(Class<?> type) {

        return type.isPrimitive()
                || type == String.class
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Boolean.class
                || type == Short.class
                || type == Byte.class
                || type == Character.class;
    }

    private static String obtenirValeur(Map<String, String[]> parametres, String nom) {

        String[] valeurs = parametres.get(nom);

        if (valeurs == null || valeurs.length == 0) {
            return null;
        }

        return valeurs[0];
    }

    private static Object construireObjet(Class<?> classe, String prefixe, Map<String, String[]> parametres) {

        try {

            Constructor<?> constructeur = classe.getDeclaredConstructor();

            constructeur.setAccessible(true);

            Object objet = constructeur.newInstance();

            for (Field champ : classe.getDeclaredFields()) {

                if (java.lang.reflect.Modifier.isStatic(
                        champ.getModifiers())) {
                    continue;
                }

                String nomChamp = champ.getName();

                String nomParametre =
                        prefixe.isEmpty()
                        ? nomChamp
                        : prefixe + "." + nomChamp;

                String valeur = obtenirValeur(
                        parametres,
                        nomParametre
                );

                if (valeur == null) {
                    continue;
                }

                Class<?> typeChamp = champ.getType();

                if (!estTypeSimple(typeChamp)) {
                    throw new IllegalArgumentException(
                            "Champ imbriqué non pris en charge : "
                            + nomParametre
                    );
                }

                Object valeurConvertie = convertir(valeur, typeChamp);

                String nomSetter = "set" + Character.toUpperCase(nomChamp.charAt(0)) + nomChamp.substring(1);

                Method setter = trouverSetter(
                        classe,
                        nomSetter,
                        typeChamp
                );

                if (setter != null) {
                    setter.invoke(objet, valeurConvertie);
                } else {
                    champ.setAccessible(true);
                    champ.set(objet, valeurConvertie);
                }
            }

            return objet;

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Impossible de construire l'objet "
                    + classe.getName()
                    + " avec le préfixe '"
                    + prefixe + "'",
                    e
            );
        }
    }

    private static Method trouverSetter(
            Class<?> classe,
            String nomSetter,
            Class<?> typeChamp) {

        for (Method methode : classe.getMethods()) {

            if (!methode.getName().equals(nomSetter)) {
                continue;
            }

            if (methode.getParameterCount() != 1) {
                continue;
            }

            Class<?> typeParametre =
                    methode.getParameterTypes()[0];

            if (typeParametre == typeChamp
                    || typeCompatible(typeParametre, typeChamp)) {
                return methode;
            }
        }

        return null;
    }

    private static boolean typeCompatible(
            Class<?> typeParametre,
            Class<?> typeChamp) {

        return emballer(typeParametre) == emballer(typeChamp);
    }

    private static Class<?> emballer(Class<?> type) {

        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == boolean.class) return Boolean.class;
        if (type == short.class) return Short.class;
        if (type == byte.class) return Byte.class;
        if (type == char.class) return Character.class;

        return type;
    }

    private static List<?> construireListe(String nomParametre, Class<?> classeElement, Map<String, String[]> parametres) {

        List<Object> liste = new ArrayList<>();

        String prefixe = nomParametre + "[";

        Map<Integer, Map<String, String[]>> groupes = new HashMap<>();

        for (Map.Entry<String, String[]> entree : parametres.entrySet()) {

            String cle = entree.getKey();

            if (!cle.startsWith(prefixe)) {
                continue;
            }

            int debut = prefixe.length();
            int fin = cle.indexOf("].", debut);

            if (fin < 0) {
                continue;
            }

            try {

                int index = Integer.parseInt(
                        cle.substring(debut, fin)
                );

                String nomChamp = cle.substring(fin + 2);

                Map<String, String[]> champs =
                        groupes.computeIfAbsent(
                                index,
                                k -> new HashMap<>()
                        );

                champs.put(nomChamp, entree.getValue());

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Index de liste invalide : " + cle,
                        e
                );
            }
        }

        List<Integer> indices = new ArrayList<>(groupes.keySet());
        indices.sort(Integer::compareTo);

        for (Integer index : indices) {

            Map<String, String[]> champs = groupes.get(index);

            Object objet = construireObjet(
                    classeElement,
                    "",
                    champs
            );

            liste.add(objet);
        }

        return liste;
    }

    public static Object convertir(String valeur, Class<?> type) {

        if (valeur == null || valeur.trim().isEmpty()) {
            return valeurParDefaut(type);
        }

        if (type == String.class) {
            return valeur;
        }

        String v = valeur.trim();

        try {

            if (type == Integer.class || type == int.class)
                return Integer.valueOf(v);

            if (type == Long.class || type == long.class)
                return Long.valueOf(v);

            if (type == Double.class || type == double.class)
                return Double.valueOf(v);

            if (type == Float.class || type == float.class)
                return Float.valueOf(v);

            if (type == Boolean.class || type == boolean.class)
                return Boolean.valueOf(v);

            if (type == Short.class || type == short.class)
                return Short.valueOf(v);

            if (type == Byte.class || type == byte.class)
                return Byte.valueOf(v);

            if (type == Character.class || type == char.class)
                return v.charAt(0);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Valeur '" + valeur
                    + "' invalide pour le type "
                    + type.getSimpleName(),
                    e
            );
        }

        throw new IllegalArgumentException(
                "Type simple non pris en charge : "
                + type.getName()
        );
    }

    private static Object valeurParDefaut(Class<?> type) {

        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0f;
        if (type == boolean.class) return false;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';

        return null;
    }
}