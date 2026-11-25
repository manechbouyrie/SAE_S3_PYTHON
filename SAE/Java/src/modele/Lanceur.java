package modele;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Lanceur {

    public static String lancer(Path config) throws Exception {
        Path fichierConfig = config.toAbsolutePath();
        Path racineProjet = fichierConfig.getParent().getParent().getParent();
        List<String> commandePython = trouverPython(racineProjet);
        List<String> commande = new ArrayList<>(commandePython);
        commande.add("Code/main.py");
        commande.add(fichierConfig.toString());
        ProcessBuilder pb = new ProcessBuilder(commande);
        pb.directory(racineProjet.toFile());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String sortie = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int codeRetour = p.waitFor();
        if (codeRetour != 0) {
            throw new java.io.IOException("Python a échoué (code " + codeRetour + ") : " + sortie.trim());
        }
        return sortie;
    }

    private static List<String> trouverPython(Path dossierProjet) throws Exception {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        List<List<String>> candidats = windows
                ? List.of(List.of("py", "-3"), List.of("python"), List.of("python3"))
                : List.of(List.of("python3"), List.of("python"));
        List<String> erreurs = new ArrayList<>();

        for (List<String> candidat : candidats) {
            List<String> commandeTest = new ArrayList<>(candidat);
            commandeTest.add("--version");
            try {
                ProcessBuilder test = new ProcessBuilder(commandeTest);
                test.directory(dossierProjet.toFile());
                test.redirectErrorStream(true);
                Process processus = test.start();
                String sortie = new String(processus.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                if (processus.waitFor() == 0) {
                    return candidat;
                }
                if (!sortie.isBlank()) {
                    erreurs.add(String.join(" ", candidat) + " : " + sortie);
                }
            } catch (java.io.IOException e) {
                erreurs.add(String.join(" ", candidat) + " : commande indisponible");
            }
        }

        throw new java.io.IOException(
                "Python 3 est introuvable. Installez Python depuis https://www.python.org/downloads/ "
                        + "en cochant « Add python.exe to PATH », puis redémarrez VS Code."
                        + (erreurs.isEmpty() ? "" : "\nDétails : " + String.join(" | ", erreurs)));
    }
}
