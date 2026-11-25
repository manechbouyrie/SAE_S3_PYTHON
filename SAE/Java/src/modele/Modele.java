package modele;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Le modèle : la config, le lancement de Python et les résultats lus.
// Il ne connaît ni la vue ni le contrôleur.
public class Modele {

    private final Config config;
    private final Map<String, ResultatAlgo> resultats = new LinkedHashMap<>();
    private List<Metriques> metriques = new ArrayList<>();

    public Modele(Path cheminConfig) throws IOException {
        this.config = new Config(cheminConfig);
        this.config.charger();
    }

    public Config getConfig() {
        return config;
    }

    // liste des algorithmes écrits dans la config (ex : [FIFO, RR, EDF])
    public List<String> getAlgos() {
        List<String> algos = new ArrayList<>();
        for (String a : config.get("algorithmes").split(",")) {
            if (!a.isBlank()) {
                algos.add(a.trim().toUpperCase(Locale.ROOT));
            }
        }
        return algos;
    }

    public String lancerPython() throws Exception {
        return Lanceur.lancer(config.getChemin());
    }

    // lit tous les fichiers de résultats produits par Python
    public void chargerResultats() throws IOException {
        resultats.clear();
        Path dossier = dossierSortie();
        for (String algo : getAlgos()) {
            String nom = nomFichier(algo);
            Path global = dossier.resolve(
                    config.getOuDefaut("fichier_global_" + nom, "resultats_" + nom + "_global.csv"));
            Path allocations = dossier.resolve(
                    config.getOuDefaut("fichier_allocations_" + nom, "resultats_" + nom + "_allocations.csv"));
            resultats.put(algo, new ResultatAlgo(
                    LecteurResultats.lireProcessus(global),
                    LecteurResultats.lireAllocations(allocations)));
        }
        metriques = LecteurResultats.lireMetriques(
                dossier.resolve(config.getOuDefaut("fichier_metriques", "metriques.csv")));
    }

    public ResultatAlgo getResultat(String algo) {
        return resultats.get(algo);
    }

    public List<Metriques> getMetriques() {
        return metriques;
    }

    // Outputs/<nom du dossier du test>
    private Path dossierSortie() {
        Path dossierTest = config.getChemin().toAbsolutePath().getParent();
        Path racineProjet = dossierTest.getParent().getParent();
        return racineProjet.resolve("Outputs").resolve(dossierTest.getFileName().toString());
    }

    private String nomFichier(String algo) {
        return switch (algo) {
            case "RR" -> "RoundRobin";
            case "PRIORITE" -> "Priorite";
            default -> algo;
        };
    }
}
