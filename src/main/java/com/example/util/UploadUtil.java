package com.example.util;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.UUID;

import com.example.servlet.FrontControllerServlet;

public class UploadUtil {

    public static MapFile construireMapFile(
            HttpServletRequest req,
            String nomParametre
    ) throws Exception {

        Part part = req.getPart(nomParametre);

        if (part == null
                || part.getSubmittedFileName() == null
                || part.getSubmittedFileName().isBlank()
                || part.getSize() == 0) {
            return null;
        }

        String nomOriginal = part.getSubmittedFileName()
                                 .replace('\\', '/');

        nomOriginal = nomOriginal.substring(
                nomOriginal.lastIndexOf('/') + 1
        );

        if (nomOriginal.isBlank()) {
            return null;
        }

        int dernierPoint = nomOriginal.lastIndexOf('.');

        String nom;
        String extension;

        if (dernierPoint > 0) {
            nom = nomOriginal.substring(0, dernierPoint);
            extension = nomOriginal.substring(dernierPoint + 1);
        } else {
            nom = nomOriginal;
            extension = "";
        }

        byte[] contenu;

        try (InputStream input = part.getInputStream()) {
            contenu = input.readAllBytes();
        }

        if (contenu.length == 0) {
            return null;
        }

        MapFile fichier = new MapFile(nom, extension, contenu);

        enregistrerFichier(fichier);

        return fichier;
    }

    public static Path enregistrerFichier(MapFile fichier)
            throws IOException {

        if (fichier == null || fichier.getContenu() == null) {
            throw new IOException(
                    "Impossible d'enregistrer un fichier sans contenu."
            );
        }

        Properties properties = new Properties();

        try (InputStream input =
                     UploadUtil.class.getClassLoader()
                             .getResourceAsStream("upload.properties")) {

            if (input == null) {
                throw new IOException(
                        "Fichier upload.properties introuvable."
                );
            }

            properties.load(input);
        }

        String dossier = properties.getProperty("upload.directory");

        if (dossier == null || dossier.isBlank()) {
            throw new IOException(
                    "La propriété upload.directory est manquante."
            );
        }

        Path cheminDossier = Paths.get(dossier)
                                 .toAbsolutePath()
                                 .normalize();

        Files.createDirectories(cheminDossier);

        String nomFichier = UUID.randomUUID().toString();

        String extension = fichier.getExtension();

        if (extension != null && !extension.isBlank()) {

            if (!extension.matches("[a-zA-Z0-9]{1,10}")) {
                throw new IOException(
                        "Extension de fichier invalide."
                );
            }

            nomFichier += "." + extension;
        }

        Path destination = cheminDossier.resolve(nomFichier).normalize();

        if (!destination.getParent().equals(cheminDossier)) {
            throw new IOException("Chemin de fichier invalide.");
        }

        Files.write(
                destination,
                fichier.getContenu(),
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        );

        return destination;
    }

    public static boolean estDepassementTaille(Throwable e) {
        while (e != null) {
            String nom = e.getClass().getName();
            if (nom.contains("FileSizeLimitExceededException")
                    || nom.contains("SizeLimitExceededException")) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    public static void repondreFichierTropVolumineux(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        MultipartConfig config = FrontControllerServlet.class.getAnnotation(MultipartConfig.class);
        long maxMo = config.maxFileSize() / (1024 * 1024);

        resp.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE); // 413
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().println("Fichier trop volumineux (maximum " + maxMo + " Mo).");
    }
}