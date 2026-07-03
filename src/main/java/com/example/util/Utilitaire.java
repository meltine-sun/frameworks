package com.example.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        for(File file : directory.listFiles()) {

            if(file.getName().endsWith(".class")) {

                String className = packageName + "." + file.getName().replace(".class", "");

                classes.add(Class.forName(className));
            }
        }

        return classes;
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

                String nomClasse = c.getName();
                String nomMethod = m.getName();

                MappingInfo mappingInfo = new MappingInfo(nomClasse, nomMethod, url);
                resuMap.put(cle, mappingInfo);
            }
            
        }

        return resuMap;
    }

}
