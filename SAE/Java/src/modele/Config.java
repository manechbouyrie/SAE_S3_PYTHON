package modele;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Lit et écrit le fichier config.txt (lignes "cle = valeur")
public class Config {

    private final Path chemin;
    private final Map<String, String> valeurs = new LinkedHashMap<>();

    public Config(Path chemin) {
        this.chemin = chemin;
    }

    public void charger() throws IOException {
        valeurs.clear();
        for (String ligne : Files.readAllLines(chemin)) {
            ligne = ligne.trim();
            if (ligne.isEmpty() || ligne.startsWith("#")) {
                continue;
            }
            String[] morceaux = ligne.split("=", 2);
            if (morceaux.length != 2) {
                throw new IOException("Ligne de configuration invalide : " + ligne);
            }
            valeurs.put(morceaux[0].trim(), morceaux[1].trim());
        }
    }

    public void sauvegarder() throws IOException {
        List<String> lignes = new ArrayList<>();
        for (Map.Entry<String, String> e : valeurs.entrySet()) {
            lignes.add(e.getKey() + " = " + e.getValue());
        }
        Files.write(chemin, lignes);
    }

    public String get(String cle) {
        return valeurs.getOrDefault(cle, "");
    }

    public String getOuDefaut(String cle, String defaut) {
        return valeurs.getOrDefault(cle, defaut);
    }

    public void set(String cle, String valeur) {
        valeurs.put(cle, valeur);
    }

    public Path getChemin() {
        return chemin;
    }
}
