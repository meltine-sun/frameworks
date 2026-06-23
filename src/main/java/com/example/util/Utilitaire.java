package com.example.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
}
