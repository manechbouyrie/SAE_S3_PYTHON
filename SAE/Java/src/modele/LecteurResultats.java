package modele;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LecteurResultats {

    private static List<String[]> lireCsv(Path chemin) throws IOException {
        List<String[]> lignes = new ArrayList<>();
        List<String> brut = Files.readAllLines(chemin, StandardCharsets.UTF_8);
        for (int i = 1; i < brut.size(); i++) {
            if (!brut.get(i).isBlank()) {
                lignes.add(decouperLigne(brut.get(i), chemin, i + 1));
            }
        }
        return lignes;
    }

    private static String[] decouperLigne(String ligne, Path chemin, int numeroLigne) throws IOException {
        List<String> colonnes = new ArrayList<>();
        StringBuilder valeur = new StringBuilder();
        boolean entreGuillemets = false;

        for (int i = 0; i < ligne.length(); i++) {
            char caractere = ligne.charAt(i);
            if (caractere == '"') {
                if (entreGuillemets && i + 1 < ligne.length() && ligne.charAt(i + 1) == '"') {
                    valeur.append('"');
                    i++;
                } else {
                    entreGuillemets = !entreGuillemets;
                }
            } else if (caractere == ',' && !entreGuillemets) {
                colonnes.add(valeur.toString());
                valeur.setLength(0);
            } else {
                valeur.append(caractere);
            }
        }

        if (entreGuillemets) {
            throw new IOException("CSV invalide dans " + chemin + ", ligne " + numeroLigne);
        }
        colonnes.add(valeur.toString());
        return colonnes.toArray(new String[0]);
    }

    public static List<Processus> lireProcessus(Path chemin) throws IOException {
        List<Processus> liste = new ArrayList<>();
        for (String[] c : lireCsv(chemin)) {
            liste.add(new Processus(c[0], Integer.parseInt(c[1]), Integer.parseInt(c[2]),
                    Integer.parseInt(c[3]), Integer.parseInt(c[4])));
        }
        return liste;
    }

    public static List<Allocation> lireAllocations(Path chemin) throws IOException {
        List<Allocation> liste = new ArrayList<>();
        for (String[] c : lireCsv(chemin)) {
            liste.add(new Allocation(c[0], Integer.parseInt(c[1]), Integer.parseInt(c[2]), c[3]));
        }
        return liste;
    }

    public static List<Metriques> lireMetriques(Path chemin) throws IOException {
        List<Metriques> liste = new ArrayList<>();
        for (String[] c : lireCsv(chemin)) {
            liste.add(new Metriques(c[0], Double.parseDouble(c[1]), Double.parseDouble(c[2]),
                    Integer.parseInt(c[3]), Integer.parseInt(c[4]), Integer.parseInt(c[5]),
                    Double.parseDouble(c[6])));
        }
        return liste;
    }
}
